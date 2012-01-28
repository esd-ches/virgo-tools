/*******************************************************************************
 * Copyright (c) 2009, 2010 SpringSource, a divison of VMware, Inc.
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
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
import org.eclipse.virgo.ide.runtime.internal.core.command.IServerCommand;
import org.eclipse.virgo.ide.runtime.internal.core.command.JmxServer20DeployCommand;
import org.eclipse.virgo.ide.runtime.internal.core.command.JmxServer20UndeployCommand;
import org.eclipse.virgo.ide.runtime.internal.core.command.JmxServer20UpdateCommand;
import org.eclipse.virgo.util.common.StringUtils;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.IModuleFile;
import org.osgi.framework.Version;


/**
 * {@link IServerVersionHandler} for version 2.0 of the dm Server.
 * @author Christian Dupuis
 * @since 2.0.0
 */
public class Server20Handler extends Server10Handler implements IServerVersionHandler {

	private static final String PAR_OBJECT_NAME = "com.springsource.kernel:type=Model,artifact-type=par,name=$NAME,version=$VERSION";

	private static final String PLAN_OBJECT_NAME = "com.springsource.kernel:type=Model,artifact-type=plan,name=$NAME,version=$VERSION";

	private static final String BUNDLE_OBJECT_NAME = "com.springsource.kernel:type=Model,artifact-type=bundle,name=$NAME,version=$VERSION";

	private static final String DEPLOYER_MBEAN_NAME = "com.springsource.kernel:category=Control,type=Deployer";

	private static final String RECOVERY_MONITOR_MBEAN_NAME = "com.springsource.kernel:category=Control,type=RecoveryMonitor";

	private static final String SHUTDOWN_MBEAN_NAME = "com.springsource.kernel:type=Shutdown";

	/**
	 * {@inheritDoc}
	 */
	public String getConfigPath(IRuntime runtime) {
		return runtime.getLocation().append("config").append("server.config").toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDeployerMBeanName() {
		return DEPLOYER_MBEAN_NAME;
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getExcludedRuntimeProgramArguments(boolean starting) {
		List<String> list = new ArrayList<String>();
		return list.toArray(new String[list.size()]);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getProfilePath(IRuntime runtime) {
		return runtime.getLocation().append("lib").append("java6-server.profile").toString();
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
	public IPath getRuntimeBaseDirectory(IServer server) {
		return server.getRuntime().getLocation();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getRuntimeClass() {
		return "com.springsource.osgi.launcher.Launcher";
	}

	/**
	 * {@inheritDoc}
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
		list.add("-config \"" + serverHome + "/lib/com.springsource.kernel.launch.properties\"");
		list.add("-Fcom.springsource.kernel.home=\"" + serverHome + "\"");
		list.add("-Fcom.springsource.kernel.config=\"" + serverHome + "/config," + serverHome + "/stage\"");
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
		list.add("-Dcom.springsource.kernel.home=\"" + serverHome + "\"");
		list.add("-Djava.io.tmpdir=\"" + serverHome + "/work/tmp/\"");
		list.add("-Dcom.sun.management.jmxremote");
		list.add("-Dcom.sun.management.jmxremote.port=" + ServerUtils.getServer(behaviour).getMBeanServerPort());
		list.add("-Dcom.sun.management.jmxremote.authenticate=false");
		list.add("-Dcom.sun.management.jmxremote.ssl=false");
		list.add("-Dcom.springsource.kernel.authentication.file=\"" + serverHome
				+ "/config/com.springsource.kernel.users.properties\"");
		list.add("-Djava.security.auth.login.config=\"" + serverHome
				+ "/config/com.springsource.kernel.authentication.config\"");
		return list.toArray(new String[list.size()]);
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
	public IServerCommand<Void> getServerUndeployCommand(IServerBehaviour serverBehaviour, IModule module) {
		return new JmxServer20UndeployCommand(serverBehaviour, module, BUNDLE_OBJECT_NAME, PAR_OBJECT_NAME, PLAN_OBJECT_NAME);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IServerCommand<DeploymentIdentity> getServerDeployCommand(IServerBehaviour IServerBehaviour, IModule module) {
		return new JmxServer20DeployCommand(IServerBehaviour, module);
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
	public String getUserLevelBundleRepositoryPath(IRuntime runtime) {
		return runtime.getLocation().append("repository").append("usr").toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getUserLevelLibraryRepositoryPath(IRuntime runtime) {
		return runtime.getLocation().append("repository").append("usr").toString();
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
				String versionString = versionProperties.getProperty("dm.server.version");

				if (versionString == null) {
					return new Status(
							Status.ERROR,
							ServerCorePlugin.PLUGIN_ID,
							".version file in lib directory is missing key 'dm.server.version'. Make sure to point to a dm Server 2.0 installation.");
				}
				else {
					Version osgiVersion = new Version(versionString);
					if (osgiVersion.getMajor() != 2 && osgiVersion.getMinor() != 0) {
						return new Status(Status.ERROR, ServerCorePlugin.PLUGIN_ID,
								".version file indicates unsupported dm Server version '" + versionString
										+ "'. Make sure to point to a dm Server 2.0 installation.");
					}
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
					".version file in lib directory is missing. Make sure to point to a dm Server 2.0 installation.");
		}
		return Status.OK_STATUS;
	}

	@Override
	public void preStartup(IServerBehaviour serverBehaviour) {
		super.preStartup(serverBehaviour);
		createRepositoryConfiguration(serverBehaviour, getRepositoryConfigurationFileName());
	}

	protected String getRepositoryConfigurationFileName() {
		return "com.springsource.repository.properties";
	}

	private void createRepositoryConfiguration(IServerBehaviour serverBehaviour, String fileName) {
		// copy com.springsource.repository.properties into the stage and add the stage repository
		File serverHome = ServerUtils.getServer(serverBehaviour).getRuntimeBaseDirectory().toFile();
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(new File(serverHome, "config" + File.separatorChar + fileName)));
		}
		catch (FileNotFoundException e) {
			// TODO CD add logging
		}
		catch (IOException e) {
			// TODO CD add logging
		}

		properties.put("stage.type", "watched");
		properties.put("stage.watchDirectory", "stage");

		String chain = properties.getProperty("chain");
		chain = "stage" + (StringUtils.hasLength(chain) ? "," + chain : "");
		properties.put("chain", chain);

		try {
			File stageDirectory = new File(serverHome, "stage");
			if (!stageDirectory.exists()) {
				stageDirectory.mkdirs();
			}
			properties.store(new FileOutputStream(new File(serverHome, "stage" + File.separator + fileName)),
					"Generated by SpringSource dm Server Tools "
							+ ServerCorePlugin.getDefault().getBundle().getVersion());
		}
		catch (FileNotFoundException e) {
			// TODO CD add logging
		}
		catch (IOException e) {
			// TODO CD add logging
		}
	}
}
