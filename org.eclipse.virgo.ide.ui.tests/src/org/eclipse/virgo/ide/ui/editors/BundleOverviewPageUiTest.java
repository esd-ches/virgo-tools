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
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.virgo.ide.ui.tests.AbstractManifestUiTestCase;

/**
 * @author Leo Dos Santos
 */
public class BundleOverviewPageUiTest extends AbstractManifestUiTestCase {

	/*
	 * These tests will fail because the links we are searching for are GC drawn
	 * and therefore not widgets/controls. This test case has been commented out
	 * from the AllSwtbotDrivenTests suite.
	 */

	public void testExportBundleHyperlink() throws Exception {
		final BundleManifestEditor manifest = openBundleManifestFile("src/META-INF/MANIFEST.MF");
		UIThreadRunnable.syncExec(new VoidResult() {
			public void run() {
				manifest.setActivePage(BundleOverviewPage.PAGE_ID);
			}
		});
		bot.hyperlink("Export Bundle").click();

		SWTBotShell exportDialog = bot.shell("Bundle Export Wizard");
		assertTrue(exportDialog.isOpen());
		exportDialog.close();
	}

	public void testDependenciesHyperlink() throws Exception {
		final BundleManifestEditor manifest = openBundleManifestFile("src/META-INF/MANIFEST.MF");
		UIThreadRunnable.syncExec(new VoidResult() {
			public void run() {
				manifest.setActivePage(BundleOverviewPage.PAGE_ID);
			}
		});
		bot.hyperlink("Dependencies").click();

		UIThreadRunnable.syncExec(new VoidResult() {
			public void run() {
				assertEquals(manifest.findPage(BundleDependenciesPage.PAGE_ID), manifest.getActiveEditor());
			}
		});
	}

	public void testRuntimeHyperlink() throws Exception {
		final BundleManifestEditor manifest = openBundleManifestFile("src/META-INF/MANIFEST.MF");
		UIThreadRunnable.syncExec(new VoidResult() {
			public void run() {
				manifest.setActivePage(BundleOverviewPage.PAGE_ID);
			}
		});
		bot.hyperlink("Runtime").click();

		UIThreadRunnable.syncExec(new VoidResult() {
			public void run() {
				assertEquals(manifest.findPage(BundleRuntimePage.PAGE_ID), manifest.getActiveEditor());
			}
		});
	}

}
