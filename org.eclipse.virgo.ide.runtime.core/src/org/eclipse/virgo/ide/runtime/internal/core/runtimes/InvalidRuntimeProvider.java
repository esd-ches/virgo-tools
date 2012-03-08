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
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.libra.framework.editor.core.model.IBundle;
import org.eclipse.virgo.ide.manifest.core.dependencies.IDependencyLocator;
import org.eclipse.virgo.ide.manifest.core.dependencies.IDependencyLocator.JavaVersion;
import org.eclipse.virgo.ide.runtime.core.IServerBehaviour;
import org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider;
import org.eclipse.virgo.ide.runtime.core.ServerCorePlugin;
import org.eclipse.virgo.ide.runtime.internal.core.DeploymentIdentity;
import org.eclipse.virgo.ide.runtime.internal.core.command.IServerCommand;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.IModuleFile;

/**
 * A {@link IServerRuntimeProvider} representing an invalid server configuration directory.
 * 
 * @author Miles Parker
 */
public class InvalidRuntimeProvider implements IServerRuntimeProvider {

	// Assumes Stateless
	public static final IServerRuntimeProvider INSTANCE = new InvalidRuntimeProvider();

	private static final String ERROR_MESSAGE = "Internal Error: Tried to reference invalid server configuration.";

	private InvalidRuntimeProvider() {
	}

	/**
	 * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#getRuntimeClass()
	 */
	public String getRuntimeClass() {
		throw new RuntimeException(ERROR_MESSAGE);
	}

	/**
	 * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#getRuntimeProgramArguments(org.eclipse.virgo.ide.runtime.core.IServerBehaviour)
	 */
	public String[] getRuntimeProgramArguments(IServerBehaviour serverBehaviour) {
		throw new RuntimeException(ERROR_MESSAGE);
	}

	/**
	 * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#createDependencyLocator(org.eclipse.wst.server.core.IRuntime, java.lang.String, java.lang.String[], java.lang.String, org.eclipse.virgo.ide.manifest.core.dependencies.IDependencyLocator.JavaVersion)
	 */
	public IDependencyLocator createDependencyLocator(IRuntime runtime, String serverHomePath,
			String[] additionalSearchPaths, String indexDirectoryPath, JavaVersion javaVersion) throws IOException {
		return null;
	}

	public IStatus verifyInstallation(IPath installPath) {
		return new Status(Status.ERROR, ServerCorePlugin.PLUGIN_ID,
							"The installation directory does not contain a valid Virgo Server.");
	}
	
	/**
	 * @see org.eclipse.virgo.ide.runtime.internal.core.runtimes.AbstractVirgoRuntimeProvider#getConfigDir()
	 */
	String getConfigDir() {
		throw new RuntimeException(ERROR_MESSAGE);
	}

	/**
	 * @see org.eclipse.virgo.ide.runtime.internal.core.runtimes.AbstractVirgoRuntimeProvider#getProfileDir()
	 */
	String getProfileDir() {
		throw new RuntimeException(ERROR_MESSAGE);
	}

	/**
	 * @see org.eclipse.virgo.ide.runtime.internal.core.runtimes.AbstractVirgoRuntimeProvider#getID()
	 */
	public String getID() {
		throw new RuntimeException(ERROR_MESSAGE);
	}

	/**
	 * @see org.eclipse.virgo.ide.runtime.internal.core.runtimes.AbstractVirgoRuntimeProvider#getName()
	 */
	public String getName() {
		throw new RuntimeException(ERROR_MESSAGE);
	}

	/**
	 * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#canAddModule(org.eclipse.wst.server.core.IModule)
	 */
	public IStatus canAddModule(IModule module) {
		throw new RuntimeException(ERROR_MESSAGE);
	}

	/**
	 * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#getExcludedRuntimeProgramArguments(boolean)
	 */
	public String[] getExcludedRuntimeProgramArguments(boolean starting) {
		throw new RuntimeException(ERROR_MESSAGE);
	}

	/**
	 * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#getRuntimeBaseDirectory(org.eclipse.wst.server.core.IServer)
	 */
	public IPath getRuntimeBaseDirectory(IServer server) {
		throw new RuntimeException(ERROR_MESSAGE);
	}

	/**
	 * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#getRuntimeClasspath(org.eclipse.core.runtime.IPath)
	 */
	public List<IRuntimeClasspathEntry> getRuntimeClasspath(IPath installPath) {
		throw new RuntimeException(ERROR_MESSAGE);
	}

	/**
	 * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#getRuntimeVMArguments(org.eclipse.virgo.ide.runtime.core.IServerBehaviour, org.eclipse.core.runtime.IPath, org.eclipse.core.runtime.IPath, org.eclipse.core.runtime.IPath)
	 */
	public String[] getRuntimeVMArguments(IServerBehaviour serverBehaviour, IPath installPath, IPath configPath,
			IPath deployPath) {
		throw new RuntimeException(ERROR_MESSAGE);
	}

	/**
	 * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#getProfilePath(org.eclipse.wst.server.core.IRuntime)
	 */
	public String getProfilePath(IRuntime runtime) {
		throw new RuntimeException(ERROR_MESSAGE);
	}

