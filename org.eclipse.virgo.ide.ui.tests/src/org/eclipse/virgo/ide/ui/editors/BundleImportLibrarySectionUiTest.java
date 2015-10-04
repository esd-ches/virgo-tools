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

package org.eclipse.virgo.ide.ui.editors;

import org.eclipse.virgo.ide.ui.tests.AbstractManifestUiTestCase;

/**
 * @author Leo Dos Santos
 */
public class BundleImportLibrarySectionUiTest extends AbstractManifestUiTestCase {

    private static String SECTION_LABEL = "Import Library";

    public void testAddButton() throws Exception {
        final BundleManifestEditor manifest = openBundleManifestFile("src/META-INF/MANIFEST.MF");
        UIThreadRunnable.syncExec(new VoidResult() {

            public void run() {
                manifest.setActivePage(BundleDependenciesPage.PAGE_ID);
            }
        });
        this.bot.flatButtonInSection("Add...", SECTION_LABEL).click();

        SWTBotShell addDialog = this.bot.shell("Library Selection");
        assertTrue(addDialog.isOpen());
        addDialog.close();
    }

    public void testDownloadButton() throws Exception {
        final BundleManifestEditor manifest = openBundleManifestFile("src/META-INF/MANIFEST.MF");
        UIThreadRunnable.syncExec(new VoidResult() {

            public void run() {
                manifest.setActivePage(BundleDependenciesPage.PAGE_ID);
            }
        });

        SWTBotTable table = this.bot.tableInSection(SECTION_LABEL);
        int count = table.rowCount();
        this.bot.flatButtonInSection("Download...", SECTION_LABEL).click();

        SWTBotShell downloadDialog = this.bot.shell("Library Selection");
        assertTrue(downloadDialog.isOpen());

        this.bot.table().select(0);
        this.bot.button("OK").click();
        assertEquals(count + 1, table.rowCount());
    }

    public void testRemoveButton() throws Exception {
        final BundleManifestEditor manifest = openBundleManifestFile("src/META-INF/MANIFEST.MF");
        UIThreadRunnable.syncExec(new VoidResult() {

            public void run() {
                manifest.setActivePage(BundleDependenciesPage.PAGE_ID);
            }
        });

        SWTBotButton button = this.bot.flatButtonInSection("Remove", SECTION_LABEL);
        assertFalse(button.isEnabled());

        SWTBotTable table = this.bot.tableInSection(SECTION_LABEL);
        int count = table.rowCount();
        assertTrue(count > 0);

        table.select(0);
        assertTrue(button.isEnabled());

        button.click();
        assertEquals(count - 1, table.rowCount());
    }

    public void testPropertiesButton() throws Exception {
        final BundleManifestEditor manifest = openBundleManifestFile("src/META-INF/MANIFEST.MF");
        UIThreadRunnable.syncExec(new VoidResult() {

            public void run() {
                manifest.setActivePage(BundleDependenciesPage.PAGE_ID);
            }
        });

        SWTBotButton button = this.bot.flatButtonInSection("Properties...", SECTION_LABEL);
        assertFalse(button.isEnabled());

        SWTBotTable table = this.bot.tableInSection(SECTION_LABEL);
        String text = table.getTableItem(0).getText();
        table.select(0);
        assertTrue(button.isEnabled());
        button.click();

        SWTBotShell propertiesDialog = this.bot.shell(text);
        assertTrue(propertiesDialog.isOpen());
        propertiesDialog.close();
    }

}
