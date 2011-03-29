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
package org.eclipse.virgo.ide.ui.editors;

import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.virgo.ide.ui.editors.BundleDependenciesPage;
import org.eclipse.virgo.ide.ui.editors.BundleManifestEditor;
import org.eclipse.virgo.ide.ui.tests.AbstractManifestUiTestCase;


/**
 * @author Leo Dos Santos
 */
public class BundleImportPackageSectionUiTest extends AbstractManifestUiTestCase {

	private static String SECTION_LABEL = "Import Package";

	public void testAddButton() throws Exception {
		final BundleManifestEditor manifest = openBundleManifestFile("src/META-INF/MANIFEST.MF");
		UIThreadRunnable.syncExec(new VoidResult() {
			public void run() {
				manifest.setActivePage(BundleDependenciesPage.PAGE_ID);
			}
		});
		bot.flatButtonInSection("Add...", SECTION_LABEL).click();

		SWTBotShell addDialog = bot.shell("Package Selection");
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

		SWTBotTable table = bot.tableInSection(SECTION_LABEL);
		int count = table.rowCount();
		bot.flatButtonInSection("Download...", SECTION_LABEL).click();

		SWTBotShell downloadDialog = bot.shell("Package Selection");
		assertTrue(downloadDialog.isOpen());

		bot.table().select(0);
		bot.button("OK").click();
		assertEquals(count + 1, table.rowCount());
	}

	public void testRemoveButton() throws Exception {
		final BundleManifestEditor manifest = openBundleManifestFile("src/META-INF/MANIFEST.MF");
		UIThreadRunnable.syncExec(new VoidResult() {
			public void run() {
				manifest.setActivePage(BundleDependenciesPage.PAGE_ID);
			}
		});

		SWTBotButton button = bot.flatButtonInSection("Remove", SECTION_LABEL);
		assertFalse(button.isEnabled());

		SWTBotTable table = bot.tableInSection(SECTION_LABEL);
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

		SWTBotButton button = bot.flatButtonInSection("Properties...", SECTION_LABEL);
		assertFalse(button.isEnabled());

		SWTBotTable table = bot.tableInSection(SECTION_LABEL);
		String text = table.getTableItem(0).getText();
		table.select(0);
		assertTrue(button.isEnabled());
		button.click();

		SWTBotShell propertiesDialog = bot.shell(text);
		assertTrue(propertiesDialog.isOpen());
		propertiesDialog.close();
	}

}