	/**
	 * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#getExtLevelBundleRepositoryPath(org.eclipse.wst.server.core.IRuntime)
	 */
	public String getExtLevelBundleRepositoryPath(IRuntime runtime) {
		throw new RuntimeException(ERROR_MESSAGE);
	}

	/**
	 * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#getUserLevelBundleRepositoryPath(org.eclipse.wst.server.core.IRuntime)
	 */
	public String getUserLevelBundleRepositoryPath(IRuntime runtime) {
		throw new RuntimeException(ERROR_MESSAGE);
	}

	/**
	 * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#getUserLevelLibraryRepositoryPath(org.eclipse.wst.server.core.IRuntime)
	 */
	public String getUserLevelLibraryRepositoryPath(IRuntime runtime) {
		throw new RuntimeException(ERROR_MESSAGE);
	}

	/**
	 * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#getConfigPath(org.eclipse.wst.server.core.IRuntime)
	 */
	public String getConfigPath(IRuntime runtime) {
		throw new RuntimeException(ERROR_MESSAGE);
	}

	/**
	 * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#getDeployerMBeanName()
	 */
	public String getDeployerMBeanName() {
		throw new RuntimeException(ERROR_MESSAGE);
	}

	/**
	 * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#getRecoveryMonitorMBeanName()
	 */
	public String getRecoveryMonitorMBeanName() {
		throw new RuntimeException(ERROR_MESSAGE);
	}

	/**
	 * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#getShutdownMBeanName()
	 */
	public String getShutdownMBeanName() {
		throw new RuntimeException(ERROR_MESSAGE);
	}

	/**
	 * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#getServerPingCommand(org.eclipse.virgo.ide.runtime.core.IServerBehaviour)
	 */
	public IServerCommand<Boolean> getServerPingCommand(IServerBehaviour serverBehaviour) {
		throw new RuntimeException(ERROR_MESSAGE);
	}

	/**
	 * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#getServerShutdownCommand(org.eclipse.virgo.ide.runtime.core.IServerBehaviour)
	 */
	public IServerCommand<Void> getServerShutdownCommand(IServerBehaviour serverBehaviour) {
		throw new RuntimeException(ERROR_MESSAGE);
	}

	/**
	 * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#getServerDeployCommand(org.eclipse.virgo.ide.runtime.core.IServerBehaviour, org.eclipse.wst.server.core.IModule)
	 */
	public IServerCommand<DeploymentIdentity> getServerDeployCommand(IServerBehaviour serverBehaviour, IModule module) {
		throw new RuntimeException(ERROR_MESSAGE);
	}

	/**
	 * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#getServerRefreshCommand(org.eclipse.virgo.ide.runtime.core.IServerBehaviour, org.eclipse.wst.server.core.IModule, java.lang.String)
	 */
	public IServerCommand<Void> getServerRefreshCommand(IServerBehaviour serverBehaviour, IModule module,
			String bundleSymbolicName) {
		throw new RuntimeException(ERROR_MESSAGE);
	}

	/**
	 * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#getServerUpdateCommand(org.eclipse.virgo.ide.runtime.core.IServerBehaviour, org.eclipse.wst.server.core.IModule, org.eclipse.wst.server.core.model.IModuleFile, org.eclipse.virgo.ide.runtime.internal.core.DeploymentIdentity, java.lang.String, java.lang.String)
	 */
	public IServerCommand<Void> getServerUpdateCommand(IServerBehaviour serverBehaviour, IModule module,
			IModuleFile moduleFile, DeploymentIdentity identity, String bundleSymbolicName, String targetPath) {
		throw new RuntimeException(ERROR_MESSAGE);
	}

	/**
	 * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#getServerUndeployCommand(org.eclipse.virgo.ide.runtime.core.IServerBehaviour, org.eclipse.wst.server.core.IModule)
	 */
	public IServerCommand<Void> getServerUndeployCommand(IServerBehaviour serverBehaviour, IModule module) {
		throw new RuntimeException(ERROR_MESSAGE);
	}

	/**
	 * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#getServerDeployCommand(org.eclipse.virgo.ide.runtime.core.IServerBehaviour, java.net.URI)
	 */
	public IServerCommand<DeploymentIdentity> getServerDeployCommand(IServerBehaviour serverBehaviour,
			URI connectorBundleUri) {
		throw new RuntimeException(ERROR_MESSAGE);
	}

	/**
	 * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#getServerBundleAdminCommand(org.eclipse.virgo.ide.runtime.core.IServerBehaviour)
	 */
	public IServerCommand<Map<Long, IBundle>> getServerBundleAdminCommand(IServerBehaviour serverBehaviour) {
		throw new RuntimeException(ERROR_MESSAGE);
	}

	/**
	 * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#getServerBundleAdminExecuteCommand(org.eclipse.virgo.ide.runtime.core.IServerBehaviour, java.lang.String)
	 */
	public IServerCommand<String> getServerBundleAdminExecuteCommand(IServerBehaviour serverBehaviour, String command) {
		throw new RuntimeException(ERROR_MESSAGE);
	}

	/**
	 * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#preStartup(org.eclipse.virgo.ide.runtime.core.IServerBehaviour)
	 */
	public void preStartup(IServerBehaviour serverBehaviour) {
		throw new RuntimeException(ERROR_MESSAGE);
	}
}
