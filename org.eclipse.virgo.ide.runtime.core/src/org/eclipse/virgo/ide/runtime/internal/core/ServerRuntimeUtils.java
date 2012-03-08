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
package org.eclipse.virgo.ide.runtime.internal.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.virgo.ide.facet.core.FacetCorePlugin;
import org.eclipse.virgo.ide.facet.core.FacetUtils;
import org.eclipse.virgo.ide.par.Bundle;
import org.eclipse.virgo.ide.par.Par;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerUtil;


/**
 * Utility class that allows callback operations with the {@link VirgoServerRuntime} that a given
 * {@link IProject} is targeted to.
 * @author Christian Dupuis
 * @since 1.0.0
 */
public class ServerRuntimeUtils { 

	/**
	 * Callback interface to do logic within the context of a {@link VirgoServerRuntime}.
	 */
	public interface ServerRuntimeCallback {

		/**
		 * Execute logic within the context of a {@link VirgoServerRuntime}
		 */
		boolean doWithRuntime(VirgoServerRuntime runtime);

	}

	/**
	 * Executes the given callback with every {@link VirgoServerRuntime} that given {@link IProject} is
	 * targeted against.
	 * @param project the project to check for targeted runtimes
	 * @param callback the callback to execute
	 */
	public static void execute(IProject project, ServerRuntimeCallback callback) {
		try {
			IFacetedProject fProject = ProjectFacetsManager.create(project);
			
			// If the facet frameworks returns null we should just exit here
			if (fProject == null) {
				return;
			}
			
			org.eclipse.wst.common.project.facet.core.runtime.IRuntime runtime = fProject
					.getPrimaryRuntime();
			// If a targeted runtime exists use the configured runtime bundle repository
			if (runtime != null) {
				IRuntime[] serverRuntimes = ServerUtil.getRuntimes(FacetCorePlugin.BUNDLE_FACET_ID,
						null);
				for (IRuntime serverRuntime : serverRuntimes) {
					if (serverRuntime.getName().equals(runtime.getName())) {
						if (!executeCallback(callback, serverRuntime)) {
							return;
						}
					}
				}
			}
			// If project is targeted to a server use the server to add library paths
			else {
				IServer[] servers = ServerUtil.getServersByModule(ServerUtil.getModule(project),
						null);
				// Check if the project is targeted directly to a server
				if (servers != null && servers.length > 0) {
					for (IServer server : servers) {
						if (!executeCallback(callback, server.getRuntime())) {
							return;
						}
					}
				}
				else {
					// Check if the project is part of a par; if so add the par project target to
					// the search path
					for (IProject parProject : ResourcesPlugin.getWorkspace().getRoot()
							.getProjects()) {
						if (FacetUtils.isParProject(parProject)) {
							Par parDefinition = FacetUtils.getParDefinition(parProject);
							if (parDefinition != null) {
								for (Bundle bundle : parDefinition.getBundle()) {
									if (project.getName().equals(bundle.getSymbolicName())) {
										execute(parProject, callback);
									}
								}
							}
						}
					}
				}
			}
		}
		catch (CoreException e) {
		}
	}

	private static boolean executeCallback(ServerRuntimeCallback callback, IRuntime runtime) {
		VirgoServerRuntime serverRuntime = (VirgoServerRuntime) runtime.loadAdapter(
				VirgoServerRuntime.class, new NullProgressMonitor());
		if (serverRuntime != null) {
			return callback.doWithRuntime(serverRuntime);
		}
		return true;
	}

}
