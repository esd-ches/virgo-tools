/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.virgo.ide.eclipse.editors;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.pde.internal.ui.editor.FormLayoutFactory;
import org.eclipse.pde.internal.ui.editor.StructuredViewerSection;
import org.eclipse.pde.internal.ui.editor.TableSection;
import org.eclipse.pde.internal.ui.editor.plugin.RequiresSection;
import org.eclipse.pde.internal.ui.parts.EditableTablePart;
import org.eclipse.pde.internal.ui.parts.StructuredViewerPart;
import org.eclipse.pde.internal.ui.parts.TablePart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * SpringSource Tool Suite Team - Portions of this class were copied from PDE's PDESection, StructuredViewerSection,
 * TableSection, and RequiresSection in order to provide a dependency editor-like section part outside of the
 * PDEFormEditor.
 */
@SuppressWarnings("restriction")
public abstract class DependenciesSection extends SectionPart {

    /**
     * @see TableSection
     */
    class PartAdapter extends EditableTablePart {

        private Label fCount;

        public PartAdapter(String[] buttonLabels) {
            super(buttonLabels);
        }

        @Override
        public void buttonSelected(Button button, int index) {
            DependenciesSection.this.buttonSelected(index);
            if (DependenciesSection.this.fHandleDefaultButton) {
                button.getShell().setDefaultButton(null);
            }
        }

