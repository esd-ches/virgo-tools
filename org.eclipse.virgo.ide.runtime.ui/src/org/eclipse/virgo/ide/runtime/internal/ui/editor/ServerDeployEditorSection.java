/*******************************************************************************
 * Copyright (c) 2009, 2010 SpringSource, a divison of VMware, Inc.
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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IMessageProvider;
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

    protected IServerWorkingCopy serverWorkingCopy;

    protected Text port;

    protected Text timeout;

    protected Text username;

    protected Text password;

    protected boolean updating;

    protected PropertyChangeListener listener;

    protected void addConfigurationChangeListener() {
        this.listener = new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent event) {
                if (ServerDeployEditorSection.this.updating) {
                    return;
                }
                ServerDeployEditorSection.this.updating = true;
                if (IServer.PROPERTY_MBEAN_SERVER_PASSWORD.equals(event.getPropertyName())) {
                    ServerDeployEditorSection.this.password.setText(event.getNewValue().toString());
                } else if (IServer.PROPERTY_DEPLOY_TIMEOUT.equals(event.getPropertyName())) {
                    ServerDeployEditorSection.this.timeout.setText(event.getNewValue().toString());
                } else if (IServer.PROPERTY_MBEAN_SERVER_PORT.equals(event.getPropertyName())) {
                    ServerDeployEditorSection.this.port.setText(event.getNewValue().toString());
                } else if (IServer.PROPERTY_MBEAN_SERVER_USERNAME.equals(event.getPropertyName())) {
                    ServerDeployEditorSection.this.username.setText(event.getNewValue().toString());
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

        Section section = toolkit.createSection(parent, ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED | ExpandableComposite.TITLE_BAR
            | Section.DESCRIPTION | ExpandableComposite.FOCUS_TITLE);
        section.setText("Deployer Control");
        section.setDescription("Configure the communication with the server Deployer Control.");
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));

        Composite composite = toolkit.createComposite(section);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.marginHeight = 5;
        layout.marginWidth = 10;
        layout.verticalSpacing = 5;
        layout.horizontalSpacing = 15;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));
        toolkit.paintBordersFor(composite);
        section.setClient(composite);

        GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);

        Label portLabel = toolkit.createLabel(composite, "Port:");
        portLabel.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
        this.port = toolkit.createText(composite, "");
        this.port.setLayoutData(data);
        this.port.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                if (ServerDeployEditorSection.this.updating) {
                    return;
                }
                getManagedForm().getMessageManager().removeMessages(ServerDeployEditorSection.this.port);
                int newPort = -1;
                try {
                    newPort = Integer.valueOf(ServerDeployEditorSection.this.port.getText());
                } catch (NumberFormatException nfe) {
                    getManagedForm().getMessageManager().addMessage("MALFORMED-PORT", "Port must be a positive number", null, IMessageProvider.ERROR,
                        ServerDeployEditorSection.this.port);
                    return;
                }
                setErrorMessage(null);
                ServerDeployEditorSection.this.updating = true;
                execute(new ModifyDeployerPortCommand(ServerDeployEditorSection.this.serverWorkingCopy, newPort));
                ServerDeployEditorSection.this.updating = false;
            }
        });

        Label timeoutLabel = toolkit.createLabel(composite, "Timeout:");
        timeoutLabel.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
        this.timeout = toolkit.createText(composite, "");
        this.timeout.setLayoutData(data);
        this.timeout.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                if (ServerDeployEditorSection.this.updating) {
                    return;
                }
                getManagedForm().getMessageManager().removeMessages(ServerDeployEditorSection.this.timeout);
                Integer newTimeout = null;
                try {
                    newTimeout = Integer.valueOf(ServerDeployEditorSection.this.timeout.getText());
                } catch (NumberFormatException nfe) {
                    getManagedForm().getMessageManager().addMessage("MALFORMED-TIMEOUT", "Timeout must be a positive number", null,
                        IMessageProvider.ERROR, ServerDeployEditorSection.this.timeout);
                    return;
                }
                if (newTimeout < 5) {
                    getManagedForm().getMessageManager().addMessage("INVALID-TIMEOUT", "Timeout cannot be less than 5", null, IMessageProvider.ERROR,
                        ServerDeployEditorSection.this.timeout);
                    return;
                }
                ServerDeployEditorSection.this.updating = true;
                execute(new ModifyDeployerTimeoutCommand(ServerDeployEditorSection.this.serverWorkingCopy, newTimeout));
                ServerDeployEditorSection.this.updating = false;
            }
        });

        // Label usernameLabel = toolkit.createLabel(composite, "Username:");
        // usernameLabel.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
        // username = toolkit.createText(composite, "");
        // username.setLayoutData(data);
        // username.addModifyListener(new ModifyListener() {
        // public void modifyText(ModifyEvent e) {
        // if (updating)
        // return;
        // updating = true;
        // execute(new ModifyDeployerUsernameCommand(serverWorkingCopy, username.getText()));
        // updating = false;
        // }
        // });
        //
        // Label passwordLabel = toolkit.createLabel(composite, "Password:");
        // passwordLabel.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
        // password = toolkit.createText(composite, "", SWT.PASSWORD);
        // password.setLayoutData(data);
        // password.addModifyListener(new ModifyListener() {
        // public void modifyText(ModifyEvent e) {
        // if (updating)
        // return;
        // updating = true;
        // execute(new ModifyDeployerPasswordCommand(serverWorkingCopy, password.getText()));
        // updating = false;
        // }
        // });

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
        this.port.setText("" + this.serverWorkingCopy.getMBeanServerPort());
        this.timeout.setText("" + this.serverWorkingCopy.getDeployTimeout());
        // this.username.setText(serverWorkingCopy.getDeployerUsername());
        // this.password.setText(serverWorkingCopy.getDeployerPassword());
        this.updating = false;
    }

    @Override
    public IStatus[] getSaveStatus() {
        // this errors should never happen as the port and timeout controls now prevent the user
        // from entering invalid values.
        try {
            Integer.valueOf(this.port.getText());
        } catch (NumberFormatException nfe) {
            return new IStatus[] { new Status(IStatus.ERROR, ServerUiPlugin.PLUGIN_ID, "'" + this.port.getText() + "' is not a valid port number") };
        }
        try {
            Integer.valueOf(this.timeout.getText());
        } catch (NumberFormatException nfe) {
            return new IStatus[] { new Status(IStatus.ERROR, ServerUiPlugin.PLUGIN_ID, "'" + this.timeout.getText() + "' is not a valid timeout") };
        }
        return super.getSaveStatus();
    }
}
