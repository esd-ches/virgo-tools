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
package org.eclipse.virgo.ide.runtime.core;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.model.IModuleFile;

/**
 * Interface for clients that want to control the deploy mechanism associated with a
 * {@link IServerRuntimeProvider}.
 * @author Christian Dupuis
 * @since 2.0.0
 */
public interface IServerDeployer {

	/**
	 * Deploys the given {@link IModule}s on the server.
	 */
	void deploy(IModule... modules);

	/**
	 * Redeploys the given {@link IModule} on the server.
	 */
	void redeploy(IModule module);

	/**
	 * Refreshes a given static resource of the given {@link IModule}s.
	 */
	void refreshStatic(IModule module, IModuleFile file);

	/**
	 * Refresh the given {@link IModule}s inside a PAR.
	 */
	void refresh(IModule parModule, IModule... modules);

	/**
	 * Undeploys the given {@link IModule}s.
	 */
	void undeploy(IModule... modules);

	/**
	 * Shuts down the server
	 */
	void shutdown() throws IOException, TimeoutException;

	/**
	 * Pings the server to if it is running
	 */
	Boolean ping() throws IOException, TimeoutException;

}