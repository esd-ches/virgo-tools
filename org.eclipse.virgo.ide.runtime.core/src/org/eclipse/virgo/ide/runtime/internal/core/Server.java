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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jst.server.core.FacetUtil;
import org.eclipse.jst.server.core.IWebModule;
import org.eclipse.virgo.ide.facet.core.FacetCorePlugin;
import org.eclipse.virgo.ide.facet.core.FacetUtils;
import org.eclipse.virgo.ide.module.core.ServerModuleDelegate;
import org.eclipse.virgo.ide.runtime.core.IServer;
import org.eclipse.virgo.ide.runtime.core.IServerConfiguration;
import org.eclipse.virgo.ide.runtime.core.IServerVersionHandler;
import org.eclipse.virgo.ide.runtime.core.IServerWorkingCopy;
import org.eclipse.virgo.ide.runtime.core.ServerCorePlugin;
import org.eclipse.wst.common.project.facet.core.FacetedProjectFramework;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.eclipse.wst.common.project.facet.core.runtime.IRuntime;
import org.eclipse.wst.common.project.facet.core.runtime.RuntimeManager;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IModuleType;
import org.eclipse.wst.server.core.model.ServerDelegate;


/**
 * Default dm server implementation.
 * @author Christian Dupuis
 * @since 1.0.0
 */
public class Server extends ServerDelegate implements IServer, IServerWorkingCopy {

	private ServerConfiguration configuration;

	protected transient List<PropertyChangeListener> listeners = new ArrayList<PropertyChangeListener>();

	protected transient IServerVersionHandler versionHandler;

	public void addConfigurationChangeListener(PropertyChangeListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}
	
	protected String getServerName() {
		return "SpringSource dm Server";
	}

	@Override
	public IStatus canModifyModules(IModule[] add, IModule[] remove) {
		if (add != null) {
			int size = add.length;
			for (int i = 0; i < size; i++) {
				IModule module = add[i];
				if (!FacetCorePlugin.WEB_FACET_ID.equals(module.getModuleType().getId())
						&& !FacetCorePlugin.BUNDLE_FACET_ID.equals(module.getModuleType().getId())
						&& !FacetCorePlugin.PAR_FACET_ID.equals(module.getModuleType().getId())
						&& !FacetCorePlugin.PLAN_FACET_ID.equals(module.getModuleType().getId())) {
					return new Status(IStatus.ERROR, ServerCorePlugin.PLUGIN_ID, 0,
							"SpringSource par or bundle projects only", null);
				}

				IProject project = module.getProject();
				// Check that nested par module is not displayed
				if (module.getId().endsWith("$" + project.getName())) {
					return new Status(IStatus.ERROR, ServerCorePlugin.PLUGIN_ID, 0,
							"No nested par modules allowed", null);
				}

				// Check that shared war is only deployed as WAR
				try {
					if (project.hasNature(JavaCore.NATURE_ID)
							&& FacetedProjectFramework.hasProjectFacet(project, FacetCorePlugin.WEB_FACET_ID)
							&& FacetUtils.isBundleProject(project)
							&& FacetCorePlugin.BUNDLE_FACET_ID.equals(module.getModuleType().getId())) {
						return new Status(IStatus.ERROR, ServerCorePlugin.PLUGIN_ID, 0,
								"Shared WAR deploy only as jst.web modules", null);
					}
				} catch (CoreException e) {
					return new Status(IStatus.ERROR, ServerCorePlugin.PLUGIN_ID, 0,
										"Core Exception when resolving project: ", e);
				}

				if (getVersionHandler() == null) {
					return new Status(IStatus.ERROR, ServerCorePlugin.PLUGIN_ID, 0,
							"No " + getServerName() + " runtime configured", null);
				}

				IStatus status = getVersionHandler().canAddModule(module);
				if (status != null && !status.isOK()) {
					return status;
				}

				if (module.getProject() == null || !module.getProject().isAccessible()) {
					return new Status(IStatus.ERROR, ServerCorePlugin.PLUGIN_ID, 0,
							"Project not accessible", null);
				}

				status = FacetUtil.verifyFacets(module.getProject(), getServer());
				if (status != null && !status.isOK()) {
					return status;
				}

			}
		}

		return Status.OK_STATUS;
	}

	public void configurationChanged() {
		configuration = null;
	}

	@Override
	public IModule[] getChildModules(IModule[] module) {
		if (module == null) {
			return null;
		}

		IModuleType moduleType = module[0].getModuleType();

		if (module.length == 1 && moduleType != null) {
			if (FacetCorePlugin.WEB_FACET_ID.equals(moduleType.getId())) {
				IWebModule webModule = (IWebModule) module[0].loadAdapter(IWebModule.class, null);
				if (webModule != null) {
					IModule[] modules = webModule.getModules();
					return modules;
				}
			}
			else if (FacetCorePlugin.PAR_FACET_ID.equals(moduleType.getId())) {
				ServerModuleDelegate parModule = (ServerModuleDelegate) module[0].loadAdapter(
						ServerModuleDelegate.class, null);
				if (parModule != null) {
					return parModule.getChildModules();
				}
			}
			else if (FacetCorePlugin.PLAN_FACET_ID.equals(moduleType.getId())) {
				ServerModuleDelegate planModule = (ServerModuleDelegate) module[0].loadAdapter(
						ServerModuleDelegate.class, null);
				if (planModule != null) {
					return planModule.getChildModules();
				}
			}
		}

		return new IModule[0];
	}

