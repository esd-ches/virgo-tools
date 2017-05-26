/*******************************************************************************
 * Copyright (c) 2009 - 2013 SpringSource, a divison of VMware, Inc.
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

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.virgo.ide.runtime.core.IServer;
import org.eclipse.virgo.ide.runtime.core.IServerWorkingCopy;
import org.eclipse.virgo.ide.runtime.internal.core.actions.ModifyCleanStartupCommand;
import org.eclipse.virgo.ide.runtime.internal.core.actions.ModifyMaxPermSizeCommand;
import org.eclipse.virgo.ide.runtime.internal.core.actions.ModifyTailLogFilesCommand;
import org.eclipse.wst.server.ui.editor.ServerEditorSection;

/**
 * {@link ServerEditorSection} section that allows to configure some startup parameters
 *
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @since 1.0.1
 */
public class StartupEditorSection extends ServerEditorSection {

    protected IServerWorkingCopy serverWorkingCopy;

    protected boolean updating;

    protected PropertyChangeListener listener;

    private Button tailLogFiles;

    private Button cleanStartup;

    private Text maxPermSizeField;

    protected void addConfigurationChangeListener() {
        this.listener = new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent event) {
                if (StartupEditorSection.this.updating) {
                    return;
                }
                StartupEditorSection.this.updating = true;
                if (IServer.PROPERTY_TAIL_LOG_FILES.equals(event.getPropertyName())) {
                    StartupEditorSection.this.tailLogFiles.setSelection(Boolean.valueOf(event.getNewValue().toString()));
                } else if (IServer.PROPERTY_CLEAN_STARTUP.equals(event.getPropertyName())) {
                    StartupEditorSection.this.cleanStartup.setSelection(Boolean.valueOf(event.getNewValue().toString()));
                } else if (IServer.PROPERTY_MAX_PERM_SIZE.equals(event.getPropertyName())) {
                    StartupEditorSection.this.maxPermSizeField.setText(event.getNewValue().toString());
                }
                StartupEditorSection.this.updating = false;
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
        section.setText(Messages.StartupEditorSection_title);
        section.setDescription(Messages.StartupEditorSection_description);
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));

        Composite composite = toolkit.createComposite(section);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        toolkit.paintBordersFor(composite);
        section.setClient(composite);

        GridData data = new GridData(SWT.FILL, SWT.TOP, true, false);
        data.horizontalSpan = 2;

        this.tailLogFiles = toolkit.createButton(composite, Messages.StartupEditorSection_tail_into_console_button, SWT.CHECK);
        this.tailLogFiles.setLayoutData(data);
        this.tailLogFiles.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (StartupEditorSection.this.updating) {
                    return;
                }
                StartupEditorSection.this.updating = true;
                execute(new ModifyTailLogFilesCommand(StartupEditorSection.this.serverWorkingCopy,
                    StartupEditorSection.this.tailLogFiles.getSelection()));
                StartupEditorSection.this.updating = false;
            }
        });

        this.cleanStartup = toolkit.createButton(composite, Messages.StartupEditorSection_start_with_clean_button, SWT.CHECK);
        this.cleanStartup.setLayoutData(data);
        this.cleanStartup.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (StartupEditorSection.this.updating) {
                    return;
                }
                StartupEditorSection.this.updating = true;
                execute(new ModifyCleanStartupCommand(StartupEditorSection.this.serverWorkingCopy,
                    StartupEditorSection.this.cleanStartup.getSelection()));
                StartupEditorSection.this.updating = false;
            }
        });

        data = new GridData(SWT.FILL, SWT.CENTER, true, false);

        Label maxPermSizeLabel = toolkit.createLabel(composite, Messages.StartupEditorSection_maxpermsize_label);
        maxPermSizeLabel.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
        this.maxPermSizeField = toolkit.createText(composite, ""); //$NON-NLS-1$
        this.maxPermSizeField.setLayoutData(data);
        this.maxPermSizeField.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                if (StartupEditorSection.this.updating) {
                    return;
                }
                StartupEditorSection.this.updating = true;
                execute(
                    new ModifyMaxPermSizeCommand(StartupEditorSection.this.serverWorkingCopy, StartupEditorSection.this.maxPermSizeField.getText()));
                StartupEditorSection.this.updating = false;
            }
        });

        toolkit.createLabel(composite, ""); //$NON-NLS-1$

        initialize();
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
        this.tailLogFiles.setSelection(this.serverWorkingCopy.shouldTailTraceFiles());
        this.cleanStartup.setSelection(this.serverWorkingCopy.shouldCleanStartup());
        this.maxPermSizeField.setText(this.serverWorkingCopy.getMaxPermSize());
        this.updating = false;
    }

}
