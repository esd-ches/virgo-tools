/*******************************************************************************
 * Copyright (c) 2010 SpringSource, a divison of VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource, a division of VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.virgo.ide.runtime.internal.core.command;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import javax.management.openmbean.CompositeData;

import org.eclipse.virgo.ide.facet.core.FacetCorePlugin;
import org.eclipse.virgo.ide.runtime.core.IServerBehaviour;
import org.eclipse.virgo.ide.runtime.core.ServerUtils;
import org.eclipse.virgo.ide.runtime.internal.core.DeploymentIdentity;
import org.eclipse.virgo.ide.runtime.internal.core.Server;
import org.eclipse.wst.server.core.IModule;

/**
 * {@link IServerCommand} to deploy a PAR or bundle.
 * 
 * @author Christian Dupuis
 * @since 1.0.1
 */
public class JmxServerDeployCommand extends AbstractJmxServerDeployerCommand<CompositeData> implements
		IServerCommand<DeploymentIdentity> {

	private static final String ITEM_SYMBOLIC_NAME = "symbolicName"; //$NON-NLS-1$

	private static final String ITEM_VERSION = "version"; //$NON-NLS-1$

	/**
	 * Creates a new {@link JmxServerDeployCommand}.
	 */
	public JmxServerDeployCommand(IServerBehaviour serverBehaviour, IModule module) {
		super(serverBehaviour, module);
	}

	/**
	 * {@inheritDoc}
	 */
	public DeploymentIdentity execute() throws IOException, TimeoutException {

		CompositeData returnValue = doExecute();
		if (returnValue != null) {
			String symbolicName = (String) returnValue.get(ITEM_SYMBOLIC_NAME);
			String version = (String) returnValue.get(ITEM_VERSION);
			Map<String, DeploymentIdentity> identities = serverBehaviour.getDeploymentIdentities();
			DeploymentIdentity identity = new DeploymentIdentity(symbolicName, version);
			identities.put(module.getId(), identity);
			return identity;
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Object[] getOperationArguments() {
		URI uri = null;
		if (module.getModuleType().getId().equals(FacetCorePlugin.PLAN_FACET_ID)) {
			String fileName = module.getId();
			fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
			uri = getUri(serverBehaviour.getModuleDeployUri(module).append(fileName));
		} else {
			uri = getUri(serverBehaviour.getModuleDeployUri(module));
		}
		return new Object[] { uri.toString(), false };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getOperationName() {
		return "deploy";
	}

	@Override
	protected int getTimeout() {
		Server server = ServerUtils.getServer(serverBehaviour);
		return server.getDeployTimeout();
	}

}
