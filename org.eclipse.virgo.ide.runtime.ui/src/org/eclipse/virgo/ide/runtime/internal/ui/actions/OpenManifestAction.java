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
package org.eclipse.virgo.ide.runtime.internal.ui.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.virgo.ide.facet.core.FacetCorePlugin;
import org.eclipse.virgo.ide.facet.core.FacetUtils;
import org.eclipse.virgo.ide.manifest.core.BundleManifestCorePlugin;
import org.eclipse.virgo.ide.manifest.core.BundleManifestUtils;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.ui.internal.view.servers.ModuleServer;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.ui.SpringUIUtils;


/**
 * Action implementation that opens a MANIFEST.MF or par.xml file of the selected module.
 * @author Christian Dupuis
 * @since 1.0.0
 */
@SuppressWarnings("restriction")
public class OpenManifestAction implements IObjectActionDelegate {

	private IModule selectedModule;

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// nothing to do here
	}

	public void run(IAction action) {
		IProject project = selectedModule.getProject();
		if (FacetUtils.isBundleProject(project)) {
			openResource(BundleManifestUtils.locateManifest(JavaCore.create(project), false));
		}
		else if (FacetUtils.isParProject(project)) {
			openResource(project.findMember(BundleManifestCorePlugin.MANIFEST_FILE_LOCATION));
		}
		else if (SpringCoreUtils.hasProjectFacet(project, FacetCorePlugin.WEB_FACET_ID)) {
			openResource(BundleManifestUtils.locateManifest(JavaCore.create(project), false));
		}
	}

	private void openResource(IResource resource) {
		if (resource instanceof IFile) {
			SpringUIUtils.openInEditor((IFile) resource, -1);
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		selectedModule = null;
		if (!selection.isEmpty()) {
			if (selection instanceof IStructuredSelection) {
				Object obj = ((IStructuredSelection) selection).getFirstElement();
				if (obj instanceof ModuleServer) {
					ModuleServer ms = (ModuleServer) obj;
					selectedModule = ms.module[ms.module.length - 1];
				}
			}
		}
	}

}
