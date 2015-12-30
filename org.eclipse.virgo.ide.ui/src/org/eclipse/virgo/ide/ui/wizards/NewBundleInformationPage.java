/*******************************************************************************
 * Copyright (c) 2009, 2011 SpringSource, a divison of VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource, a division of VMware, Inc. - initial API and implementation
 *******************************************************************************/

package org.eclipse.virgo.ide.ui.wizards;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.pde.internal.ui.wizards.IProjectProvider;
import org.eclipse.pde.internal.ui.wizards.plugin.AbstractFieldData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.virgo.ide.eclipse.wizards.RuntimeConfigurationPage;
import org.eclipse.virgo.ide.facet.core.BundleFacetInstallDataModelProvider;
import org.eclipse.virgo.ide.facet.core.FacetCorePlugin;
import org.eclipse.virgo.ide.ui.ServerIdeUiPlugin;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;

/**
 * @author Christian Dupuis
 * @author Martin Lippert
 */
public class NewBundleInformationPage extends RuntimeConfigurationPage {

    private Button moduleType;

    private Button enableClasspathContainer;

    private final IDataModel model;

    private final SelectionListener moduleTypeListener = new SelectionListener() {

        public void widgetSelected(SelectionEvent e) {
            NewBundleProjectWizard wizard = (NewBundleProjectWizard) getWizard();
            AbstractPropertiesPage page;
            if (!NewBundleInformationPage.this.moduleType.getSelection()) {
                page = (AbstractPropertiesPage) wizard.getPage(NullPropertiesPage.ID_PAGE);
                if (page != null) {
                    wizard.setPropertiesPage(page);
                } else {
                    wizard.setPropertiesPage(new NullPropertiesPage());
                }
            } else {
                page = (AbstractPropertiesPage) wizard.getPage(WebModulePropertiesPage.ID_PAGE);
                if (page != null) {
                    wizard.setPropertiesPage(page);
                } else {
                    wizard.setPropertiesPage(new WebModulePropertiesPage());
                }
            }
            validatePage();
        }

        public void widgetDefaultSelected(SelectionEvent e) {
        }

    };

    protected NewBundleInformationPage(String pageName, IProjectProvider provider, AbstractFieldData data, IDataModel model) {
        super(pageName, provider, data, model);
        this.model = model;
    }

    @Override
    protected void createAdditionalPropertiesGroup(Composite container) {
        Group propertiesGroup = new Group(container, SWT.NONE);
        propertiesGroup.setLayout(new GridLayout(3, false));
        propertiesGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        propertiesGroup.setText(Messages.NewBundleInformationPage_Additional_Properties);

        this.moduleType = new Button(propertiesGroup, SWT.CHECK);
        this.moduleType.setText(Messages.NewBundleInformationPage_WAB);
        this.moduleType.setSelection(false);
        this.moduleType.addSelectionListener(this.moduleTypeListener);

        Group classpathGroup = new Group(container, SWT.NONE);
        classpathGroup.setLayout(new GridLayout(1, false));
        classpathGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        classpathGroup.setText(Messages.NewBundleInformationPage_Classpath_Management);

        this.enableClasspathContainer = new Button(classpathGroup, SWT.CHECK);
        this.enableClasspathContainer.setText(Messages.NewBundleInformationPage_enable_container);
        this.enableClasspathContainer.setSelection(true);
    }

    @Override
    protected String getContentPageDescription() {
        return ProjectContentPageStrings.Bundle_ContentPage_desc;
    }

    @Override
    protected String getContentPageGroupLabel() {
        return ProjectContentPageStrings.Bundle_ContentPage_pGroup;
    }

    @Override
    protected String getContentPageIdLabel() {
        return ProjectContentPageStrings.Bundle_ContentPage_pid;
    }

    @Override
    protected String getContentPageNameLabel() {
        return ProjectContentPageStrings.Bundle_ContentPage_pname;
    }

    @Override
    protected String getContentPagePluginLabel() {
        return ProjectContentPageStrings.Bundle_ContentPage_plugin;
    }

    @Override
    protected String getContentPageProviderLabel() {
        return ProjectContentPageStrings.Bundle_ContentPage_pprovider;
    }

    @Override
    protected String getContentPageTitle() {
        return ProjectContentPageStrings.Bundle_ContentPage_title;
    }

    @Override
    protected String getContentPageVersionLabel() {
        return ProjectContentPageStrings.Bundle_ContentPage_pversion;
    }

    @Override
    protected IDialogSettings getDialogSettings() {
        return ServerIdeUiPlugin.getDefault().getDialogSettings();
    }

    @Override
    protected String getModuleTypeID() {
        return FacetCorePlugin.BUNDLE_FACET_ID;
    }

    @Override
    protected void validatePage() {
        super.validatePage();
        if (isPageComplete()) {
            // validate the additional properties
        }
    }

    @Override
    public IWizardPage getNextPage() {
        NewBundleProjectWizard wizard = (NewBundleProjectWizard) getWizard();
        if (wizard.getPropertiesPage() instanceof NullPropertiesPage) {
            return wizard.getFinalPage();
        } else {
            return wizard.getPropertiesPage();
        }
    }

    @Override
    public void performPageFinish() {
        super.performPageFinish();
        this.model.setBooleanProperty(BundleFacetInstallDataModelProvider.ENABLE_SERVER_CLASSPATH_CONTAINER,
            this.enableClasspathContainer.getSelection());
        this.model.setBooleanProperty(BundleFacetInstallDataModelProvider.ENABLE_WEB_BUNDLE, this.moduleType.getSelection());
    }
}
