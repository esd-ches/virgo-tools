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
import org.eclipse.virgo.ide.runtime.core.IServerWorkingCopy;
import org.eclipse.virgo.ide.runtime.internal.core.actions.ModifyDeployerPortCommand;
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

	protected Text username;

	protected Text password;

	protected boolean updating;

	protected PropertyChangeListener listener;

	protected void addConfigurationChangeListener() {
		listener = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (updating)
					return;
				updating = true;
				if (IServerWorkingCopy.PROPERTY_MBEAN_SERVER_PASSWORD.equals(event.getPropertyName())) {
					password.setText(event.getNewValue().toString());
				} else if (IServerWorkingCopy.PROPERTY_MBEAN_SERVER_PORT.equals(event.getPropertyName())) {
					port.setText(event.getNewValue().toString());
				} else if (IServerWorkingCopy.PROPERTY_MBEAN_SERVER_USERNAME.equals(event.getPropertyName())) {
					username.setText(event.getNewValue().toString());
				}
				updating = false;
			}
		};
		serverWorkingCopy.addConfigurationChangeListener(listener);
	}

	public void createSection(Composite parent) {
		super.createSection(parent);
		FormToolkit toolkit = getFormToolkit(parent.getDisplay());

		Section section = toolkit.createSection(parent, ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED
				| ExpandableComposite.TITLE_BAR | Section.DESCRIPTION | ExpandableComposite.FOCUS_TITLE);
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
		port = toolkit.createText(composite, "");
		port.setLayoutData(data);
		port.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (updating)
					return;
				int newPort = -1;
				try {
					newPort = Integer.valueOf(port.getText());
				} catch (NumberFormatException nfe) {
					setErrorMessage(port.getText() + " is not a valid port number");
					return;
				}
				setErrorMessage(null);
				updating = true;
				execute(new ModifyDeployerPortCommand(serverWorkingCopy, newPort));
				updating = false;
			}
		});

//		Label usernameLabel = toolkit.createLabel(composite, "Username:");
//		usernameLabel.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
//		username = toolkit.createText(composite, "");
//		username.setLayoutData(data);
//		username.addModifyListener(new ModifyListener() {
//			public void modifyText(ModifyEvent e) {
//				if (updating)
//					return;
//				updating = true;
//				execute(new ModifyDeployerUsernameCommand(serverWorkingCopy, username.getText()));
//				updating = false;
//			}
//		});
//
//		Label passwordLabel = toolkit.createLabel(composite, "Password:");
//		passwordLabel.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
//		password = toolkit.createText(composite, "", SWT.PASSWORD);
//		password.setLayoutData(data);
//		password.addModifyListener(new ModifyListener() {
//			public void modifyText(ModifyEvent e) {
//				if (updating)
//					return;
//				updating = true;
//				execute(new ModifyDeployerPasswordCommand(serverWorkingCopy, password.getText()));
//				updating = false;
//			}
//		});

		initialize();
	}

	/**
	 * @see ServerEditorSection#dispose()
	 */
	public void dispose() {
		if (server != null)
			server.removePropertyChangeListener(listener);
	}

	/**
	 * @see ServerEditorSection#init(IEditorSite, IEditorInput)
	 */
	public void init(IEditorSite site, IEditorInput input) {
		super.init(site, input);
		serverWorkingCopy = (IServerWorkingCopy) server.loadAdapter(IServerWorkingCopy.class, new NullProgressMonitor());
		addConfigurationChangeListener();
	}

	/**
	 * Initialize the fields in this editor.
	 */
	protected void initialize() {
		updating = true;
		this.port.setText("" + serverWorkingCopy.getMBeanServerPort());
//		this.username.setText(serverWorkingCopy.getDeployerUsername());
//		this.password.setText(serverWorkingCopy.getDeployerPassword());
		updating = false;
	}

	@Override
	public IStatus[] getSaveStatus() {
		try {
			Integer.valueOf(port.getText());
		} catch (NumberFormatException nfe) {
			return new IStatus[] { new Status(IStatus.ERROR, ServerUiPlugin.PLUGIN_ID, "'" + port.getText()
					+ "' is not a valid port number") };
		}
		return super.getSaveStatus();
	}
}
