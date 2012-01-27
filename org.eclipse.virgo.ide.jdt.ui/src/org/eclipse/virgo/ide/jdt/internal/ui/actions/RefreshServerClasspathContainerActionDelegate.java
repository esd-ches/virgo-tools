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
package org.eclipse.virgo.ide.jdt.internal.ui.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.virgo.ide.facet.core.FacetUtils;
import org.eclipse.virgo.ide.jdt.internal.core.classpath.ServerClasspathContainerUpdateJob;
import org.eclipse.virgo.ide.manifest.internal.core.BundleManifestManager;


/**
 * Menu action to refresh the bundle classpath container.
 * @author Christian Dupuis
 * @since 1.0.1
 */
public class RefreshServerClasspathContainerActionDelegate implements IObjectActionDelegate {

	private final List<IProject> selected = new ArrayList<IProject>();

	public void run(IAction action) {
		Set<IJavaProject> projects = new LinkedHashSet<IJavaProject>();
		Iterator<IProject> iter = selected.iterator();
		while (iter.hasNext()) {
			IProject project = iter.next();
			if (FacetUtils.isBundleProject(project)) {
				projects.add(JavaCore.create(project));
			}
			else if (FacetUtils.isParProject(project)) {
				for (IProject bundle : FacetUtils.getBundleProjects(project)) {
					projects.add(JavaCore.create(bundle));
				}
			}
		}

		for (IJavaProject javaProject : projects) {
			ServerClasspathContainerUpdateJob.scheduleClasspathContainerUpdateJob(javaProject,
					BundleManifestManager.IMPORTS_CHANGED);
		}

	}

	public void selectionChanged(IAction action, ISelection selection) {
		selected.clear();
		if (selection instanceof IStructuredSelection) {
			boolean enabled = true;
			Iterator<?> iter = ((IStructuredSelection) selection).iterator();
			while (iter.hasNext()) {
				Object obj = iter.next();
				if (obj instanceof IJavaProject) {
					obj = ((IJavaProject) obj).getProject();
				}
				if (obj instanceof IProject) {
					IProject project = (IProject) obj;
					if (!project.isOpen()) {
						enabled = false;
						break;
					}
					else {
						selected.add(project);
					}
				}
				else {
					enabled = false;
					break;
				}
			}
			action.setEnabled(enabled);
		}
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

}
