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
package org.eclipse.virgo.ide.bundlor.ui.internal.properties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.viewsupport.FilteredElementTreeSelectionDialog;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.JavaElementSorter;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.virgo.ide.bundlor.internal.core.BundlorCorePlugin;
import org.eclipse.virgo.ide.bundlor.ui.BundlorUiPlugin;
import org.osgi.service.prefs.BackingStoreException;

/**
 * {@link PropertyPage} to configure properties files for Bundlor variable substitution
 * 
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @author Miles Parker
 * @since 2.0.0
 */
@SuppressWarnings("deprecation")
public class BundlorPreferencePage extends PropertyPage {

	private IProject project;

	private boolean modified = false;

	private Table filenameTable;

	private TableViewer filenamesTableViewer;

	private Button addButton;

	private Button deleteButton;

	private List<String> filenames;

	private Button scanByteCode;

	private Button formatManifests;

	private boolean checkScanByteCodeButton;

	private boolean checkFormatManifestsButton;

	@Override
	protected Control createContents(Composite parent) {

		Font font = parent.getFont();

		Composite parentComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		parentComposite.setLayout(layout);
		parentComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		parentComposite.setFont(font);

		initialize();

		scanByteCode = new Button(parentComposite, SWT.CHECK);
		scanByteCode.setText("Scan output folders instead of source folders to generate MANIFEST.MF");
		scanByteCode.setSelection(checkScanByteCodeButton);
		scanByteCode.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				modified = true;
			}
		});

		formatManifests = new Button(parentComposite, SWT.CHECK);
		formatManifests.setText("Auto-format generated MANIFEST.MF and TEST.MF files");
		formatManifests.setSelection(checkFormatManifestsButton);
		formatManifests.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				modified = true;
			}
		});

		Label description = createDescriptionLabel(parentComposite);
		description.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		Composite composite = new Composite(parentComposite, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 5;
		layout.marginWidth = 0;
		layout.verticalSpacing = 5;
		layout.horizontalSpacing = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.VERTICAL_ALIGN_FILL));

		filenameTable = new Table(composite, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		GridData data = new GridData(GridData.FILL_BOTH);
		filenameTable.setLayoutData(data);
		filenamesTableViewer = new TableViewer(filenameTable);

		filenamesTableViewer.setContentProvider(new PropertiesFileContentProvider());
		filenamesTableViewer.setLabelProvider(new FilenameLabelProvider());

		filenamesTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				Object obj = ((IStructuredSelection) event.getSelection()).getFirstElement();
				if (obj != null) {
					deleteButton.setEnabled(true);
				} else {
					deleteButton.setEnabled(false);
				}
			}

		});

		Composite buttonComposite = new Composite(composite, SWT.NONE);
		buttonComposite.setLayout(new GridLayout(1, true));
		data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		buttonComposite.setLayoutData(data);

		addButton = new Button(buttonComposite, SWT.PUSH);
		addButton.setText("Add");
		data = new GridData();
		data.widthHint = 100;
		addButton.setLayoutData(data);
		addButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				FilteredElementTreeSelectionDialog selDialog = new FilteredElementTreeSelectionDialog(
						Display.getCurrent().getActiveShell(), new JavaElementLabelProvider(),
						new WorkspaceResourceContentProvider());
				selDialog.setTitle("Select properties files");
				selDialog.setMessage("Select properties files in the workspace that should be\nused for variable substitution:");
				selDialog.setValidator(new ISelectionStatusValidator() {
					public IStatus validate(Object[] selection) {
						for (Object object : selection) {
							if (object instanceof IStorage) {
								return new Status(IStatus.OK, BundlorUiPlugin.PLUGIN_ID, IStatus.OK, "", null); //$NON-NLS-1$;
							}
						}
						return new Status(IStatus.ERROR, BundlorUiPlugin.PLUGIN_ID, IStatus.OK, "", null); //$NON-NLS-1$;
					}
				});
				selDialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
				selDialog.setSorter(new JavaElementSorter());
				if (selDialog.open() == Window.OK) {
					IResource resource = (IResource) selDialog.getFirstResult();
					if (resource instanceof IFile) {
						if (resource.getProject().equals(project)) {
							IPath projectRelativePath = resource.getProjectRelativePath();
							String string = projectRelativePath.toString();
							filenames.add(string);
						} else {
							filenames.add(resource.getFullPath().toString());
						}
					}
					modified = true;
					filenamesTableViewer.setInput(project);
				}
			}
		});

		deleteButton = new Button(buttonComposite, SWT.PUSH);
		deleteButton.setText("Delete");
		deleteButton.setLayoutData(data);
		deleteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Object selected = ((IStructuredSelection) filenamesTableViewer.getSelection()).getFirstElement();
				filenames.remove(selected);
				filenamesTableViewer.setInput(project);
				modified = true;
			}
		});

		filenamesTableViewer.setInput(project);

		return parentComposite;
	}

	private void initialize() {
		project = (IProject) getElement().getAdapter(IResource.class);
		noDefaultAndApplyButton();
		setDescription("Define properties files that should be used for variable substitution during\ngeneration of MANIFEST.MF file:");

		if (project != null) {
			IEclipsePreferences node = getProjectPreferences(project);
			String properties = node.get(BundlorCorePlugin.TEMPLATE_PROPERTIES_FILE_KEY,
					BundlorCorePlugin.TEMPLATE_PROPERTIES_FILE_DEFAULT);
			filenames = Arrays.asList(StringUtils.split(properties, ";"));
			checkScanByteCodeButton = node.getBoolean(BundlorCorePlugin.TEMPLATE_BYTE_CODE_SCANNING_KEY,
					BundlorCorePlugin.TEMPLATE_BYTE_CODE_SCANNING_DEFAULT);
			checkFormatManifestsButton = node.getBoolean(BundlorCorePlugin.FORMAT_GENERATED_MANIFESTS_KEY,
					BundlorCorePlugin.FORMAT_GENERATED_MANIFESTS_DEFAULT);
		} else {
			filenames = new ArrayList<String>();
			checkScanByteCodeButton = BundlorCorePlugin.TEMPLATE_BYTE_CODE_SCANNING_DEFAULT;
			checkFormatManifestsButton = BundlorCorePlugin.FORMAT_GENERATED_MANIFESTS_DEFAULT;
		}
	}

	public IEclipsePreferences getProjectPreferences(IProject project) {
		IScopeContext context = new ProjectScope(project);
		IEclipsePreferences node = context.getNode(BundlorCorePlugin.PLUGIN_ID);
		return node;
	}

	@Override
	public boolean performOk() {
		if (!modified) {
			return true;
		}
		IEclipsePreferences node = getProjectPreferences(project);

		node.put(BundlorCorePlugin.TEMPLATE_PROPERTIES_FILE_KEY, StringUtils.join(filenames, ";"));
		boolean oldScanByteCode = node.getBoolean(BundlorCorePlugin.TEMPLATE_BYTE_CODE_SCANNING_KEY,
				BundlorCorePlugin.TEMPLATE_BYTE_CODE_SCANNING_DEFAULT);
		node.putBoolean(BundlorCorePlugin.TEMPLATE_BYTE_CODE_SCANNING_KEY, scanByteCode.getSelection());
		node.putBoolean(BundlorCorePlugin.FORMAT_GENERATED_MANIFESTS_KEY, formatManifests.getSelection());
		try {
			node.flush();
		} catch (BackingStoreException e) {
			throw new RuntimeException(e);
		}
		if (oldScanByteCode != scanByteCode.getSelection()) {
			BundlorCorePlugin.getDefault().getManifestManager().clearPartialManifest(JavaCore.create(project));
		}

		return true;
	}

	class PropertiesFileContentProvider implements ITreeContentProvider {

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

		}

		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof IProject) {
				return filenames.toArray();
			}
			return new Object[0];
		}

		public Object[] getChildren(Object parentElement) {
			return getElements(parentElement);
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}

	}

	class FilenameLabelProvider extends LabelProvider {

		@Override
		public Image getImage(Object element) {
			return BundlorUiPlugin.getImage("full/obj16/file_obj.gif");
		}

		@Override
		public String getText(Object element) {
			return element.toString();
		}

	}

	class WorkspaceResourceContentProvider implements ITreeContentProvider {

		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof IContainer) {
				try {
					return ((IContainer) parentElement).members();
				} catch (CoreException e) {
				}
			}
			return new Object[0];
		}

		public Object getParent(Object element) {
			if (element instanceof IResource) {
				return ((IResource) element).getParent();
			}
			return null;
		}

		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}

		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof IWorkspaceRoot) {
				List<IProject> projects = new ArrayList<IProject>();
				for (IProject project : ((IWorkspaceRoot) inputElement).getProjects()) {
					if (project.isOpen() && project.isAccessible()) {
						projects.add(project);
					}
				}
				return projects.toArray();
			}
			return new Object[0];
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

}
