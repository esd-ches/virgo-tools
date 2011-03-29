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
package org.eclipse.virgo.ide.runtime.internal.core.command;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeoutException;

import org.eclipse.virgo.ide.runtime.core.IServerBehaviour;
import org.eclipse.virgo.ide.runtime.internal.core.DeploymentIdentity;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.model.IModuleFile;


/**
 * {@link IServerCommand} to update/refresh a single static resource on the dm Server.
 * @author Christian Dupuis
 * @since 1.0.1
 */
public class JmxServerUpdateCommand extends AbstractJmxServerDeployerCommand<Object> implements
		IServerCommand<Void> {
	
	/** Symbolic name of the bundle inside a PAR that contains the resource to update */
	private final String bundleSymbolicName;
	
	/** {@link DeploymentIdentity} of the deployed PAR or bundle */
	private final DeploymentIdentity identity;
	
	/** File to refresh or update */
	private final IModuleFile moduleFile;
	
	/** Target path of the file relative to the PAR or bundle root */
	private final String targetPath;

	/**
	 * Creates a new {@link JmxServerUpdateCommand}. 
	 */
	public JmxServerUpdateCommand(IServerBehaviour serverBehaviour, IModule module,
			IModuleFile moduleFile, DeploymentIdentity identity, String bundleSymbolicName,
			String targetPath) {
		super(serverBehaviour, module);
		this.bundleSymbolicName = bundleSymbolicName;
		this.moduleFile = moduleFile;
		this.identity = identity;
		this.targetPath = targetPath;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Void execute() throws IOException, TimeoutException {
		doExecute();
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	protected Object[] getOperationArguments() {
		URI uri = getUri(serverBehaviour.getModuleDeployUri(module).append(
				moduleFile.getModuleRelativePath()).append(moduleFile.getName()));
		if (bundleSymbolicName == null) {
			return new Object[] { identity.getSymbolicName(), identity.getVersion(),
					uri.toString(), targetPath };
		}
		else {
			return new Object[] { identity.getSymbolicName(), identity.getVersion(),
					bundleSymbolicName, uri.toString(), targetPath };
		}
	}

	/**
	 * {@inheritDoc}
	 */
	protected String getOperationName() {
		return "updateResource";
	}

}
