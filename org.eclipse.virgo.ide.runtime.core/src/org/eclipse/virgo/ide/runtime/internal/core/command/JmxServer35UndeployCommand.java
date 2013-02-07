/*******************************************************************************
 * Copyright (c) 2013 Eclipse Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial implementation
 *******************************************************************************/
package org.eclipse.virgo.ide.runtime.internal.core.command;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import javax.management.openmbean.CompositeData;

import org.eclipse.virgo.ide.runtime.core.IServerBehaviour;
import org.eclipse.virgo.ide.runtime.internal.core.DeploymentIdentity;
import org.eclipse.wst.server.core.IModule;

/**
 * {@link IServerCommand} to undeploy artifacts on Virgo 3.5 and greater
 * 
 * @author Leo Dos Santos
 * @since 1.0.1
 */
public class JmxServer35UndeployCommand extends AbstractJmxServerDeployerCommand<CompositeData> implements
		IServerCommand<Void> {

	private final DeploymentIdentity identity;

	public JmxServer35UndeployCommand(IServerBehaviour serverBehaviour, IModule module) {
		super(serverBehaviour, module);
		identity = serverBehaviour.getDeploymentIdentities().remove(module.getId());
	}

	public Void execute() throws IOException, TimeoutException {
		doExecute();
		return null;
	}

	@Override
	protected Object[] getOperationArguments() {
		return new Object[] { identity.getSymbolicName(), identity.getVersion() };
	}

	@Override
	protected String getOperationName() {
		return "undeploy";
	}

}
