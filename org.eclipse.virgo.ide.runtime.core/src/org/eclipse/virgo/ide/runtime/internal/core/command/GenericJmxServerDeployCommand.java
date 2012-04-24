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
package org.eclipse.virgo.ide.runtime.internal.core.command;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import javax.management.openmbean.CompositeData;

import org.eclipse.virgo.ide.runtime.core.IServerBehaviour;
import org.eclipse.virgo.ide.runtime.internal.core.DeploymentIdentity;

/**
 * {@link IServerCommand} to deploy a PAR or bundle.
 * 
 * @author Christian Dupuis
 * @since 1.0.1
 */
public class GenericJmxServerDeployCommand extends AbstractJmxServerDeployerCommand<CompositeData> implements
		IServerCommand<DeploymentIdentity> {

	private static final String ITEM_SYMBOLIC_NAME = "symbolicName"; //$NON-NLS-1$

	private static final String ITEM_VERSION = "version"; //$NON-NLS-1$

	private final URI uri;

	/**
	 * Creates a new {@link GenericJmxServerDeployCommand}.
	 */
	public GenericJmxServerDeployCommand(IServerBehaviour serverBehaviour, URI uri) {
		super(serverBehaviour, null);
		this.uri = uri;
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
			identities.put(uri.toString(), identity);
			return identity;
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	protected Object[] getOperationArguments() {
		return new Object[] { uri.toString(), false };
	}

	/**
	 * {@inheritDoc}
	 */
	protected String getOperationName() {
		return "deploy";
	}

}