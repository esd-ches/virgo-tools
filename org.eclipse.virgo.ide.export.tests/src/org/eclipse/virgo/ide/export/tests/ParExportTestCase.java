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
package org.eclipse.virgo.ide.export.tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.virgo.ide.export.ParExportWizard;
import org.eclipse.virgo.ide.tests.util.VirgoIdeTestCase;
import org.eclipse.virgo.ide.tests.util.VirgoIdeTestUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Terry Hon
 * @author Christian Dupuis
 * @author Steffen Pingel
 */
public class ParExportTestCase extends VirgoIdeTestCase {

	@Test
	public void testExportOperation() throws InvocationTargetException, InterruptedException, IOException, CoreException {
		IPath parLocation = Path.fromOSString(VirgoIdeTestUtil.getWorkspaceRoot().getLocation().toFile().getCanonicalPath()).append("test-1.0.0.par");
		createPredefinedProject("bundlor-test");
		
		IProject project = VirgoIdeTestUtil.setUpProject("bundlor-test-par", "1.4", getSourceWorkspacePath());
		VirgoIdeTestUtil.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);

		boolean status = ParExportWizard.exportPar(project, parLocation, PlatformUI.getWorkbench().getActiveWorkbenchWindow(), Display.getDefault().getActiveShell());
		Assert.assertTrue("Expects status is OK", status);
		
		File file = new File(parLocation.toOSString());
		FileInputStream fileStream = new FileInputStream(file);
		ZipInputStream stream = new ZipInputStream(fileStream);
		
		List<String> fileNames = new ArrayList<String>();
		while(stream.available() > 0) {
			ZipEntry entry = stream.getNextEntry();
			if (entry != null) {
				fileNames.add(entry.getName());
			}
		}
		
		String[] sortedFileNames = fileNames.toArray(new String[fileNames.size()]);
		Arrays.sort(sortedFileNames);

		Assert.assertTrue("Expects 2 entries", sortedFileNames.length == 2);
		Assert.assertEquals("Expects 1st entry to be META-INF/MANIFEST.MF", "META-INF/MANIFEST.MF", sortedFileNames[0]);
		Assert.assertEquals("Expects 2nd entry to be com.springsource.bundlor-1.0.0.jar", "com.springsource.bundlor-1.0.0.jar", sortedFileNames[1]);
		
		fileStream.close();
		stream.close();
	}

	@Override
	protected String getBundleName() {
		return "org.eclipse.virgo.ide.export.tests";
	}
	
}
