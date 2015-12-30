/*******************************************************************************
 *  Copyright (c) 2015 GianMaria Romanato
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     GianMaria Romanato - initial API and implementation
 *******************************************************************************/

package org.eclipse.virgo.ide.pde.core.internal;

import static org.eclipse.virgo.ide.pde.core.internal.Constants.META_INF;
import static org.eclipse.virgo.ide.pde.core.internal.Constants.WebContent;
import static org.eclipse.virgo.ide.pde.core.internal.Helper.DEBUG;

import java.util.Map;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.osgi.util.NLS;

/**
 * A custom build command that ensures that the content of META-INF is copied to the binary folder of the plug-in
 * project so that Virgo Server can find it via class-path.
 * <p>
 * Additionally the builder parsers the MANIFEST.MF and copies to bin folder any nested JAR (Bundle-ClassPath header)
 * and native libraries (Bundle-NativeCode header).
 *
 *
 *
 */
public class Builder extends IncrementalProjectBuilder {

    private static abstract class Predicate<T> {

        public abstract boolean accept(T t);

        public final static <T> Predicate<T> tautology() {
            return new Predicate<T>() {

                @Override
                public boolean accept(T t) {
                    return true;
                }
            };
        }
    }

    public Builder() {
        super();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.core.internal.events.InternalBuilder#build(int, java.util.Map,
     * org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    @SuppressWarnings("rawtypes")
    protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
        Helper.forcePDEEditor(getProject());

        IPath outputLocation = Helper.getOutputLocation(getProject());

        if (DEBUG) {
            String typeStr = "UNKNOWN"; //$NON-NLS-1$
            switch (kind) {
                case INCREMENTAL_BUILD:
                    typeStr = "INCREMENTAL"; //$NON-NLS-1$
                    break;
                case AUTO_BUILD:
                    typeStr = "AUTO"; //$NON-NLS-1$
                    break;
                case CLEAN_BUILD:
                    typeStr = "CLEAN"; //$NON-NLS-1$
                    break;
                case FULL_BUILD:
                    typeStr = "FULL"; //$NON-NLS-1$
                    break;
            }
            debug("Build type " + typeStr + " output location: " + outputLocation.toString()); //$NON-NLS-1$ //$NON-NLS-2$
        }

        if (kind == CLEAN_BUILD) {
            if (DEBUG) {
                debug("Doing nothing"); //$NON-NLS-1$
            }
            return null;
        }

        if (kind == FULL_BUILD) {
            fullBuild(outputLocation, monitor);
        } else {
            IResourceDelta delta = getDelta(getProject());
            if (delta == null) {
                if (DEBUG) {
                    debug("Incremental build was requested but delta is null, performing full build"); //$NON-NLS-1$
                }
                fullBuild(outputLocation, monitor);
                return null;
            }

            if (!Helper.checkMETAINFFolder(getProject())) {
                if (DEBUG) {
                    debug("Incremental build was requested but META-INF folder is missing from output location, performing full build"); //$NON-NLS-1$
                }
                fullBuild(outputLocation, monitor);
                return null;
            }

            if (!Helper.checkLibraries(getProject())) {
                if (DEBUG) {
                    debug("Incremental build was requested but some libraries are missing from output location, performing full build"); //$NON-NLS-1$
                }
                fullBuild(outputLocation, monitor);
                return null;
            }

            if (DEBUG) {
                debug("Incremental build"); //$NON-NLS-1$
            }
            incrementalBuild(outputLocation, delta, monitor);
            return null;
        }

        return null;
    }

    private void incrementalBuild(IPath outputLocation, final IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
        monitor.beginTask(Messages.Builder_IncrementalBuildMessage, 3);

        if (delta.findMember(new Path(META_INF)) != null) {
            buildMetaInf(outputLocation, new SubProgressMonitor(monitor, 1));
        } else {
            monitor.worked(1);
        }

        if (delta.findMember(new Path(Constants.WebContent)) != null) {
            buildWebContent(outputLocation, new SubProgressMonitor(monitor, 1));
        } else {
            monitor.worked(1);
        }

        Predicate<String> selectChanged = new Predicate<String>() {

            @Override
            public boolean accept(String t) {
                return delta.findMember(new Path(t)) != null;
            }
        };
        buildLibraries(selectChanged, outputLocation, new SubProgressMonitor(monitor, 1));
        monitor.done();
    }

    private void fullBuild(IPath outputLocation, IProgressMonitor monitor) throws CoreException {
        if (DEBUG) {
            debug("Full build, output location: " + outputLocation.toOSString()); //$NON-NLS-1$
        }
        monitor.beginTask(Messages.Builder_FullBuildMessage, 3);

        buildMetaInf(outputLocation, new SubProgressMonitor(monitor, 1));
        buildWebContent(outputLocation, new SubProgressMonitor(monitor, 1));

        buildLibraries(null, outputLocation, new SubProgressMonitor(monitor, 1));

        monitor.done();

    }

