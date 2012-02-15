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
import org.eclipse.jface.dialogs.Dialog;
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
		listener = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (IServerWorkingCopy.PROPERTY_STATIC_FILENAMES.equals(event.getPropertyName())) {
					filenamesTableViewer.setInput(server);
				}
			}
		};
		serverWorkingCopy.addConfigurationChangeListener(listener);
	}

	public void createSection(Composite parent) {
		super.createSection(parent);
		FormToolkit toolkit = getFormToolkit(parent.getDisplay());

		Section section = toolkit.createSection(parent, ExpandableComposite.TWISTIE
				| ExpandableComposite.EXPANDED | ExpandableComposite.TITLE_BAR
				| Section.DESCRIPTION | ExpandableComposite.FOCUS_TITLE);
		section.setText("Redeploy Behavior");
		section
				.setDescription("Configure server redeploy and refresh behavior on changes to project resources.\n\nDefine patterns for files that should be copied into the server without redeploying the bundle or application. Spring XML configuration files be handled separately.");
		section
				.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));

		Composite composite = toolkit.createComposite(section);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 5;
		layout.marginWidth = 1;
		layout.verticalSpacing = 5;
		layout.horizontalSpacing = 10;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
				| GridData.VERTICAL_ALIGN_FILL));
		toolkit.paintBordersFor(composite);
		section.setClient(composite);

		filenameTable = toolkit.createTable(composite, SWT.SINGLE | SWT.V_SCROLL
				| SWT.FULL_SELECTION);
		GridData data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
		data.heightHint = 130;
		filenameTable.setLayoutData(data);
		filenamesTableViewer = new TableViewer(filenameTable);
		contentProvider = new StaticFilenamesContentProvider();
		filenamesTableViewer.setContentProvider(contentProvider);
		filenamesTableViewer.setLabelProvider(new FilenameLabelProvider());

		filenamesTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				Object obj = ((IStructuredSelection) event.getSelection()).getFirstElement();
				deleteButton.setEnabled(obj != null);
				updateButtons(obj);
			}
		});

		Composite buttonComposite = new Composite(composite, SWT.NONE);
		GridLayout buttonLayout = new GridLayout(1, true);
		buttonLayout.marginWidth = 0;
		buttonLayout.marginHeight = 0;
		buttonComposite.setLayout(buttonLayout);
		GridDataFactory.fillDefaults().applyTo(buttonComposite);
		
		addButton = toolkit.createButton(buttonComposite, "Add", SWT.PUSH);
		GridDataFactory.fillDefaults().applyTo(addButton);
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (updating)
					return;

				InputDialog dialog = new InputDialog(
						getShell(),
						"New filename pattern",
						"Enter a new filename pattern for static resources (wildcards such as '*' and '?' are supported):",
						"", new IInputValidator() {

							public String isValid(String newText) {
								if (!StringUtils.isNotBlank(newText)) {
									return "Pattern can't be empty";
								}
								return null;
							}
						});
				if (dialog.open() == Dialog.OK) {
					updating = true;
					List<Object> filenames = new ArrayList<Object>(Arrays.asList(contentProvider
							.getElements(server)));
					filenames.add(dialog.getValue());
					execute(new ModifyStaticResourcesCommand(serverWorkingCopy, StringUtils
							.join(filenames, ",")));
					filenamesTableViewer.setInput(server);
					filenamesTableViewer.setSelection(new StructuredSelection(dialog.getValue()));
					// update buttons
					updating = false;
				}
			}
		});

		deleteButton = toolkit.createButton(buttonComposite, "Delete", SWT.PUSH);
		GridDataFactory.fillDefaults().applyTo(deleteButton);
		deleteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Object selectedArtefact = ((IStructuredSelection) filenamesTableViewer
						.getSelection()).getFirstElement();
				if (updating)
					return;
				updating = true;
				List<Object> filenames = new ArrayList<Object>(Arrays.asList(contentProvider
						.getElements(server)));
				filenames.remove(selectedArtefact);
				execute(new ModifyStaticResourcesCommand(serverWorkingCopy, StringUtils
						.join(filenames, ",")));
				filenamesTableViewer.setInput(server);
				// update buttons
				updating = false;
			}
		});

		upButton = toolkit.createButton(buttonComposite, "Up", SWT.PUSH);
		GridDataFactory.fillDefaults().applyTo(upButton);
		upButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Object selectedArtefact = ((IStructuredSelection) filenamesTableViewer
						.getSelection()).getFirstElement();
				List<Object> modules = new ArrayList<Object>();
				modules.addAll(Arrays.asList(contentProvider.getElements(server)));
				int index = modules.indexOf(selectedArtefact);
				modules.remove(selectedArtefact);
				modules.add(index - 1, selectedArtefact);
				if (updating)
					return;
				updating = true;
				execute(new ModifyStaticResourcesCommand(serverWorkingCopy, StringUtils
						.join(modules, ",")));
				filenamesTableViewer.setInput(server);
				updateButtons(selectedArtefact);
				updating = false;
			}
		});

		downButton = toolkit.createButton(buttonComposite, "Down", SWT.PUSH);
		GridDataFactory.fillDefaults().applyTo(downButton);
		downButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Object selectedArtefact = ((IStructuredSelection) filenamesTableViewer
						.getSelection()).getFirstElement();
				List<Object> modules = new ArrayList<Object>();
				modules.addAll(Arrays.asList(contentProvider.getElements(server)));
				int index = modules.indexOf(selectedArtefact);
				modules.remove(selectedArtefact);
				modules.add(index + 1, selectedArtefact);
				if (updating)
					return;
				updating = true;
				execute(new ModifyStaticResourcesCommand(serverWorkingCopy, StringUtils
				                 						.join(modules, ",")));
				filenamesTableViewer.setInput(server);
				updateButtons(selectedArtefact);
				updating = false;
			}
		});

		FormText restoreDefault = toolkit.createFormText(composite, true);
		restoreDefault
				.setText(
						"<form><p><a href=\"exportbundle\">Restore default</a> filename pattern</p></form>",
						true, false);
		restoreDefault.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				updating = true;
				execute(new ModifyStaticResourcesCommand(serverWorkingCopy,
						IServerWorkingCopy.DEFAULT_STATIC_FILENAMES));
				filenamesTableViewer.setInput(server);
				updating = false;
			}
		});
		updateEnablement();
		initialize();
	}

	private void updateButtons() {
		IStructuredSelection selection = (IStructuredSelection) filenamesTableViewer.getSelection();
		Object selectedArtefact = selection.getFirstElement();
		updateButtons(selectedArtefact);
	}

	private void updateButtons(Object obj) {
		if (obj instanceof String) {
			List<Object> modules = Arrays.asList(contentProvider.getElements(server));
			int index = modules.indexOf(obj);
			upButton.setEnabled(index > 0);
			downButton.setEnabled(index < modules.size() - 1);
			deleteButton.setEnabled(true);
		}
		else {
			upButton.setEnabled(false);
			downButton.setEnabled(false);
			deleteButton.setEnabled(false);
		}
	}

	private void updateEnablement() {
		addButton.setEnabled(true);
		updateButtons();
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
		serverWorkingCopy = (IServerWorkingCopy) server.loadAdapter(IServerWorkingCopy.class,
				new NullProgressMonitor());
		addConfigurationChangeListener();
	}

	/**
	 * Initialize the fields in this editor.
	 */
	protected void initialize() {
		updating = true;
		filenamesTableViewer.setInput(server);
		deleteButton.setEnabled(false);
		updating = false;
	}

	class StaticFilenamesContentProvider implements ITreeContentProvider {

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

		}

		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof IServer) {
				IServer server = (IServer) inputElement;
				IServerWorkingCopy dmServer = (IServerWorkingCopy) server.loadAdapter(
						IServerWorkingCopy.class, null);
				String[] filenames = StringUtils.split(dmServer
						.getStaticFilenamePatterns(), ",");
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

		public Image getImage(Object element) {
			return ServerUiImages.getImage(ServerUiImages.IMG_OBJ_FILE);
		}

		public String getText(Object element) {
			return element.toString();
		}

	}

}
