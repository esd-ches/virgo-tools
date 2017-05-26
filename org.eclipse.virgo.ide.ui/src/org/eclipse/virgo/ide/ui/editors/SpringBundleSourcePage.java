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

package org.eclipse.virgo.ide.ui.editors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.pde.core.IBaseModel;
import org.eclipse.pde.core.plugin.IPluginLibrary;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.core.text.IDocumentKey;
import org.eclipse.pde.internal.core.text.IDocumentRange;
import org.eclipse.pde.internal.core.text.IEditingModel;
import org.eclipse.pde.internal.core.text.bundle.Bundle;
import org.eclipse.pde.internal.core.text.bundle.BundleClasspathHeader;
import org.eclipse.pde.internal.core.text.bundle.BundleModel;
import org.eclipse.pde.internal.core.text.bundle.ExecutionEnvironment;
import org.eclipse.pde.internal.core.text.bundle.ExportPackageHeader;
import org.eclipse.pde.internal.core.text.bundle.ExportPackageObject;
import org.eclipse.pde.internal.core.text.bundle.ImportPackageHeader;
import org.eclipse.pde.internal.core.text.bundle.ImportPackageObject;
import org.eclipse.pde.internal.core.text.bundle.ManifestHeader;
import org.eclipse.pde.internal.core.text.bundle.PackageObject;
import org.eclipse.pde.internal.core.text.bundle.RequireBundleHeader;
import org.eclipse.pde.internal.core.text.bundle.RequireBundleObject;
import org.eclipse.pde.internal.core.text.bundle.RequiredExecutionEnvironmentHeader;
import org.eclipse.pde.internal.core.text.plugin.PluginModel;
import org.eclipse.pde.internal.ui.PDELabelProvider;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.PDEPluginImages;
import org.eclipse.pde.internal.ui.editor.IFoldingStructureProvider;
import org.eclipse.pde.internal.ui.editor.PDEFormEditor;
import org.eclipse.pde.internal.ui.editor.PDEFormEditorContributor;
import org.eclipse.pde.internal.ui.editor.actions.HyperlinkAction;
import org.eclipse.pde.internal.ui.editor.actions.PDEActionConstants;
import org.eclipse.pde.internal.ui.editor.plugin.BundleSourcePage;
import org.eclipse.pde.internal.ui.editor.plugin.ExtensionHyperLink;
import org.eclipse.pde.internal.ui.editor.plugin.PluginFoldingStructureProvider;
import org.eclipse.pde.internal.ui.editor.text.ChangeAwareSourceViewerConfiguration;
import org.eclipse.pde.internal.ui.editor.text.IColorManager;
import org.eclipse.pde.internal.ui.editor.text.ReconcilingStrategy;
import org.eclipse.pde.internal.ui.util.SharedLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.texteditor.ContentAssistAction;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.virgo.ide.manifest.core.IHeaderConstants;
import org.eclipse.virgo.ide.manifest.core.editor.model.ImportBundleHeader;
import org.eclipse.virgo.ide.manifest.core.editor.model.ImportBundleObject;
import org.eclipse.virgo.ide.manifest.core.editor.model.ImportLibraryHeader;
import org.eclipse.virgo.ide.manifest.core.editor.model.ImportLibraryObject;
import org.eclipse.virgo.ide.ui.ServerIdeUiPlugin;
import org.eclipse.virgo.ide.ui.editors.text.BundleColorManager;
import org.eclipse.virgo.ide.ui.editors.text.BundleManifestConfiguration;
import org.eclipse.virgo.ide.ui.internal.actions.ManifestFormatAction;

/**
 * @author Christian Dupuis
 * @author Steffen Pingel
 * @author Leo Dos Santos
 */
public class SpringBundleSourcePage extends BundleSourcePage {

    private final String ID_CONTEXT_MENU = "org.eclipse.virgo.ide.bundlemanifest.text.menu";

    private IFoldingStructureProvider fFoldingStructureProvider;

    private final ChangeAwareSourceViewerConfiguration fConfiguration;

    public SpringBundleSourcePage(PDEFormEditor editor, String id, String title) {
        super(editor, id, title);
        this.fConfiguration = createSourceViewerConfiguration(BundleColorManager.getDefault());
        if (this.fConfiguration != null) {
            setSourceViewerConfiguration(this.fConfiguration);
        }
    }

    @Override
    protected ChangeAwareSourceViewerConfiguration createSourceViewerConfiguration(IColorManager colorManager) {
        return new BundleManifestConfiguration(colorManager, this);
    }

    @Override
    protected void setEditorContextMenuId(String contextMenuId) {
        super.setEditorContextMenuId(this.ID_CONTEXT_MENU);
    }