        @Override
        protected void createButtons(Composite parent, FormToolkit toolkit) {
            super.createButtons(parent, toolkit);
            enableButtons();
            if (createCount()) {
                Composite comp = toolkit.createComposite(this.fButtonContainer);
                comp.setLayout(createButtonsLayout());
                comp.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_END | GridData.FILL_BOTH));
                this.fCount = toolkit.createLabel(comp, ""); //$NON-NLS-1$
                this.fCount.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
                this.fCount.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                getTablePart().getTableViewer().getTable().addPaintListener(new PaintListener() {

                    public void paintControl(PaintEvent e) {
                        updateLabel();
                    }
                });
            }
        }

        @Override
        public void entryModified(Object entry, String value) {
            DependenciesSection.this.entryModified(entry, value);
        }

        @Override
        public void handleDoubleClick(IStructuredSelection selection) {
            DependenciesSection.this.handleDoubleClick(selection);
        }

        @Override
        public void selectionChanged(IStructuredSelection selection) {
            getManagedForm().fireSelectionChanged(DependenciesSection.this, selection);
            DependenciesSection.this.selectionChanged(selection);
        }

        protected void updateLabel() {
            if (this.fCount != null && !this.fCount.isDisposed()) {
                this.fCount.setText(NLS.bind(PDEUIMessages.TableSection_itemCount, Integer.toString(getTableViewer().getTable().getItemCount())));
            }
        }
    }

    // RequiresSection
    private static final int ADD_INDEX = 0;

    private static final int REMOVE_INDEX = 1;

    private static final int UP_INDEX = 2;

    private static final int DOWN_INDEX = 3;

    private static final int PROPERTIES_INDEX = 4;

    private TableViewer fImportViewer;

    // TableSection
    protected boolean fHandleDefaultButton = true;

    // StructuredViewerSection
    private final StructuredViewerPart fViewerPart;

    public DependenciesSection(FormPage page, Composite parent, String[] buttonLabels) {
        super(parent, page.getManagedForm().getToolkit(), Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);

        // PDESection
        initialize(page.getManagedForm());
        getSection().clientVerticalSpacing = FormLayoutFactory.SECTION_HEADER_VERTICAL_SPACING;
        getSection().setData("part", this); //$NON-NLS-1$

        // StructuredViewerSection
        this.fViewerPart = createViewerPart(buttonLabels);
        this.fViewerPart.setMinimumSize(50, 50);
        createClient(getSection(), page.getManagedForm().getToolkit());

        // RequiresSection
        getSection().setText(PDEUIMessages.RequiresSection_title);
        getTablePart().setEditable(false);
    }

    /**
     * @see RequiresSection
     */
    private void buttonSelected(int index) {
        switch (index) {
            case ADD_INDEX:
                handleAdd();
                break;
            case REMOVE_INDEX:
                handleRemove();
                break;
            case UP_INDEX:
                handleUp();
                break;
            case DOWN_INDEX:
                handleDown();
                break;
            case PROPERTIES_INDEX:
                handleOpenProperties();
                break;
        }
    }

    /**
     * @see RequiresSection
     */
    private void createClient(Section section, FormToolkit toolkit) {
        Composite container = createClientContainer(section, 2, toolkit);
        createViewerPartControl(container, SWT.MULTI, 2, toolkit);
        TablePart tablePart = getTablePart();
        this.fImportViewer = tablePart.getTableViewer();

        toolkit.paintBordersFor(container);
        // makeActions();
        section.setClient(container);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.minimumWidth = 250;
        gd.grabExcessVerticalSpace = true;
        section.setLayout(FormLayoutFactory.createClearGridLayout(false, 1));
        section.setLayoutData(gd);
        section.setText(PDEUIMessages.RequiresSection_title);
        createSectionToolbar(section, toolkit);
        enableButtons();
    }

    /**
     * @see StructuredViewerSection
     */
    private Composite createClientContainer(Composite parent, int span, FormToolkit toolkit) {
        Composite container = toolkit.createComposite(parent);
        container.setLayout(FormLayoutFactory.createSectionClientGridLayout(false, span));
        return container;
    }

    /**
     * @see RequiresSection
     */
    private boolean createCount() {
        return true;
    }

    /**
     * @see RequiresSection
     */
    private void createSectionToolbar(Section section, FormToolkit toolkit) {
        ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
        ToolBar toolbar = toolBarManager.createControl(section);
        final Cursor handCursor = new Cursor(Display.getCurrent(), SWT.CURSOR_HAND);
        toolbar.setCursor(handCursor);
        // Cursor needs to be explicitly disposed
        toolbar.addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                if (handCursor != null && handCursor.isDisposed() == false) {
                    handCursor.dispose();
                }
            }
        });

        // Add sort action to the tool bar
        // fSortAction = new SortAction(fImportViewer,
        // PDEUIMessages.RequiresSection_sortAlpha, null, null, this);
        // toolBarManager.add(fSortAction);

        toolBarManager.update(true);
        section.setTextClient(toolbar);
    }

    /**
     * @see TableSection
     */
    private StructuredViewerPart createViewerPart(String[] buttonLabels) {
        return new PartAdapter(buttonLabels);
    }

    /**
     * @see StructuredViewerSection
     */
    private void createViewerPartControl(Composite parent, int style, int span, FormToolkit toolkit) {
        this.fViewerPart.createControl(parent, style, span, toolkit);
        MenuManager popupMenuManager = new MenuManager();
        IMenuListener listener = new IMenuListener() {

            public void menuAboutToShow(IMenuManager mng) {
                // fillContextMenu(mng);
            }
        };
        popupMenuManager.addMenuListener(listener);
        popupMenuManager.setRemoveAllWhenShown(true);
        Control control = this.fViewerPart.getControl();
        Menu menu = popupMenuManager.createContextMenu(control);
        control.setMenu(menu);
    }

    /**
     * @see TableSection
     */
    protected abstract void enableButtons();

    /**
     * @see TableSection
     */
    protected abstract void entryModified(Object entry, String value);

    protected int getAddIndex() {
        return ADD_INDEX;
    }

    protected int getDownIndex() {
        return DOWN_INDEX;
    }

    protected int getPropertiesIndex() {
        return PROPERTIES_INDEX;
    }

    protected int getRemoveIndex() {
        return REMOVE_INDEX;
    }

    /**
     * @see TableSection
     */
    protected EditableTablePart getTablePart() {
        return (EditableTablePart) this.fViewerPart;
    }

    protected TableViewer getTableViewer() {
        return this.fImportViewer;
    }

    protected int getUpIndex() {
        return UP_INDEX;
    }

    /**
     * @see RequiresSection
     */
    protected abstract void handleAdd();

    /**
     * @see TableSection
     */
    protected abstract void handleDoubleClick(IStructuredSelection selection);

    /**
     * @see RequiresSection
     */
    protected abstract void handleDown();

    /**
     * @see RequiresSection
     */
    protected abstract void handleOpenProperties();

    /**
     * @see RequiresSection
     */
    protected abstract void handleRemove();

    /**
     * @see RequiresSection
     */
    protected abstract void handleUp();

    /**
     * @see RequiresSection
     */
    protected abstract void initialize();

    /**
     * @see TableSection
     */
    protected abstract void selectionChanged(IStructuredSelection selection);

}
