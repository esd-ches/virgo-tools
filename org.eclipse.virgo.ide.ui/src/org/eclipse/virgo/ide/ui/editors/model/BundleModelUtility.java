/*******************************************************************************
 * Copyright (c) 2009, 2012 SpringSource, a divison of VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource, a division of VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.virgo.ide.ui.editors.model;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Hashtable;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.pde.core.IBaseModel;
import org.eclipse.pde.core.plugin.ISharedExtensionsModel;
import org.eclipse.pde.internal.core.bundle.BundleFragmentModel;
import org.eclipse.pde.internal.core.bundle.BundlePluginModel;
import org.eclipse.pde.internal.core.ibundle.IBundleModel;
import org.eclipse.pde.internal.core.ibundle.IBundlePluginModelBase;
import org.eclipse.pde.internal.core.text.AbstractEditingModel;
import org.eclipse.pde.internal.core.text.IModelTextChangeListener;
import org.eclipse.pde.internal.core.text.build.PropertiesTextChangeListener;
import org.eclipse.pde.internal.core.text.bundle.BundleTextChangeListener;
import org.eclipse.pde.internal.core.text.plugin.XMLTextChangeListener;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.editor.PDEFormEditor;
import org.eclipse.pde.internal.ui.editor.build.BuildEditor;
import org.eclipse.pde.internal.ui.editor.build.BuildInputContext;
import org.eclipse.pde.internal.ui.editor.build.BuildSourcePage;
import org.eclipse.pde.internal.ui.editor.context.InputContext;
import org.eclipse.pde.internal.ui.editor.plugin.ManifestEditor;
import org.eclipse.pde.internal.ui.util.ModelModification;
import org.eclipse.pde.internal.ui.util.PDEModelUtility;
import org.eclipse.swt.widgets.Display;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditGroup;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.virgo.ide.manifest.core.editor.model.SpringBundleModel;
import org.eclipse.virgo.ide.ui.editors.BundleManifestEditor;
import org.osgi.framework.Constants;

/**
 * Adapted from PDEModelUtility to create SpringBundleModels for modification. This will either retrieve a model from an
 * open editor if one exists or create a new SpringBundleModel from file.
 * 
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @see PDEModelUtility
 */
@SuppressWarnings({ "unchecked" })
public class BundleModelUtility {

	public static final String F_MANIFEST = "MANIFEST.MF"; //$NON-NLS-1$

	public static final String F_MANIFEST_FP = "META-INF/" + F_MANIFEST; //$NON-NLS-1$

	public static final String F_PLUGIN = "plugin.xml"; //$NON-NLS-1$

	public static final String F_FRAGMENT = "fragment.xml"; //$NON-NLS-1$

	public static final String F_PROPERTIES = ".properties"; //$NON-NLS-1$

	public static final String F_BUILD = "build" + F_PROPERTIES; //$NON-NLS-1$

	public static void modifyModel(final ModelModification modification, final IProgressMonitor monitor) {
		// ModelModification was not supplied with the right files
		// TODO should we just fail silently?
		if (getFile(modification) == null) {
			return;
		}

		PDEFormEditor editor = getOpenEditor(modification);
		IBaseModel model = getModelFromEditor(editor, modification);

		if (model != null) {
			// open editor found, should have underlying text listeners -> apply
			// modification
			modifyEditorModel((BundleModelModification) modification, editor, model, monitor);
		} else {
			generateModelEdits((BundleModelModification) modification, monitor, true);
		}
	}

