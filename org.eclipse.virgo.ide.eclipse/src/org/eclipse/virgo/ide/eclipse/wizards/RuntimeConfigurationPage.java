/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.virgo.ide.eclipse.wizards;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.pde.internal.ui.IHelpContextIds;
import org.eclipse.pde.internal.ui.wizards.IProjectProvider;
import org.eclipse.pde.internal.ui.wizards.plugin.AbstractFieldData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetProjectCreationDataModelProperties;
import org.eclipse.wst.common.frameworks.datamodel.DataModelEvent;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.frameworks.datamodel.IDataModelListener;
import org.eclipse.wst.common.frameworks.internal.datamodel.ui.DataModelSynchHelper;
import org.eclipse.wst.common.frameworks.internal.datamodel.ui.DataModelWizardPage;
import org.eclipse.wst.common.frameworks.internal.operations.IProjectCreationPropertiesNew;
import org.eclipse.wst.common.frameworks.internal.ui.ValidationStatus;
import org.eclipse.wst.web.internal.ResourceHandler;
import org.eclipse.wst.web.ui.internal.wizards.DataModelFacetCreationWizardPage;

/**
 * SpringSource Tool Suite Team - Portions of this class were copied from WebTools' DataModelWizardPage and
 * DataModelFacetCreationWizardPage in order to add runtime environment selection functionality to the
 * ProjectContentPage.
 */
@SuppressWarnings("restriction")
public abstract class RuntimeConfigurationPage extends ProjectContentPage implements IDataModelListener, IFacetProjectCreationDataModelProperties {

    // DataModelWizardPage
    private final ValidationStatus status = new ValidationStatus();

    private final IDataModel model;

    @SuppressWarnings("unchecked")
    private Map validationMap;

    private String[] validationPropertyNames;

    private final DataModelSynchHelper synchHelper;

    // DataModelFacetCreationWizardPage
    private Combo serverTargetCombo;

    protected RuntimeConfigurationPage(String pageName, IProjectProvider provider, AbstractFieldData data, IDataModel model) {
        super(pageName, provider, data);
        this.model = model;
        model.addListener(this);
        this.synchHelper = initializeSynchHelper(model);
    }

