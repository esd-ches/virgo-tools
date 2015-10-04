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

    protected ServerRuntimeComposite(Composite parent, IWizardHandle wizard, String wizardTitle, String wizardDescription) {
        this(parent, wizard, wizardTitle, wizardDescription, ServerUiImages.DESC_WIZB_SERVER);
    }

    protected ServerRuntimeComposite(Composite parent, IWizardHandle wizard, String wizardTitle, String wizardDescription,
        ImageDescriptor imageDescriptor) {
        super(parent, SWT.NONE);
        this.wizard = wizard;

        wizard.setTitle(wizardTitle);
        wizard.setDescription(wizardDescription);
        wizard.setImageDescriptor(imageDescriptor);

        createControl();
    }

    protected void setRuntime(IRuntimeWorkingCopy newRuntime) {
        if (newRuntime == null) {
            this.runtimeWC = null;
            this.runtime = null;
        } else {
            this.runtimeWC = newRuntime;
            this.runtime = (IServerRuntimeWorkingCopy) newRuntime.loadAdapter(IServerRuntimeWorkingCopy.class, null);
        }

        if (this.runtimeWC == null) {
            this.ir = null;
            this.install.setEnabled(false);
            this.installLabel.setText("");
        } else {
            this.ir = ServerPlugin.findInstallableRuntime(this.runtimeWC.getRuntimeType().getId());
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

        this.name = new Text(this, SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        this.name.setLayoutData(data);
        this.name.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                ServerRuntimeComposite.this.runtimeWC.setName(ServerRuntimeComposite.this.name.getText());
                validate();
            }
        });

        label = new Label(this, SWT.NONE);
        label.setText(ServerUiPlugin.getResourceString("installDir"));
        data = new GridData();
        data.horizontalSpan = 2;
        label.setLayoutData(data);

        this.installDir = new Text(this, SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        this.installDir.setLayoutData(data);
        this.installDir.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                ServerRuntimeComposite.this.runtimeWC.setLocation(new Path(ServerRuntimeComposite.this.installDir.getText()));
                validate();
            }
        });

        Button browse = SWTUtil.createButton(this, ServerUiPlugin.getResourceString("browse"));
        browse.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent se) {
                DirectoryDialog dialog = new DirectoryDialog(ServerRuntimeComposite.this.getShell());
                dialog.setMessage(ServerUiPlugin.getResourceString("selectInstallDir"));
                dialog.setFilterPath(ServerRuntimeComposite.this.installDir.getText());
                String selectedDirectory = dialog.open();
                if (selectedDirectory != null) {
                    ServerRuntimeComposite.this.installDir.setText(selectedDirectory);
                }
            }
        });

        // Composite configuration = new Composite(this, SWT.BORDER);
        // GridLayout configLayout = new GridLayout();
        // configLayout.numColumns = 2;
        // configuration.setLayout(configLayout);
        // data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        // configuration.setLayoutData(data);
        //
        // Label versionLabel = new Label(configuration, SWT.NONE);
        // versionLabel.setText(ServerUiPlugin.getResourceString("version"));
        // data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        // versionLabel.setLayoutData(data);
        //
        // versionCombo = new Combo(configuration, SWT.DROP_DOWN | SWT.READ_ONLY);
        //
        // List<String> names = new ArrayList<String>();
        // for (ServerVirgoHandler version : ServerVersionAdapter.ALL_HANDLERS) {
        // names.add(version.getName());
        // }
        // versionCombo.setItems(names.toArray(new String[] {}));
        // data = new GridData(GridData.FILL_HORIZONTAL);
        //
        // versionCombo.setLayoutData(data);
        //
        // versionCombo.addSelectionListener(new SelectionListener() {
        // public void widgetSelected(SelectionEvent e) {
        // int sel = versionCombo.getSelectionIndex();
        // runtime.setVirgoVersion(ServerVersionAdapter.ALL_HANDLERS[sel]);
        // validate();
        // }
        //
        // public void widgetDefaultSelected(SelectionEvent e) {
        // widgetSelected(e);
        // }
        // });

        updateJREs();

        // JDK location
        label = new Label(this, SWT.NONE);
        label.setText(ServerUiPlugin.getResourceString("installedJRE"));
        data = new GridData();
        data.horizontalSpan = 2;
        label.setLayoutData(data);

        this.vmCombo = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
        this.vmCombo.setItems(this.jreNames);
        data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        this.vmCombo.setLayoutData(data);

        this.vmCombo.addSelectionListener(new SelectionListener() {

            public void widgetSelected(SelectionEvent e) {
                int sel = ServerRuntimeComposite.this.vmCombo.getSelectionIndex();
                IVMInstall vmInstall = null;
                if (sel > 0) {
                    vmInstall = ServerRuntimeComposite.this.installedJREs.get(sel - 1);
                }

                ServerRuntimeComposite.this.runtime.setVMInstall(vmInstall);
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
                String currentVM = ServerRuntimeComposite.this.vmCombo.getText();
                if (showPreferencePage()) {
                    updateJREs();
                    ServerRuntimeComposite.this.vmCombo.setItems(ServerRuntimeComposite.this.jreNames);
                    ServerRuntimeComposite.this.vmCombo.setText(currentVM);
                    if (ServerRuntimeComposite.this.vmCombo.getSelectionIndex() == -1) {
                        ServerRuntimeComposite.this.vmCombo.select(0);
                    }
                    validate();
                }
            }
        });

        init();
        validate();

        Dialog.applyDialogFont(this);

        this.name.forceFocus();
    }

    protected void updateJREs() {
        // get all installed JVMs
        this.installedJREs = new ArrayList<IVMInstall>();
        IVMInstallType[] vmInstallTypes = JavaRuntime.getVMInstallTypes();
        int size = vmInstallTypes.length;
        for (int i = 0; i < size; i++) {
            IVMInstall[] vmInstalls = vmInstallTypes[i].getVMInstalls();
            int size2 = vmInstalls.length;
            for (int j = 0; j < size2; j++) {
                this.installedJREs.add(vmInstalls[j]);
            }
        }

        // get names
        size = this.installedJREs.size();
        this.jreNames = new String[size + 1];
        this.jreNames[0] = ServerUiPlugin.getResourceString("runtimeDefaultJRE");
        for (int i = 0; i < size; i++) {
            IVMInstall vmInstall = this.installedJREs.get(i);
            this.jreNames[i + 1] = vmInstall.getName();
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
        return dialog.open() == Window.OK;
    }

    protected void init() {
        if (this.name == null || this.runtime == null) {
            return;
        }

        if (this.runtimeWC.getName() != null) {
            this.name.setText(this.runtimeWC.getName());
        } else {
            this.name.setText("");
        }

        if (this.runtimeWC.getLocation() != null) {
            this.installDir.setText(this.runtimeWC.getLocation().toOSString());
        } else {
            this.installDir.setText("");
        }

        // updateConfiguration();

        // set selection
        if (this.runtime.isUsingDefaultJRE()) {
            this.vmCombo.select(0);
        } else {
            boolean found = false;
            int size = this.installedJREs.size();
            for (int i = 0; i < size; i++) {
                IVMInstall vmInstall = this.installedJREs.get(i);
                if (vmInstall.equals(this.runtime.getVMInstall())) {
                    this.vmCombo.select(i + 1);
                    found = true;
                }
            }
            if (!found) {
                this.vmCombo.select(0);
            }
        }
    }

    // private void updateConfiguration() {
    // int v = 0;
    // for (ServerVirgoHandler version : ServerVersionAdapter.ALL_HANDLERS) {
    // if (version.isHandlerFor(runtimeWC)) {
    // versionCombo.select(v);
    // break;
    // }
    // v++;
    // }
    // }

    protected void validate() {
        if (this.runtime == null) {
            this.wizard.setMessage("", IMessageProvider.ERROR);
            return;
        }

        IStatus status = this.runtimeWC.validate(null);
        if (status == null) {
            this.wizard.setMessage(null, IMessageProvider.NONE);
        } else if (status.isOK()) {
            this.wizard.setMessage(status.getMessage(), IMessageProvider.INFORMATION);
        } else if (status.getSeverity() == IStatus.WARNING) {
            this.wizard.setMessage(status.getMessage(), IMessageProvider.WARNING);
        } else {
            this.wizard.setMessage(status.getMessage(), IMessageProvider.ERROR);
        }
        this.wizard.update();
        // updateConfiguration();
    }
}
