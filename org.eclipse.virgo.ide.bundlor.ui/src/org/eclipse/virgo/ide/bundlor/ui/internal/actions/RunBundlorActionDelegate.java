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
package org.eclipse.virgo.ide.bundlor.ui.internal.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.virgo.ide.bundlor.ui.BundlorUiPlugin;
import org.eclipse.virgo.ide.facet.core.FacetUtils;
import org.springframework.ide.eclipse.ui.SpringUIUtils;


/**
 * Action delegate that runs Bundlor on the selected projects. 
 * @author Christian Dupuis
 * @since 1.1.3
 */
public class RunBundlorActionDelegate implements IObjectActionDelegate {

	private final List<IProject> selected = new ArrayList<IProject>();

	public void run(IAction action) {
		Set<IJavaProject> projects = new LinkedHashSet<IJavaProject>();
		Iterator<IProject> iter = selected.iterator();
		while (iter.hasNext()) {
			IProject project = iter.next();
			if (FacetUtils.isBundleProject(project)) {
				projects.add(JavaCore.create(project));
			}
		}

		for (final IJavaProject javaProject : projects) {
			BundlorUiPlugin.runBundlorOnProject(javaProject);
		}

	}

	public void selectionChanged(IAction action, ISelection selection) {
		selected.clear();
		boolean enabled = true;
		if (selection instanceof IStructuredSelection) {
			Iterator<?> iter = ((IStructuredSelection) selection).iterator();
			while (iter.hasNext()) {
				Object obj = iter.next();
				if (obj instanceof IJavaProject) {
					obj = ((IJavaProject) obj).getProject();
				}
				if (obj instanceof IResource) {
					IResource project = (IResource) obj;
					if (!project.getProject().isOpen()) {
						enabled = false;
						break;
					}
					else {
						selected.add(project.getProject());
					}
				}
				else {
					enabled = false;
					break;
				}
			}
		}
		else {
			if (SpringUIUtils.getActiveEditor() != null) {
				if (SpringUIUtils.getActiveEditor().getEditorInput() instanceof IFileEditorInput) {
					selected.add(((IFileEditorInput) SpringUIUtils.getActiveEditor().getEditorInput()).getFile().getProject());
					enabled = true;
				}
			}
		}
		action.setEnabled(enabled);
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

}
