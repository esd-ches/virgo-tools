/*******************************************************************************
 * Copyright (c) 2009, 2012 SpringSource, a divison of VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource, a division of VMware, Inc. - initial API and implementation
 *     GianMaria Romanato
 *******************************************************************************/

package org.eclipse.virgo.ide.runtime.internal.ui.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.virgo.ide.runtime.core.IServerWorkingCopy;
import org.eclipse.virgo.ide.runtime.internal.core.actions.ModifyArtefactOrderCommand;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.ui.ServerUICore;
import org.eclipse.wst.server.ui.editor.ServerEditorSection;

/**
 * {@link ServerEditorSection} section that allows to configure the JMX deployer credentials.
 *
 * Largely rewritten to allow multiple selection and to avoid that user actions are immediately applied to the server
 * configuration. Changes are now applied only when the user saves the editor.
 *
 * @author Christian Dupuis
 * @author Steffen Pingel
 * @author GianMaria Romanato - apply changes to configuration only when editor is saved
 * @since 1.0.1
 */
public class ArtefactOrderEditorSection extends ServerEditorSection {

    /**
     * An operation used for making ordering changes in the UI, triggering dirty state and supporting UNDO.
     *
     * Changes are applied to the server only when the editor is saved using a different operation, see
     * {@link ArtefactOrderEditorSection#doSave(IProgressMonitor)}
     */
    private class ModifyArtefactOrderEditorCommand extends AbstractOperation {

        private List<IModule> oldOrder;

        private final List<IModule> newOrder;

        ModifyArtefactOrderEditorCommand(List<IModule> newOrder) {
            super("Modefy artefact order in editor UI"); //$NON-NLS-1$
            this.newOrder = newOrder;
        }

        @Override
        public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
            oldOrder = ArtefactOrderEditorSection.this.orderedModules;
            ArtefactOrderEditorSection.this.orderedModules = newOrder;
            bundleTableViewer.setInput(orderedModules);
            return Status.OK_STATUS;
        }

