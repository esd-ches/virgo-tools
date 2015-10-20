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
import java.util.Iterator;

import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.core.ibundle.IBundle;
import org.eclipse.pde.internal.core.ibundle.IBundleModel;
import org.eclipse.pde.internal.core.ibundle.IManifestHeader;
import org.eclipse.pde.internal.core.text.bundle.ExecutionEnvironment;
import org.eclipse.pde.internal.core.text.bundle.RequiredExecutionEnvironmentHeader;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.PDEPluginImages;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.pde.internal.ui.editor.FormLayoutFactory;
import org.eclipse.pde.internal.ui.editor.PDEFormPage;
import org.eclipse.pde.internal.ui.editor.TableSection;
import org.eclipse.pde.internal.ui.editor.context.InputContextManager;
import org.eclipse.pde.internal.ui.editor.plugin.BundleInputContext;
import org.eclipse.pde.internal.ui.elements.DefaultTableProvider;
import org.eclipse.pde.internal.ui.parts.EditableTablePart;
import org.eclipse.pde.internal.ui.parts.TablePart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.osgi.framework.Constants;

/**
 * Adapted from PDE's <code>ExcecutionEnvironmentSection</code>. *
 *
 * @author Christian Dupuis
 * @author Steffen Pingel
 */
public class BundleExecutionEnvironmentSection extends TableSection {

    private TableViewer fEETable;

    private Action fRemoveAction;

    private Action fAddAction;

    class EELabelProvider extends LabelProvider {

        private final Image fImage;

        public EELabelProvider() {
            this.fImage = PDEPluginImages.DESC_JAVA_LIB_OBJ.createImage();
        }

        @Override
        public Image getImage(Object element) {
            return this.fImage;
        }

        @Override
        public String getText(Object element) {
            if (element instanceof IExecutionEnvironment) {
                return ((IExecutionEnvironment) element).getId();
            }
            return super.getText(element);
        }

        @Override
        public void dispose() {
            if (this.fImage != null) {
                this.fImage.dispose();
            }
            super.dispose();
        }
    }