    private void buildLibraries(Predicate<String> predicate, IPath outputLocation, IProgressMonitor monitor) throws CoreException {
        IFolder binFolder = ResourcesPlugin.getWorkspace().getRoot().getFolder(outputLocation);
        binFolder.refreshLocal(IResource.DEPTH_ONE, null);

        java.util.List<String> toCopy = Helper.getLibraryEntries(getProject());

        monitor.beginTask(Messages.Builder_copy_libraries, toCopy.size());
        for (String path : toCopy) {
            if (predicate == null || predicate.accept(path)) {
                Helper.copyLibraryToBin(getProject(), path);
            }
            monitor.worked(1);
        }
        monitor.done();
    }

    private void buildWebContent(IPath outputLocation, IProgressMonitor monitor) throws CoreException, JavaModelException {
        IFolder webContentFolder = getProject().getFolder(WebContent);
        if (webContentFolder.exists()) {
            buildFilesInFolder(monitor, webContentFolder, ResourcesPlugin.getWorkspace().getRoot().getFolder(outputLocation),
                Predicate.<IResource> tautology(), true);

        }
    }

    private void buildMetaInf(IPath outputLocation, IProgressMonitor monitor) throws CoreException, JavaModelException {
        IProject project = getProject();
        IFolder infFolder = project.getFolder(META_INF);

        if (!infFolder.exists()) {
            error(META_INF + " folder not found for project: " + getProject().getName()); //$NON-NLS-1$
            return;
        }

        IFolder binFolder = ResourcesPlugin.getWorkspace().getRoot().getFolder(outputLocation);
        if (!binFolder.exists()) {
            binFolder.create(true, true, null);
        } else {
            if (!binFolder.isSynchronized(IResource.DEPTH_ONE)) {
                binFolder.refreshLocal(IResource.DEPTH_ONE, null);
            }
        }
        IFolder binaryInfFolder = binFolder.getFolder(META_INF);
        if (!binaryInfFolder.exists()) {
            binaryInfFolder.create(true, true, null);
        } else {
            if (!binaryInfFolder.isSynchronized(IResource.DEPTH_ONE)) {
                binaryInfFolder.refreshLocal(IResource.DEPTH_ONE, null);
            }
        }

        buildFilesInFolder(monitor, infFolder, binaryInfFolder, Predicate.<IResource> tautology(), false);
        monitor.done();
    }

    private void buildFilesInFolder(IProgressMonitor monitor, IFolder from, IFolder to, Predicate<IResource> predicate, boolean merge)
        throws CoreException {
        IResource[] children = from.members();

        SubMonitor sub = SubMonitor.convert(monitor, children.length);

        sub.beginTask(NLS.bind(Messages.Builder_CopyContent, from.getName()), children.length);
        for (IResource iResource : children) {
            if (predicate.accept(iResource)) {
                if (!iResource.isTeamPrivateMember() && !iResource.isDerived()) {
                    IPath target = to.getFullPath().append(iResource.getName());
                    IResource res = ResourcesPlugin.getWorkspace().getRoot().findMember(target);
                    if (res != null && res.exists()) {
                        if (DEBUG) {
                            debug(res.getFullPath().toString() + " exists"); //$NON-NLS-1$
                        }
                        res.refreshLocal(IResource.DEPTH_INFINITE, null);

                        if (res.getType() == IResource.FILE) {
                            res.refreshLocal(IResource.DEPTH_ONE, null);
                            res.delete(true, null);
                            if (DEBUG) {
                                debug(res.getFullPath().toString() + " deleted"); //$NON-NLS-1$
                            }
                            iResource.copy(target, true, null);
                            if (DEBUG) {
                                debug("Copied " + iResource.getFullPath().toString() + " to " + target.toString()); //$NON-NLS-1$ //$NON-NLS-2$
                            }
                        } else {
                            // folder
                            if (!merge) {
                                res.delete(true, null);
                                iResource.copy(target, true, null);
                                if (DEBUG) {
                                    debug("Copied " + iResource.getFullPath().toString() + " to " + target.toString()); //$NON-NLS-1$ //$NON-NLS-2$
                                }
                            } else {
                                buildFilesInFolder(new NullProgressMonitor(), (IFolder) iResource, (IFolder) res, predicate, true);
                                if (DEBUG) {
                                    debug("Merged " + iResource.getFullPath().toString() + " into " + target.toString()); //$NON-NLS-1$ //$NON-NLS-2$
                                }
                            }
                        }
                    } else {
                        iResource.copy(target, true, null);
                        if (DEBUG) {
                            debug("Copied " + iResource.getFullPath().toString() + " to " + target.toString()); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                    }
                }
                sub.worked(1);
            }
        }
        monitor.done();
    }

    private void debug(String string) {
        Helper.debug(getProject().getName() + " - " + string); //$NON-NLS-1$
    }

    private void error(String string) {
        Helper.error(getProject().getName() + " - " + string); //$NON-NLS-1$
    }

}
