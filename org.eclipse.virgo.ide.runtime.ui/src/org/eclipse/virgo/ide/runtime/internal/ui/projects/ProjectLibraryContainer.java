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

import java.io.File;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.virgo.ide.runtime.core.artefacts.IArtefact;
import org.eclipse.virgo.ide.runtime.core.artefacts.ILocalArtefact;
import org.eclipse.virgo.ide.runtime.core.artefacts.LocalArtefactSet;
import org.eclipse.wst.server.core.IServer;

/**
 * 
 * @author Miles Parker
 * 
 */
public class ProjectLibraryContainer implements IServerProjectContainer {

	private final LocalArtefactSet artefactSet;

	private final IFolder folder;

	private final ServerProject serverProject;

	protected ProjectLibraryContainer(ServerProject serverProject, LocalArtefactSet artefactSet) {
		this.serverProject = serverProject;
		this.artefactSet = artefactSet;
		IProject project = serverProject.getProject();
		IWorkspace workspace = project.getWorkspace();
		folder = project.getFolder(artefactSet.getRelativePath());
		createFolder(folder);
		for (IArtefact artefact : artefactSet.getArtefacts()) {
			if (artefact instanceof ILocalArtefact) {
				File file = ((ILocalArtefact) artefact).getFile();
				String artefactRelative = file.getAbsolutePath()
						.replaceAll(artefactSet.getFile().getAbsolutePath(), "");
				IFile artefactFile = folder.getFile(artefactRelative);
				try {
					file.setReadOnly();
					artefactFile.createLink(new Path(file.getAbsolutePath()), IResource.REPLACE, null);
				} catch (CoreException e) {
					throw new RuntimeException(e);
				}
			}
		}
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
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * @see org.eclipse.virgo.ide.runtime.internal.ui.projects.IServerProjectContainer#getMembers()
	 */
	public Object[] getMembers() {
		try {
			return folder.members();
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
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

	public IFolder getFolder() {
		return folder;
	}
}
