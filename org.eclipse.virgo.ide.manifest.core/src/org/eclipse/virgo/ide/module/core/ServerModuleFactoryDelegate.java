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
package org.eclipse.virgo.ide.module.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.virgo.ide.facet.core.FacetCorePlugin;
import org.eclipse.virgo.ide.facet.core.FacetUtils;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.model.ModuleDelegate;
import org.eclipse.wst.server.core.util.ProjectModuleFactoryDelegate;
import org.springframework.ide.eclipse.core.java.JdtUtils;


/**
 * {@link ProjectModuleFactoryDelegate} extension that knows how to handle par and bundle projects.
 * @author Christian Dupuis
 * @since 1.0.0
 */
public class ServerModuleFactoryDelegate extends ProjectModuleFactoryDelegate {

	public static final String MODULE_FACTORY_ID = "org.eclipse.virgo.server.modulefactory";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ModuleDelegate getModuleDelegate(IModule module) {
		return new ServerModuleDelegate(module.getProject());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected IModule[] createModules(final IProject project) {
		final Set<IModule> modules = new HashSet<IModule>();
		if (FacetUtils.isBundleProject(project)) {

			// Add module for bundle deployment
			modules.add(createModule(project.getName(), project.getName(), FacetCorePlugin.BUNDLE_FACET_ID, "1.0",
 					project));

			// Add module for par deployment
			for (IProject parProject : FacetUtils.getParProjects(project)) {
				modules.add(createModule(parProject.getName() + "$" + project.getName(), project.getName(),
						FacetCorePlugin.BUNDLE_FACET_ID, "1.0", project));
			}

		}
		else if (FacetUtils.isParProject(project)) {
			modules.add(createModule(project.getName(), project.getName(), FacetCorePlugin.PAR_FACET_ID, "1.0",
							project));
		}

		// Every project can also be a plan project
		if (FacetUtils.isPlanProject(project)) {

			// Collect output locations if java project
			final Set<IPath> outputLocations = new HashSet<IPath>();
			try {
				if (project.hasNature(JavaCore.NATURE_ID)) {
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
					}
					catch (JavaModelException e) {
						//safe to ignore
					}
				}
			} catch (CoreException e) {
				//safe to ignore
			}

			try {
				project.accept(new IResourceVisitor() {

					public boolean visit(IResource resource) throws CoreException {
						if (resource instanceof IFile && resource.getName().endsWith(".plan")) {
							modules.add(createModule(resource.getFullPath().toString(), resource.getProject().getName()
									+ "/" + resource.getProjectRelativePath().toString(),
									FacetCorePlugin.PLAN_FACET_ID, "2.0", project));
						}
						else if (resource instanceof IContainer) {
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
			}
			catch (CoreException e) {
				// TODO CD log exception
			}
		}
		return (IModule[]) modules.toArray(new IModule[modules.size()]);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected IPath[] getListenerPaths() {
		final List<IPath> paths = new ArrayList<IPath>();
		for (IModule module : getModules()) {
			if (FacetUtils.isPlanProject(module.getProject())) {
				try {
					module.getProject().accept(new IResourceVisitor() {

						public boolean visit(IResource resource) throws CoreException {
							if (resource instanceof IFile && resource.getName().endsWith(".plan")) {
								paths.add(resource.getProjectRelativePath());
							}
							return true;
						}
					});
				}
				catch (CoreException e) {
				}

			}
		}
		paths.add(new Path(".settings/org.eclipse.wst.common.project.facet.core.xml"));
		paths.add(new Path(".project"));
		paths.add(new Path(".settings"));
		paths.add(new Path(".classpath"));
		return (IPath[]) paths.toArray(new IPath[paths.size()]);
	}

}
