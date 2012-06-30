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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
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
public abstract class ServerFileContentProvider extends GenericTreeProvider {

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof IServer) {
			IServer server = (IServer) inputElement;
			ServerProject project = ServerProjectManager.getInstance().getProject(server);
			if (project != null) {
				List<ServerFile> files = new ArrayList<ServerFile>();
				for (String serverDir : getServerDirectories(server)) {
					IFolder folder = project.getWorkspaceProject().getFolder(getBaseDirectory() + "/" + serverDir);
					try {
						IResource[] members = folder.members();
						for (IResource resource : members) {
							if (isIncludeType(resource)) {
								files.add(new ServerFile(server, (IFile) resource));
							}
						}
					} catch (CoreException e) {
						throw new RuntimeException(e);
					}
				}
				return files.toArray(new Object[files.size()]);
			}
		}
		return super.getElements(inputElement);
	}

	public boolean isIncludeType(IResource resource) {
		if (resource instanceof IFile) {
			String ext = resource.getFileExtension();
			if (ext == null) {
				return isIncludeNoExtension();
			}
			if (getIncludeExtensions() != null) {
				return ArrayUtils.contains(getIncludeExtensions(), ext);
			}
			if (getExcludeExtensions() != null) {
				return !ArrayUtils.contains(getExcludeExtensions(), ext);
			}
			return true;
		}
		return false;
	}

	public abstract String getBaseDirectory();

	/**
	 * If not null only files with include extensions will be added. Note: excluded files will be ignored in this case.
	 */
	public abstract String[] getIncludeExtensions();

	/**
	 * If not null files with extensions will not be added.
	 */
	public abstract String[] getExcludeExtensions();

	public abstract String[] getServerDirectories(IServer server);

	/**
	 * Should files that don't have extensions be included?
	 */
	public abstract boolean isIncludeNoExtension();

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