/*******************************************************************************
 * Copyright (c) 2009, 2012 SpringSource, a divison of VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource, a division of VMware, Inc. - initial API and implementation
 *******************************************************************************/

package org.eclipse.virgo.ide.runtime.internal.ui.projects;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.virgo.ide.runtime.core.ServerCorePlugin;
import org.eclipse.virgo.ide.runtime.core.artefacts.IArtefact;
import org.eclipse.virgo.ide.runtime.core.artefacts.ILocalArtefact;
import org.eclipse.virgo.ide.runtime.core.artefacts.LocalArtefactSet;
import org.eclipse.wst.server.core.IServer;

/**
 * @author Miles Parker
 */
public class ProjectFileContainer implements IServerProjectContainer {

	private final LocalArtefactSet artefactSet;

	private final IFolder folder;

	private final ServerProject serverProject;

	private final ProjectFileReference[] fileReferences;

	protected ProjectFileContainer(ServerProject serverProject, LocalArtefactSet artefactSet) {
		this.serverProject = serverProject;
		this.artefactSet = artefactSet;
		IProject project = serverProject.getWorkspaceProject();
		folder = project.getFolder(artefactSet.getRelativePath());
		List<ProjectFileReference> references = new ArrayList<ProjectFileReference>();
		createFolder(folder);
		for (IArtefact artefact : artefactSet.getArtefacts()) {
			if (artefact instanceof ILocalArtefact) {
				ProjectFileReference fileReference = new ProjectFileReference(this, (ILocalArtefact) artefact);
				references.add(fileReference);
			}
		}
		fileReferences = references.toArray(new ProjectFileReference[0]);
	}

	public static void createFolder(IFolder folder) {
		IContainer parent = folder.getParent();
		if (parent instanceof IFolder) {
			createFolder((IFolder) parent);
		}
		if (!folder.exists()) {
			try {
				folder.create(true, true, null);
			} catch (CoreException e) {
				StatusManager.getManager().handle(
						new Status(IStatus.ERROR, ServerCorePlugin.PLUGIN_ID,
								"Problem occurred while managing server project.", e));
			}
		}
	}

	/**
	 * @see org.eclipse.virgo.ide.runtime.internal.ui.projects.IServerProjectContainer#getMembers()
	 */
	public Object[] getMembers() {
		return getFileReferences();
	}

	/**
	 * @see org.eclipse.virgo.ide.runtime.internal.ui.projects.IServerProjectContainer#getServer()
	 */
	public IServer getServer() {
		return serverProject.getServer();
	}

	/**
	 * @see org.eclipse.virgo.ide.runtime.internal.ui.projects.IServerProjectContainer#getArtefactSet()
	 */
	public LocalArtefactSet getArtefactSet() {
		return artefactSet;
	}

	public ProjectFileReference[] getFileReferences() {
		return fileReferences;
	}

	public IFolder getFolder() {
		return folder;
	}
}