    @Override
    public void projectionEnabled() {
        IBaseModel model = getInputContext().getModel();
        if (model instanceof IEditingModel) {
            this.fFoldingStructureProvider = getFoldingStructureProvider((IEditingModel) model);
            if (this.fFoldingStructureProvider != null) {
                this.fFoldingStructureProvider.initialize();
                IReconciler rec = getSourceViewerConfiguration().getReconciler(getSourceViewer());
                IReconcilingStrategy startegy = rec.getReconcilingStrategy(new String());
                if (startegy instanceof ReconcilingStrategy) {
                    ((ReconcilingStrategy) startegy).addParticipant(this.fFoldingStructureProvider);
                }
            }
        }
    }

    @Override
    protected IFoldingStructureProvider getFoldingStructureProvider(IEditingModel model) {
        if (model instanceof PluginModel) {
            return new PluginFoldingStructureProvider(this, model);
        }
        if (model instanceof BundleModel) {
            return new SpringBundleFoldingStructureProvider(this, model);
        }
        // return super.getFoldingStructureProvider(model);
        return null;
    }

    @Override
    public void projectionDisabled() {
        this.fFoldingStructureProvider = null;
    }

    @Override
    protected boolean affectsTextPresentation(PropertyChangeEvent event) {
        if (this.fConfiguration == null) {
            return false;
        }
        return this.fConfiguration.affectsTextPresentation(event) || super.affectsTextPresentation(event);
    }

    @Override
    public void dispose() {
        if (this.fConfiguration != null) {
            this.fConfiguration.dispose();
        }
        super.dispose();
    }

    @Override
    protected void handlePreferenceStoreChanged(PropertyChangeEvent event) {
        try {
            if (this.fConfiguration != null) {
                ISourceViewer sourceViewer = getSourceViewer();
                if (sourceViewer != null) {
                    this.fConfiguration.adaptToPreferenceChange(event);
                }
            }
        } finally {
            super.handlePreferenceStoreChanged(event);
        }
    }

    @Override
    protected void editorContextMenuAboutToShow(IMenuManager menu) {
        super.editorContextMenuAboutToShow(menu);
        PDEFormEditorContributor contributor = ((PDEFormEditor) getEditor()).getContributor();
        if (contributor instanceof BundleManifestEditorContributor) {
            BundleManifestEditorContributor textContributor = (BundleManifestEditorContributor) contributor;
            HyperlinkAction action = textContributor.getHyperlinkAction();
            if (action != null && action.isEnabled() && action.getHyperLink() instanceof ExtensionHyperLink == false) {
                // Another detector handles this the extension hyperlink case
                // org.eclipse.pde.internal.ui.editor.plugin.
                // ExtensionAttributePointDectector.java
                // Implemented at a higher level. As a result, need to disable
                // the action here to prevent duplicate entries in the context
                // menu
                menu.add(action);
            }
            ManifestFormatAction formatManifestAction = textContributor.getFormatAction();
            if (formatManifestAction != null && formatManifestAction.isEnabled()) {
                // add format action after Outline. This is the same order as
                // the hyperlink actions
                menu.insertAfter(PDEActionConstants.COMMAND_ID_QUICK_OUTLINE, formatManifestAction);
            }
        }
    }

    @Override
    protected void createActions() {
        super.createActions();
        PDEFormEditorContributor contributor = ((PDEFormEditor) getEditor()).getContributor();
        if (contributor instanceof BundleManifestEditorContributor) {
            BundleManifestEditorContributor textContributor = (BundleManifestEditorContributor) contributor;
            setAction(PDEActionConstants.OPEN, textContributor.getHyperlinkAction());
            setAction(PDEActionConstants.FORMAT, textContributor.getFormatAction());
            if (textContributor.supportsContentAssist()) {
                createContentAssistAction();
            }
        }
    }

    private void createContentAssistAction() {
        IAction contentAssist = new ContentAssistAction(getBundleForConstructedKeys(), "ContentAssistProposal.", this); //$NON-NLS-1$
        contentAssist.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
        setAction("ContentAssist", contentAssist); //$NON-NLS-1$
        markAsStateDependentAction("ContentAssist", true); //$NON-NLS-1$
    }

    @Override
    public ILabelProvider createOutlineLabelProvider() {
        return new SpringBundleLabelProvider();
    }

    @Override
    public ITreeContentProvider createOutlineContentProvider() {
        return new SpringBundleOutlineContentProvider();
    }

