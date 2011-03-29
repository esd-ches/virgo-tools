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

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.wst.common.project.facet.core.IDelegate;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.springframework.ide.eclipse.core.SpringCoreUtils;

/**
 * Uninstall delegate to remove the class path container.
 * @author Christian Dupuis
 * @since 1.0.0
 */
public class BundleFacetUninstallDelegate implements IDelegate {

	public void execute(IProject project, IProjectFacetVersion fv, Object config,
			IProgressMonitor monitor) throws CoreException {
		IJavaProject jproj = JavaCore.create(project);
		removeFromClasspath(jproj, JavaCore.newContainerEntry(FacetCorePlugin.CLASSPATH_CONTAINER_PATH),
				monitor);
		SpringCoreUtils.removeProjectNature(project, FacetCorePlugin.BUNDLE_NATURE_ID, monitor);
	}

	protected void removeFromClasspath(IJavaProject javaProject, IClasspathEntry entry,
			IProgressMonitor monitor) throws CoreException {
		Set<IClasspathEntry> entries = new LinkedHashSet<IClasspathEntry>();
		for (IClasspathEntry existingEntry : javaProject.getRawClasspath()) {
			if (!existingEntry.equals(entry)) {
				entries.add(existingEntry);
			}
		}
		javaProject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), monitor);
	}
}