	public synchronized IServerConfiguration getConfiguration() {
		if (configuration == null) {
			try {
				configuration = new ServerConfiguration(getServer().getServerConfiguration());
			}
			catch (IOException e) {
			}
		}
		return configuration;
	}

	public String getDeployDirectory() {
		return getAttribute(PROPERTY_DEPLOY_DIR, DEFAULT_DEPLOYDIR);
	}

	public String getMBeanServerPassword() {
		return getAttribute(PROPERTY_MBEAN_SERVER_PASSWORD, DEFAULT_MBEAN_SERVER_PASSWORD);
	}

	public int getMBeanServerPort() {
		return Integer.valueOf(getAttribute(PROPERTY_MBEAN_SERVER_PORT, DEFAULT_MBEAN_SERVER_PORT));
	}

	public String getMBeanServerUsername() {
		return getAttribute(PROPERTY_MBEAN_SERVER_USERNAME, DEFAULT_MBEAN_SERVER_USERNAME);
	}

	public IPath getModuleDeployDirectory(IModule module) {
		if (FacetCorePlugin.BUNDLE_FACET_ID.equals(module.getModuleType().getId())) {
			return getServerDeployDirectory().append(module.getName() + ".jar");
		}
		else if (FacetCorePlugin.PAR_FACET_ID.equals(module.getModuleType().getId())) {
			return getServerDeployDirectory().append(module.getName() + ".par");
		}
		else if (FacetCorePlugin.PLAN_FACET_ID.equals(module.getModuleType().getId())) {
			return getServerDeployDirectory();
		}
		else {
			return getServerDeployDirectory().append(module.getName() + ".war");
		}
	}

	@Override
	public IModule[] getRootModules(IModule module) throws CoreException {
		if (FacetCorePlugin.WEB_FACET_ID.equals(module.getModuleType().getId())
				|| FacetCorePlugin.BUNDLE_FACET_ID.equals(module.getModuleType().getId())
				|| FacetCorePlugin.PAR_FACET_ID.equals(module.getModuleType().getId())
				|| FacetCorePlugin.PLAN_FACET_ID.equals(module.getModuleType().getId())) {
			IStatus status = canModifyModules(new IModule[] { module }, null);
			if (status == null || !status.isOK()) {
				return new IModule[0];
			}
			return new IModule[] { module };
		}
		return new IModule[0];
	}

	public ServerRuntime getRuntime() {
		if (getServer().getRuntime() == null) {
			return null;
		}

		return (ServerRuntime) getServer().getRuntime().loadAdapter(ServerRuntime.class, null);
	}

	public IPath getRuntimeBaseDirectory() {
		IServerVersionHandler tvh = getVersionHandler();
		if (tvh != null) {
			return tvh.getRuntimeBaseDirectory(this.getServer());
		}
		return null;
	}

	public IPath getServerDeployDirectory() {
		String deployDir = getDeployDirectory();
		IPath deployPath = new Path(deployDir);
		if (!deployPath.isAbsolute()) {
			IPath base = getRuntimeBaseDirectory();
			deployPath = base.append(deployPath);
		}
		// Make sure that stage directory is accessible and we can write into it; if we can't the stage will be in the
		// plugin's statelocation
		File deployPathFile = deployPath.toFile();
		if (!deployPathFile.exists() && !deployPathFile.getParentFile().canWrite()) {
			deployPath = ServerCorePlugin.getDefault().getStateLocation().append(deployDir);
		}
		else if (deployPathFile.exists() && !deployPathFile.canWrite()) {
			deployPath = ServerCorePlugin.getDefault().getStateLocation().append(deployDir);
		}
		return deployPath;
	}

	public IServerVersionHandler getVersionHandler() {
		if (versionHandler == null) {
			if (getServer().getRuntime() == null || getRuntime() == null) {
				return null;
			}
			versionHandler = getRuntime().getVersionHandler();
		}
		return versionHandler;
	}