    @Override
    public IDocumentRange findRange() {
        java.lang.reflect.Field field = null;
        Class classAncestor = this.getClass();
        while (classAncestor != Object.class) {
            try {
                field = classAncestor.getDeclaredField("fSelection");
                break;
            } catch (NoSuchFieldException e) {
            }
            // meh, just move up the hierarchy
            classAncestor = classAncestor.getSuperclass();
        }
        if (field != null) {
            field.setAccessible(true);
            Object selection;
            try {
                selection = field.get(this);
            } catch (Exception e) {
                throw new RuntimeException("Internal Error", e);
            }

            if (selection instanceof ImportLibraryObject) {
                return getSpecificRange(((ImportLibraryObject) selection).getModel(), IHeaderConstants.IMPORT_LIBRARY,
                    ((ImportLibraryObject) selection).getId());
            } else if (selection instanceof ImportBundleObject) {
                return getSpecificRange(((ImportBundleObject) selection).getModel(), IHeaderConstants.IMPORT_BUNDLE,
                    ((ImportBundleObject) selection).getId());
            } else if (selection instanceof ImportPackageObject) {
                ImportPackageObject impObj = (ImportPackageObject) selection;
                String key = impObj.getHeader().getKey();
                if (key != null && key.equalsIgnoreCase(IHeaderConstants.IMPORT_TEMPLATE)) {
                    return getSpecificRange(((ImportPackageObject) selection).getModel(), IHeaderConstants.IMPORT_TEMPLATE,
                        ((ImportPackageObject) selection).getValue());
                } else if (key != null && key.equalsIgnoreCase(IHeaderConstants.EXCLUDED_IMPORTS)) {
                    return getSpecificRange(((ImportPackageObject) selection).getModel(), IHeaderConstants.EXCLUDED_IMPORTS,
                        ((ImportPackageObject) selection).getValue());
                } else if (key != null && key.equalsIgnoreCase(IHeaderConstants.UNVERSIONED_IMPORTS)) {
                    return getSpecificRange(((ImportPackageObject) selection).getModel(), IHeaderConstants.UNVERSIONED_IMPORTS,
                        ((ImportPackageObject) selection).getValue());
                }
            } else if (selection instanceof ExportPackageObject) {
                ExportPackageObject expObj = (ExportPackageObject) selection;
                String key = expObj.getHeader().getKey();
                if (key != null && key.equalsIgnoreCase(IHeaderConstants.EXPORT_TEMPLATE)) {
                    return getSpecificRange(((ExportPackageObject) selection).getModel(), IHeaderConstants.EXPORT_TEMPLATE,
                        ((ExportPackageObject) selection).getValue());
                } else if (key != null && key.equalsIgnoreCase(IHeaderConstants.EXCLUDED_EXPORTS)) {
                    return getSpecificRange(((ExportPackageObject) selection).getModel(), IHeaderConstants.EXCLUDED_EXPORTS,
                        ((ExportPackageObject) selection).getValue());
                }
            }
        }
        return super.findRange();
    }

    private class SpringBundleOutlineContentProvider implements ITreeContentProvider {

        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

        public Object[] getChildren(Object parent) {
            // Need an identifying class for label provider
            if (parent instanceof ImportPackageHeader) {
                return ((ImportPackageHeader) parent).getPackages();
            } else if (parent instanceof ExportPackageHeader) {
                return ((ExportPackageHeader) parent).getPackages();
            } else if (parent instanceof RequiredExecutionEnvironmentHeader) {
                return ((RequiredExecutionEnvironmentHeader) parent).getEnvironments();
            } else if (parent instanceof RequireBundleHeader) {
                return ((RequireBundleHeader) parent).getRequiredBundles();
            } else if (parent instanceof BundleClasspathHeader) {
                return getPluginLibraries();
            } else if (parent instanceof ImportBundleHeader) {
                return ((ImportBundleHeader) parent).getImportedBundles();
            } else if (parent instanceof ImportLibraryHeader) {
                return ((ImportLibraryHeader) parent).getImportedLibraries();
            }
            return new Object[0];
        }

        private Object[] getPluginLibraries() {
            IPluginLibrary[] libraries = getBundleClasspathLibraries();
            if (libraries == null || libraries.length == 0) {
                return new Object[0];
            }
            return libraries;
        }

        public boolean hasChildren(Object parent) {
            return getChildren(parent).length > 0;
        }

        public Object getParent(Object child) {
            return null;
        }

        @SuppressWarnings("unchecked")
        public Object[] getElements(Object parent) {
            if (parent instanceof BundleModel) {
                BundleModel model = (BundleModel) parent;
                Map manifest = ((Bundle) model.getBundle()).getHeaders();
                ArrayList keys = new ArrayList();
                for (Iterator elements = manifest.keySet().iterator(); elements.hasNext();) {
                    IDocumentKey key = (IDocumentKey) manifest.get(elements.next());
                    if (key.getOffset() > -1) {
                        keys.add(key);
                    }
                }
                return keys.toArray();
            }
            return new Object[0];
        }
    }

