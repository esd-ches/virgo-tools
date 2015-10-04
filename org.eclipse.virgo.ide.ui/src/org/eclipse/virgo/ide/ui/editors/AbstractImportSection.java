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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.pde.core.IBaseModel;
import org.eclipse.pde.core.IModelChangeProvider;
import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.core.IModelChangedListener;
import org.eclipse.pde.internal.core.ibundle.IBundle;
import org.eclipse.pde.internal.core.ibundle.IBundleModel;
import org.eclipse.pde.internal.core.text.bundle.ImportPackageObject;
import org.eclipse.pde.internal.core.text.bundle.PDEManifestElement;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.pde.internal.ui.editor.FormLayoutFactory;
import org.eclipse.pde.internal.ui.editor.PDEFormPage;
import org.eclipse.pde.internal.ui.editor.TableSection;
import org.eclipse.pde.internal.ui.editor.context.InputContextManager;
import org.eclipse.pde.internal.ui.editor.plugin.BundleInputContext;
import org.eclipse.pde.internal.ui.parts.TablePart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * Abstract class with common functionality for Eclipse Forms sections that display and edit a list of artifacts --
 * Import Package, Import Bundle, and Import Library. Originally based on <code>PDESection</code>.
 *
 * @author Christian Dupuis
 */
public abstract class AbstractImportSection extends TableSection implements IModelChangedListener {

    protected TableViewer fViewer;

    private Action fAddAction;

    private Action fRemoveAction;

    private Action fPropertiesAction;

    public AbstractImportSection(PDEFormPage page, Composite parent) {
        this(page, parent, Section.DESCRIPTION, new String[] { PDEUIMessages.ImportPackageSection_add, PDEUIMessages.ImportPackageSection_remove,
            PDEUIMessages.ImportPackageSection_properties });
    }

    public AbstractImportSection(PDEFormPage formPage, Composite parent, int style, String[] buttonLabels) {
        super(formPage, parent, style, true, buttonLabels);
    }

    @Override
    protected void createClient(Section section, FormToolkit toolkit) {
        section.setText(PDEUIMessages.ImportPackageSection_required);
        section.setDescription(PDEUIMessages.ImportPackageSection_desc);

        Composite container = createClientContainer(section, 2, toolkit);
        createViewerPartControl(container, SWT.MULTI, 2, toolkit);
        TablePart tablePart = getTablePart();
        this.fViewer = tablePart.getTableViewer();
        this.fViewer.setContentProvider(getContentProvider());
        this.fViewer.setLabelProvider(getLabelProvider());
        this.fViewer.setComparator(new ViewerComparator() {

            @Override
            public int compare(Viewer viewer, Object e1, Object e2) {
                String s1 = e1.toString();
                String s2 = e2.toString();
                if (s1.indexOf(" ") != -1) {
                    s1 = s1.substring(0, s1.indexOf(" ")); //$NON-NLS-1$
                }
                if (s2.indexOf(" ") != -1) {
                    s2 = s2.substring(0, s2.indexOf(" ")); //$NON-NLS-1$
                }
                return super.compare(viewer, s1, s2);
            }
        });
        toolkit.paintBordersFor(container);
        section.setClient(container);
        section.setLayout(FormLayoutFactory.createClearGridLayout(false, 1));
        section.setLayoutData(new GridData(GridData.FILL_BOTH));
        makeActions();

        IBundleModel model = getBundleModel();
        this.fViewer.setInput(model);
        model.addModelChangedListener(this);
        updateButtons();
    }

    protected abstract ITableLabelProvider getLabelProvider();

    protected abstract IContentProvider getContentProvider();

    @Override
    public boolean doGlobalAction(String actionId) {

        if (!isEditable()) {
            return false;
        }

        if (actionId.equals(ActionFactory.DELETE.getId())) {
            handleRemove();
            return true;
        }
        if (actionId.equals(ActionFactory.CUT.getId())) {
            // delete here and let the editor transfer
            // the selection to the clipboard
            handleRemove();
            return false;
        }
        if (actionId.equals(ActionFactory.PASTE.getId())) {
            doPaste();
            return true;
        }
        return false;
    }

    @Override
    public void dispose() {
        IBundleModel model = getBundleModel();
        if (model != null) {
            model.removeModelChangedListener(this);
        }
        super.dispose();
    }

    @Override
    protected void selectionChanged(IStructuredSelection sel) {
        getPage().getPDEEditor().setSelection(sel);
        updateButtons();
    }

    protected void updateButtons() {
        Object[] selected = ((IStructuredSelection) this.fViewer.getSelection()).toArray();
        int size = selected.length;
        TablePart tablePart = getTablePart();
        tablePart.setButtonEnabled(getAddIndex(), isEditable());
        tablePart.setButtonEnabled(getRemoveIndex(), isEditable() && size > 0);
        tablePart.setButtonEnabled(getPropertiesIndex(), shouldEnableProperties(selected));
    }

    @Override
    protected void buttonSelected(int index) {
        if (index == getAddIndex()) {
            handleAdd();
        } else if (index == getRemoveIndex()) {
            handleRemove();
        } else if (index == getPropertiesIndex()) {
            handleOpenProperties();
        }
    }

    protected abstract int getAddIndex();

