
package org.eclipse.virgo.ide.pde.core.internal.cmd;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.pde.core.project.IBundleProjectDescription;
import org.eclipse.pde.core.project.IBundleProjectService;
import org.eclipse.virgo.ide.facet.core.FacetCorePlugin;
import org.eclipse.virgo.ide.pde.core.internal.Constants;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;

/**
 * SetupProjectOperation updates an existing PDE project to make it a Virgo-PDE project. If a context root is passed, it
 * is made a Web Bundle PDE project.
 * <p>
 * Note that this runnable is meant to adapt for Virgo a newly created PDE project. It is not intended for migrating a
 * PDE project that has been modified after initial creation.
 * <p>
 */
public class SetupProjectOperation implements IWorkspaceRunnable {

    private static final String WST_FACET_NATURE = org.eclipse.wst.common.project.facet.core.internal.FacetCorePlugin.PLUGIN_ID + ".nature"; //$NON-NLS-1$

    private static final String WEB_CONTENT_FOLDER = "WebContent"; //$NON-NLS-1$

    private static final String WEB_INF_FOLDER = "WEB-INF"; //$NON-NLS-1$

    private static final String BIN_WEB_INF_CLASSES = "bin/WEB-INF/classes"; //$NON-NLS-1$

    private static final String SRC = "src"; //$NON-NLS-1$

    private static final String BIN = "bin"; //$NON-NLS-1$

    private static final String WEB_XML = "web.xml"; //$NON-NLS-1$

    private static final String INDEX_HTML = "index.html"; //$NON-NLS-1$

    private static final String BUILD_PROPERTIES = "build.properties"; //$NON-NLS-1$

    private static final String UTF_8 = "UTF-8"; //$NON-NLS-1$

    private static final String ISO_8859_1 = "ISO-8859-1"; //$NON-NLS-1$

    private static final String CLASS_PATH_VALUE = ".,WEB-INF/classes"; //$NON-NLS-1$

    private static final String SLASH = "/"; //$NON-NLS-1$

    private static final String HEADER_BUNDLE_CLASS_PATH = "Bundle-ClassPath"; //$NON-NLS-1$

    private static final String HEADER_WEB_CONTEXT_PATH = "Web-ContextPath"; //$NON-NLS-1$

    private final IProject project;

    private final String contextRoot;

    private final IBundleProjectService service;

    /**
     * Creates a new instance.
     *
     * @param project a mandatory project resource
     * @param contextRoot a context root name or <code>null</code> if the project is not a Web project
     * @param service a {@link IBundleProjectService} instance
     */
    public SetupProjectOperation(IProject project, String contextRoot, IBundleProjectService service) {
        Assert.isNotNull(project, "project cannot be null"); //$NON-NLS-1$
        Assert.isNotNull(service, "service cannot be null"); //$NON-NLS-1$
        this.project = project;
        this.contextRoot = sanitizeContextRoot(contextRoot);
        this.service = service;
    }

    private String sanitizeContextRoot(String contextRoot2) {
        if (contextRoot2 != null) {
            if (contextRoot2.startsWith(SLASH)) {
                contextRoot2 = contextRoot2.substring(1);
            }
        }
        return contextRoot2;
    }

    /**
     * {@inheritDoc}
     */
    public void run(IProgressMonitor monitor) throws CoreException {
        monitor.beginTask("", 4); //$NON-NLS-1$

        org.eclipse.virgo.ide.pde.core.internal.Helper.forcePDEEditor(project);
        monitor.worked(1);

        addNatures(project);
        monitor.worked(1);

        if (contextRoot != null && contextRoot.length() > 0) {
            IPath webContentPath = configureWABClasspath(project);

            IPath WebXMLPath = webContentPath.append(WEB_INF_FOLDER);
            createWebXML(contextRoot, WebXMLPath);
            createIndexHTML(contextRoot, webContentPath);
            createBuildProperties();

            IBundleProjectDescription bundleDescription = service.getDescription(project);

            bundleDescription.setHeader(HEADER_WEB_CONTEXT_PATH, SLASH + contextRoot);
            bundleDescription.setHeader(HEADER_BUNDLE_CLASS_PATH, CLASS_PATH_VALUE);
            bundleDescription.apply(null);
        }
        monitor.worked(1);

        IFacetedProject fProject = ProjectFacetsManager.create(project.getProject(), true, null);
        fProject.installProjectFacet(ProjectFacetsManager.getProjectFacet(FacetCorePlugin.BUNDLE_FACET_ID).getDefaultVersion(), null, null);
        monitor.worked(1);
        monitor.done();
    }