    private IPluginLibrary[] getBundleClasspathLibraries() {
        // The bundle classpath header has no model data members
        // Retrieve the plug-in library equivalents from the editor model
        FormEditor editor = getEditor();
        if (editor instanceof PDEFormEditor) {
            PDEFormEditor formEditor = (PDEFormEditor) editor;
            IBaseModel baseModel = formEditor.getAggregateModel();
            if (baseModel instanceof IPluginModelBase) {
                IPluginLibrary[] libraries = ((IPluginModelBase) baseModel).getPluginBase().getLibraries();
                return libraries;
            }
        }
        return null;
    }

    private class SpringBundleLabelProvider extends LabelProvider {

        // TODO: MP: QO: LOW: Move to PDELabelProvider
        @Override
        public String getText(Object obj) {
            if (obj instanceof PackageObject) {
                return ((PackageObject) obj).getName();
            } else if (obj instanceof ExecutionEnvironment) {
                return ((ExecutionEnvironment) obj).getName();
            } else if (obj instanceof RequireBundleObject) {
                return getTextRequireBundle((RequireBundleObject) obj);
            } else if (obj instanceof ImportLibraryObject) {
                return ((ImportLibraryObject) obj).getId();
            } else if (obj instanceof ImportBundleObject) {
                return ((ImportBundleObject) obj).getId();
            } else if (obj instanceof ManifestHeader) {
                return ((ManifestHeader) obj).getName();
            }
            return super.getText(obj);
        }

        private String getTextRequireBundle(RequireBundleObject bundle) {
            StringBuffer label = new StringBuffer();
            // Append the ID
            label.append(bundle.getId());
            // Get the version
            String version = bundle.getVersion();
            // If there is no version, just return what we have
            if (version == null || version.length() == 0) {
                return label.toString();
            }
            // Append a space
            label.append(' ');
            // If the first character does not have a range indicator,
            // add a default one. This can happen when there is only one
            // value specified for either min or max
            char firstChar = version.charAt(0);
            if (firstChar != '(' && firstChar != '[') {
                label.append('(');
            }
            // Append the version
            label.append(version);
            // If the last character does not have a range indicator,
            // add a default one. This can happen when there is only one
            // value specified for either min or max
            char lastChar = version.charAt(version.length() - 1);
            if (lastChar != ')' && lastChar != ']') {
                label.append(')');
            }
            // Return what we have
            return label.toString();
        }

        @Override
        public Image getImage(Object obj) {
            PDELabelProvider labelProvider = PDEPlugin.getDefault().getLabelProvider();
            if (obj instanceof PackageObject) {
                return labelProvider.get(PDEPluginImages.DESC_PACKAGE_OBJ);
            } else if (obj instanceof ExecutionEnvironment) {
                return labelProvider.get(PDEPluginImages.DESC_JAVA_LIB_OBJ);
            } else if (obj instanceof RequireBundleObject) {
                int flags = SharedLabelProvider.F_EXTERNAL;
                if (((RequireBundleObject) obj).isReexported()) {
                    flags = flags | SharedLabelProvider.F_EXPORT;
                }
                return labelProvider.get(PDEPluginImages.DESC_REQ_PLUGIN_OBJ, flags);
            } else if (obj instanceof ImportLibraryObject) {
                return labelProvider.get(PDEPluginImages.DESC_JAR_LIB_OBJ);
            } else if (obj instanceof ImportBundleObject) {
                return labelProvider.get(PDEPluginImages.DESC_BUNDLE_OBJ);
            } else if (obj instanceof ManifestHeader) {
                if (isSpringHeader(((ManifestHeader) obj).getKey())) {
                    return labelProvider.get(ServerIdeUiPlugin.getImageDescriptor("full/view16/green_ball_obj.gif"));
                }
                return labelProvider.get(PDEPluginImages.DESC_BUILD_VAR_OBJ);
            } else if (obj instanceof IPluginLibrary) {
                return labelProvider.get(PDEPluginImages.DESC_JAVA_LIB_OBJ);
            }
            return null;
        }
    }

    private boolean isSpringHeader(String key) {
        ResourceBundle bundle = ResourceBundle.getBundle("org.eclipse.virgo.ide.ui.editors.text.headers");
        List<String> headers = Collections.list(bundle.getKeys());
        return headers.contains(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object getAdapter(Class adapter) {
        if (IHyperlinkDetector.class.equals(adapter)) {
            return new SpringBundleHyperlinkDetector(this);
        }
        return super.getAdapter(adapter);
    }
}
