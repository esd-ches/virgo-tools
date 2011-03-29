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
package org.eclipse.virgo.ide.facet.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.common.project.facet.core.IDelegate;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.SpringCoreUtils;

/**
 * Facet install delegate that installs the class path container. 
 * @author Christian Dupuis
 * @since 1.0.0
 */
public class BundleFacetInstallDelegate implements IDelegate {

	public void execute(IProject project, IProjectFacetVersion fv, Object config,
			IProgressMonitor monitor) throws CoreException {
//		IJavaProject jproj = JavaCore.create(project);
//		addToClasspath(jproj, JavaCore.newContainerEntry(FacetCorePlugin.CLASSPATH_CONTAINER_PATH), monitor);
		SpringCoreUtils.addProjectNature(project, SpringCore.NATURE_ID, monitor);
		SpringCoreUtils.addProjectNature(project, FacetCorePlugin.BUNDLE_NATURE_ID, monitor);
	}

//	protected void addToClasspath(IJavaProject javaProject, IClasspathEntry entry, IProgressMonitor monitor)
//			throws CoreException {
//		IClasspathEntry[] current = javaProject.getRawClasspath();
//		IClasspathEntry[] updated = new IClasspathEntry[current.length + 1];
//		System.arraycopy(current, 0, updated, 0, current.length);
//		updated[current.length] = entry;
//		javaProject.setRawClasspath(updated, monitor);
//	}
}
