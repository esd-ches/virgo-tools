/*******************************************************************************
 * Copyright (c) 2007, 2009, 2010 SpringSource
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource - initial API and implementation
 *******************************************************************************/

package org.eclipse.virgo.ide.tests.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ConcurrentModificationException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * @author Steffen Pingel
 */
public class VirgoIdeTestUtil {

    public static final boolean ECLIPSE_3_4 = Platform.getBundle("org.eclipse.equinox.p2.repository") == null;

    public static final boolean ECLIPSE_3_6_OR_LATER;

    static {
        boolean found = false;
        try {
            StyledText.class.getMethod("setTabStops", int[].class); //$NON-NLS-1$
            found = true;
        } catch (NoSuchMethodException e) {
        }
        ECLIPSE_3_6_OR_LATER = found;
    }

    public static final long WAIT_TIME = 2000;

    public static String canocalizeXml(String originalServerXml) throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(originalServerXml)));
        document.normalize();

        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer();
        StringWriter writer = new StringWriter();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(new DOMSource(document.getDocumentElement()), new StreamResult(writer));
        return writer.toString().replace("\\s+\\n", "\\n");
    }

    public static void cleanUpProjects() throws CoreException {
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeAllEditors(false);
        deleteAllProjects();
    }

    /**
     * Copy file from src (path to the original file) to dest (path to the destination file).
     */
    private static void copy(File src, File dest) throws IOException {
        InputStream in = new FileInputStream(src);
        try {
            OutputStream out = new FileOutputStream(dest);
            try {
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }

    /**
     * Copy the given source directory (and all its contents) to the given target directory.
     */
    private static void copyDirectory(File source, File target) throws IOException {
        if (!target.exists()) {
            target.mkdirs();
        }
        File[] files = source.listFiles();
        if (files == null) {
            return;
        }
        for (File sourceChild : files) {
            String name = sourceChild.getName();
            if (name.equals(".svn")) {
                continue;
            }
            File targetChild = new File(target, name);
            if (sourceChild.isDirectory()) {
                copyDirectory(sourceChild, targetChild);
            } else {
                copy(sourceChild, targetChild);
            }
        }
    }

    public static IProject createPredefinedProject(final String projectName, String bundleName) throws CoreException, IOException {
        IJavaProject jp = setUpJavaProject(projectName, bundleName);
        VirgoIdeTestUtil.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
        return jp.getProject();
    }

    public static File createTempDirectory() throws IOException {
        return createTempDirectory("sts", null);
    }

    public static File createTempDirectory(String prefix, String suffix) throws IOException {
        File file = File.createTempFile(prefix, suffix);
        file.delete();
        file.mkdirs();
        return file;
    }

    public static void deleteAllProjects() throws CoreException {
        IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        for (IProject project : allProjects) {
            project.refreshLocal(IResource.DEPTH_INFINITE, null);
            deleteProject(project, true);
        }
        getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
    }

    public static void deleteProject(IProject project, boolean force) throws CoreException {
        if (project.exists() && !project.isOpen()) {
            // force opening so that project can be deleted without logging (see
            // bug 23629)
            project.open(null);
        }
        deleteResource(project, force);
    }

    /**
     * Delete this resource.
     */
    private static void deleteResource(IResource resource, boolean force) throws CoreException {
        if (!resource.exists() || !resource.isAccessible()) {
            return;
        }
        waitForManualBuild();
        waitForAutoBuild();
        CoreException lastException = null;
        try {
            resource.delete(force, null);
        } catch (CoreException e) {
            lastException = e;
            // just print for info
            System.out.println("(CoreException): " + e.getMessage() + " Resource " + resource.getFullPath()); //$NON-NLS-1$ //$NON-NLS-2$
            e.printStackTrace();
        } catch (IllegalArgumentException iae) {
            // just print for info
            System.out.println("(IllegalArgumentException): " + iae.getMessage() + ", resource " + resource.getFullPath()); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (!force) {
            return;
        }
        int retryCount = 10; // wait 1 minute at most
        while (resource.isAccessible() && --retryCount >= 0) {
            waitForAutoBuild();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            try {
                resource.delete(true, null);
            } catch (CoreException e) {
                lastException = e;
                // just print for info
                System.out.println("(CoreException) Retry " + retryCount + ": " + e.getMessage() + ", resource " + resource.getFullPath()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            } catch (IllegalArgumentException iae) {
                // just print for info
                System.out.println(
                    "(IllegalArgumentException) Retry " + retryCount + ": " + iae.getMessage() + ", resource " + resource.getFullPath()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
        }
        if (!resource.isAccessible()) {
            return;
        }
        System.err.println("Failed to delete " + resource.getFullPath()); //$NON-NLS-1$
        if (lastException != null) {
            throw lastException;
        }
    }

    public static File getBundlePath(String pluginId) throws IOException {
        URL platformURL = Platform.getBundle(pluginId).getEntry("/"); //$NON-NLS-1$
        return new File(FileLocator.toFileURL(platformURL).getFile());
    }

    public static File getFilePath(String pluginId, String segment) throws IOException {
        URL platformURL = Platform.getBundle(pluginId).getEntry(segment);
        return new File(FileLocator.toFileURL(platformURL).getFile());
    }

    public static String getMarkerMessages(IMarker[] markers) throws CoreException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < markers.length; i++) {
            IMarker currMarker = markers[i];
            String message = (String) currMarker.getAttribute("message");
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(message);
        }
        return sb.toString();
    }

    /**
     * Returns the OS path to the directory that contains this plugin.
     */
    private static String getPluginDirectoryPath(String bundleName) {
        try {
            URL platformURL = Platform.getBundle(bundleName).getEntry("/"); //$NON-NLS-1$
            return new File(FileLocator.toFileURL(platformURL).getFile()).getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static IProject getProject(String project) {
        return getWorkspaceRoot().getProject(project);
    }

    /**
     * Get an IResource indicated by a given path starting at the workspace root.
     * <p>
     * Different type of resource is returned based on the length of the path and whether or not it ends with a path
     * separator.
     */
    public static IResource getResource(IPath path) {
        try {
            IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
            if (path.segmentCount() == 0) {
                return root;
            }
            IProject project = root.getProject(path.segment(0));
            if (path.segmentCount() == 1) {
                return project;
            }
            if (path.hasTrailingSeparator()) {
                return root.getFolder(path);
            } else {
                return root.getFile(path);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get an IResource from a path String starting at the workspace root.
     * <p>
     * Different type of resource is returned based on the length of the path and whether or not it ends with a path
     * separator.
     * <p>
     * For example "" length = 0 => type of resource is IWorkspaceRoot "foo" length = 1 => type of resource is IProject
     * "foo/src/Foo.java" length > 1 and no trailing "/" => type is IFile "foo/src/          length > 1 and a trailing "
     * /" => type is IFolder
     */
    public static IResource getResource(String pathToFile) {
        return getResource(Path.ROOT.append(pathToFile));
    }

    static String getSourceWorkspacePath(String bundleName) {
        return getPluginDirectoryPath(bundleName) + java.io.File.separator + "workspace"; //$NON-NLS-1$
    }

    /**
     * Returns the IWorkspace this test suite is running on.
     */
    public static IWorkspace getWorkspace() {
        return ResourcesPlugin.getWorkspace();
    }

    public static IWorkspaceRoot getWorkspaceRoot() {
        return getWorkspace().getRoot();
    }

    public static void saveAndWaitForEditor(final IEditorPart editor) throws CoreException {
        Display.getDefault().syncExec(new Runnable() {

            public void run() {
                editor.doSave(null);
            }
        });
        waitForEditor(editor);
    }

    private static IJavaProject setUpJavaProject(final String projectName, String bundleName) throws CoreException, IOException {
        return VirgoIdeTestUtil.setUpJavaProject(projectName, "1.4", getSourceWorkspacePath(bundleName)); //$NON-NLS-1$
    }

    public static IJavaProject setUpJavaProject(final String projectName, String compliance, String sourceWorkspacePath)
        throws CoreException, IOException {
        IProject project = setUpProject(projectName, compliance, sourceWorkspacePath);
        IJavaProject javaProject = JavaCore.create(project);
        return javaProject;
    }

    public static IProject setUpProject(final String projectName, String compliance, String sourceWorkspacePath) throws CoreException, IOException {
        // copy files in project from source workspace to target workspace
        String targetWorkspacePath = getWorkspaceRoot().getLocation().toFile().getCanonicalPath();
        copyDirectory(new File(sourceWorkspacePath, projectName), new File(targetWorkspacePath, projectName));

        // create project
        final IProject project = getWorkspaceRoot().getProject(projectName);
        IWorkspaceRunnable populate = new IWorkspaceRunnable() {

            public void run(IProgressMonitor monitor) throws CoreException {
                project.create(null);
                try {
                    project.open(null);
                } catch (ConcurrentModificationException e) {
                    // wait and try again to work-around
                    // ConcurrentModificationException (bug 280488)
                    try {
                        Thread.sleep(500);
                        project.open(null);
                        project.refreshLocal(IResource.DEPTH_INFINITE, null);
                    } catch (InterruptedException e1) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        };
        getWorkspace().run(populate, null);
        return project;
    }

    /**
     * Wait for autobuild notification to occur
     */
    public static void waitForAutoBuild() {
        waitForJobFamily(ResourcesPlugin.FAMILY_AUTO_BUILD);
    }

    public static void waitForDisplay() {
        while (Display.getDefault().readAndDispatch()) {
            // do nothing
        }
    }

    public static void waitForEditor(IEditorPart editor) throws CoreException {
        IFileEditorInput editorInput = (IFileEditorInput) editor.getEditorInput();
        IFile file = editorInput.getFile();
        waitForResource(file);
    }

    public static void waitForJobFamily(Object jobFamily) {
        boolean wasInterrupted = false;
        do {
            try {
                Job.getJobManager().join(jobFamily, null);
                wasInterrupted = false;
            } catch (OperationCanceledException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                wasInterrupted = true;
            }
        } while (wasInterrupted);
    }

    public static void waitForManualBuild() {
        waitForJobFamily(ResourcesPlugin.FAMILY_MANUAL_BUILD);
    }

    public static void waitForResource(IResource resource) throws CoreException {
        waitForAutoBuild();
        waitForManualBuild();
        waitForJobFamily(ResourcesPlugin.FAMILY_AUTO_REFRESH);
        waitForJobFamily(ResourcesPlugin.FAMILY_MANUAL_REFRESH);
        resource.refreshLocal(IResource.DEPTH_ONE, null);
    }

    public static void setAutoBuilding(boolean enabled) throws CoreException {
        IWorkspaceDescription wsd = getWorkspace().getDescription();
        if (!wsd.isAutoBuilding() == enabled) {
            wsd.setAutoBuilding(enabled);
            getWorkspace().setDescription(wsd);
        }
    }
}
