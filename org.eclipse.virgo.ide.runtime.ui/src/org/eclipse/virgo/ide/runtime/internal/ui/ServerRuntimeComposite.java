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
package org.eclipse.virgo.ide.runtime.internal.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.virgo.ide.runtime.core.IServerRuntimeWorkingCopy;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.internal.IInstallableRuntime;
import org.eclipse.wst.server.core.internal.ServerPlugin;
import org.eclipse.wst.server.ui.internal.SWTUtil;
import org.eclipse.wst.server.ui.wizard.IWizardHandle;

/**
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public class ServerRuntimeComposite extends Composite {

	protected IRuntimeWorkingCopy runtimeWC;

	protected IServerRuntimeWorkingCopy runtime;

	protected IWizardHandle wizard;

	protected Text installDir;

	protected Text name;

	protected Combo vmCombo;

	protected List<IVMInstall> installedJREs;

	protected String[] jreNames;

	protected IInstallableRuntime ir;

	protected Label installLabel;

	protected Button install;

	private Combo versionCombo;

	protected ServerRuntimeComposite(Composite parent, IWizardHandle wizard, String wizardTitle,
			String wizardDescription) {
		this(parent, wizard, wizardTitle, wizardDescription, ServerUiImages.DESC_WIZB_SERVER);
	}

	protected ServerRuntimeComposite(Composite parent, IWizardHandle wizard, String wizardTitle,
			String wizardDescription, ImageDescriptor imageDescriptor) {
		super(parent, SWT.NONE);
		this.wizard = wizard;

		wizard.setTitle(wizardTitle);
		wizard.setDescription(wizardDescription);
		wizard.setImageDescriptor(imageDescriptor);

		createControl();
	}

	protected void setRuntime(IRuntimeWorkingCopy newRuntime) {
		if (newRuntime == null) {
			runtimeWC = null;
			runtime = null;
		} else {
			runtimeWC = newRuntime;
			runtime = (IServerRuntimeWorkingCopy) newRuntime.loadAdapter(IServerRuntimeWorkingCopy.class, null);
		}

		if (runtimeWC == null) {
			ir = null;
			install.setEnabled(false);
			installLabel.setText("");
		} else {
			ir = ServerPlugin.findInstallableRuntime(runtimeWC.getRuntimeType().getId());
		}

		init();
		validate();
	}

	protected void createControl() {
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		setLayout(layout);
		setLayoutData(new GridData(GridData.FILL_BOTH));

		Label label = new Label(this, SWT.NONE);
		label.setText(ServerUiPlugin.getResourceString("runtimeName"));
		GridData data = new GridData();
		data.horizontalSpan = 2;
		label.setLayoutData(data);

		name = new Text(this, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		name.setLayoutData(data);
		name.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				runtimeWC.setName(name.getText());
				validate();
			}
		});

		label = new Label(this, SWT.NONE);
		label.setText(ServerUiPlugin.getResourceString("installDir"));
		data = new GridData();
		data.horizontalSpan = 2;
		label.setLayoutData(data);

		installDir = new Text(this, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		installDir.setLayoutData(data);
		installDir.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				runtimeWC.setLocation(new Path(installDir.getText()));
				validate();
			}
		});

		Button browse = SWTUtil.createButton(this, ServerUiPlugin.getResourceString("browse"));
		browse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent se) {
				DirectoryDialog dialog = new DirectoryDialog(ServerRuntimeComposite.this.getShell());
				dialog.setMessage(ServerUiPlugin.getResourceString("selectInstallDir"));
				dialog.setFilterPath(installDir.getText());
				String selectedDirectory = dialog.open();
				if (selectedDirectory != null) {
					installDir.setText(selectedDirectory);
				}
			}
		});

//		Composite configuration = new Composite(this, SWT.BORDER);
//		GridLayout configLayout = new GridLayout();
//		configLayout.numColumns = 2;
//		configuration.setLayout(configLayout);
//		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
//		configuration.setLayoutData(data);
//
//		Label versionLabel = new Label(configuration, SWT.NONE);
//		versionLabel.setText(ServerUiPlugin.getResourceString("version"));
//		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
//		versionLabel.setLayoutData(data);
//
//		versionCombo = new Combo(configuration, SWT.DROP_DOWN | SWT.READ_ONLY);
//
//		List<String> names = new ArrayList<String>();
//		for (ServerVirgoHandler version : ServerVersionAdapter.ALL_HANDLERS) {
//			names.add(version.getName());
//		}
//		versionCombo.setItems(names.toArray(new String[] {}));
//		data = new GridData(GridData.FILL_HORIZONTAL);
//
//		versionCombo.setLayoutData(data);
//
//		versionCombo.addSelectionListener(new SelectionListener() {
//			public void widgetSelected(SelectionEvent e) {
//				int sel = versionCombo.getSelectionIndex();
//				runtime.setVirgoVersion(ServerVersionAdapter.ALL_HANDLERS[sel]);
//				validate();
//			}
//
//			public void widgetDefaultSelected(SelectionEvent e) {
//				widgetSelected(e);
//			}
//		});

		updateJREs();

		// JDK location
		label = new Label(this, SWT.NONE);
		label.setText(ServerUiPlugin.getResourceString("installedJRE"));
		data = new GridData();
		data.horizontalSpan = 2;
		label.setLayoutData(data);

		vmCombo = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
		vmCombo.setItems(jreNames);
		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		vmCombo.setLayoutData(data);

		vmCombo.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				int sel = vmCombo.getSelectionIndex();
				IVMInstall vmInstall = null;
				if (sel > 0) {
					vmInstall = installedJREs.get(sel - 1);
				}

				runtime.setVMInstall(vmInstall);
				validate();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		Button button = SWTUtil.createButton(this, ServerUiPlugin.getResourceString("installedJREs"));
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String currentVM = vmCombo.getText();
				if (showPreferencePage()) {
					updateJREs();
					vmCombo.setItems(jreNames);
					vmCombo.setText(currentVM);
					if (vmCombo.getSelectionIndex() == -1) {
						vmCombo.select(0);
					}
					validate();
				}
			}
		});

		init();
		validate();

		Dialog.applyDialogFont(this);

		name.forceFocus();
	}

	protected void updateJREs() {
		// get all installed JVMs
		installedJREs = new ArrayList<IVMInstall>();
		IVMInstallType[] vmInstallTypes = JavaRuntime.getVMInstallTypes();
		int size = vmInstallTypes.length;
		for (int i = 0; i < size; i++) {
			IVMInstall[] vmInstalls = vmInstallTypes[i].getVMInstalls();
			int size2 = vmInstalls.length;
			for (int j = 0; j < size2; j++) {
				installedJREs.add(vmInstalls[j]);
			}
		}

		// get names
		size = installedJREs.size();
		jreNames = new String[size + 1];
		jreNames[0] = ServerUiPlugin.getResourceString("runtimeDefaultJRE");
		for (int i = 0; i < size; i++) {
			IVMInstall vmInstall = installedJREs.get(i);
			jreNames[i + 1] = vmInstall.getName();
		}
	}

	protected boolean showPreferencePage() {
		String id = "org.eclipse.jdt.debug.ui.preferences.VMPreferencePage";
		PreferenceManager manager = PlatformUI.getWorkbench().getPreferenceManager();
		IPreferenceNode node = manager.find("org.eclipse.jdt.ui.preferences.JavaBasePreferencePage").findSubNode(id);
		PreferenceManager manager2 = new PreferenceManager();
		manager2.addToRoot(node);
		PreferenceDialog dialog = new PreferenceDialog(getShell(), manager2);
		dialog.create();
		return (dialog.open() == Window.OK);
	}

	protected void init() {
		if (name == null || runtime == null) {
			return;
		}

		if (runtimeWC.getName() != null) {
			name.setText(runtimeWC.getName());
		} else {
			name.setText("");
		}

		if (runtimeWC.getLocation() != null) {
			installDir.setText(runtimeWC.getLocation().toOSString());
		} else {
			installDir.setText("");
		}

//		updateConfiguration();

		// set selection
		if (runtime.isUsingDefaultJRE()) {
			vmCombo.select(0);
		} else {
			boolean found = false;
			int size = installedJREs.size();
			for (int i = 0; i < size; i++) {
				IVMInstall vmInstall = installedJREs.get(i);
				if (vmInstall.equals(runtime.getVMInstall())) {
					vmCombo.select(i + 1);
					found = true;
				}
			}
			if (!found) {
				vmCombo.select(0);
			}
		}
	}

//	private void updateConfiguration() {
//		int v = 0;
//		for (ServerVirgoHandler version : ServerVersionAdapter.ALL_HANDLERS) {
//			if (version.isHandlerFor(runtimeWC)) {
//				versionCombo.select(v);
//				break;
//			}
//			v++;
//		}
//	}

	protected void validate() {
		if (runtime == null) {
			wizard.setMessage("", IMessageProvider.ERROR);
			return;
		}

		IStatus status = runtimeWC.validate(null);
		if (status == null) {
			wizard.setMessage(null, IMessageProvider.NONE);
		} else if (status.isOK()) {
			wizard.setMessage(status.getMessage(), IMessageProvider.INFORMATION);
		} else if (status.getSeverity() == IStatus.WARNING) {
			wizard.setMessage(status.getMessage(), IMessageProvider.WARNING);
		} else {
			wizard.setMessage(status.getMessage(), IMessageProvider.ERROR);
		}
		wizard.update();
//		updateConfiguration();
	}
}
