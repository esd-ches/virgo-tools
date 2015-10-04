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

import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPageOne;
import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPageTwo;
import org.eclipse.jface.wizard.IWizardPage;

/**
 * @author Christian Dupuis
 * @author Leo Dos Santos
 */
public class NewBundleProjectCreationPage extends NewJavaProjectWizardPageTwo {

    public NewBundleProjectCreationPage(NewJavaProjectWizardPageOne mainPage) {
        super(mainPage);
    }

    @Override
    public IWizardPage getPreviousPage() {
        NewBundleProjectWizard wizard = (NewBundleProjectWizard) getWizard();
        if (wizard.getPropertiesPage() instanceof NullPropertiesPage) {
            return wizard.getInformationPage();
        } else {
            return wizard.getPropertiesPage();
        }
    }

    @Override
    public IWizardPage getNextPage() {
        return null;
    }

}
