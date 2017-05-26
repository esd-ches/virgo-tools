// TODO - externalize strings ""
// grid data della tabella non funziona
// controlli sul file name nel dialog di aggiunta

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

package org.eclipse.virgo.ide.runtime.internal.ui.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.virgo.ide.runtime.core.IServerWorkingCopy;
import org.eclipse.virgo.ide.runtime.internal.core.actions.ModifyStaticResourcesCommand;
import org.eclipse.virgo.ide.runtime.internal.ui.ServerUiImages;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.ui.editor.ServerEditorSection;

/**
 * {@link ServerEditorSection} section that allows to configure the JMX deployer credentials
 *
 * @author Christian Dupuis
 * @author Steffen Pingel
 * @since 1.1.3
 */
public class StaticResourcesEditorSection extends ServerEditorSection {

    protected IServerWorkingCopy serverWorkingCopy;

    private Table filenameTable;

    private TableViewer filenamesTableViewer;

    private Button addButton;

    private Button deleteButton;

    private Button upButton;

    private Button downButton;

    protected boolean updating;

    protected PropertyChangeListener listener;

    private StaticFilenamesContentProvider contentProvider;

    protected void addConfigurationChangeListener() {
        this.listener = new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent event) {
                if (org.eclipse.virgo.ide.runtime.core.IServer.PROPERTY_STATIC_FILENAMES.equals(event.getPropertyName())) {
                    StaticResourcesEditorSection.this.filenamesTableViewer.setInput(StaticResourcesEditorSection.this.server);
                }
            }
        };
        this.serverWorkingCopy.addConfigurationChangeListener(this.listener);
    }

    @Override
    public void createSection(Composite parent) {
        super.createSection(parent);
        FormToolkit toolkit = getFormToolkit(parent.getDisplay());

        Section section = toolkit.createSection(parent, ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED | ExpandableComposite.TITLE_BAR
            | Section.DESCRIPTION | ExpandableComposite.FOCUS_TITLE);
        section.setText(Messages.StaticResourcesEditorSection_title);
        section.setDescription(Messages.StaticResourcesEditorSection_description);
        section.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        Composite composite = toolkit.createComposite(section);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        toolkit.paintBordersFor(composite);
        section.setClient(composite);

        this.filenameTable = toolkit.createTable(composite, SWT.SINGLE | SWT.V_SCROLL | SWT.FULL_SELECTION);
        GridData data = new GridData(SWT.FILL, SWT.TOP, true, true);
        data.heightHint = filenameTable.getItemHeight() * 5 + filenameTable.getBorderWidth() * 2;

        // workaround for wrong item height in GTK3 Mars
        data.heightHint = Math.min(150, data.heightHint);

        this.filenameTable.setLayoutData(data);
        this.filenamesTableViewer = new TableViewer(this.filenameTable);
        this.contentProvider = new StaticFilenamesContentProvider();
        this.filenamesTableViewer.setContentProvider(this.contentProvider);
        this.filenamesTableViewer.setLabelProvider(new FilenameLabelProvider());

        this.filenamesTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                Object obj = ((IStructuredSelection) event.getSelection()).getFirstElement();
                StaticResourcesEditorSection.this.deleteButton.setEnabled(obj != null);
                updateButtons(obj);
            }
        });

        Composite buttonComposite = new Composite(composite, SWT.NONE);
        GridLayout buttonLayout = new GridLayout(1, true);
        buttonLayout.marginWidth = 0;
        buttonLayout.marginHeight = 0;
        buttonComposite.setLayout(buttonLayout);
        GridDataFactory.fillDefaults().applyTo(buttonComposite);

        this.addButton = toolkit.createButton(buttonComposite, Messages.StaticResourcesEditorSection_add_button, SWT.PUSH);
        GridDataFactory.fillDefaults().applyTo(this.addButton);
        this.addButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (StaticResourcesEditorSection.this.updating) {
                    return;
                }

                InputDialog dialog = new InputDialog(getShell(), Messages.StaticResourcesEditorSection_new_filename_dialog_title,
                    Messages.StaticResourcesEditorSection_new_filename_dialog_message, "", new IInputValidator() { //$NON-NLS-1$

                    public String isValid(String newText) {
                        if (!StringUtils.isNotBlank(newText)) {
                            return Messages.StaticResourcesEditorSection_empty_filename_error;
                        }

                        if ("*".equals(newText)) { //$NON-NLS-1$
                            return Messages.StaticResourcesEditorSection_wildcard_too_greedy;
                        }

                        String replaceWildcards = newText.replaceAll("\\?", "a").replaceAll("\\*", "b"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                        if (!Path.isValidWindowsSegment(replaceWildcards) || !Path.isValidWindowsSegment(replaceWildcards)) {
                            return Messages.StaticResourcesEditorSection_invalid_path;
                        }
                        return null;
                    }
                });
                if (dialog.open() == Window.OK) {
                    StaticResourcesEditorSection.this.updating = true;
                    List<Object> filenames = new ArrayList<Object>(
                        Arrays.asList(StaticResourcesEditorSection.this.contentProvider.getElements(StaticResourcesEditorSection.this.server)));
                    filenames.add(dialog.getValue());
                    execute(new ModifyStaticResourcesCommand(StaticResourcesEditorSection.this.serverWorkingCopy, StringUtils.join(filenames, ","))); //$NON-NLS-1$
                    StaticResourcesEditorSection.this.filenamesTableViewer.setInput(StaticResourcesEditorSection.this.server);
                    StaticResourcesEditorSection.this.filenamesTableViewer.setSelection(new StructuredSelection(dialog.getValue()));
                    // update buttons
                    StaticResourcesEditorSection.this.updating = false;
                }
            }
        });

        this.deleteButton = toolkit.createButton(buttonComposite, Messages.StaticResourcesEditorSection_delete_button, SWT.PUSH);
        GridDataFactory.fillDefaults().applyTo(this.deleteButton);
        this.deleteButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Object selectedArtefact = ((IStructuredSelection) StaticResourcesEditorSection.this.filenamesTableViewer.getSelection()).getFirstElement();
                if (StaticResourcesEditorSection.this.updating) {
                    return;
                }
                StaticResourcesEditorSection.this.updating = true;
                List<Object> filenames = new ArrayList<Object>(
                    Arrays.asList(StaticResourcesEditorSection.this.contentProvider.getElements(StaticResourcesEditorSection.this.server)));
                filenames.remove(selectedArtefact);
                execute(new ModifyStaticResourcesCommand(StaticResourcesEditorSection.this.serverWorkingCopy, StringUtils.join(filenames, ","))); //$NON-NLS-1$
                StaticResourcesEditorSection.this.filenamesTableViewer.setInput(StaticResourcesEditorSection.this.server);
                // update buttons
                StaticResourcesEditorSection.this.updating = false;
            }
        });

        this.upButton = toolkit.createButton(buttonComposite, Messages.StaticResourcesEditorSection_up_button, SWT.PUSH);
        GridDataFactory.fillDefaults().applyTo(this.upButton);
        this.upButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Object selectedArtefact = ((IStructuredSelection) StaticResourcesEditorSection.this.filenamesTableViewer.getSelection()).getFirstElement();
                List<Object> modules = new ArrayList<Object>();
                modules.addAll(
                    Arrays.asList(StaticResourcesEditorSection.this.contentProvider.getElements(StaticResourcesEditorSection.this.server)));
                int index = modules.indexOf(selectedArtefact);
                modules.remove(selectedArtefact);
                modules.add(index - 1, selectedArtefact);
                if (StaticResourcesEditorSection.this.updating) {
                    return;
                }
                StaticResourcesEditorSection.this.updating = true;
                execute(new ModifyStaticResourcesCommand(StaticResourcesEditorSection.this.serverWorkingCopy, StringUtils.join(modules, ","))); //$NON-NLS-1$
                StaticResourcesEditorSection.this.filenamesTableViewer.setInput(StaticResourcesEditorSection.this.server);
                updateButtons(selectedArtefact);
                StaticResourcesEditorSection.this.updating = false;
            }
        });

        this.downButton = toolkit.createButton(buttonComposite, Messages.StaticResourcesEditorSection_down_button, SWT.PUSH);
        GridDataFactory.fillDefaults().applyTo(this.downButton);
        this.downButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Object selectedArtefact = ((IStructuredSelection) StaticResourcesEditorSection.this.filenamesTableViewer.getSelection()).getFirstElement();
                List<Object> modules = new ArrayList<Object>();
                modules.addAll(
                    Arrays.asList(StaticResourcesEditorSection.this.contentProvider.getElements(StaticResourcesEditorSection.this.server)));
                int index = modules.indexOf(selectedArtefact);
                modules.remove(selectedArtefact);
                modules.add(index + 1, selectedArtefact);
                if (StaticResourcesEditorSection.this.updating) {
                    return;
                }
                StaticResourcesEditorSection.this.updating = true;
                execute(new ModifyStaticResourcesCommand(StaticResourcesEditorSection.this.serverWorkingCopy, StringUtils.join(modules, ","))); //$NON-NLS-1$
                StaticResourcesEditorSection.this.filenamesTableViewer.setInput(StaticResourcesEditorSection.this.server);
                updateButtons(selectedArtefact);
                StaticResourcesEditorSection.this.updating = false;
            }
        });

        FormText restoreDefault = toolkit.createFormText(composite, true);
        restoreDefault.setText(Messages.StaticResourcesEditorSection_14, true, false);
        restoreDefault.addHyperlinkListener(new HyperlinkAdapter() {

            @Override
            public void linkActivated(HyperlinkEvent e) {
                StaticResourcesEditorSection.this.updating = true;
                execute(new ModifyStaticResourcesCommand(StaticResourcesEditorSection.this.serverWorkingCopy,
                    IServerWorkingCopy.DEFAULT_STATIC_FILENAMES));
                StaticResourcesEditorSection.this.filenamesTableViewer.setInput(StaticResourcesEditorSection.this.server);
                StaticResourcesEditorSection.this.updating = false;
            }
        });
        updateEnablement();
        initialize();
    }

    private void updateButtons() {
        IStructuredSelection selection = (IStructuredSelection) this.filenamesTableViewer.getSelection();
        Object selectedArtefact = selection.getFirstElement();
        updateButtons(selectedArtefact);
    }

    private void updateButtons(Object obj) {
        if (obj instanceof String) {
            List<Object> modules = Arrays.asList(this.contentProvider.getElements(this.server));
            int index = modules.indexOf(obj);
            this.upButton.setEnabled(index > 0);
            this.downButton.setEnabled(index < modules.size() - 1);
            this.deleteButton.setEnabled(true);
        } else {
            this.upButton.setEnabled(false);
            this.downButton.setEnabled(false);
            this.deleteButton.setEnabled(false);
        }
    }

    private void updateEnablement() {
        this.addButton.setEnabled(true);
        updateButtons();
    }

    /**
     * @see ServerEditorSection#dispose()
     */
    @Override
    public void dispose() {
        if (this.server != null) {
            this.server.removePropertyChangeListener(this.listener);
        }
    }

    /**
     * @see ServerEditorSection#init(IEditorSite, IEditorInput)
     */
    @Override
    public void init(IEditorSite site, IEditorInput input) {
        super.init(site, input);
        this.serverWorkingCopy = (IServerWorkingCopy) this.server.loadAdapter(IServerWorkingCopy.class, new NullProgressMonitor());
        addConfigurationChangeListener();
    }

    /**
     * Initialize the fields in this editor.
     */
    protected void initialize() {
        this.updating = true;
        this.filenamesTableViewer.setInput(this.server);
        this.deleteButton.setEnabled(false);
        this.updating = false;
    }

    class StaticFilenamesContentProvider implements ITreeContentProvider {

        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

        }

        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof IServer) {
                IServer server = (IServer) inputElement;
                IServerWorkingCopy dmServer = (IServerWorkingCopy) server.loadAdapter(IServerWorkingCopy.class, null);
                String[] filenames = StringUtils.split(dmServer.getStaticFilenamePatterns(), ","); //$NON-NLS-1$
                return filenames;

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
            return ServerUiImages.getImage(ServerUiImages.IMG_OBJ_FILE);
        }

        @Override
        public String getText(Object element) {
            return element.toString();
        }

    }

}
