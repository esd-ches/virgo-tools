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
package org.eclipse.virgo.ide.runtime.internal.ui.model;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.virgo.ide.management.remote.Bundle;
import org.eclipse.virgo.ide.runtime.core.IServerBehaviour;
import org.eclipse.wst.server.core.IServer;


/**
 * Client that connects to the management MBean to retrieve bundle and service
 * data.
 * @author Christian Dupuis
 * @since 2.0.0
 */
public class ManagementConnectorClient {

	public static Map<Long, Bundle> getBundles(IServer server) {

		IServerBehaviour behaviour = (IServerBehaviour) server.loadAdapter(IServerBehaviour.class,
				new NullProgressMonitor());
		if (behaviour != null) {
			try {
				return behaviour.getVersionHandler().getServerBundleAdminCommand(behaviour).execute();
			}
			catch (IOException e) {
			}
			catch (TimeoutException e) {
			}
		}
		return Collections.emptyMap();
	}

	public static String execute(IServer server, String cmdLine) {

		IServerBehaviour behaviour = (IServerBehaviour) server.loadAdapter(IServerBehaviour.class,
				new NullProgressMonitor());
		if (behaviour != null) {
			try {
				return behaviour.getVersionHandler().getServerBundleAdminExecuteCommand(behaviour, cmdLine).execute();
			}
			catch (IOException e) {
			}
			catch (TimeoutException e) {
			}
		}
		return "<error>";
	}

}
