/*******************************************************************************
 * Copyright (c) 2009 - 2012 SpringSource, a divison of VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource, a division of VMware, Inc. - initial API and implementation
 *     GianMaria Romanato - add utilities for management of nested plans
 *******************************************************************************/

package org.eclipse.virgo.ide.facet.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.common.util.WrappedException;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.PackageNotFoundException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.virgo.ide.facet.internal.core.Plan;
import org.eclipse.virgo.ide.facet.internal.core.PlanReader;
import org.eclipse.virgo.ide.facet.internal.core.PlanReference;
import org.eclipse.virgo.ide.par.Bundle;
import org.eclipse.virgo.ide.par.Par;
import org.eclipse.virgo.ide.par.ParPackage;
import org.eclipse.wst.common.project.facet.core.FacetedProjectFramework;

/**
 * Utility to check if the given {@link IResource} belongs to a project that has the par or bundle facet.
 *
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @author GianMaria Romanato
 * @since 1.0.0
 */
public class FacetUtils {

    /**
     * Checks if a given {@link IResource} has the bundle facet.
     */
    public static boolean isBundleProject(IResource resource) {
        return hasNature(resource, JavaCore.NATURE_ID) && hasProjectFacet(resource, FacetCorePlugin.BUNDLE_FACET_ID);
    }

    /**
     * Checks if a given {@link IResource} has the par facet.
     */
    public static boolean isParProject(IResource resource) {
        return hasProjectFacet(resource, FacetCorePlugin.PAR_FACET_ID);
    }

    /**
     * Checks if a given {@link IResource} has the par facet.
     */
    public static boolean isPlanProject(IResource resource) {
        return hasProjectFacet(resource, FacetCorePlugin.PLAN_FACET_ID);
    }

    /**
     * Checks if a {@link IResource} has a given project facet.
     */
    public static boolean hasProjectFacet(IResource resource, String facetId) {
        if (resource != null && resource.isAccessible()) {
            try {
                return FacetedProjectFramework.hasProjectFacet(resource.getProject(), facetId);
            } catch (CoreException e) {
                StatusManager.getManager().handle(
                    new Status(IStatus.ERROR, FacetCorePlugin.PLUGIN_ID, "An error occurred inspecting project facet", e));
            }
        }
        return false;
    }

    /**
     * Checks if a {@link IResource} has a given project nature.
     */
    public static boolean hasNature(IResource resource, String natureId) {
        if (resource != null && resource.isAccessible()) {
            IProject project = resource.getProject();
            if (project != null) {
                try {
                    return project.hasNature(natureId);
                } catch (CoreException e) {
                    StatusManager.getManager().handle(
                        new Status(IStatus.ERROR, FacetCorePlugin.PLUGIN_ID, "An error occurred inspecting project nature", e));
                }
            }
        }
        return false;
    }

    /**
     * Returns all bundle project in the current workspace regardless weather they are open or closed.
     */
    public static IProject[] getBundleProjects() {
        List<IProject> bundles = new ArrayList<IProject>();
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        for (IProject candidate : projects) {
            if (FacetUtils.isBundleProject(candidate)) {
                bundles.add(candidate);
            }
        }
        return bundles.toArray(new IProject[bundles.size()]);
    }

    public static IProject[] getParProjects(IProject project) {
        Set<IProject> bundles = new HashSet<IProject>();
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        for (IProject candidate : projects) {
            if (FacetUtils.isParProject(candidate)) {
                if (Arrays.asList(getBundleProjects(candidate)).contains(project)) {
                    bundles.add(candidate);
                }
            }
        }
        return bundles.toArray(new IProject[bundles.size()]);
    }

    public static IProject[] getBundleProjects(IProject parProject) {
        Set<IProject> bundles = new HashSet<IProject>();
        if (isParProject(parProject)) {
            Par par = getParDefinition(parProject);
            if (par != null && par.getBundle() != null) {
                for (Bundle bundle : par.getBundle()) {
                    IProject bundleProject = ResourcesPlugin.getWorkspace().getRoot().getProject(bundle.getSymbolicName());
                    if (FacetUtils.isBundleProject(bundleProject)) {
                        bundles.add(bundleProject);
                    }
                }
            }
        }
        return bundles.toArray(new IProject[bundles.size()]);
    }

