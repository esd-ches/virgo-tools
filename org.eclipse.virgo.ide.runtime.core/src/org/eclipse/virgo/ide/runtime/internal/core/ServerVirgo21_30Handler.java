/*******************************************************************************
 * Copyright (c) 2012 Eclipse Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial implementation
 *******************************************************************************/
package org.eclipse.virgo.ide.runtime.internal.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.virgo.ide.runtime.core.IServerBehaviour;
import org.eclipse.virgo.ide.runtime.core.IServerVersionHandler;
import org.eclipse.virgo.ide.runtime.core.ServerUtils;

/**
 * {@link IServerVersionHandler} for Virgo Server 2.1.x through 3.0.x.
 * 
 * @author Borislav Kapukaranov
 * @author Miles Parker
 */
public class ServerVirgo21_30Handler extends ServerVirgoHandler {

	//Assumes Stateless
	public static final ServerVirgoHandler INSTANCE = new ServerVirgo21_30Handler();
	
	private static final String SERVER_VIRGO_21x_31x = SERVER_VIRGO_BASE;

	private ServerVirgo21_30Handler() {
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getRuntimeClass() {
		return "org.eclipse.virgo.osgi.launcher.Launcher";
	}

	/**
	 * {@inheritDoc}
	 */
	String getConfigDir() {
		return "config";
	}

	/**
	 * {@inheritDoc}
	 */
	String getProfileDir() {
		return "lib";
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getRuntimeProgramArguments(IServerBehaviour behaviour) {
		String serverHome = ServerUtils.getServer(behaviour).getRuntimeBaseDirectory().toOSString();
		List<String> list = new ArrayList<String>();
		list.add("-config \"" + serverHome + "/lib/org.eclipse.virgo.kernel.launch.properties\"");
		list.add("-Forg.eclipse.virgo.kernel.home=\"" + serverHome + "\"");
		list.add("-Forg.eclipse.virgo.kernel.config=\"" + serverHome + "/config," + serverHome + "/stage\"");
		list.add("-Fosgi.configuration.area=\"" + serverHome + "/work/osgi/configuration\"");
		list.add("-Fosgi.java.profile=\"file:" + serverHome + "/lib/java6-server.profile\"");
		list.add("-Fosgi.clean=true");
		return list.toArray(new String[list.size()]);
	}

	/**
	 * @see org.eclipse.virgo.ide.runtime.internal.core.ServerVirgoHandler#getID()
	 */
	public String getID() {
		return SERVER_VIRGO_21x_31x;
	}
	
	public String getName() {
		return "v2.1-3.0";
	}
}