	private static TextFileChange[] generateModelEdits(final BundleModelModification modification,
			final IProgressMonitor monitor, boolean performEdits) {
		ArrayList edits = new ArrayList();
		// create own model, attach listeners and grab text edits
		ITextFileBufferManager manager = FileBuffers.getTextFileBufferManager();
		IFile[] files;
		if (isFullBundleModification(modification)) {
			files = new IFile[2];
			files[F_Bi] = getManifestFile(modification);
			files[F_Xi] = getXMLFile(modification);
		} else {
			files = new IFile[] { getFile(modification) };
		}
		// need to monitor number of successful buffer connections for
		// disconnection purposes
		// @see } finally { statement
		int sc = 0;
		try {
			ITextFileBuffer[] buffers = new ITextFileBuffer[files.length];
			IDocument[] documents = new IDocument[files.length];
			for (int i = 0; i < files.length; i++) {
				if (files[i] == null || !files[i].exists()) {
					continue;
				}
				manager.connect(files[i].getFullPath(), LocationKind.NORMALIZE, monitor);
				sc++;
				buffers[i] = manager.getTextFileBuffer(files[i].getFullPath(), LocationKind.NORMALIZE);
				if (performEdits && buffers[i].isDirty()) {
					buffers[i].commit(monitor, true);
				}
				documents[i] = buffers[i].getDocument();
			}

			IBaseModel editModel;
			if (isFullBundleModification(modification)) {
				editModel = prepareBundlePluginModel(files, documents, !performEdits);
			} else {
				editModel = prepareAbstractEditingModel(files[0], documents[0], !performEdits);
			}

			modification.modifySpringBundle(editModel, monitor);

			IModelTextChangeListener[] listeners = gatherListeners(editModel);
			for (int i = 0; i < listeners.length; i++) {
				if (listeners[i] == null) {
					continue;
				}
				TextEdit[] currentEdits = listeners[i].getTextOperations();
				if (currentEdits.length > 0) {
					MultiTextEdit multi = new MultiTextEdit();
					multi.addChildren(currentEdits);
					if (performEdits) {
						multi.apply(documents[i]);
						buffers[i].commit(monitor, true);
					}
					TextFileChange change = new TextFileChange(files[i].getName(), files[i]);
					change.setEdit(multi);
					// If the edits were performed right away (performEdits ==
					// true) then
					// all the names are null and we don't need the granular
					// detail anyway.
					if (!performEdits) {
						for (TextEdit currentEdit : currentEdits) {
							String name = listeners[i].getReadableName(currentEdit);
							if (name != null) {
								change.addTextEditGroup(new TextEditGroup(name, currentEdit));
							}
						}
					}
					// save the file after the change applied
					change.setSaveMode(TextFileChange.FORCE_SAVE);
					PDEModelUtility.setChangeTextType(change, files[i]);
					edits.add(change);
				}
			}
		} catch (CoreException e) {
			PDEPlugin.log(e);
		} catch (MalformedTreeException e) {
			PDEPlugin.log(e);
		} catch (BadLocationException e) {
			PDEPlugin.log(e);
		} finally {
			// don't want to over-disconnect in case we ran into an exception
			// during connections
			// dc <= sc stops this from happening
			int dc = 0;
			for (int i = 0; i < files.length && dc <= sc; i++) {
				if (files[i] == null || !files[i].exists()) {
					continue;
				}
				try {
					manager.disconnect(files[i].getFullPath(), LocationKind.NORMALIZE, monitor);
					dc++;
				} catch (CoreException e) {
					PDEPlugin.log(e);
				}
			}
		}
		return (TextFileChange[]) edits.toArray(new TextFileChange[edits.size()]);
	}

	private static IBaseModel prepareBundlePluginModel(IFile[] files, IDocument[] docs, boolean generateEditNames)
			throws CoreException {
		AbstractEditingModel[] models = new AbstractEditingModel[docs.length];

		boolean isFragment = false;
		models[F_Bi] = prepareAbstractEditingModel(files[F_Bi], docs[F_Bi], generateEditNames);
		if (models[F_Bi] instanceof IBundleModel) {
			isFragment = ((IBundleModel) models[F_Bi]).getBundle().getHeader(Constants.FRAGMENT_HOST) != null;
		}

		IBundlePluginModelBase pluginModel;
		if (isFragment) {
			pluginModel = new BundleFragmentModel();
		} else {
			pluginModel = new BundlePluginModel();
		}

		pluginModel.setBundleModel((IBundleModel) models[F_Bi]);
		if (files.length > F_Xi && files[F_Xi] != null) {
			models[F_Xi] = prepareAbstractEditingModel(files[F_Xi], docs[F_Xi], generateEditNames);
			pluginModel.setExtensionsModel((ISharedExtensionsModel) models[F_Xi]);
		}
		return pluginModel;
	}

	private static AbstractEditingModel prepareAbstractEditingModel(IFile file, IDocument doc, boolean generateEditNames) {
		AbstractEditingModel model;
		String filename = file.getName();
		if (filename.equals(F_MANIFEST)) {
			// TODO: include template.mf && TEST.MF??
			model = new SpringBundleModel(doc, true);
		} else {
			return null;
		}
		model.setUnderlyingResource(file);
		try {
			model.load();
			IModelTextChangeListener listener = createListener(filename, doc, generateEditNames);
			model.addModelChangedListener(listener);
		} catch (CoreException e) {
			PDEPlugin.log(e);
		}
		return model;
	}

