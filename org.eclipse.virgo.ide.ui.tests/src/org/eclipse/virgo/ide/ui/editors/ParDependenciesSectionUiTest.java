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

import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.virgo.ide.ui.tests.AbstractManifestUiTestCase;

/**
 * @author Leo Dos Santos
 */
public class ParDependenciesSectionUiTest extends AbstractManifestUiTestCase {

    private static String SECTION_LABEL = "Nested Bundles";

    public void testAddButton() throws Exception {
        final ParManifestEditor manifest = openParManifestFile("META-INF/MANIFEST.MF");
        UIThreadRunnable.syncExec(new VoidResult() {

            public void run() {
                manifest.setActivePage(ParXmlEditorPage.ID_EDITOR);
            }
        });
        this.bot.flatButtonInSection("Add...", SECTION_LABEL).click();

        SWTBotShell addDialog = this.bot.shell("Bundle Selection");
        assertTrue(addDialog.isOpen());
        addDialog.close();
    }

    public void testRemoveButton() throws Exception {
        final ParManifestEditor manifest = openParManifestFile("META-INF/MANIFEST.MF");
        UIThreadRunnable.syncExec(new VoidResult() {

            public void run() {
                manifest.setActivePage(ParXmlEditorPage.ID_EDITOR);
            }
        });

        SWTBotButton button = this.bot.flatButtonInSection("Remove", SECTION_LABEL);
        assertFalse(button.isEnabled());

        SWTBotTable table = this.bot.tableInSection(SECTION_LABEL);
        assertEquals(1, table.rowCount());

        table.select(0);
        assertTrue(button.isEnabled());

        button.click();
        assertEquals(0, table.rowCount());
        assertFalse(button.isEnabled());
    }

}
