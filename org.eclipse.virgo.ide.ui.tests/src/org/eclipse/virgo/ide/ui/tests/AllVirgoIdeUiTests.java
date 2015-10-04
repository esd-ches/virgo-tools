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

package org.eclipse.virgo.ide.ui.tests;

import org.eclipse.virgo.ide.ui.editors.BundleImportLibrarySectionUiTest;
import org.eclipse.virgo.ide.ui.editors.BundleImportPackageSectionUiTest;
import org.eclipse.virgo.ide.ui.editors.BundleImportSectionUiTest;
import org.eclipse.virgo.ide.ui.editors.BundleLibrarySectionUiTest;
import org.eclipse.virgo.ide.ui.editors.ParDependenciesSectionUiTest;
import org.eclipse.virgo.ide.ui.wizards.NewBundleProjectWizardUiTest;
import org.eclipse.virgo.ide.ui.wizards.NewParProjectWizardUiTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Leo Dos Santos
 */
public class AllVirgoIdeUiTests {

    public static Test suite() {
        TestSuite suite = new TestSuite(AllVirgoIdeUiTests.class.getName());
        suite.addTestSuite(BundleLibrarySectionUiTest.class);
        suite.addTestSuite(BundleImportLibrarySectionUiTest.class);
        suite.addTestSuite(BundleImportPackageSectionUiTest.class);
        suite.addTestSuite(BundleImportSectionUiTest.class);
        // suite.addTestSuite(BundleOverviewPageUiTest.class);
        suite.addTestSuite(ParDependenciesSectionUiTest.class);
        suite.addTestSuite(NewBundleProjectWizardUiTest.class);
        suite.addTestSuite(NewParProjectWizardUiTest.class);
        return suite;
    }

}