	private static PDEFormEditor getOpenEditor(ModelModification modification) {
		IProject project = getFile(modification).getProject();
		String name = getFile(modification).getName();
		if (name.equals(F_PLUGIN) || name.equals(F_FRAGMENT) || name.equals(F_MANIFEST)
				|| name.equalsIgnoreCase("template.mf") || name.equalsIgnoreCase("TEST.MF")) {
			return getOpenManifestEditor(project, getFile(modification));
		} else if (name.equals(F_BUILD)) {
			PDEFormEditor openEditor = PDEModelUtility.getOpenBuildPropertiesEditor(project);
			if (openEditor == null) {
				openEditor = getOpenManifestEditor(project, getFile(modification));
			}
			return openEditor;
		}
		return null;
	}

	public static ManifestEditor getOpenManifestEditor(IProject project, IFile file) {
		return (ManifestEditor) getOpenEditor(project, BundleManifestEditor.ID_EDITOR, file);
	}

	private static PDEFormEditor getOpenEditor(IProject project, String editorId, IFile file) {
		ArrayList list = (ArrayList) getOpenPDEEditors().get(project);
		if (list == null) {
			return null;
		}
		for (int i = 0; i < list.size(); i++) {
			PDEFormEditor editor = (PDEFormEditor) list.get(i);
			if (editor.getEditorSite().getId().equals(editorId)) {
				if (editor.getEditorInput() instanceof IFileEditorInput) {
					if (((IFileEditorInput) editor.getEditorInput()).getFile().equals(file)) {
						return editor;
					}
				}
			}
		}
		return null;
	}

	private static IBaseModel getModelFromEditor(PDEFormEditor openEditor, ModelModification modification) {
		if (openEditor == null) {
			return null;
		}
		String name = getFile(modification).getName();
		IBaseModel model = null;
		if (name.equals(F_PLUGIN) || name.equals(F_FRAGMENT)) {
			model = openEditor.getAggregateModel();
			if (model instanceof IBundlePluginModelBase) {
				model = ((IBundlePluginModelBase) model).getExtensionsModel();
			}
		} else if (name.equals(F_BUILD)) {
			if (openEditor instanceof BuildEditor) {
				model = openEditor.getAggregateModel();
			} else if (openEditor instanceof ManifestEditor) {
				IFormPage page = openEditor.findPage(BuildInputContext.CONTEXT_ID);
				if (page instanceof BuildSourcePage) {
					model = ((BuildSourcePage) page).getInputContext().getModel();
				}
			}
		} else if (name.equals(F_MANIFEST) || name.equalsIgnoreCase("template.mf") || name.equalsIgnoreCase("TEST.MF")) {
			model = openEditor.getAggregateModel();
			if (model instanceof IBundlePluginModelBase) {
				return model;
			}
		}
		if (model instanceof AbstractEditingModel) {
			return model;
		}
		return null;
	}

	private static void modifyEditorModel(final BundleModelModification mod, final PDEFormEditor editor,
			final IBaseModel model, final IProgressMonitor monitor) {
		getDisplay().syncExec(new Runnable() {
			public void run() {
				try {
					mod.modifySpringBundle(model, monitor);
					IFile[] files = new IFile[] { getManifestFile(mod), getXMLFile(mod), getPropertiesFile(mod) };
					for (IFile element : files) {
						if (element == null) {
							continue;
						}
						InputContext con = editor.getContextManager().findContext(element);
						if (con != null) {
							con.flushEditorInput();
						}
					}
					if (mod.saveOpenEditor()) {
						editor.doSave(monitor);
					}
				}
				// catch (CoreException e) {
				// PDEPlugin.log(e);
				// }
				catch (Exception e) {
					PDEPlugin.log(e);
				}
			}
		});
	}

