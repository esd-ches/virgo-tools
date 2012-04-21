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
package org.eclipse.virgo.ide.runtime.internal.core.runtimes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.virgo.ide.manifest.core.dependencies.IDependencyLocator;
import org.eclipse.virgo.ide.manifest.core.dependencies.IDependencyLocator.JavaVersion;
import org.eclipse.virgo.ide.runtime.core.IServerBehaviour;
import org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider;
import org.eclipse.virgo.ide.runtime.core.ServerUtils;
import org.eclipse.virgo.kernel.osgi.provisioning.tools.Pre35DependencyLocatorVirgo;
import org.eclipse.wst.server.core.IRuntime;

/**
 * {@link IServerRuntimeProvider} for Virgo Server 2.1.x through 3.0.x.
 * 
 * @author Borislav Kapukaranov
 * @author Miles Parker
 */
public class Virgo21_30Provider extends VirgoRuntimeProvider {

	//Assumes Stateless
	public static final VirgoRuntimeProvider INSTANCE = new Virgo21_30Provider();
	
	private static final String SERVER_VIRGO_21x_31x = SERVER_VIRGO_BASE;

	Virgo21_30Provider() {
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
	 * @see org.eclipse.virgo.ide.runtime.internal.core.runtimes.VirgoRuntimeProvider#getID()
	 */
	public String getID() {
		return SERVER_VIRGO_21x_31x;
	}
	
	public String getSupportedVersions() {
		return "2.1-3.0";
	}
	
	/**
	 * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#createDependencyLocator(org.eclipse.wst.server.core.IRuntime,
	 *      java.lang.String, java.lang.String[], java.lang.String,
	 *      org.eclipse.virgo.ide.manifest.core.dependencies.IDependencyLocator.JavaVersion)
	 */
	public IDependencyLocator createDependencyLocator(IRuntime runtime, String serverHomePath,
			String[] additionalSearchPaths, String indexDirectoryPath, JavaVersion javaVersion) throws IOException {
		return new Pre35DependencyLocatorVirgo(serverHomePath, additionalSearchPaths, indexDirectoryPath, javaVersion);
	}
}
