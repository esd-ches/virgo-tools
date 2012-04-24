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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
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
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.ui.ServerUICore;
import org.eclipse.wst.server.ui.editor.ServerEditorSection;

/**
 * {@link ServerEditorSection} section that allows to configure the JMX deployer credentials
 * 
 * @author Christian Dupuis
 * @author Steffen Pingel
 * @since 1.0.1
 */
public class ArtefactOrderEditorSection extends ServerEditorSection {

	protected IServerWorkingCopy serverWorkingCopy;

	protected boolean updating;

	protected PropertyChangeListener listener;

	private Table bundleTable;

	private TableViewer bundleTableViewer;

	private Button upButton;

	private Button downButton;

	private ArtefactContentProvider contentProvider;

	protected void addConfigurationChangeListener() {
		listener = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (updating) {
					return;
				}
				updating = true;
				if (IServerWorkingCopy.PROPERTY_ARTEFACT_ORDER.equals(event.getPropertyName())) {
					bundleTableViewer.setInput(server);
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
		section.setText("Artefact Deployment Order");
		section.setDescription("Specify the deployment order of targeted bundles and PARs on server startup.");
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));

		Composite composite = toolkit.createComposite(section);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 5;
		layout.marginWidth = 1;
		layout.verticalSpacing = 5;
		layout.horizontalSpacing = 1;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));
		toolkit.paintBordersFor(composite);
		section.setClient(composite);

		bundleTable = toolkit.createTable(composite, SWT.SINGLE | SWT.V_SCROLL | SWT.FULL_SELECTION);
		GridData data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
		data.heightHint = 100;
		bundleTable.setLayoutData(data);
		bundleTableViewer = new TableViewer(bundleTable);
		contentProvider = new ArtefactContentProvider();
		bundleTableViewer.setContentProvider(contentProvider);
		bundleTableViewer.setLabelProvider(ServerUICore.getLabelProvider());

		bundleTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				Object obj = ((IStructuredSelection) event.getSelection()).getFirstElement();
				updateButtons(obj);
			}

		});

		Composite buttonComposite = new Composite(composite, SWT.NONE);
		buttonComposite.setLayout(new GridLayout(1, true));
		data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		buttonComposite.setLayoutData(data);

		upButton = toolkit.createButton(buttonComposite, "Up", SWT.PUSH);
		data = new GridData();
		data.widthHint = 50;
		upButton.setLayoutData(data);
		upButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Object selectedArtefact = ((IStructuredSelection) bundleTableViewer.getSelection()).getFirstElement();
				List<Object> modules = new ArrayList<Object>();
				modules.addAll(Arrays.asList(contentProvider.getElements(server)));
				int index = modules.indexOf(selectedArtefact);
				modules.remove(selectedArtefact);
				modules.add(index - 1, selectedArtefact);
				List<String> artefactOrder = new ArrayList<String>();
				for (Object module : modules) {
					artefactOrder.add(((IModule) module).getId());
				}

				if (updating) {
					return;
				}
				updating = true;
				execute(new ModifyArtefactOrderCommand(serverWorkingCopy, artefactOrder));
				bundleTableViewer.setInput(server);
				updateButtons(selectedArtefact);
				updating = false;
			}
		});

		downButton = toolkit.createButton(buttonComposite, "Down", SWT.PUSH);
		downButton.setLayoutData(data);
		downButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Object selectedArtefact = ((IStructuredSelection) bundleTableViewer.getSelection()).getFirstElement();
				List<Object> modules = new ArrayList<Object>();
				modules.addAll(Arrays.asList(contentProvider.getElements(server)));
				int index = modules.indexOf(selectedArtefact);
				modules.remove(selectedArtefact);
				modules.add(index + 1, selectedArtefact);
				if (updating) {
					return;
				}
				updating = true;

				List<String> artefactOrder = new ArrayList<String>();
				for (Object module : modules) {
					artefactOrder.add(((IModule) module).getId());
				}
				execute(new ModifyArtefactOrderCommand(serverWorkingCopy, artefactOrder));
				bundleTableViewer.setInput(server);
				updateButtons(selectedArtefact);
				updating = false;
			}
		});

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
		bundleTableViewer.setInput(server);
		upButton.setEnabled(false);
		downButton.setEnabled(false);
		updating = false;
	}

	private void updateButtons(Object obj) {
		upButton.setEnabled(false);
		downButton.setEnabled(false);
		if (obj instanceof IModule) {
			List<Object> modules = Arrays.asList(contentProvider.getElements(server));
			int index = modules.indexOf(obj);
			if (index > 0) {
				upButton.setEnabled(true);
			}
			if (index < modules.size() - 1) {
				downButton.setEnabled(true);
			}
		}
	}

	class ArtefactContentProvider implements ITreeContentProvider {

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

		}

		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof IServer) {
				IServer server = (IServer) inputElement;
				IModule[] modules = server.getModules();

				IServerWorkingCopy workingServer = (IServerWorkingCopy) server.loadAdapter(IServerWorkingCopy.class,
						null);
				final List<String> orderedArtefacts = workingServer.getConfiguration().getArtefactOrder();

				List<IModule> orderedModules = Arrays.asList(modules);

				// sort the modules according the order defined in the server configuration
				Collections.sort(orderedModules, new Comparator<IModule>() {

					public int compare(IModule o1, IModule o2) {
						Integer m1 = (orderedArtefacts.contains(o1.getId())
								? orderedArtefacts.indexOf(o1.getId())
								: Integer.MAX_VALUE);
						Integer m2 = (orderedArtefacts.contains(o2.getId())
								? orderedArtefacts.indexOf(o2.getId())
								: Integer.MAX_VALUE);
						return m1.compareTo(m2);
					}
				});

				return orderedModules.toArray();

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

}
