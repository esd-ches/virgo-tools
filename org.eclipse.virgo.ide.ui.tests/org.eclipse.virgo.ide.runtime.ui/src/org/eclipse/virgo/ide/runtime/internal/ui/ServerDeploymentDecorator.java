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
package org.eclipse.virgo.ide.runtime.internal.ui;

import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.virgo.ide.runtime.internal.core.DeploymentIdentity;
import org.eclipse.virgo.ide.runtime.internal.core.ServerBehaviour;
import org.eclipse.virgo.ide.runtime.internal.core.ServerVersionHelper;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.ui.internal.view.servers.ModuleServer;


/**
 * @author Leo Dos Santos
 */
@SuppressWarnings("restriction")
public class ServerDeploymentDecorator implements ILightweightLabelDecorator {

	public void decorate(Object element, IDecoration decoration) {
		if (element instanceof ModuleServer) {
			ModuleServer mServer = (ModuleServer) element;
			IModule[] modules = mServer.module;
			IServer server =  mServer.getServer();
			if (modules != null && server != null) {
				IServerType serverType = server.getServerType();
				String runtime = serverType.getRuntimeType().getId();
				if (runtime.equals(ServerVersionHelper.SERVER_10) || runtime.equals(ServerVersionHelper.SERVER_20)) {
					boolean deployed = true;
					for (int i = 0; i < modules.length; i++) {
						IModule module = modules[i];
						ServerBehaviour behaviour = (ServerBehaviour) server.getAdapter(ServerBehaviour.class);
						if (behaviour != null) {
							DeploymentIdentity identity = behaviour.getDeploymentIdentities().get(module.getId());
							if (identity == null) {
								deployed = false;
							}
						}
					}
					if (deployed) {
						decoration.addSuffix(" [Deployed]");
					} else {
						decoration.addSuffix(" [Not Running]");
					}
				}
			}
		}		
	}

	public void addListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub
		
	}

	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	public boolean isLabelProperty(Object element, String property) {
		// TODO Auto-generated method stub
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub
		
	}

}
