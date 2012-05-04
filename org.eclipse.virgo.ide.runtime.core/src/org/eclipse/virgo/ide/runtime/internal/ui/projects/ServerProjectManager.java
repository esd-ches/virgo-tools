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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.virgo.ide.runtime.core.ServerCorePlugin;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerLifecycleListener;
import org.eclipse.wst.server.core.ServerCore;

/**
 * The server project manager is responsible for tracking existing server projects and creating and destorying them as
 * appropriate. Server Projects are created on demand.
 * 
 * @author Miles Parker
 */
public class ServerProjectManager implements IServerLifecycleListener {

	private static ServerProjectManager INSTANCE;

	private final Map<IServer, ServerProject> projectForServer = new HashMap<IServer, ServerProject>();

	public static ServerProjectManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ServerProjectManager();
			ServerCore.addServerLifecycleListener(INSTANCE);
		}
		return INSTANCE;
	}

	/**
	 * Returns a project for the provided server.
	 * 
	 * @param server
	 *            the target server
	 * @param create
	 *            If a project doesn't already exist, create one.
	 * @param refresh
	 *            (update) the contents of the server as well
	 */
	public synchronized ServerProject getProject(IServer server, boolean create, boolean refresh) {
		ServerProject serverProject = projectForServer.get(server);
		if (serverProject == null && create) {
			serverProject = new ServerProject(server);
			projectForServer.put(server, serverProject);
		}
		if (serverProject != null && refresh) {
			serverProject.refresh();
		}
		return serverProject;
	}

	/**
	 * Returns a project for the server, without creating or refrehing it.
	 */
	public ServerProject getProject(IServer server) {
		return getProject(server, false, false);
	}

	public synchronized void updateProjects() {
		Map<String, IProject> oldProjectForName = getExistingServerProjects();

		for (IServer server : ServerCore.getServers()) {
			ServerProject project = getProject(server, true, true);
			oldProjectForName.remove(project.getWorkspaceProjectName());
		}
		for (IProject oldProject : oldProjectForName.values()) {
			try {
				oldProject.delete(true, null);
			} catch (CoreException e) {
				ServerProjectManager.handleException(e);
			}
		}
	}

	protected synchronized Map<String, IProject> getExistingServerProjects() {
		Map<String, IProject> oldProjectForName = new HashMap<String, IProject>();

		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		for (IProject project : root.getProjects()) {
			try {
				project.open(null);
				if (project.hasNature(ServerProject.SERVER_PROJECT_ID)) {
					oldProjectForName.put(project.getName(), project);
				}
			} catch (CoreException e) {
				ServerProjectManager.handleException(e);
			}
		}
		return oldProjectForName;
	}

	protected static void handleException(CoreException e) {
		StatusManager.getManager().handle(
				new Status(IStatus.ERROR, ServerCorePlugin.PLUGIN_ID,
						"Problem occurred while managing server project.", e));
	}

	/**
	 * @see org.eclipse.wst.server.core.IServerLifecycleListener#serverAdded(org.eclipse.wst.server.core.IServer)
	 */
	public void serverAdded(IServer server) {
		getProject(server, true, true);
	}

	/**
	 * @see org.eclipse.wst.server.core.IServerLifecycleListener#serverChanged(org.eclipse.wst.server.core.IServer)
	 */
	public synchronized void serverChanged(IServer server) {
		Map<String, IProject> oldProjectForName = getExistingServerProjects();
		ServerProject project = getProject(server, false, false);
		if (project.getWorkspaceProject() == null
				|| !oldProjectForName.containsKey(project.getWorkspaceProject().getName())) {
			//The project name has probably changed, so update everything
			updateProjects();
		}
	}

	/**
	 * @see org.eclipse.wst.server.core.IServerLifecycleListener#serverRemoved(org.eclipse.wst.server.core.IServer)
	 */
	public synchronized void serverRemoved(IServer server) {
		ServerProject project = getProject(server);
		if (project != null) {
			projectForServer.remove(server);
			project.deleteWorkspaceProject();
		}
	}
}