    protected abstract int getRemoveIndex();

    protected abstract int getPropertiesIndex();

    protected abstract void handleRemove();

    protected abstract void handleAdd();

    protected abstract void handleOpenProperties();

    @Override
    public void refresh() {
        this.fViewer.refresh();
        super.refresh();
    }

    protected void makeActions() {
        this.fAddAction = new Action(PDEUIMessages.RequiresSection_add) {

            @Override
            public void run() {
                handleAdd();
            }
        };
        this.fAddAction.setEnabled(isEditable());

        this.fRemoveAction = new Action(PDEUIMessages.RequiresSection_delete) {

            @Override
            public void run() {
                handleRemove();
            }
        };
        this.fRemoveAction.setEnabled(isEditable());

        this.fPropertiesAction = new Action(PDEUIMessages.ImportPackageSection_propertyAction) {

            @Override
            public void run() {
                handleOpenProperties();
            }
        };
    }

    @Override
    protected void fillContextMenu(IMenuManager manager) {
        final ISelection selection = this.fViewer.getSelection();
        manager.add(this.fAddAction);
        manager.add(new Separator());
        if (!selection.isEmpty()) {
            manager.add(this.fRemoveAction);
        }
        getPage().getPDEEditor().getContributor().contextMenuAboutToShow(manager);
        if (shouldEnableProperties(((IStructuredSelection) this.fViewer.getSelection()).toArray())) {
            manager.add(new Separator());
            manager.add(this.fPropertiesAction);
        }
    }

    private BundleInputContext getBundleContext() {
        InputContextManager manager = getPage().getPDEEditor().getContextManager();
        return (BundleInputContext) manager.findContext(BundleInputContext.CONTEXT_ID);
    }

    protected IBundleModel getBundleModel() {
        BundleInputContext context = getBundleContext();
        return context != null ? (IBundleModel) context.getModel() : null;

    }

    protected IBundle getBundle() {
        IBundleModel model = getBundleModel();
        return model != null ? model.getBundle() : null;
    }

    @Override
    protected boolean createCount() {
        return true;
    }

    protected boolean shouldEnableProperties(Object[] selected) {
        if (selected.length == 0) {
            return false;
        }
        if (selected.length == 1) {
            return true;
        }

        String version = ((ImportPackageObject) selected[0]).getVersion();
        boolean optional = ((ImportPackageObject) selected[0]).isOptional();
        for (int i = 1; i < selected.length; i++) {
            ImportPackageObject object = (ImportPackageObject) selected[i];
            if (version == null) {
                if (object.getVersion() != null || !(optional == object.isOptional())) {
                    return false;
                }
            } else if (!version.equals(object.getVersion()) || !(optional == object.isOptional())) {
                return false;
            }
        }
        return true;
    }

    protected abstract String getHeaderConstant();

    @Override
    public void modelChanged(IModelChangedEvent event) {
        IBaseModel model = getPage().getModel();
        if (model instanceof IModelChangeProvider) {
            ((IModelChangeProvider) model).fireModelChanged(event);
        }

        if (event.getChangeType() == IModelChangedEvent.WORLD_CHANGED) {
            markStale();
            return;
        }

        if (getHeaderConstant().equals(event.getChangedProperty())) {
            refresh();
            return;
        }

        Object[] objects = event.getChangedObjects();
        for (Object element : objects) {
            if (element instanceof PDEManifestElement && ((PDEManifestElement) element).getHeader().getName().equals(getHeaderConstant())) {
                switch (event.getChangeType()) {
                    case IModelChangedEvent.INSERT:
                        this.fViewer.add(element);
                        this.fViewer.setSelection(new StructuredSelection(element));
                        this.fViewer.getTable().setFocus();
                        break;
                    case IModelChangedEvent.REMOVE:
                        Table table = this.fViewer.getTable();
                        int index = table.getSelectionIndex();
                        this.fViewer.remove(element);
                        table.setSelection(index < table.getItemCount() ? index : table.getItemCount() - 1);
                        break;
                    default:
                        this.fViewer.refresh(element);
                }
            }
        }
    }

    protected abstract class AbstractSectionViewerLabelProvider implements ITableLabelProvider {

        public abstract Image getColumnImage(Object element, int columnIndex);

        public abstract String getColumnText(Object element, int columnIndex);

        public void addListener(ILabelProviderListener listener) {
        }

        public void dispose() {
        }

        public boolean isLabelProperty(Object element, String property) {
            return false;
        }

        public void removeListener(ILabelProviderListener listener) {
        }
    }

    protected class ImportListSelectionDialog extends ElementListSelectionDialog {

        private Object[] fElements;

        public ImportListSelectionDialog(Shell parent, ILabelProvider renderer) {
            super(parent, renderer);
        }

        @Override
        protected Control createDialogArea(Composite parent) {
            Composite comp = (Composite) super.createDialogArea(parent);
            final Object[] allElements = new Object[this.fElements.length];
            if (this.fElements != null) {
                System.arraycopy(this.fElements, 0, allElements, 0, this.fElements.length);
            }
            return comp;
        }

        @Override
        public void setElements(Object[] elements) {
            super.setElements(elements);
            this.fElements = elements;
        }
    }

}
