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

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.virgo.ide.runtime.core.IServerWorkingCopy;
import org.eclipse.virgo.ide.runtime.internal.core.actions.ModifyCleanStartupCommand;
import org.eclipse.virgo.ide.runtime.internal.core.actions.ModifyTailLogFilesCommand;
import org.eclipse.wst.server.ui.editor.ServerEditorSection;

/**
 * {@link ServerEditorSection} section that allows to configure the JMX deployer credentials
 * 
 * @author Christian Dupuis
 * @since 1.0.1
 */
public class StartupEditorSection extends ServerEditorSection {

	protected IServerWorkingCopy serverWorkingCopy;

	protected boolean updating;

	protected PropertyChangeListener listener;

	private Button tailLogFiles;

	private Button cleanStartup;

	protected void addConfigurationChangeListener() {
		listener = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (updating) {
					return;
				}
				updating = true;
				if (IServerWorkingCopy.PROPERTY_TAIL_LOG_FILES.equals(event.getPropertyName())) {
					tailLogFiles.setSelection(Boolean.valueOf(event.getNewValue().toString()));
				} else if (IServerWorkingCopy.PROPERTY_CLEAN_STARTUP.equals(event.getPropertyName())) {
					cleanStartup.setSelection(Boolean.valueOf(event.getNewValue().toString()));
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
		section.setText("Server Startup Configuration");
		section.setDescription("Specify startup options. Changing a setting requires a server restart.");
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));

		Composite composite = toolkit.createComposite(section);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginHeight = 5;
		layout.marginWidth = 10;
		layout.verticalSpacing = 5;
		layout.horizontalSpacing = 15;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));
		toolkit.paintBordersFor(composite);
		section.setClient(composite);

		GridData data = new GridData(SWT.FILL, SWT.TOP, true, false);

		tailLogFiles = toolkit.createButton(composite, "Tail application trace files into Console view", SWT.CHECK);
		tailLogFiles.setLayoutData(data);
		tailLogFiles.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (updating) {
					return;
				}
				updating = true;
				execute(new ModifyTailLogFilesCommand(serverWorkingCopy, tailLogFiles.getSelection()));
				updating = false;
			}
		});

		cleanStartup = toolkit.createButton(composite, "Start server with -clean option", SWT.CHECK);
		cleanStartup.setLayoutData(data);
		cleanStartup.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (updating) {
					return;
				}
				updating = true;
				execute(new ModifyCleanStartupCommand(serverWorkingCopy, cleanStartup.getSelection()));
				updating = false;
			}
		});

		toolkit.createLabel(composite, "");

		initialize();
	}

	/**
	 * @see ServerEditorSection#dispose()
	 */
	public void dispose() {
		if (server != null) {
			server.removePropertyChangeListener(listener);
		}
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
		this.tailLogFiles.setSelection(serverWorkingCopy.shouldTailTraceFiles());
		this.cleanStartup.setSelection(serverWorkingCopy.shouldCleanStartup());
		updating = false;
	}

}
