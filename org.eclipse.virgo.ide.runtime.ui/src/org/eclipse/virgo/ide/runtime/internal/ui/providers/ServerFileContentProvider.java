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
package org.eclipse.virgo.ide.runtime.internal.ui.providers;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.virgo.ide.runtime.internal.ui.projects.ServerProject;
import org.eclipse.virgo.ide.runtime.internal.ui.projects.ServerProjectManager;
import org.eclipse.wst.server.core.IServer;

/**
 * Common content provider for views on server content.
 * 
 * @author Miles Parker
 */
public class ServerFileContentProvider extends GenericTreeProvider {

	private final String serverDir;

	public ServerFileContentProvider(String serverDir) {
		this.serverDir = serverDir;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof IServer) {
			IServer server = (IServer) inputElement;
			ServerProject project = ServerProjectManager.getInstance().getProject(server);
			if (project != null) {
				IFolder folder = project.getWorkspaceProject().getFolder(serverDir);
				try {
					IResource[] members = folder.members();
					Object[] serverFiles = new Object[members.length];
					for (int i = 0; i < serverFiles.length; i++) {
						serverFiles[i] = new ServerFile(server, (IFile) members[i]);
					}
					return serverFiles;
				} catch (CoreException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return super.getElements(inputElement);
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof ServerFile) {
			return ((ServerFile) element).getServer();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		return element instanceof IServer && ServerProject.isVirgo((IServer) element);
	}
}