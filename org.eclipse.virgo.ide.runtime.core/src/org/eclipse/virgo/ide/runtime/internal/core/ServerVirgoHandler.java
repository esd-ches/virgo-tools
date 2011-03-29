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
package org.eclipse.virgo.ide.runtime.internal.core;

import java.io.File;
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
import org.eclipse.virgo.ide.runtime.core.IServerBehaviour;
import org.eclipse.virgo.ide.runtime.core.IServerVersionHandler;
import org.eclipse.virgo.ide.runtime.core.ServerCorePlugin;
import org.eclipse.virgo.ide.runtime.core.ServerUtils;
import org.eclipse.virgo.ide.runtime.internal.core.command.IServerCommand;
import org.eclipse.virgo.ide.runtime.internal.core.command.JmxServer20UndeployCommand;
import org.eclipse.virgo.ide.runtime.internal.core.command.JmxServer20UpdateCommand;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.model.IModuleFile;


/**
 * {@link IServerVersionHandler} for Virgo Server.
 * @author Terry Hon
 * @author Christian Dupuis
 * @since 2.0.0
 */
public class ServerVirgoHandler extends Server20Handler implements IServerVersionHandler {

	private static final String BUNDLE_OBJECT_NAME = "org.eclipse.virgo.kernel:type=Model,artifact-type=bundle,name=$NAME,version=$VERSION";

	private static final String DEPLOYER_MBEAN_NAME = "org.eclipse.virgo.kernel:category=Control,type=Deployer";

	private static final String PAR_OBJECT_NAME = "org.eclipse.virgo.kernel:type=Model,artifact-type=par,name=$NAME,version=$VERSION";

	private static final String PLAN_OBJECT_NAME = "org.eclipse.virgo.kernel:type=Model,artifact-type=plan,name=$NAME,version=$VERSION";

	private static final String RECOVERY_MONITOR_MBEAN_NAME = "org.eclipse.virgo.kernel:category=Control,type=RecoveryMonitor";

	private static final String SHUTDOWN_MBEAN_NAME = "org.eclipse.virgo.kernel:type=Shutdown";

	/**
	 * {@inheritDoc}
	 */
	public String getDeployerMBeanName() {
		return DEPLOYER_MBEAN_NAME;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getRecoveryMonitorMBeanName() {
		return RECOVERY_MONITOR_MBEAN_NAME;
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
	public String[] getRuntimeVMArguments(IServerBehaviour behaviour, IPath installPath, IPath configPath,
			IPath deployPath) {
		String serverHome = ServerUtils.getServer(behaviour).getRuntimeBaseDirectory().toOSString();
		List<String> list = new ArrayList<String>();
		list.add("-XX:+HeapDumpOnOutOfMemoryError");
		list.add("-XX:ErrorFile=\"" + serverHome + "/serviceability/error.log\"");
		list.add("-XX:HeapDumpPath=\"" + serverHome + "/serviceability/heap_dump.hprof\"");
		list.add("-Djava.rmi.server.hostname=127.0.0.1");
		list.add("-Dorg.eclipse.virgo.kernel.home=\"" + serverHome + "\"");
		list.add("-Djava.io.tmpdir=\"" + serverHome + "/work/tmp/\"");
		list.add("-Dcom.sun.management.jmxremote");
		list.add("-Dcom.sun.management.jmxremote.port=" + ServerUtils.getServer(behaviour).getMBeanServerPort());
		list.add("-Dcom.sun.management.jmxremote.authenticate=false");
		list.add("-Dcom.sun.management.jmxremote.ssl=false");
		list.add("-Dorg.eclipse.virgo.kernel.authentication.file=\"" + serverHome
				+ "/config/org.eclipse.virgo.kernel.users.properties\"");
		list.add("-Djava.security.auth.login.config=\"" + serverHome
				+ "/config/org.eclipse.virgo.kernel.authentication.config\"");
		return list.toArray(new String[list.size()]);
	}

	/**
	 * {@inheritDoc}
	 */
	public IServerCommand<Void> getServerUndeployCommand(IServerBehaviour serverBehaviour, IModule module) {
		return new JmxServer20UndeployCommand(serverBehaviour, module, BUNDLE_OBJECT_NAME, PAR_OBJECT_NAME,
				PLAN_OBJECT_NAME);
	}

	/**
	 * {@inheritDoc}
	 */
	public IServerCommand<Void> getServerUpdateCommand(IServerBehaviour serverBehaviour, IModule module,
			IModuleFile moduleFile, DeploymentIdentity identity, String bundleSymbolicName, String targetPath) {
		return new JmxServer20UpdateCommand(serverBehaviour, module, moduleFile, identity, bundleSymbolicName,
				targetPath, BUNDLE_OBJECT_NAME, PAR_OBJECT_NAME, PLAN_OBJECT_NAME);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getShutdownMBeanName() {
		return SHUTDOWN_MBEAN_NAME;
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
			}
			catch (FileNotFoundException e) {
			}
			catch (IOException e) {
			}
			finally {
				if (is != null) {
					try {
						is.close();
					}
					catch (IOException e) {
					}
				}
			}
		}
		else {
			return new Status(Status.ERROR, ServerCorePlugin.PLUGIN_ID,
					".version file in lib directory is missing. Make sure to point to a Virgo Server installation.");
		}
		return Status.OK_STATUS;
	}

	@Override
	protected String getRepositoryConfigurationFileName() {
		return "org.eclipse.virgo.repository.properties";
	}

}
