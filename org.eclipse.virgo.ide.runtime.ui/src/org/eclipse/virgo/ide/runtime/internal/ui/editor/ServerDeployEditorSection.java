/*******************************************************************************
 * Copyright (c) 2009, 2015 SpringSource, a divison of VMware, Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource, a division of VMware, Inc. - initial API and implementation
 *     GianMaria Romanato - externalize strings and perform changes only on save
 *******************************************************************************/

package org.eclipse.virgo.ide.runtime.internal.ui.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
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
import org.eclipse.virgo.ide.runtime.internal.core.actions.ModifyDeployerPortCommand;
import org.eclipse.virgo.ide.runtime.internal.core.actions.ModifyDeployerTimeoutCommand;
import org.eclipse.virgo.ide.runtime.internal.ui.ServerUiPlugin;
import org.eclipse.wst.server.ui.editor.ServerEditorSection;

/**
 * {@link ServerEditorSection} section that allows to configure the JMX deployer credentials
 *
 * @author Christian Dupuis
 * @since 1.0.1
 */
public class ServerDeployEditorSection extends ServerEditorSection {

    class ChangePortUICommand extends AbstractOperation {

        private int oldValue;

        private int newValue;

        public ChangePortUICommand(int newValue) {
            super("Change deployer port"); //$NON-NLS-1$
            this.newValue = newValue;
        }

        @Override
        public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
            this.oldValue = ServerDeployEditorSection.this.port;
            ServerDeployEditorSection.this.port = this.newValue;
            ServerDeployEditorSection.this.updating = true;
            initialize();
            ServerDeployEditorSection.this.updating = false;
            return Status.OK_STATUS;
        }