        @Override
        public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
            return execute(monitor, info);
        }

        @Override
        public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
            ArtefactOrderEditorSection.this.orderedModules = oldOrder;
            oldOrder = null;
            bundleTableViewer.setInput(orderedModules);
            return Status.OK_STATUS;
        }

    }

    protected IServerWorkingCopy serverWorkingCopy;

    // represents the model for the table
    private List<IModule> orderedModules;

    protected boolean updating;

    protected PropertyChangeListener listener;

    private Table bundleTable;

    private TableViewer bundleTableViewer;

    private Button upButton;

    private Button downButton;

    protected void addConfigurationChangeListener() {
        listener = new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent event) {
                if (updating) {
                    return;
                }
                updating = true;
                if (org.eclipse.virgo.ide.runtime.core.IServer.PROPERTY_ARTEFACT_ORDER.equals(event.getPropertyName())) {
                    initialize();
                }
                updating = false;
            }
        };
        serverWorkingCopy.addConfigurationChangeListener(listener);
    }

    @Override
    public void createSection(Composite parent) {
        super.createSection(parent);
        FormToolkit toolkit = getFormToolkit(parent.getDisplay());

        Section section = toolkit.createSection(parent, ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED | ExpandableComposite.TITLE_BAR
            | Section.DESCRIPTION | ExpandableComposite.FOCUS_TITLE);
        section.setText(Messages.ArtefactOrderEditorSection_title);
        section.setDescription(Messages.ArtefactOrderEditorSection_description);
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));

        Composite composite = toolkit.createComposite(section);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.marginHeight = 5;
        layout.marginWidth = 1;
        layout.verticalSpacing = 5;
        layout.horizontalSpacing = 1;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL, GridData.FILL_VERTICAL, true, true));
        toolkit.paintBordersFor(composite);
        section.setClient(composite);

        bundleTable = toolkit.createTable(composite, SWT.MULTI | SWT.V_SCROLL | SWT.FULL_SELECTION);
        GridData data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
        int modulesNumber = server.getModules().length;
        data.heightHint = bundleTable.getItemHeight() * Math.min(Math.max(5, modulesNumber), 10) + bundleTable.getBorderWidth() * 2;
        bundleTable.setLayoutData(data);
        bundleTableViewer = new TableViewer(bundleTable);
        bundleTableViewer.setContentProvider(new ArrayContentProvider());
        bundleTableViewer.setLabelProvider(ServerUICore.getLabelProvider());

        bundleTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                updateButtons(event.getSelection());
            }

        });

        Composite buttonComposite = new Composite(composite, SWT.NONE);
        buttonComposite.setLayout(new GridLayout(1, true));
        data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
        buttonComposite.setLayoutData(data);

        upButton = toolkit.createButton(buttonComposite, Messages.ArtefactOrderEditorSection_up_button, SWT.PUSH);
        data = new GridData();
        upButton.setLayoutData(data);
        upButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                IStructuredSelection selection = (IStructuredSelection) bundleTableViewer.getSelection();
                List<IModule> allModules = new ArrayList<IModule>(orderedModules);

                for (IModule aModule : (List<IModule>) selection.toList()) {
                    int index = allModules.indexOf(aModule);
                    allModules.remove(aModule);
                    allModules.add(index - 1, aModule);
                }

                execute(new ModifyArtefactOrderEditorCommand(allModules));
                updateButtons(selection);
            }
        });
        downButton = toolkit.createButton(buttonComposite, Messages.ArtefactOrderEditorSection_down_button, SWT.PUSH);
        downButton.setLayoutData(data);
        downButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                IStructuredSelection selection = (IStructuredSelection) bundleTableViewer.getSelection();
                List<IModule> allModules = new ArrayList<IModule>(orderedModules);

                List<IModule> reversedSelection = new ArrayList<IModule>(selection.toList());
                Collections.reverse(reversedSelection);

                for (IModule aModule : reversedSelection) {
                    int index = allModules.indexOf(aModule);
                    allModules.remove(aModule);
                    allModules.add(index + 1, aModule);
                }

                execute(new ModifyArtefactOrderEditorCommand(allModules));
                updateButtons(selection);
            }
        });
        initialize();
    }

    /**
     * @see ServerEditorSection#dispose()
     */
    @Override
    public void dispose() {
        if (server != null) {
            server.removePropertyChangeListener(listener);
        }
    }

    /**
     * @see ServerEditorSection#init(IEditorSite, IEditorInput)
     */
    @Override
    public void init(IEditorSite site, IEditorInput input) {
        super.init(site, input);

        serverWorkingCopy = (IServerWorkingCopy) server.loadAdapter(IServerWorkingCopy.class, new NullProgressMonitor());

        addConfigurationChangeListener();
    }

    /**
     * Initialize model and view.
     */
    protected void initialize() {
        final List<String> orderedArtefacts = serverWorkingCopy.getConfiguration().getArtefactOrder();

        orderedModules = new ArrayList(Arrays.asList(server.getModules()));

        // sort the modules according the order defined in the server configuration
        Collections.sort(orderedModules, new java.util.Comparator<IModule>() {

            public int compare(IModule o1, IModule o2) {
                Integer m1 = (orderedArtefacts.contains(o1.getId()) ? orderedArtefacts.indexOf(o1.getId()) : Integer.MAX_VALUE);
                Integer m2 = (orderedArtefacts.contains(o2.getId()) ? orderedArtefacts.indexOf(o2.getId()) : Integer.MAX_VALUE);
                return m1.compareTo(m2);
            }
        });
        bundleTableViewer.setInput(orderedModules);
    }

    private void updateButtons(ISelection selections) {
        IStructuredSelection ss = (IStructuredSelection) selections;
        List<IModule> selectedModules = ss.toList();

        List<IModule> allModules = orderedModules;

        final int lowerBound = allModules.size() - 1;
        boolean initialState = !selections.isEmpty() && !allModules.isEmpty();
        boolean canMoveUp = initialState;
        boolean canMoveDown = initialState;

        for (int i = 0; i < selectedModules.size() && initialState; i++) {
            IModule obj = selectedModules.get(i);
            int index = allModules.indexOf(obj);
            canMoveUp = canMoveUp && index > 0;
            canMoveDown = canMoveDown && index < lowerBound;
        }

        upButton.setEnabled(canMoveUp);
        downButton.setEnabled(canMoveDown);
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        List<String> artefactOrder = new ArrayList<String>();
        for (Object module : orderedModules) {
            artefactOrder.add(((IModule) module).getId());
        }

        if (!serverWorkingCopy.getArtefactOrder().equals(artefactOrder)) {
            updating = true;
            execute(new ModifyArtefactOrderCommand(serverWorkingCopy, artefactOrder));
            updating = false;
        }
    }

}