    private void createBuildProperties() throws CoreException {
        String template = readResourceFromClassPath(BUILD_PROPERTIES, ISO_8859_1);
        IFile file = project.getFile(BUILD_PROPERTIES);
        try {
            if (!file.exists()) {
                file.create(new ByteArrayInputStream(template.getBytes(ISO_8859_1)), true, null);
            } else {
                file.setContents(new ByteArrayInputStream(template.getBytes(ISO_8859_1)), true, false, null);
            }
        } catch (UnsupportedEncodingException e) {
            throw new CoreException(new Status(IStatus.ERROR, Constants.PLUGIN_ID, e.getMessage(), e));
        }
    }

    private void createIndexHTML(String contextRoot, IPath webContentPath) throws CoreException {
        createFileFromTemplate(contextRoot, webContentPath, INDEX_HTML, UTF_8);
    }

    private void createWebXML(String contextRoot, IPath webContentPath) throws CoreException {
        createFileFromTemplate(contextRoot, webContentPath, WEB_XML, UTF_8);
    }

    private void createFileFromTemplate(String contextRoot, IPath webContentPath, String fileNane, String charset) throws CoreException {
        IFolder webContentFolder = ResourcesPlugin.getWorkspace().getRoot().getFolder(webContentPath);

        java.util.List<IFolder> toBeCreated = new ArrayList<IFolder>();
        IFolder tmp = webContentFolder;
        while (!tmp.exists()) {
            toBeCreated.add(0, tmp);
            if (tmp.getParent() instanceof IFolder) {
                tmp = (IFolder) tmp.getParent();
            } else {
                break;
            }
        }
        for (IFolder iFolder : toBeCreated) {
            iFolder.create(true, false, null);
        }

        IFile file = webContentFolder.getFile(fileNane);
        if (!file.exists()) {
            String template = readResourceFromClassPath(fileNane, charset);
            String newWab = MessageFormat.format(template, contextRoot);
            try {
                file.create(new ByteArrayInputStream(newWab.getBytes(charset)), true, null);
            } catch (UnsupportedEncodingException e) {
                throw new CoreException(new Status(IStatus.ERROR, Constants.PLUGIN_ID, e.getMessage(), e));
            }
        }
    }

    private String readResourceFromClassPath(String path, String charset) throws CoreException {
        InputStream is = getClass().getResourceAsStream(path);
        if (is != null) {
            try {
                InputStreamReader r = new InputStreamReader(is, charset);
                StringBuilder sb = new StringBuilder();
                int c;
                while ((c = r.read()) != -1) {
                    sb.append((char) c);
                }
                return sb.toString();
            } catch (IOException e) {
                throw new CoreException(new Status(IStatus.ERROR, Constants.PLUGIN_ID, e.getMessage(), e));
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        } else {
            throw new CoreException(new Status(IStatus.ERROR, Constants.PLUGIN_ID, "Template file missing " + path)); //$NON-NLS-1$
        }

    }

    private IPath configureWABClasspath(IProject project) throws CoreException, JavaModelException {
        IJavaProject javaProject = (IJavaProject) project.getNature(JavaCore.NATURE_ID);
        javaProject.setOutputLocation(project.getFullPath().append(BIN), null);
        IClasspathEntry[] entries = javaProject.getRawClasspath();
        IClasspathEntry[] newEntries = new IClasspathEntry[entries.length + 1];
        for (int i = 0; i < entries.length; i++) {
            IClasspathEntry iClasspathEntry = entries[i];
            if (iClasspathEntry.getEntryKind() == IClasspathEntry.CPE_SOURCE && iClasspathEntry.getPath().lastSegment().equals(SRC)) {
                IClasspathEntry newEntry = JavaCore.newSourceEntry(iClasspathEntry.getPath(), iClasspathEntry.getInclusionPatterns(),
                    iClasspathEntry.getExclusionPatterns(), project.getFullPath().append(BIN_WEB_INF_CLASSES));
                newEntries[i] = newEntry;
                break;
            } else {
                newEntries[i] = entries[i];
            }
        }

        IPath webContentPath = project.getFullPath().append(WEB_CONTENT_FOLDER);
        newEntries[entries.length] = JavaCore.newLibraryEntry(webContentPath, null, null);

        javaProject.setRawClasspath(newEntries, null);
        return webContentPath;
    }

    private IProjectDescription addNatures(IProject project) throws CoreException {
        IProjectDescription description = project.getDescription();
        String[] natures = description.getNatureIds();

        // Add the natures
        String[] newNatures = new String[natures.length + 3];
        System.arraycopy(natures, 0, newNatures, 0, natures.length);

        // <nature>org.eclipse.wst.common.project.facet.core.nature</nature>
        newNatures[natures.length + 0] = WST_FACET_NATURE;
        // <nature>org.eclipse.virgo.ide.facet.core.bundlenature</nature> <nature>
        newNatures[natures.length + 1] = org.eclipse.virgo.ide.facet.core.FacetCorePlugin.BUNDLE_NATURE_ID;
        newNatures[natures.length + 2] = org.eclipse.virgo.ide.pde.core.internal.Constants.NATURE_ID;

        description.setNatureIds(newNatures);
        project.setDescription(description, null);
        return description;
    }
}
