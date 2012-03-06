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

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.virgo.ide.runtime.core.IServerBehaviour;
import org.eclipse.virgo.ide.runtime.core.IServerVersionHandler;
import org.eclipse.virgo.ide.runtime.core.ServerCorePlugin;
import org.eclipse.virgo.ide.runtime.core.ServerUtils;
import org.eclipse.wst.server.core.IRuntime;

/**
 * {@link IServerVersionHandler} for Virgo Server 2.1.x through 3.0.x.
 * 
 * @author Borislav Kapukaranov
 * @author Miles Parker
 */
public class ServerVirgo21_30Handler extends ServerVirgoHandler {

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
	 * @see org.eclipse.virgo.ide.runtime.core.IServerVersionHandler#getRuntimeClasspath(org.eclipse.core.runtime.IPath)
	 */
	public List<IRuntimeClasspathEntry> getRuntimeClasspath(IPath installPath) {
		List<IRuntimeClasspathEntry> cp = new ArrayList<IRuntimeClasspathEntry>();

		IPath binPath = installPath.append("lib");
		if (binPath.toFile().exists()) {
			File libFolder = binPath.toFile();
			for (File library : libFolder.listFiles(new FileFilter() {
				public boolean accept(File pathname) {
					return pathname.isFile() && pathname.toString().endsWith(".jar");
				}
			})) {
				IPath path = binPath.append(library.getName());
				cp.add(JavaRuntime.newArchiveRuntimeClasspathEntry(path));
			}
		}

		return cp;
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
	 * {@inheritDoc}
	 */
	public IStatus verifyInstallation(IPath installPath) {
		String version = installPath.append("lib").append(".version").toOSString();
		File versionFile = new File(version);
		if (versionFile.exists()) {
			InputStream is = null;
			try {
				is = new FileInputStream(versionFile);
				Properties versionProperties = new Properties();
				versionProperties.load(is);
				String versionString = versionProperties.getProperty("virgo.server.version");

				if (versionString == null) {
					return new Status(
						Status.ERROR,
						ServerCorePlugin.PLUGIN_ID,
						".version file in lib directory is missing key 'virgo.server.version'. Make sure to point to a Virgo Server installation.");
				}
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
					}
				}
			}
		} else {
			return new Status(Status.ERROR, ServerCorePlugin.PLUGIN_ID,
				".version file in lib directory is missing. Make sure to point to a Virgo Server installation.");
		}
		return Status.OK_STATUS;
	}

}
