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
package org.eclipse.virgo.ide.facet.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.common.util.WrappedException;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.PackageNotFoundException;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.virgo.ide.par.Bundle;
import org.eclipse.virgo.ide.par.Par;
import org.eclipse.virgo.ide.par.ParPackage;
import org.eclipse.wst.common.project.facet.core.FacetedProjectFramework;

/**
 * Utility to check if the given {@link IResource} belongs to a project that has
 * the par or bundle facet.
 * 
 * @author Christian Dupuis
 * @since 1.0.0
 */
public class FacetUtils {

	/**
	 * Checks if a given {@link IResource} has the bundle facet.
	 */
	public static boolean isBundleProject(IResource resource) {
		boolean b;
		try {
			b = resource.getProject().hasNature(JavaCore.NATURE_ID)
				&& hasProjectFacet(resource, FacetCorePlugin.BUNDLE_FACET_ID);
			return b;
		} catch (CoreException e) {
			return false;
		}
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
				// TODO CD handle exception
			}
		}
		return false;
	}

	/**
	 * Returns all bundle project in the current workspace regardless weather
	 * they are open or closed.
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
		return (IProject[]) bundles.toArray(new IProject[bundles.size()]);
	}

	public static IProject[] getBundleProjects(IProject parProject) {
		Set<IProject> bundles = new HashSet<IProject>();
		if (isParProject(parProject)) {
			Par par = getParDefinition(parProject);
			if (par != null && par.getBundle() != null) {
				for (Bundle bundle : par.getBundle()) {
					IProject bundleProject = ResourcesPlugin.getWorkspace().getRoot()
							.getProject(bundle.getSymbolicName());
					if (FacetUtils.isBundleProject(bundleProject)) {
						bundles.add(bundleProject);
					}
				}
			}
		}
		return (IProject[]) bundles.toArray(new IProject[bundles.size()]);
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
					//Handle case where we need to update old par file format.
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
						project.refreshLocal(IProject.DEPTH_INFINITE, null);
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
		IResource resource = project.findMember(new Path(".settings")
				.append("org.eclipse.virgo.ide.runtime.core.par.xml"));
		if (resource instanceof IFile) {
			return (IFile) resource;
		}
		return null;
	}

}