    protected abstract void createAdditionalPropertiesGroup(Composite container);

    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout());

        createPluginPropertiesGroup(container);
        createAdditionalPropertiesGroup(container);
        createServerTargetComposite(container);

        Dialog.applyDialogFont(container);
        setControl(container);
        setDefaults();
        initializeValidationProperties();
        PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IHelpContextIds.NEW_PROJECT_REQUIRED_DATA);
    }

    /**
     * @see DataModelFacetCreationWizardPage
     */
    private void createServerTargetComposite(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        group.setText(ResourceHandler.TargetRuntime);
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        group.setLayout(new GridLayout(2, false));
        this.serverTargetCombo = new Combo(group, SWT.BORDER | SWT.READ_ONLY);
        this.serverTargetCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        Button newServerTargetButton = new Button(group, SWT.NONE);
        newServerTargetButton.setText(ResourceHandler.NewDotDotDot);
        newServerTargetButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (!internalLaunchNewRuntimeWizard(getShell(), RuntimeConfigurationPage.this.model)) {
                    // Bugzilla 135288
                    // setErrorMessage(ResourceHandler.InvalidServerTarget);
                }
            }
        });
        Control[] deps = new Control[] { newServerTargetButton };
        this.synchHelper.synchCombo(this.serverTargetCombo, FACET_RUNTIME, deps);
        if (this.serverTargetCombo.getSelectionIndex() == -1 && this.serverTargetCombo.getVisibleItemCount() != 0) {
            this.serverTargetCombo.select(0);
        }
    }

    /**
     * @see DataModelFacetCreationWizardPage
     */
    protected abstract String getModuleTypeID();

    /**
     * @see DataModelWizardPage
     */
    private boolean getStatus(Integer key) {
        return this.status.hasError(key);
    }

    /**
     * @see DataModelFacetCreationWizardPage
     */
    private String[] getValidationPropertyNames() {
        return new String[] { IProjectCreationPropertiesNew.PROJECT_LOCATION, FACET_RUNTIME };
    }

    /**
     * @see DataModelWizardPage
     */
    private DataModelSynchHelper initializeSynchHelper(IDataModel dm) {
        return new DataModelSynchHelper(dm);
    }

    /**
     * @see DataModelWizardPage
     */
    @SuppressWarnings("unchecked")
    private void initializeValidationProperties() {
        this.validationPropertyNames = getValidationPropertyNames();
        if (this.validationPropertyNames == null || this.validationPropertyNames.length == 0) {
            this.validationMap = Collections.EMPTY_MAP;
        } else {
            this.validationMap = new HashMap(this.validationPropertyNames.length);
            for (int i = 0; i < this.validationPropertyNames.length; i++) {
                this.validationMap.put(this.validationPropertyNames[i], new Integer(i));
            }
        }
    }

    /**
     * @see DataModelFacetCreationWizardPage
     */
    private boolean internalLaunchNewRuntimeWizard(Shell shell, IDataModel model) {
        return DataModelFacetCreationWizardPage.launchNewRuntimeWizard(shell, model, getModuleTypeID());
    }

    public void performPageFinish() {
        super.updateData();
        storeDefaultSettings();
    }

    /**
     * @see DataModelWizardPage
     */
    public void propertyChanged(DataModelEvent event) {
        // DataModelWizard w = getDataModelWizard();
        // if (w == null || !w.isExecuting()) {
        String propertyName = event.getPropertyName();
        if (this.validationPropertyNames != null
            && (event.getFlag() == DataModelEvent.VALUE_CHG || !isPageComplete() && event.getFlag() == DataModelEvent.VALID_VALUES_CHG)) {
            for (String element : this.validationPropertyNames) {
                if (element.equals(propertyName)) {
                    // validatePage(showValidationErrorsOnEnter());
                    validatePage();
                    break;
                }
            }
        }
    }

    /**
     * @see DataModelFacetCreationWizardPage
     */
    private void restoreDefaultSettings() {
        IDialogSettings settings = getDialogSettings();
        DataModelFacetCreationWizardPage.restoreRuntimeSettings(settings, this.model);
    }

    /**
     * @see DataModelWizardPage
     */
    private void setDefaults() {
        restoreDefaultSettings();
    }

    /**
     * @see DataModelWizardPage
     */
    private void setErrorMessage() {
        String error = this.status.getLastErrMsg();
        if (error == null) {
            if (getErrorMessage() != null) {
                setErrorMessage((String) null);
            }
            String warning = this.status.getLastWarningMsg();
            if (warning == null) {
                if (getMessage() != null && getMessageType() == IMessageProvider.WARNING) {
                    setMessage(null, IMessageProvider.WARNING);
                } else {
                    String info = this.status.getLastInfoMsg();
                    if (info == null) {
                        if (getMessage() != null && getMessageType() == IMessageProvider.INFORMATION) {
                            setMessage(null, IMessageProvider.INFORMATION);
                        }
                    } else if (!info.equals(getMessage())) {
                        setMessage(info, IMessageProvider.INFORMATION);
                    }
                }
            } else if (!warning.equals(getMessage())) {
                setMessage(warning, IMessageProvider.WARNING);
            }
        } else if (!error.equals(getErrorMessage())) {
            setErrorMessage(error);
        }
    }

    /**
     * @see DataModelWizardPage
     */
    private void setErrorStatus(Integer key, String errorMessage) {
        this.status.setErrorStatus(key, errorMessage);
    }

    /**
     * @see DataModelWizardPage
     */
    private void setInfoStatus(Integer key, String infoMessage) {
        this.status.setInfoStatus(key, infoMessage);
    }

    /**
     * @see DataModelWizardPage
     */
    private void setOKStatus(Integer key) {
        this.status.setOKStatus(key);
    }

    /**
     * @see DataModelWizardPage
     */
    private void setWarningStatus(Integer key, String warningMessage) {
        this.status.setWarningStatus(key, warningMessage);
    }

    /**
     * @see DataModelFacetCreationWizardPage
     */
    private void storeDefaultSettings() {
        IDialogSettings settings = getDialogSettings();
        DataModelFacetCreationWizardPage.saveRuntimeSettings(settings, this.model);
    }

    /**
     * @see DataModelWizardPage
     */
    private void updateControls() {

    }

    /**
     * @see DataModelWizardPage
     */
    private String validateControlsBase() {
        if (!this.validationMap.isEmpty()) {
            String propName;
            for (String element : this.validationPropertyNames) {
                propName = element;
                Integer valKey = (Integer) this.validationMap.get(propName);
                if (valKey != null) {
                    validateProperty(propName, valKey);
                }
                if (!getStatus(valKey)) {
                    return propName;
                }
            }
        }
        return null;
    }

    /**
     * @see DataModelWizardPage
     */
    @Override
    protected void validatePage() {
        super.validatePage();
        if (isPageComplete()) {
            validateControlsBase();
            updateControls();
            setErrorMessage();
            setPageComplete(this.status.getLastErrMsg() == null);
        }
    }

    /**
     * @see DataModelWizardPage
     */
    private void validateProperty(String propertyName, Integer validationKey) {
        setOKStatus(validationKey);
        IStatus status1 = this.model.validateProperty(propertyName);
        if (!status1.isOK()) {
            String message = status1.isMultiStatus() ? status1.getChildren()[0].getMessage() : status1.getMessage();
            switch (status1.getSeverity()) {
                case IStatus.ERROR:
                    setErrorStatus(validationKey, message);
                    break;
                case IStatus.WARNING:
                    setWarningStatus(validationKey, message);
                    break;
                case IStatus.INFO:
                    setInfoStatus(validationKey, message);
                    break;
            }
        }
    }
}
