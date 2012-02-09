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
package org.eclipse.virgo.ide.ui.wizards;

import java.util.Map;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Christian Dupuis
 */
public abstract class AbstractPropertiesPage extends WizardPage {

	public AbstractPropertiesPage(String name) {
		super(name);
		setTitle("Bundle Properties");
		setDescription("Set the additional properties of the bundle. "
				+ "You can modify the properties at any time in the MANIFEST.MF file.");
	}

	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout());

		createPropertiesGroup(container);
		setControl(container);
	}

	protected abstract void createPropertiesGroup(Composite container);

	public abstract String getModuleType();

	public abstract Map<String, String> getProperties();

	@Override
	public IWizardPage getNextPage() {
		NewBundleProjectWizard wizard = (NewBundleProjectWizard) getWizard();
		return wizard.getFinalPage();
	}

	@Override
	public IWizardPage getPreviousPage() {
		NewBundleProjectWizard wizard = (NewBundleProjectWizard) getWizard();
		return wizard.getInformationPage();
	}

}
