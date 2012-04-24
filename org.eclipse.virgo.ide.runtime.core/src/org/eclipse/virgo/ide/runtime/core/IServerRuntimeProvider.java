/*******************************************************************************
 * Copyright (c) 2009, 2011 SpringSource, a divison of VMware, Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource, a division of VMware, Inc. - initial API and implementation
 *     SAP AG - moving to Eclipse Libra project and enhancements
 *******************************************************************************/
package org.eclipse.virgo.ide.runtime.core;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.libra.framework.editor.core.model.IBundle;
import org.eclipse.virgo.ide.manifest.core.dependencies.IDependencyLocator;
import org.eclipse.virgo.ide.manifest.core.dependencies.IDependencyLocator.JavaVersion;
import org.eclipse.virgo.ide.runtime.internal.core.DeploymentIdentity;
import org.eclipse.virgo.ide.runtime.internal.core.command.IServerCommand;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.model.IModuleFile;

/**
 * Interface that encapsulates server settings such as for versions.
 * 
 * @author Christian Dupuis
 * @author Kaloyan Raev
 * @author Miles Parker
 * @since 1.0.0
 */
public interface IServerRuntimeProvider {

	/**
	 * Check if a certain dm server can serve a given module(type)
	 */
	IStatus canAddModule(IModule module);

	/**
	 * Returns the to-be excluded program arguments
	 */
	String[] getExcludedRuntimeProgramArguments(boolean starting);

	/**
	 * Returns the runtime base directory.
	 */
	IPath getRuntimeBaseDirectory(org.eclipse.wst.server.core.IServer server);

	/**
	 * Returns the runtime class
	 */
	String getRuntimeClass();

	/**
	 * Returns the runtime class path
	 */
	List<IRuntimeClasspathEntry> getRuntimeClasspath(IPath installPath);

	/**
	 * Returns the runtime program arguments
	 */
	String[] getRuntimeProgramArguments(IServerBehaviour serverBehaviour);

	/**
	 * Returns the runtime vm arguments
	 */
	String[] getRuntimeVMArguments(IServerBehaviour serverBehaviour, IPath installPath, IPath configPath,
			IPath deployPath);

	/**
	 * Verifies the installation directory
	 */
	IStatus verifyInstallation(IRuntime runtime);

	/**
	 * Returns the path to the server.config file
	 */
	String getProfilePath(IRuntime runtime);

	/**
	 * Returns the path to the ext directory in the bundle repository.
	 */
	String getExtLevelBundleRepositoryPath(IRuntime runtime);

	/**
	 * Returns the path to the user directory in the bundle repository.
	 */
	String getUserLevelBundleRepositoryPath(IRuntime runtime);

	/**
	 * Returns the path to the library repository.
	 */
	String getUserLevelLibraryRepositoryPath(IRuntime runtime);

	/**
	 * Returns the path to the server config.
	 */
	String getConfigPath(IRuntime runtime);

	/**
	 * Returns the name of the Deployer MBean.
	 */
	String getDeployerMBeanName();

	/**
	 * Returns the Recovery Monitor MBean.
	 */
	String getRecoveryMonitorMBeanName();

	/**
	 * Returns the Shutdown MBean.
	 */
	String getShutdownMBeanName();

	/**
	 * Returns the server ping command.
	 */
	IServerCommand<Boolean> getServerPingCommand(IServerBehaviour serverBehaviour);

	/**
	 * Returns the server shutdown command.
	 */
	IServerCommand<Void> getServerShutdownCommand(IServerBehaviour serverBehaviour);

	/**
	 * Returns the server deployer command.
	 */
	IServerCommand<DeploymentIdentity> getServerDeployCommand(IServerBehaviour serverBehaviour, IModule module);

	/**
	 * Returns the server refresh command.
	 */
	IServerCommand<Void> getServerRefreshCommand(IServerBehaviour serverBehaviour, IModule module,
			String bundleSymbolicName);

	/**
	 * Returns the server update command.
	 */
	IServerCommand<Void> getServerUpdateCommand(IServerBehaviour serverBehaviour, IModule module,
			IModuleFile moduleFile, DeploymentIdentity identity, String bundleSymbolicName, String targetPath);

	/**
	 * Returns the server undeploy command.
	 */
	IServerCommand<Void> getServerUndeployCommand(IServerBehaviour serverBehaviour, IModule module);

	/**
	 * Returns the server deploy command.
	 */
	IServerCommand<DeploymentIdentity> getServerDeployCommand(IServerBehaviour serverBehaviour, URI connectorBundleUri);

	/**
	 * Returns the server bundle admin command.
	 */
	IServerCommand<Map<Long, IBundle>> getServerBundleAdminCommand(IServerBehaviour serverBehaviour);

	/**
	 * Returns the server bundle execute command.
	 */
	IServerCommand<String> getServerBundleAdminExecuteCommand(IServerBehaviour serverBehaviour, String command);

	/**
	 * Callback method for version handlers to setup servers before the server
	 * starts
	 */
	void preStartup(IServerBehaviour serverBehaviour);

	IDependencyLocator createDependencyLocator(IRuntime runtime, String serverHomePath, String[] additionalSearchPaths,
			String indexDirectoryPath, JavaVersion javaVersion) throws IOException;

}
