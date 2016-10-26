/*******************************************************************************
 *  Copyright (c) 2016 GianMaria Romanato
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     GianMaria Romanato - initial API and implementation
 *******************************************************************************/

package org.eclipse.virgo.ide.facet.core;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ide.undo.CreateProjectOperation;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;

/**
 * CreatePlanProjectOperation creates a new plan project. A plan project is a Java project with the plan facet. The
 * operation optionally creates an empty plan file as well.
 * <p>
 */
public class CreatePlanProjectOperation extends AbstractOperation {

    private static final String UTF_8 = "UTF-8";

    private static final String TEMPLATE_PATH = "/org/eclipse/virgo/ide/facet/internal/core/plan_template.xml";

    private static final String WST_FACET_NATURE = org.eclipse.wst.common.project.facet.core.internal.FacetCorePlugin.PLUGIN_ID + ".nature"; //$NON-NLS-1$

    private final IProject projectHandle;

    private final URI location;

    private final Shell shell;

    private final boolean scoped;

    private final boolean atomic;

    private final String planName;

    /**
     * Creates a new instance
     *
     * @param projectHandle the handle to the project to be created
     * @param location an optional location if the project is not to be created in the default location
     * @param planName an optional plan name is a plan name is to be created within the project
     * @param scoped whether the plan is scoped
     * @param atomic whether the plan is atomic
     * @param shell a shell for error reporting
     */
    public CreatePlanProjectOperation(IProject projectHandle, URI location, String planName, boolean scoped, boolean atomic, Shell shell) {
        Assert.isNotNull(projectHandle, "projectHandle cannot be null"); //$NON-NLS-1$
        Assert.isNotNull(shell, "shell cannot be null"); //$NON-NLS-1$
        this.projectHandle = projectHandle;
        this.location = location;
        this.shell = shell;
        this.scoped = scoped;
        this.atomic = atomic;
        this.planName = planName;
    }

    public void run(IProgressMonitor monitor) throws CoreException {
        monitor.beginTask("", planName != null ? 4 : 3);

        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IProjectDescription description = workspace.newProjectDescription(projectHandle.getName());
        description.setLocationURI(location);

        // create the new project operation
        CreateProjectOperation op = new CreateProjectOperation(description, "");
        try {
            op.execute(new NullProgressMonitor(), WorkspaceUndoUtil.getUIInfoAdapter(shell));
        } catch (ExecutionException e1) {
            CoreException cex = new CoreException(new Status(IStatus.ERROR, FacetCorePlugin.PLAN_FACET_ID, e1.getMessage()));
            cex.initCause(e1);
            throw cex;
        }
        monitor.worked(1);

        // make it a Java/Plan project
        description = projectHandle.getDescription();
        String[] natures = description.getNatureIds();
        String[] newNatures = new String[natures.length + 3];
        System.arraycopy(natures, 0, newNatures, 0, natures.length);
        System.arraycopy(new String[] { WST_FACET_NATURE, FacetCorePlugin.PLAN_NATURE_ID, JavaCore.NATURE_ID }, 0, newNatures, natures.length, 3);
        description.setNatureIds(newNatures);
        projectHandle.setDescription(description, null);

        monitor.worked(1);

        // setup classpath
        IJavaProject javaProject = JavaCore.create(projectHandle);
        IFolder binFolder = projectHandle.getFolder("bin");
        binFolder.create(false, true, null);
        javaProject.setOutputLocation(binFolder.getFullPath(), null);

        IFolder sourceFolder = projectHandle.getFolder("src");
        sourceFolder.create(false, true, null);
        IClasspathEntry sourceEntry = JavaCore.newSourceEntry(sourceFolder.getFullPath());

        List<IClasspathEntry> entries = Arrays.asList(sourceEntry, JavaRuntime.getDefaultJREContainerEntry());
        javaProject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), null);

        IFacetedProject fProject = ProjectFacetsManager.create(projectHandle, true, null);
        fProject.installProjectFacet(ProjectFacetsManager.getProjectFacet(FacetCorePlugin.PLAN_FACET_ID).getDefaultVersion(), null, null);

        monitor.worked(1);

        if (planName != null) {
            String content = readResourceFromClassPath(TEMPLATE_PATH, UTF_8);
            content = MessageFormat.format(content, planName, scoped, atomic);
            IPath p = new Path(planName);
            if (!"plan".equals(p.getFileExtension())) {
                p = p.addFileExtension("plan");
            }
            IFile planFile = sourceFolder.getFile(p);
            try {
                planFile.create(new ByteArrayInputStream(content.getBytes(UTF_8)), true, null);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e); // will never happen, all JVMs support UTF-8
            }
            monitor.worked(1);

        }

        monitor.done();
    }

}
