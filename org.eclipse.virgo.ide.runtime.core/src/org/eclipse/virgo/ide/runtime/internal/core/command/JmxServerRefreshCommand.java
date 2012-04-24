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
import java.util.concurrent.TimeoutException;

import org.eclipse.virgo.ide.runtime.core.IServerBehaviour;
import org.eclipse.wst.server.core.IModule;

/**
 * {@link IServerCommand} to update a PAR or bundle on a dm Server.
 * 
 * @author Christian Dupuis
 * @since 1.0.1
 */
public class JmxServerRefreshCommand extends AbstractJmxServerDeployerCommand<Object> implements IServerCommand<Void> {

	/** Symbolic name of the bundle to update */
	private final String bundleSymbolicName;

	/**
	 * Creates a new {@link JmxServerRefreshCommand}.
	 */
	public JmxServerRefreshCommand(IServerBehaviour serverBehaviour, IModule module, String bundleSymbolicName) {
		super(serverBehaviour, module);
		this.bundleSymbolicName = bundleSymbolicName;
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
		URI uri = getUri(serverBehaviour.getModuleDeployUri(module));
		return new Object[] { uri.toString(), bundleSymbolicName };
	}

	/**
	 * {@inheritDoc}
	 */
	protected String getOperationName() {
		return "refresh";
	}

}
