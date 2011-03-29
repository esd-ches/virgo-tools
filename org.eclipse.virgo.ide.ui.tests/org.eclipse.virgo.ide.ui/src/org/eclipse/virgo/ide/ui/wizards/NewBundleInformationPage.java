/*******************************************************************************
 * Copyright (c) 2009 SpringSource, a divison of VMware, Inc.
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
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.pde.internal.ui.wizards.IProjectProvider;
import org.eclipse.pde.internal.ui.wizards.plugin.AbstractFieldData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.virgo.ide.eclipse.wizards.RuntimeConfigurationPage;
import org.eclipse.virgo.ide.facet.core.BundleFacetInstallDataModelProvider;
import org.eclipse.virgo.ide.facet.core.FacetCorePlugin;
import org.eclipse.virgo.ide.ui.ServerIdeUiPlugin;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;


/**
 * @author Christian Dupuis
 */
public class NewBundleInformationPage extends RuntimeConfigurationPage {

	private Combo moduleCombo;

	private int selectionIndex = 0;

	private final ModifyListener comboListener = new ModifyListener() {

		public void modifyText(ModifyEvent e) {
			if (moduleCombo.getSelectionIndex() != selectionIndex) {
				NewBundleProjectWizard wizard = (NewBundleProjectWizard) getWizard();
				AbstractPropertiesPage page;
				if (moduleCombo.getSelectionIndex() == 0) {
					page = (AbstractPropertiesPage) wizard.getPage(NullPropertiesPage.ID_PAGE);
					if (page != null) {
						wizard.setPropertiesPage(page);
					}
					else {
						wizard.setPropertiesPage(new NullPropertiesPage());
					}
				}
				else if (moduleCombo.getSelectionIndex() == 1) {
					page = (AbstractPropertiesPage) wizard.getPage(WebModulePropertiesPage.ID_PAGE);
					if (page != null) {
						wizard.setPropertiesPage(page);
					}
					else {
						wizard.setPropertiesPage(new WebModulePropertiesPage());
					}
				}
				selectionIndex = moduleCombo.getSelectionIndex();
			}
			validatePage();
		}

	};

	private Button enableClasspathContainer;

	private final IDataModel model;

	protected NewBundleInformationPage(String pageName, IProjectProvider provider, AbstractFieldData data,
			IDataModel model) {
		super(pageName, provider, data, model);
		this.model = model;
	}

	@Override
	protected void createAdditionalPropertiesGroup(Composite container) {
		Group propertiesGroup = new Group(container, SWT.NONE);
		propertiesGroup.setLayout(new GridLayout(3, false));
		propertiesGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		propertiesGroup.setText("Additional Properties");

		Label label = new Label(propertiesGroup, SWT.NONE);
		label.setText("Module Type");

		moduleCombo = new Combo(propertiesGroup, SWT.READ_ONLY);
		moduleCombo.setItems(new String[] { "None", "Web" });
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).span(2, 1).applyTo(moduleCombo);
		moduleCombo.select(selectionIndex);
		moduleCombo.addModifyListener(comboListener);

		Group classpathGroup = new Group(container, SWT.NONE);
		classpathGroup.setLayout(new GridLayout(1, false));
		classpathGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		classpathGroup.setText("Classpath Management");

		enableClasspathContainer = new Button(classpathGroup, SWT.CHECK);
		enableClasspathContainer.setText("Enable Bundle Classpath Container");
		enableClasspathContainer.setSelection(true);
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
		}
		else {
			return wizard.getPropertiesPage();
		}
	}

	@Override
	public void performPageFinish() {
		super.performPageFinish();
		model.setBooleanProperty(BundleFacetInstallDataModelProvider.ENABLE_SERVER_CLASSPATH_CONTAINER,
				enableClasspathContainer.getSelection());
	}
}
