/*******************************************************************************
 * Copyright (c) 2009, 2012 SpringSource, a divison of VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource, a division of VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.virgo.ide.runtime.core;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.virgo.ide.runtime.internal.core.DeploymentIdentity;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.model.IURLProvider;



/**
 * Implementations of this interface are intended to provide extra knowledge
 * about the runtime behavior or a dm Server instance.
 * @author Christian Dupuis
 * @since 1.0.0
 */
public interface IServerBehaviour extends IURLProvider {
	
	/** Property indicating the ip of the deployer JMX control */
	String PROPERTY_MBEAN_SERVER_IP = ServerCorePlugin.PLUGIN_ID + "deployer.ip";

	/**
	 * Configure the given {@link ILaunch} and add all runtime settings like
	 * class path and command line options.
	 */
	void setupLaunch(ILaunch launch, String launchMode, IProgressMonitor monitor)
			throws CoreException;
	
	/**
	 * Returns the ip address of the controlled server
	 */
	String getMBeanServerIp();
	
	/**
	 * Returns the deployer control
	 */
	IServerDeployer getServerDeployer();

	IServerVersionHandler getVersionHandler();

	Map<String, DeploymentIdentity> getDeploymentIdentities();

	IPath getModuleDeployUri(IModule module);

	void onModuleStateChange(IModule[] modules, int stateStopped);

	void onModulePublishStateChange(IModule[] modules, int publishStateNone);

}
