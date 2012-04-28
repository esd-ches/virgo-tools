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

	private Map<IServer, ServerProject> projectForServer;

	public static ServerProjectManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ServerProjectManager();
		}
		return INSTANCE;
	}

	public synchronized ServerProject getProject(IServer server) {
		if (projectForServer == null) {
			projectForServer = new HashMap<IServer, ServerProject>();
		}
		ServerProject serverProject = projectForServer.get(server);
		if (serverProject == null) {
			serverProject = new ServerProject(server);
			serverProject.refresh();
			projectForServer.put(server, serverProject);
		}
		return serverProject;
	}

	public void initialize() {
		for (IServer server : ServerCore.getServers()) {
			getProject(server);
		}
		ServerCore.addServerLifecycleListener(this);
	}

	/**
	 * @see org.eclipse.wst.server.core.IServerLifecycleListener#serverAdded(org.eclipse.wst.server.core.IServer)
	 */
	public void serverAdded(IServer server) {
		getProject(server).refresh();
	}

	/**
	 * @see org.eclipse.wst.server.core.IServerLifecycleListener#serverChanged(org.eclipse.wst.server.core.IServer)
	 */
	public void serverChanged(IServer server) {
		getProject(server).refresh();
	}

	/**
	 * @see org.eclipse.wst.server.core.IServerLifecycleListener#serverRemoved(org.eclipse.wst.server.core.IServer)
	 */
	public void serverRemoved(IServer server) {
		ServerProject project = getProject(server);
		project.deleteProject();
	}
}
