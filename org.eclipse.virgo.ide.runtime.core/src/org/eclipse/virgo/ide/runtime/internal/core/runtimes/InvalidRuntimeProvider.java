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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.libra.framework.editor.core.model.IBundle;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.virgo.ide.manifest.core.dependencies.IDependencyLocator;
import org.eclipse.virgo.ide.manifest.core.dependencies.IDependencyLocator.JavaVersion;
import org.eclipse.virgo.ide.runtime.core.IServerBehaviour;
import org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider;
import org.eclipse.virgo.ide.runtime.core.ServerCorePlugin;
import org.eclipse.virgo.ide.runtime.core.ServerUtils;
import org.eclipse.virgo.ide.runtime.internal.core.DeploymentIdentity;
import org.eclipse.virgo.ide.runtime.internal.core.Server;
import org.eclipse.virgo.ide.runtime.internal.core.VirgoServerRuntime;
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

    private InvalidRuntimeProvider() {
    }

    /**
     * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#getRuntimeClass()
     */
    public String getRuntimeClass() {
        handleError();
        return null;
    }

    // Potential leak source? Unlikely, but possible.
    public static Collection<String> errorsReported = new HashSet<String>();

    private void handleError(IRuntime runtime) {
        String runtimeName = null;
        // boolean showError = false;
        if (runtime != null) {
            runtimeName = runtime.getName();
            // showError = errorsReported.add(runtimeName);
        } else {
            runtimeName = "The runtime";
        }
        String message = runtimeName + " is not a valid environment. Go to Preferences:Server:Runtime Environments to define a valid server.";
        message += "\n(This error may be followed by related exceptions.)";
        int type = StatusManager.LOG | StatusManager.SHOW;
        // if (showError) {
        // type |= StatusManager.SHOW;
        // }
        StatusManager.getManager().handle(new Status(IStatus.ERROR, ServerCorePlugin.PLUGIN_ID, message), type);
    }

    private void handleError(IServerBehaviour serverBehaviour) {
        Server server = ServerUtils.getServer(serverBehaviour);
        VirgoServerRuntime runtime = server.getRuntime();
        handleError(runtime.getRuntime());
    }

    private void handleError() {
        handleError((IRuntime) null);
    }

    /**
     * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#getRuntimeProgramArguments(org.eclipse.virgo.ide.runtime.core.IServerBehaviour)
     */
    public String[] getRuntimeProgramArguments(IServerBehaviour serverBehaviour) {
        handleError(serverBehaviour);
        return null;
    }

    /**
     * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#createDependencyLocator(org.eclipse.wst.server.core.IRuntime,
     *      java.lang.String, java.lang.String[], java.lang.String,
     *      org.eclipse.virgo.ide.manifest.core.dependencies.IDependencyLocator.JavaVersion)
     */
    public IDependencyLocator createDependencyLocator(IRuntime runtime, String serverHomePath, String[] additionalSearchPaths,
        String indexDirectoryPath, JavaVersion javaVersion) throws IOException {
        handleError(runtime);
        return null;
    }

    public IStatus verifyInstallation(IRuntime runtime) {
        return new Status(IStatus.ERROR, ServerCorePlugin.PLUGIN_ID, "No Virgo Runtime found: " + runtime.getLocation() + ".");
    }

    /**
     * @see org.eclipse.virgo.ide.runtime.internal.core.runtimes.VirgoRuntimeProvider#getConfigurationDir()
     */
    String getConfigDir() {
        handleError();
        return null;
    }

    /**
     * @see org.eclipse.virgo.ide.runtime.internal.core.runtimes.VirgoRuntimeProvider#getProfileDir()
     */
    String getProfileDir() {
        handleError();
        return null;
    }

    /**
     * @see org.eclipse.virgo.ide.runtime.internal.core.runtimes.VirgoRuntimeProvider#getID()
     */
    public String getID() {
        handleError();
        return null;
    }

    /**
     * @see org.eclipse.virgo.ide.runtime.internal.core.runtimes.VirgoRuntimeProvider#getName()
     */
    public String getName() {
        handleError();
        return "Invalid Runtime";
    }

    /**
     * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#canAddModule(org.eclipse.wst.server.core.IModule)
     */
    public IStatus canAddModule(IModule module) {
        return new Status(IStatus.ERROR, ServerCorePlugin.PLUGIN_ID, "No Virgo Runtime found.");
    }

    /**
     * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#getExcludedRuntimeProgramArguments(boolean)
     */
    public String[] getExcludedRuntimeProgramArguments(boolean starting) {
        handleError();
        return null;
    }

    /**
     * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#getRuntimeBaseDirectory(org.eclipse.wst.server.core.IServer)
     */
    public IPath getRuntimeBaseDirectory(IServer server) {
        handleError(server.getRuntime());
        return null;
    }

    /**
     * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#getRuntimeClasspath(org.eclipse.core.runtime.IPath)
     */
    public List<IRuntimeClasspathEntry> getRuntimeClasspath(IPath installPath) {
        handleError();
        return Collections.EMPTY_LIST;
    }

    /**
     * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#getRuntimeVMArguments(org.eclipse.virgo.ide.runtime.core.IServerBehaviour,
     *      org.eclipse.core.runtime.IPath, org.eclipse.core.runtime.IPath, org.eclipse.core.runtime.IPath)
     */
    public String[] getRuntimeVMArguments(IServerBehaviour serverBehaviour, IPath installPath, IPath configPath, IPath deployPath) {
        handleError(serverBehaviour);
        return new String[] {};
    }

    /**
     * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#getProfilePath(org.eclipse.wst.server.core.IRuntime)
     */
    public String getProfilePath(IRuntime runtime) {
        handleError(runtime);
        return null;
    }

    /**
     * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#getExtLevelBundleRepositoryPath(org.eclipse.wst.server.core.IRuntime)
     */
    public String getExtLevelBundleRepositoryPath(IRuntime runtime) {
        handleError(runtime);
        return null;
    }

    /**
     * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#getUserLevelBundleRepositoryPath(org.eclipse.wst.server.core.IRuntime)
     */
    public String getUserLevelBundleRepositoryPath(IRuntime runtime) {
        handleError(runtime);
        return null;
    }

    /**
     * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#getUserLevelLibraryRepositoryPath(org.eclipse.wst.server.core.IRuntime)
     */
    public String getUserLevelLibraryRepositoryPath(IRuntime runtime) {
        handleError(runtime);
        return null;
    }

    /**
     * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#getConfigPath(org.eclipse.wst.server.core.IRuntime)
     */
    public String getConfigPath(IRuntime runtime) {
        handleError(runtime);
        return null;
    }

    /**
     * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#getDeployerMBeanName()
     */
    public String getDeployerMBeanName() {
        handleError();
        return null;
    }

    /**
     * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#getRecoveryMonitorMBeanName()
     */
    public String getRecoveryMonitorMBeanName() {
        handleError();
        return null;
    }

    /**
     * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#getShutdownMBeanName()
     */
    public String getShutdownMBeanName() {
        handleError();
        return null;
    }

    /**
     * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#getServerPingCommand(org.eclipse.virgo.ide.runtime.core.IServerBehaviour)
     */
    public IServerCommand<Boolean> getServerPingCommand(IServerBehaviour serverBehaviour) {
        handleError();
        return null;
    }

    /**
     * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#getServerShutdownCommand(org.eclipse.virgo.ide.runtime.core.IServerBehaviour)
     */
    public IServerCommand<Void> getServerShutdownCommand(IServerBehaviour serverBehaviour) {
        handleError(serverBehaviour);
        return null;
    }

    /**
     * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#getServerDeployCommand(org.eclipse.virgo.ide.runtime.core.IServerBehaviour,
     *      org.eclipse.wst.server.core.IModule)
     */
    public IServerCommand<DeploymentIdentity> getServerDeployCommand(IServerBehaviour serverBehaviour, IModule module) {
        handleError(serverBehaviour);
        return null;
    }

    /**
     * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#getServerRedeployCommand(org.eclipse.virgo.ide.runtime.core.IServerBehaviour,
     *      org.eclipse.wst.server.core.IModule)
     */
    public IServerCommand<DeploymentIdentity> getServerRedeployCommand(IServerBehaviour serverBehaviour, IModule module) {
        handleError(serverBehaviour);
        return null;
    }

    /**
     * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#getServerRefreshCommand(org.eclipse.virgo.ide.runtime.core.IServerBehaviour,
     *      org.eclipse.wst.server.core.IModule, java.lang.String)
     */
    public IServerCommand<Void> getServerRefreshCommand(IServerBehaviour serverBehaviour, IModule module, String bundleSymbolicName) {
        handleError(serverBehaviour);
        return null;
    }

    /**
     * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#getServerUpdateCommand(org.eclipse.virgo.ide.runtime.core.IServerBehaviour,
     *      org.eclipse.wst.server.core.IModule, org.eclipse.wst.server.core.model.IModuleFile,
     *      org.eclipse.virgo.ide.runtime.internal.core.DeploymentIdentity, java.lang.String, java.lang.String)
     */
    public IServerCommand<Void> getServerUpdateCommand(IServerBehaviour serverBehaviour, IModule module, IModuleFile moduleFile,
        DeploymentIdentity identity, String bundleSymbolicName, String targetPath) {
        handleError(serverBehaviour);
        return null;
    }

    /**
     * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#getServerUndeployCommand(org.eclipse.virgo.ide.runtime.core.IServerBehaviour,
     *      org.eclipse.wst.server.core.IModule)
     */
    public IServerCommand<Void> getServerUndeployCommand(IServerBehaviour serverBehaviour, IModule module) {
        handleError(serverBehaviour);
        return null;
    }

    /**
     * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#getServerDeployCommand(org.eclipse.virgo.ide.runtime.core.IServerBehaviour,
     *      java.net.URI)
     */
    public IServerCommand<DeploymentIdentity> getServerDeployCommand(IServerBehaviour serverBehaviour, URI connectorBundleUri) {
        handleError(serverBehaviour);
        return null;
    }

    /**
     * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#getServerBundleAdminCommand(org.eclipse.virgo.ide.runtime.core.IServerBehaviour)
     */
    public IServerCommand<Map<Long, IBundle>> getServerBundleAdminCommand(IServerBehaviour serverBehaviour) {
        handleError(serverBehaviour);
        return null;
    }

    /**
     * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#getServerBundleAdminExecuteCommand(org.eclipse.virgo.ide.runtime.core.IServerBehaviour,
     *      java.lang.String)
     */
    public IServerCommand<String> getServerBundleAdminExecuteCommand(IServerBehaviour serverBehaviour, String command) {
        handleError(serverBehaviour);
        return null;
    }

    /**
     * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#preStartup(org.eclipse.virgo.ide.runtime.core.IServerBehaviour)
     */
    public void preStartup(IServerBehaviour serverBehaviour) {
        handleError(serverBehaviour);
    }

    /**
     * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#getServerDeployCommand(org.eclipse.virgo.ide.runtime.core.IServerBehaviour)
     */
    public IServerCommand<DeploymentIdentity> getServerDeployCommand(IServerBehaviour serverBehaviour) {
        return null;
    }

    /**
     * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#getConnectorBundleUri()
     */
    public URI getConnectorBundleUri() {
        return null;
    }

    /**
     * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#getServerPropertiesDirectories()
     */
    public String[] getServerPropertiesDirectories() {
        return new String[0];
    }

    /**
     * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#getServerLogDirectories()
     */
    public String[] getServerLogDirectories() {
        return new String[0];
    }
}
