/*******************************************************************************
 * Copyright (c) 2009 - 2012 SpringSource, a divison of VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource, a division of VMware, Inc. - initial API and implementation
 *******************************************************************************/

package org.eclipse.virgo.ide.ui.wizards;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.virgo.ide.ui.editors.ParManifestEditor;
import org.eclipse.virgo.ide.ui.tests.AbstractManifestUiTestCase;

/**
 * @author Leo Dos Santos
 */
public class NewParProjectWizardUiTest extends AbstractManifestUiTestCase {

    private static String PROJECT_NAME = "ParProject";

    public void testProjectCreation() {
        this.bot.menu("File").menu("New").menu("Project...").click();
        SWTBotShell wizard = this.bot.shell("New Project");
        wizard.activate();
        this.bot.tree().expandNode("Virgo").select("PAR Project");

        SWTBotButton next = this.bot.button("Next >");
        next.click();
        assertFalse(next.isEnabled());

        this.bot.textWithLabel("Project name:").setText(PROJECT_NAME);
        assertTrue(next.isEnabled());
        next.click();

        SWTBotButton finish = this.bot.button("Finish");
        assertTrue(finish.isEnabled());
        finish.click();
        this.bot.waitUntil(shellCloses(wizard), 15000);

        SWTBotEditor editor = this.bot.editorById(ParManifestEditor.ID_EDITOR);
        // Occasional failure. Caused by race condition?
        assertEquals(PROJECT_NAME, editor.getTitle());
    }

    public void testProjectCreationWithWorkingSets() {
        final IWorkingSetManager manager = PlatformUI.getWorkbench().getWorkingSetManager();
        IWorkingSet newSet = manager.createWorkingSet("Testing", new IAdaptable[0]);
        newSet.setId("org.eclipse.jdt.ui.JavaWorkingSetPage");
        manager.addWorkingSet(newSet);
        assertTrue(newSet.getElements().length == 0);

        this.bot.menu("File").menu("New").menu("Project...").click();
        SWTBotShell wizard = this.bot.shell("New Project");
        wizard.activate();
        this.bot.tree().expandNode("Virgo").select("PAR Project");

        SWTBotButton next = this.bot.button("Next >");
        next.click();
        assertFalse(next.isEnabled());

        this.bot.textWithLabel("Project name:").setText(PROJECT_NAME);
        this.bot.checkBoxInGroup("Working sets").select();
        this.bot.button("Select...").click();
        this.bot.table().getTableItem("Testing").check();
        this.bot.button("OK").click();
        assertTrue(next.isEnabled());
        next.click();

        SWTBotButton finish = this.bot.button("Finish");
        assertTrue(finish.isEnabled());
        finish.click();
        this.bot.waitUntil(shellCloses(wizard));

        IWorkingSet testingSet = manager.getWorkingSet("Testing");
        IAdaptable[] elements = testingSet.getElements();
        assertTrue(elements.length == 1);
        manager.removeWorkingSet(testingSet);
    }

}
