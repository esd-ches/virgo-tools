/*******************************************************************************
 * Copyright (c) 2009, 2010 SpringSource, a divison of VMware, Inc.
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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.jarpackager.IJarExportRunnable;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.virgo.ide.export.BundleExportUtils;
import org.eclipse.virgo.ide.tests.util.VirgoIdeTestCase;
import org.eclipse.virgo.ide.tests.util.VirgoIdeTestUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Terry Hon
 * @author Christian Dupuis
 * @author Steffen Pingel
 */
public class BundleExportTestCase extends VirgoIdeTestCase {

    @Test
    public void testExportOperation() throws InvocationTargetException, InterruptedException, IOException, CoreException {
        IPath jarLocation = Path.fromOSString(VirgoIdeTestUtil.getWorkspaceRoot().getLocation().toFile().getCanonicalPath()).append(
            "bundlor-test-1.0.0.jar");
        IJavaProject javaProject = JavaCore.create(createPredefinedProject("bundlor-test"));

        IJarExportRunnable op = BundleExportUtils.createExportOperation(javaProject, jarLocation, Display.getDefault().getActiveShell(),
            new ArrayList<IStatus>());
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().run(true, true, op);
        IStatus status = op.getStatus();
        Assert.assertTrue("Expects status is OK", status.isOK());

        File file = new File(jarLocation.toOSString());
        FileInputStream fileStream = new FileInputStream(file);
        ZipInputStream stream = new ZipInputStream(fileStream);

        List<String> fileNames = new ArrayList<String>();
        while (stream.available() > 0) {
            ZipEntry entry = stream.getNextEntry();
            if (entry != null) {
                fileNames.add(entry.getName());
            }
        }

        String[] sortedFileNames = fileNames.toArray(new String[fileNames.size()]);
        Arrays.sort(sortedFileNames);

        Assert.assertTrue("Expects 4 entries", sortedFileNames.length == 4);
        Assert.assertEquals("Expects 1st entry to be META-INF/MANIFEST.MF", "META-INF/MANIFEST.MF", sortedFileNames[0]);
        Assert.assertEquals("Expects 2nd entry to be META-INF/spring/module-context.xml", "META-INF/spring/module-context.xml", sortedFileNames[1]);
        Assert.assertEquals("Expects 3rd entry to be com/springsource/Foo.class", "com/springsource/Foo.class", sortedFileNames[2]);
        Assert.assertEquals("Expects 4th entry to be com/springsource/bar/Bar.class", "com/springsource/bar/Bar.class", sortedFileNames[3]);

        fileStream.close();
        stream.close();
    }

    @Override
    protected String getBundleName() {
        return "org.eclipse.virgo.ide.export.tests";
    }

}