        @Override
        public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
            return execute(monitor, info);
        }

        @Override
        public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
            this.newValue = ServerDeployEditorSection.this.port;
            ServerDeployEditorSection.this.port = this.oldValue;
            ServerDeployEditorSection.this.updating = true;
            initialize();
            ServerDeployEditorSection.this.updating = false;
            return Status.OK_STATUS;
        }
    }

    class ChangeDeployerTimeoutUICommand extends AbstractOperation {

        private int oldValue;

        private int newValue;

        public ChangeDeployerTimeoutUICommand(int newValue) {
            super("Change deployer timeout"); //$NON-NLS-1$
            this.newValue = newValue;
        }

        @Override
        public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
            this.oldValue = ServerDeployEditorSection.this.timeout;
            ServerDeployEditorSection.this.timeout = this.newValue;
            ServerDeployEditorSection.this.updating = true;
            initialize();
            ServerDeployEditorSection.this.updating = false;
            return Status.OK_STATUS;
        }

        @Override
        public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
            return execute(monitor, info);
        }

        @Override
        public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
            this.newValue = ServerDeployEditorSection.this.timeout;
            ServerDeployEditorSection.this.timeout = this.oldValue;
            ServerDeployEditorSection.this.updating = true;
            initialize();
            ServerDeployEditorSection.this.updating = false;
            return Status.OK_STATUS;
        }
    }

    protected IServerWorkingCopy serverWorkingCopy;

    protected int port;

    protected Text portText;

    protected int timeout;

    protected Text timeoutText;

    protected boolean updating;

    protected PropertyChangeListener listener;

    protected void addConfigurationChangeListener() {
        this.listener = new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent event) {
                if (ServerDeployEditorSection.this.updating) {
                    return;
                }
                ServerDeployEditorSection.this.updating = true;
                if (IServer.PROPERTY_DEPLOY_TIMEOUT.equals(event.getPropertyName())) {
                    ServerDeployEditorSection.this.timeoutText.setText(event.getNewValue().toString());
                } else if (IServer.PROPERTY_MBEAN_SERVER_PORT.equals(event.getPropertyName())) {
                    ServerDeployEditorSection.this.portText.setText(event.getNewValue().toString());
                }
                ServerDeployEditorSection.this.updating = false;
            }
        };
        this.serverWorkingCopy.addConfigurationChangeListener(this.listener);
    }

    @Override
    public void createSection(Composite parent) {
        super.createSection(parent);
        FormToolkit toolkit = getFormToolkit(parent.getDisplay());

        Section section = toolkit.createSection(parent, ExpandableComposite.TWISTIE /* | ExpandableComposite.EXPANDED */
            | ExpandableComposite.TITLE_BAR | Section.DESCRIPTION | ExpandableComposite.FOCUS_TITLE);
        section.setText(Messages.ServerDeployEditorSection_title);
        section.setDescription(Messages.ServerDeployEditorSection_description);
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));

        Composite composite = toolkit.createComposite(section);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL, GridData.FILL_VERTICAL, true, false));
        toolkit.paintBordersFor(composite);
        section.setClient(composite);

        Label portLabel = toolkit.createLabel(composite, Messages.ServerDeployEditorSection_port_label);
        portLabel.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
        this.portText = toolkit.createText(composite, ""); //$NON-NLS-1$
        GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        this.portText.setLayoutData(data);

        Label timeoutLabel = toolkit.createLabel(composite, Messages.ServerDeployEditorSection_timeout_label);
        timeoutLabel.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
        this.timeoutText = toolkit.createText(composite, ""); //$NON-NLS-1$
        this.timeoutText.setLayoutData(data);

        this.port = this.serverWorkingCopy.getMBeanServerPort();
        this.timeout = this.serverWorkingCopy.getDeployTimeout();

        initialize();
        addConfigurationChangeListener();

        this.portText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                if (ServerDeployEditorSection.this.updating) {
                    return;
                }
                getManagedForm().getMessageManager().removeMessages(ServerDeployEditorSection.this.portText);
                int newPort = -1;
                try {
                    newPort = Integer.valueOf(ServerDeployEditorSection.this.portText.getText());
                } catch (NumberFormatException nfe) {
                    getManagedForm().getMessageManager().addMessage("MALFORMED-PORT", //$NON-NLS-1$
                        Messages.ServerDeployEditorSection_invalid_port_form_error, null, IMessageProvider.ERROR,
                        ServerDeployEditorSection.this.portText);
                    return;
                }
                setErrorMessage(null);
                ServerDeployEditorSection.this.updating = true;
                execute(new ChangePortUICommand(newPort));
                ServerDeployEditorSection.this.updating = false;
            }
        });

        this.timeoutText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                if (ServerDeployEditorSection.this.updating) {
                    return;
                }
                getManagedForm().getMessageManager().removeMessages(ServerDeployEditorSection.this.timeoutText);
                Integer newTimeout = null;
                try {
                    newTimeout = Integer.valueOf(ServerDeployEditorSection.this.timeoutText.getText());
                } catch (NumberFormatException nfe) {
                    getManagedForm().getMessageManager().addMessage("MALFORMED-TIMEOUT", //$NON-NLS-1$
                        Messages.ServerDeployEditorSection_invalid_timeout_form_error, null, IMessageProvider.ERROR,
                        ServerDeployEditorSection.this.timeoutText);
                    return;
                }
                if (newTimeout < 5) {
                    getManagedForm().getMessageManager().addMessage("INVALID-TIMEOUT", //$NON-NLS-1$
                        Messages.ServerDeployEditorSection_timeout_too_small_form_error, null, IMessageProvider.ERROR,
                        ServerDeployEditorSection.this.timeoutText);
                    return;
                }
                ServerDeployEditorSection.this.updating = true;
                execute(new ChangeDeployerTimeoutUICommand(newTimeout));
                ServerDeployEditorSection.this.updating = false;
            }
        });
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
    }

    /**
     * Initialize the fields in this editor.
     */
    protected void initialize() {
        this.portText.setText(Integer.toString(this.port));
        this.timeoutText.setText(Integer.toString(this.timeout));
    }

    @Override
    public IStatus[] getSaveStatus() {
        try {
            Integer.valueOf(this.portText.getText());
        } catch (NumberFormatException nfe) {
            String errorMessage = NLS.bind(Messages.ServerDeployEditorSection_invalid_port_save_message, this.portText.getText());
            return new IStatus[] { new Status(IStatus.ERROR, ServerUiPlugin.PLUGIN_ID, errorMessage) };
        }
        try {
            Integer.valueOf(this.timeoutText.getText());
        } catch (NumberFormatException nfe) {
            String errorMessage = NLS.bind(Messages.ServerDeployEditorSection_invalid_timeout_save_message, this.timeoutText.getText());
            return new IStatus[] { new Status(IStatus.ERROR, ServerUiPlugin.PLUGIN_ID, errorMessage) };
        }
        return super.getSaveStatus();
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        if (this.serverWorkingCopy.getDeployTimeout() != this.timeout) {
            execute(new ModifyDeployerTimeoutCommand(this.serverWorkingCopy, this.timeout));
        }
        if (this.serverWorkingCopy.getMBeanServerPort() != this.port) {
            execute(new ModifyDeployerPortCommand(this.serverWorkingCopy, this.port));
        }
        super.doSave(monitor);

    }
}
