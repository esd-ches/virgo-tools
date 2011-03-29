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

import java.io.File;

import junit.framework.TestCase;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.ide.IDE;
import org.eclipse.virgo.ide.tests.util.VirgoIdeTestUtil;
import org.eclipse.virgo.ide.ui.editors.BundleManifestEditor;
import org.eclipse.virgo.ide.ui.editors.ParManifestEditor;
import org.eclipse.virgo.ide.ui.tests.ServerIdeUiTestPlugin;


/**
 * @author Steffen Pingel
 * @author Leo Dos Santos
 */
public class BundleManifestEditorTest extends TestCase {

	private IWorkbenchPage activePage;

	@Override
	protected void setUp() throws Exception {
		activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		activePage.closeAllEditors(false);
		assertEquals(0, activePage.getEditorReferences().length);
	}

	@Override
	protected void tearDown() throws Exception {
		activePage.closeAllEditors(false);
	}

	public void testCreateSystemFileContextsFromDotSettings() throws Exception {
		File source = VirgoIdeTestUtil.getFilePath(ServerIdeUiTestPlugin.PLUGIN_ID, "/testdata/bundle/manifest.mf");
		IPath path = new Path(source.getAbsolutePath());
		IFileStore file = EFS.getLocalFileSystem().getStore(path);
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.openEditor(new FileStoreEditorInput(file), BundleManifestEditor.ID_EDITOR);

		assertEquals(1, activePage.getEditorReferences().length);
		IEditorPart editor = activePage.getEditorReferences()[0].getEditor(true);
		assertEquals(BundleManifestEditor.class, editor.getClass());
		BundleManifestEditor parEditor = (BundleManifestEditor) editor;
		assertEquals(4, parEditor.getParts().length);
	}

	public void testOpenInDefaultEditor() throws Exception {
		IProject project = VirgoIdeTestUtil.createPredefinedProject("SimpleBundleProject",
				"org.eclipse.virgo.ide.ui.tests");
		IFile file = project.getFile("src/META-INF/MANIFEST.MF");
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IEditorPart editor = IDE.openEditor(page, file);
		assertTrue(editor instanceof BundleManifestEditor);
		assertFalse(editor instanceof ParManifestEditor);
		VirgoIdeTestUtil.cleanUpProjects();
	}

}