	@Override
	public void modifyModules(IModule[] add, IModule[] remove, IProgressMonitor monitor)
			throws CoreException {
		// Add target runtime to new projects in order to refresh their classpath based on the
		// manifest
		if (add != null && add.length > 0) {
			// Get the facet runtime which is != the server runtime
			IRuntime runtime = RuntimeManager.getRuntime(getRuntime().getRuntime().getName());
			if (runtime != null) {
				for (IModule addedModule : add) {
					IFacetedProject project = ProjectFacetsManager.create(addedModule.getProject(),
							false, monitor);
					if (project != null) {
						Set<IRuntime> runtimes = new HashSet<IRuntime>(project
								.getTargetedRuntimes());

						// Add this server's runtime to the target runtime
						if (!runtimes.contains(runtime)) {
							runtimes.add(runtime);
							project.setTargetedRuntimes(runtimes,
									new SubProgressMonitor(monitor, 1));
							if (runtimes.size() > 1) {
								project.setPrimaryRuntime(runtime, new SubProgressMonitor(monitor,
										1));
							}
						}
					}
				}
			}
		}

		// Remove and add the modules to the ordered list
		if (remove != null && remove.length > 0) {
			for (IModule module : remove) {
				getConfiguration().removeArtefact(module.getId());
			}
		}
		if (add != null && add.length > 0) {
			for (IModule module : add) {
				getConfiguration().addArtefact(module.getId());
			}
		}
	}

	public void removeConfigurationChangeListener(PropertyChangeListener listener) {
		listeners.remove(listener);
	}

	public void saveConfiguration(IProgressMonitor monitor) throws CoreException {
		if (configuration == null)
			return;
	}

	@Override
	public void setDefaults(IProgressMonitor monitor) {
		setAttribute(PROPERTY_AUTO_PUBLISH_TIME, 2);
	}

	public void setDeployDirectory(String deployDir) {
		setAttribute(PROPERTY_DEPLOY_DIR, deployDir);
	}

	public void setMBeanServerPassword(String password) {
		String oldPassword = getMBeanServerPassword();
		setAttribute(PROPERTY_MBEAN_SERVER_PASSWORD, password);
		fireConfigurationChanged(PROPERTY_MBEAN_SERVER_PASSWORD, oldPassword, password);
	}

	public void setMBeanServerPort(int port) {
		int oldPort = getMBeanServerPort();
		setAttribute(PROPERTY_MBEAN_SERVER_PORT, port);
		fireConfigurationChanged(PROPERTY_MBEAN_SERVER_PORT, oldPort, port);
	}

	public void setMBeanServerUsername(String username) {
		String oldPassword = getMBeanServerUsername();
		setAttribute(PROPERTY_MBEAN_SERVER_USERNAME, username);
		fireConfigurationChanged(PROPERTY_MBEAN_SERVER_USERNAME, oldPassword, username);
	}

	public boolean shouldTailTraceFiles() {
		return getAttribute(PROPERTY_TAIL_LOG_FILES, DEFAULT_TAIL_LOG_FILES);
	}

	public void shouldTailTraceFiles(boolean shouldTailTraceFiles) {
		boolean oldValue = shouldTailTraceFiles();
		setAttribute(PROPERTY_TAIL_LOG_FILES, shouldTailTraceFiles);
		fireConfigurationChanged(PROPERTY_TAIL_LOG_FILES, oldValue, shouldTailTraceFiles);

	}
	
	public boolean shouldCleanStartup() {
		return getAttribute(PROPERTY_CLEAN_STARTUP, DEFAULT_CLEAN_STARTUP);
	}
	
	public void shouldCleanStartup(boolean shouldCleanStartup) {
		boolean oldValue = shouldCleanStartup();
		setAttribute(PROPERTY_CLEAN_STARTUP, shouldCleanStartup);
		fireConfigurationChanged(PROPERTY_CLEAN_STARTUP, oldValue, shouldCleanStartup);
		
	}

	public List<String> getArtefactOrder() {
		return getConfiguration().getArtefactOrder();
	}

	public void setArtefactOrder(List<String> artefactOrder) {
		List<String> oldOrder = getArtefactOrder();
		getConfiguration().setArtefactOrder(artefactOrder);
		fireConfigurationChanged(PROPERTY_ARTEFACT_ORDER, oldOrder, artefactOrder);

	}

	@Override
	public String toString() {
		return getServerName();
	}

	protected void fireConfigurationChanged(String key, Object oldValue, Object newValue) {
		PropertyChangeEvent event = new PropertyChangeEvent(this, key, oldValue, newValue);
		for (PropertyChangeListener listener : listeners) {
			listener.propertyChange(event);
		}
	}

	protected String renderCommandLine(String[] commandLine, String separator) {
		if (commandLine == null || commandLine.length < 1) {
			return "";
		}
		StringBuffer buf = new StringBuffer(commandLine[0]);
		for (int i = 1; i < commandLine.length; i++) {
			buf.append(separator);
			buf.append(commandLine[i]);
		}
		return buf.toString();
	}

	public void setStaticFilenamePatterns(String filenamePatterns) {
		String old = getStaticFilenamePatterns();
		setAttribute(PROPERTY_STATIC_FILENAMES, filenamePatterns);
		fireConfigurationChanged(PROPERTY_STATIC_FILENAMES, old, filenamePatterns);
	}

	public String getStaticFilenamePatterns() {
		return getAttribute(PROPERTY_STATIC_FILENAMES, DEFAULT_STATIC_FILENAMES);
	}

}