    class ContentProvider extends DefaultTableProvider {

        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof IBundleModel) {
                IBundleModel model = (IBundleModel) inputElement;
                IBundle bundle = model.getBundle();
                IManifestHeader header = bundle.getManifestHeader(Constants.BUNDLE_REQUIREDEXECUTIONENVIRONMENT);
                if (header instanceof RequiredExecutionEnvironmentHeader) {
                    return ((RequiredExecutionEnvironmentHeader) header).getEnvironments();
                }
            }
            return new Object[0];
        }
    }

    public BundleExecutionEnvironmentSection(PDEFormPage page, Composite parent) {
        super(page, parent, Section.DESCRIPTION,
            new String[] { PDEUIMessages.RequiredExecutionEnvironmentSection_add, PDEUIMessages.RequiredExecutionEnvironmentSection_remove,
                PDEUIMessages.RequiredExecutionEnvironmentSection_up, PDEUIMessages.RequiredExecutionEnvironmentSection_down });
        createClient(getSection(), page.getEditor().getToolkit());
    }

    @Override
    protected void createClient(Section section, FormToolkit toolkit) {
        section.setText(PDEUIMessages.RequiredExecutionEnvironmentSection_title);
        if (isFragment()) {
            section.setDescription(PDEUIMessages.RequiredExecutionEnvironmentSection_fragmentDesc);
        } else {
            section.setDescription(PDEUIMessages.RequiredExecutionEnvironmentSection_pluginDesc);
        }

        section.setLayout(FormLayoutFactory.createClearTableWrapLayout(false, 1));

        TableWrapData data = new TableWrapData(TableWrapData.FILL_GRAB);
        section.setLayoutData(data);

        Composite container = createClientContainer(section, 2, toolkit);
        EditableTablePart tablePart = getTablePart();
        tablePart.setEditable(isEditable());

        createViewerPartControl(container, SWT.FULL_SELECTION | SWT.MULTI, 2, toolkit);
        this.fEETable = tablePart.getTableViewer();
        this.fEETable.setContentProvider(new ContentProvider());
        this.fEETable.setLabelProvider(PDEPlugin.getDefault().getLabelProvider());

        Hyperlink link = toolkit.createHyperlink(container, PDEUIMessages.BuildExecutionEnvironmentSection_configure, SWT.NONE);
        link.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
        link.addHyperlinkListener(new IHyperlinkListener() {

            public void linkEntered(HyperlinkEvent e) {
            }

            public void linkExited(HyperlinkEvent e) {
            }

            public void linkActivated(HyperlinkEvent e) {
                showPreferencePage(new String[] { "org.eclipse.jdt.debug.ui.jreProfiles" }, PDEPlugin.getActiveWorkbenchShell()); //$NON-NLS-1$
            }
        });
        GridData gd = new GridData();
        gd.horizontalSpan = 2;
        link.setLayoutData(gd);

        makeActions();

        IBundleModel model = getBundleModel();
        if (model != null) {
            this.fEETable.setInput(model);
            model.addModelChangedListener(this);
        }
        toolkit.paintBordersFor(container);
        section.setClient(container);
    }

    public static boolean showPreferencePage(String[] pageIds, final Shell shell) {
        final PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(shell, pageIds[0], pageIds, null);
        return dialog.open() == Window.OK;
    }

    @Override
    public void dispose() {
        IBundleModel model = getBundleModel();
        if (model != null) {
            model.removeModelChangedListener(this);
        }
    }

    @Override
    public void refresh() {
        this.fEETable.refresh();
        updateButtons();
    }

    @Override
    protected void buttonSelected(int index) {
        switch (index) {
            case 0:
                handleAdd();
                break;
            case 1:
                handleRemove();
                break;
            case 2:
                handleUp();
                break;
            case 3:
                handleDown();
                break;
        }
    }

    @Override
    protected void fillContextMenu(IMenuManager manager) {
        manager.add(this.fAddAction);
        if (!this.fEETable.getSelection().isEmpty()) {
            manager.add(new Separator());
            manager.add(this.fRemoveAction);
        }
        getPage().getPDEEditor().getContributor().contextMenuAboutToShow(manager);
    }

    private void makeActions() {
        this.fAddAction = new Action(PDEUIMessages.RequiredExecutionEnvironmentSection_add) {

            @Override
            public void run() {
                handleAdd();
            }
        };
        this.fAddAction.setEnabled(isEditable());

        this.fRemoveAction = new Action(PDEUIMessages.NewManifestEditor_LibrarySection_remove) {

            @Override
            public void run() {
                handleRemove();
            }
        };
        this.fRemoveAction.setEnabled(isEditable());
    }

    private void updateButtons() {
        Table table = this.fEETable.getTable();
        int count = table.getItemCount();
        boolean canMoveUp = count > 0 && table.getSelection().length == 1 && table.getSelectionIndex() > 0;
        boolean canMoveDown = count > 0 && table.getSelection().length == 1 && table.getSelectionIndex() < count - 1;

        TablePart tablePart = getTablePart();
        tablePart.setButtonEnabled(0, isEditable());
        tablePart.setButtonEnabled(1, isEditable() && table.getSelection().length > 0);
        tablePart.setButtonEnabled(2, isEditable() && canMoveUp);
        tablePart.setButtonEnabled(3, isEditable() && canMoveDown);
    }

    private void handleDown() {
        int selection = this.fEETable.getTable().getSelectionIndex();
        swap(selection, selection + 1);
    }

    private void handleUp() {
        int selection = this.fEETable.getTable().getSelectionIndex();
        swap(selection, selection - 1);
    }

    public void swap(int index1, int index2) {
        RequiredExecutionEnvironmentHeader header = getHeader();
        header.swap(index1, index2);
    }

    @SuppressWarnings("unchecked")
    private void handleRemove() {
        IStructuredSelection ssel = (IStructuredSelection) this.fEETable.getSelection();
        if (ssel.size() > 0) {
            Iterator iter = ssel.iterator();
            while (iter.hasNext()) {
                Object object = iter.next();
                if (object instanceof ExecutionEnvironment) {
                    getHeader().removeExecutionEnvironment((ExecutionEnvironment) object);
                }
            }
        }
    }

    private void handleAdd() {
        ElementListSelectionDialog dialog = new ElementListSelectionDialog(PDEPlugin.getActiveWorkbenchShell(), new EELabelProvider());
        dialog.setElements(getEnvironments());
        dialog.setAllowDuplicates(false);
        dialog.setMultipleSelection(true);
        dialog.setTitle(PDEUIMessages.RequiredExecutionEnvironmentSection_dialog_title);
        dialog.setMessage(PDEUIMessages.RequiredExecutionEnvironmentSection_dialogMessage);
        if (dialog.open() == Window.OK) {
            addExecutionEnvironments(dialog.getResult());
        }
    }

    private void addExecutionEnvironments(Object[] result) {
        IManifestHeader header = getHeader();
        if (header == null) {
            StringBuffer buffer = new StringBuffer();
            for (Object element : result) {
                String id = null;
                if (element instanceof IExecutionEnvironment) {
                    id = ((IExecutionEnvironment) element).getId();
                } else if (element instanceof ExecutionEnvironment) {
                    id = ((ExecutionEnvironment) element).getName();
                } else {
                    continue;
                }
                if (buffer.length() > 0) {
                    buffer.append(","); //$NON-NLS-1$
                    buffer.append(getLineDelimiter());
                    buffer.append(" "); //$NON-NLS-1$
                }
                buffer.append(id);
            }
            getBundle().setHeader(Constants.BUNDLE_REQUIREDEXECUTIONENVIRONMENT, buffer.toString());
        } else {
            RequiredExecutionEnvironmentHeader ee = (RequiredExecutionEnvironmentHeader) header;
            ee.addExecutionEnvironments(result);
        }
    }

    private String getLineDelimiter() {
        BundleInputContext inputContext = getBundleContext();
        if (inputContext != null) {
            return inputContext.getLineDelimiter();
        }
        return System.getProperty("line.separator"); //$NON-NLS-1$
    }

    @SuppressWarnings("unchecked")
    private Object[] getEnvironments() {
        RequiredExecutionEnvironmentHeader header = getHeader();
        IExecutionEnvironment[] envs = JavaRuntime.getExecutionEnvironmentsManager().getExecutionEnvironments();
        if (header == null) {
            return envs;
        }
        ArrayList list = new ArrayList();
        for (int i = 0; i < envs.length; i++) {
            if (!header.hasExecutionEnvironment(envs[i])) {
                list.add(envs[i]);
            }
        }
        return list.toArray();
    }

    @Override
    public void modelChanged(IModelChangedEvent e) {
        if (e.getChangeType() == IModelChangedEvent.WORLD_CHANGED) {
            markStale();
        } else if (e.getChangeType() == IModelChangedEvent.REMOVE) {
            Object[] objects = e.getChangedObjects();
            for (Object element : objects) {
                Table table = this.fEETable.getTable();
                if (element instanceof ExecutionEnvironment) {
                    int index = table.getSelectionIndex();
                    this.fEETable.remove(element);
                    table.setSelection(index < table.getItemCount() ? index : table.getItemCount() - 1);
                }
            }
            updateButtons();
        } else if (e.getChangeType() == IModelChangedEvent.INSERT) {
            Object[] objects = e.getChangedObjects();
            for (Object element : objects) {
                if (element instanceof ExecutionEnvironment) {
                    this.fEETable.add(element);
                }
            }
            if (objects.length > 0) {
                this.fEETable.setSelection(new StructuredSelection(objects[objects.length - 1]));
            }
            updateButtons();
        } else if (Constants.BUNDLE_REQUIREDEXECUTIONENVIRONMENT.equals(e.getChangedProperty())) {
            refresh();
        }
    }

    private BundleInputContext getBundleContext() {
        InputContextManager manager = getPage().getPDEEditor().getContextManager();
        return (BundleInputContext) manager.findContext(BundleInputContext.CONTEXT_ID);
    }

    private IBundle getBundle() {
        IBundleModel model = getBundleModel();
        return model == null ? null : model.getBundle();
    }

    private IBundleModel getBundleModel() {
        BundleInputContext context = getBundleContext();
        return context == null ? null : (IBundleModel) context.getModel();
    }

    protected RequiredExecutionEnvironmentHeader getHeader() {
        IBundle bundle = getBundle();
        if (bundle == null) {
            return null;
        }
        IManifestHeader header = bundle.getManifestHeader(Constants.BUNDLE_REQUIREDEXECUTIONENVIRONMENT);
        if (header instanceof RequiredExecutionEnvironmentHeader) {
            return (RequiredExecutionEnvironmentHeader) header;
        }
        return null;
    }

    protected boolean isFragment() {
        InputContextManager manager = getPage().getPDEEditor().getContextManager();
        IPluginModelBase model = (IPluginModelBase) manager.getAggregateModel();
        return model.isFragmentModel();
    }

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
    protected boolean canPaste(Object target, Object[] objects) {
        RequiredExecutionEnvironmentHeader header = getHeader();
        for (Object element : objects) {
            if (element instanceof ExecutionEnvironment) {
                String env = ((ExecutionEnvironment) element).getName();
                if (header == null || !header.hasElement(env)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void selectionChanged(IStructuredSelection selection) {
        getPage().getPDEEditor().setSelection(selection);
        if (getPage().getModel().isEditable()) {
            updateButtons();
        }
    }

    @Override
    protected void doPaste(Object target, Object[] objects) {
        addExecutionEnvironments(objects);
    }

}