    public static Par getParDefinition(IProject project) {
        // Create a resource set to hold the resources.
        ResourceSet resourceSet = new ResourceSetImpl();
        // Register the package to ensure it is available during loading.
        resourceSet.getPackageRegistry().put(ParPackage.eNS_URI, ParPackage.eINSTANCE);

        File parFile = new File(new File(project.getLocation().toString() + File.separatorChar + ".settings"),
            "org.eclipse.virgo.ide.runtime.core.par.xml");
        if (parFile.exists()) {
            URI fileUri = URI.createFileURI(parFile.toString());
            Resource resource = null;
            try {
                resource = resourceSet.getResource(fileUri, true);
            } catch (WrappedException e) {
                if (e.getCause() instanceof PackageNotFoundException) {
                    // Handle case where we need to update old par file format.
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(parFile));
                        StringBuilder sb = new StringBuilder();
                        String next = br.readLine();
                        do {
                            next = next.replaceAll("http:///com/springsource/server/ide/par.ecore", "http://eclipse.org/virgo/par.ecore");
                            next = next.replaceAll("com\\.springsource\\.server", "org.eclipse.virgo");
                            sb.append(next + "\n");
                            next = br.readLine();
                        } while (next != null);
                        br.close();
                        BufferedWriter bw = new BufferedWriter(new FileWriter(parFile));
                        bw.write(sb.toString());
                        bw.close();
                        project.refreshLocal(IResource.DEPTH_INFINITE, null);
                        resource = resourceSet.getResource(fileUri, true);
                    } catch (IOException e1) {
                        throw new RuntimeException(e1);
                    } catch (CoreException e2) {
                        throw new RuntimeException(e2);
                    }
                }
            }
            return (Par) resource.getContents().iterator().next();
        }

        return null;
    }

    public static IFile getParFile(IProject project) {
        IResource resource = project.findMember(new Path(".settings").append("org.eclipse.virgo.ide.runtime.core.par.xml"));
        if (resource instanceof IFile) {
            return (IFile) resource;
        }
        return null;
    }

    /**
     * Gets all the plan files found in the given project.
     * 
     * @param project
     * @return
     */
    public static Collection<IFile> getPlansInPlanProject(IProject project) {
        if (!isPlanProject(project)) {
            return Collections.emptyList();
        }

        final List<IFile> planFiles = new ArrayList<IFile>();

        // Collect output locations if java project
        final Set<IPath> outputLocations = new HashSet<IPath>();
        try {
            if (FacetUtils.hasNature(project, JavaCore.NATURE_ID)) {
                IJavaProject je = JavaCore.create(project);
                try {
                    outputLocations.add(je.getOutputLocation());
                    for (IClasspathEntry entry : je.getRawClasspath()) {
                        if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
                            if (entry.getOutputLocation() != null) {
                                outputLocations.add(entry.getOutputLocation());
                            }
                        }
                    }
                } catch (JavaModelException e) {
                    // safe to ignore
                }
            }
            project.accept(new IResourceVisitor() {

                public boolean visit(IResource resource) throws CoreException {
                    if (resource.isTeamPrivateMember() || resource.isDerived()) {
                        return false;
                    }
                    if (resource instanceof IFile && "plan".equals(resource.getFileExtension())) {
                        planFiles.add((IFile) resource);
                    } else if (resource instanceof IContainer) {
                        IPath path = ((IContainer) resource).getFullPath();
                        for (IPath outputLocation : outputLocations) {
                            if (outputLocation.isPrefixOf(path)) {
                                return false;
                            }
                        }
                        return true;
                    }
                    return true;
                }
            });
        } catch (CoreException e) {
            // TODO CD log exception
        }

        return planFiles;
    }

    /**
     * Returns all the plans in the workspace as a map project to list of plan files
     *
     * @return
     */
    private static Map<IProject, Collection<IFile>> getPlansInWorkspace() {
        Map<IProject, Collection<IFile>> plans = new HashMap<IProject, Collection<IFile>>();
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        for (IProject iProject : projects) {
            if (iProject.isOpen() && isPlanProject(iProject)) {
                Collection<IFile> ps = getPlansInPlanProject(iProject);
                plans.put(iProject, ps);
            }
        }
        return plans;
    }

    /**
     * Returns the list of nested plans for a given plan file. The look only in the planFile project, unless the project
     * is also a Java project. In such case, the project classpath is used to look for plans in required projects.
     *
     * @param planFile
     * @return
     */
    public static List<IFile> getNestedPlanFiles(IFile planFile) {
        if (!isPlanProject(planFile)) {
            return Collections.emptyList();
        }

        // parse the top level plan file
        PlanReader reader = new PlanReader();
        Plan topLevelPlan;
        try {
            topLevelPlan = reader.read(planFile.getContents());
        } catch (Exception e1) {
            return Collections.emptyList();
        }

        List<PlanReference> nestedReferences = topLevelPlan.getNestedPlans();

        if (nestedReferences.isEmpty()) {
            return Collections.emptyList();
        }

        List<IFile> nestedPlanFiles = new ArrayList<IFile>();

        /*
         * examine the containing Java project class path (if a Java projecT) and find all the required Java projects.
         * Nested plans will be searched not only in the current project but also in its dependencies
         */
        List<IProject> orderedProjects = getOrderedProjectDependencies(planFile.getProject());
        orderedProjects.add(0, planFile.getProject());

        Map<IProject, Collection<IFile>> allPlans = FacetUtils.getPlansInWorkspace();

        // used for searching a nested plan that is referred only by name (no version)
        Map<String, Plan> name2PlanLookup = new HashMap<String, Plan>();

        // used for searching a nested plan that is referred by name and version
        Map<PlanReference, Plan> ref2Plan = new HashMap<PlanReference, Plan>();

        // plan to related file
        Map<PlanReference, IFile> ref2File = new HashMap<PlanReference, IFile>();

        // loop over the list of ordered projects and search for plans
        for (IProject iProject : orderedProjects) {
            Collection<IFile> candidates = allPlans.get(iProject);
            if (candidates != null) {
                for (IFile iFile : candidates) {
                    // ignore self
                    if (!planFile.equals(iFile)) {
                        try {
                            Plan p = reader.read(iFile.getContents());
                            PlanReference r = p.asRefence();

                            /*
                             * in case of duplicate plans (same name and version) first found in classpath wins and
                             * found is assumed to be the right one
                             */
                            if (!ref2Plan.containsKey(r)) {
                                // add for name+version lookup
                                ref2Plan.put(r, p);

                                /*
                                 * in case of duplicates plans with the same name and different version if an outer plan
                                 * is referring to a nested plan via name only, the first found in classpath wins
                                 */
                                if (!name2PlanLookup.containsKey(r.getName())) {
                                    name2PlanLookup.put(r.getName(), p);
                                }

                                ref2File.put(r, iFile);

                            }
                        } catch (Exception e) {
                            // ignore
                        }
                    }
                }
            }
        }

        // finally compute the list of nested plans. Use a queue instead of recursion.
        Queue<PlanReference> toBeProcessed = new ArrayBlockingQueue<PlanReference>(ref2Plan.size() + 1);
        Set<PlanReference> alreadyProcessed = new HashSet<PlanReference>();
        toBeProcessed.addAll(nestedReferences);

        while (toBeProcessed.peek() != null) {
            PlanReference planReference = toBeProcessed.poll();
            alreadyProcessed.add(planReference);

            // search for exact match name + version
            Plan nestedPlan = ref2Plan.get(planReference); // search for exact match name + version
            if (nestedPlan == null && planReference.getVersion() == null) {
                nestedPlan = name2PlanLookup.get(planReference.getName());
            }

            if (nestedPlan != null) {
                IFile nestedFile = ref2File.get(nestedPlan.asRefence());

                nestedPlanFiles.add(nestedFile);

                for (PlanReference aRef : nestedPlan.getNestedPlans()) {
                    if (!alreadyProcessed.contains(aRef)) {
                        toBeProcessed.add(aRef);
                    }
                }
            }
        }

        return nestedPlanFiles;
    }

    /**
     * Returns the ordered list of project dependencies for the given Java project or an empty list if the project is
     * not a Java project.
     *
     * @param project the Java project
     * @return the list of required projects
     */
    private static List<IProject> getOrderedProjectDependencies(IProject project) {
        LinkedHashSet<IProject> projects = new LinkedHashSet<IProject>();
        if (FacetUtils.hasNature(project, JavaCore.NATURE_ID)) {
            IJavaProject je = JavaCore.create(project);
            String[] names;
            try {
                names = je.getRequiredProjectNames();
                for (String prjName : names) {
                    IProject prj = ResourcesPlugin.getWorkspace().getRoot().getProject(prjName);
                    if (prj.exists() && prj.isOpen() && isPlanProject(prj)) {
                        projects.add(prj);
                        projects.addAll(getOrderedProjectDependencies(prj));
                    }
                }
            } catch (JavaModelException e) {
            }
        }
        return new ArrayList<IProject>(projects);
    }

}
