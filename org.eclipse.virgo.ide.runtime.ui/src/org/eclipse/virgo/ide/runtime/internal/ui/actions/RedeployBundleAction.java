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
package org.eclipse.virgo.ide.runtime.internal.ui.actions;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.virgo.ide.facet.core.FacetCorePlugin;
import org.eclipse.virgo.ide.runtime.core.IServerBehaviour;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.ui.internal.view.servers.ModuleServer;


/**
 * Action implementation that redeploys the selected module or bundle
 * @author Christian Dupuis
 * @since 1.0.0
 */
@SuppressWarnings("restriction")
public class RedeployBundleAction implements IObjectActionDelegate {

	private IModule selectedModule;

	private IServer server;

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// nothing to do here
	}

	public void run(IAction action) {
		
		if (server.getServerState() != IServer.STATE_STARTED) {
			return;
		}
		
		Job publishJob = new Job("Redeploy of module '" + selectedModule.getName() +"'") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				IServerBehaviour behaviour = (IServerBehaviour) server.loadAdapter(
						IServerBehaviour.class, null);
				// PAR selected
				if (FacetCorePlugin.PAR_FACET_ID.equals(selectedModule.getModuleType().getId())) {
					behaviour.getServerDeployer().redeploy(selectedModule);
				}
				// Bundle selected
				else if (FacetCorePlugin.BUNDLE_FACET_ID.equals(selectedModule.getModuleType().getId())) {
					List<IModule> modules = Arrays.asList(server.getModules());
					if (modules.contains(selectedModule)) {
						// Single deployed module
						behaviour.getServerDeployer().redeploy(selectedModule);
					}
					else {
						for (IModule module : modules) {
							List<IModule> childModules = Arrays.asList(server.getChildModules(
									new IModule[] { module }, null));
							if (childModules.contains(selectedModule)
									&& FacetCorePlugin.PAR_FACET_ID.equals(module.getModuleType().getId())) {
								behaviour.getServerDeployer().refresh(module, selectedModule);
							}
						}
					}
				}
				// Web module selected
				else if (FacetCorePlugin.WEB_FACET_ID.equals(selectedModule.getModuleType().getId())) {
					behaviour.getServerDeployer().redeploy(selectedModule);
				}
				return Status.OK_STATUS;
			}
		};
		publishJob.setPriority(Job.INTERACTIVE);
		publishJob.schedule();
		
	}

	public void selectionChanged(IAction action, ISelection selection) {
		selectedModule = null;
		if (!selection.isEmpty()) {
			if (selection instanceof IStructuredSelection) {
				Object obj = ((IStructuredSelection) selection).getFirstElement();
				if (obj instanceof ModuleServer) {
					ModuleServer ms = (ModuleServer) obj;
					this.server = ms.server;
					this.selectedModule = ms.module[ms.module.length - 1];
				}
			}
		}
	}

}