	private static Display getDisplay() {
		Display display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault();
		}
		return display;
	}

	private static Hashtable getOpenPDEEditors() {
		try {
			Field openEditorsField = PDEModelUtility.class.getDeclaredField("fOpenPDEEditors");
			openEditorsField.setAccessible(true);
			Hashtable openEditors = (Hashtable) openEditorsField.get(null);
			return openEditors;
		} catch (Exception e) {
			return new Hashtable();
		}
	}

	private static IFile getFile(ModelModification mod) {
		try {
			Method getFileMethod = ModelModification.class.getDeclaredMethod("getFile", (Class[]) null);
			getFileMethod.setAccessible(true);
			Object obj = getFileMethod.invoke(mod, (Object[]) null);
			if (obj == null) {
				if (mod instanceof BundleModelModification) {
					return ((BundleModelModification) mod).getIfile();
				}
				return null;
			} else {
				return (IFile) obj;
			}
		} catch (Exception e) {
			return null;
		}
	}

	private static IFile getManifestFile(ModelModification mod) {
		try {
			Method getFileMethod = ModelModification.class.getDeclaredMethod("getManifestFile", (Class[]) null);
			getFileMethod.setAccessible(true);
			Object obj = getFileMethod.invoke(mod, (Object[]) null);
			if (obj instanceof IFile) {
				return (IFile) obj;
			} else if (mod instanceof BundleModelModification) {
				return ((BundleModelModification) mod).getIfile();
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	private static IFile getXMLFile(ModelModification mod) {
		try {
			Method getFileMethod = ModelModification.class.getDeclaredMethod("getXMLFile", (Class[]) null);
			getFileMethod.setAccessible(true);
			Object obj = getFileMethod.invoke(mod, (Object[]) null);
			if (obj instanceof IFile) {
				return (IFile) obj;
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	private static IFile getPropertiesFile(ModelModification mod) {
		try {
			Method getFileMethod = ModelModification.class.getDeclaredMethod("getPropertiesFile", (Class[]) null);
			getFileMethod.setAccessible(true);
			Object obj = getFileMethod.invoke(mod, (Object[]) null);
			if (obj instanceof IFile) {
				return (IFile) obj;
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	private static boolean isFullBundleModification(ModelModification mod) {
		try {
			Method getBooleanMethod = ModelModification.class.getDeclaredMethod("isFullBundleModification",
					(Class[]) null);
			getBooleanMethod.setAccessible(true);
			Object obj = getBooleanMethod.invoke(mod, (Object[]) null);
			if (obj instanceof Boolean) {
				Boolean bool = (Boolean) obj;
				return bool.booleanValue();
			} else if (mod instanceof BundleModelModification) {
				IFile file = ((BundleModelModification) mod).getIfile();
				return file.getName().equals(F_MANIFEST);
			}
			return false;
		} catch (Exception e) {
			if (mod instanceof BundleModelModification) {
				IFile file = ((BundleModelModification) mod).getIfile();
				return file.getName().equals(F_MANIFEST);
			}
			return false;
		}

	}

	private static IModelTextChangeListener[] gatherListeners(IBaseModel editModel) {
		IModelTextChangeListener[] listeners = new IModelTextChangeListener[0];
		if (editModel instanceof AbstractEditingModel) {
			listeners = new IModelTextChangeListener[] { ((AbstractEditingModel) editModel).getLastTextChangeListener() };
		}
		if (editModel instanceof IBundlePluginModelBase) {
			IBundlePluginModelBase modelBase = (IBundlePluginModelBase) editModel;
			listeners = new IModelTextChangeListener[2];
			listeners[F_Bi] = gatherListener(modelBase.getBundleModel());
			listeners[F_Xi] = gatherListener(modelBase.getExtensionsModel());
			return listeners;
		}
		return listeners;
	}

	private static IModelTextChangeListener createListener(String filename, IDocument doc, boolean generateEditNames) {
		if (filename.equals(F_PLUGIN) || filename.equals(F_FRAGMENT)) {
			return new XMLTextChangeListener(doc, generateEditNames);
		} else if (filename.equals(F_MANIFEST)) {
			// TODO: include template.mf && TEST.MF??
			return new BundleTextChangeListener(doc, generateEditNames);
		} else if (filename.endsWith(F_PROPERTIES)) {
			return new PropertiesTextChangeListener(doc, generateEditNames);
		}
		return null;
	}

	private static IModelTextChangeListener gatherListener(IBaseModel model) {
		if (model instanceof AbstractEditingModel) {
			return ((AbstractEditingModel) model).getLastTextChangeListener();
		}
		return null;
	}

	// bundle / xml various Object[] indices
	private static final int F_Bi = 0; // the manifest.mf-related object will

	// always be 1st

	private static final int F_Xi = 1; // the xml-related object will always be
	// 2nd
}
