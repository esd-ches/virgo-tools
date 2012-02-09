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

import java.util.List;

/**
 * dm Server specific interface to be implemented by a server
 * @author Christian Dupuis
 * @since 1.0.0
 */
public interface IServer {

	/** Property indicating the auto publish setting */
	String PROPERTY_AUTO_PUBLISH_TIME = "auto-publish-time";

	/** Property indicating the installation directory */
	String PROPERTY_INSTANCE_DIR = ServerCorePlugin.PLUGIN_ID + ".instance.dir";

	/** Property indicating the deploy directory */
	String PROPERTY_DEPLOY_DIR = ServerCorePlugin.PLUGIN_ID + "deploy.dir";

	/** Property indicating the username of the deployer JMX control */
	String PROPERTY_MBEAN_SERVER_USERNAME = ServerCorePlugin.PLUGIN_ID + "deployer.username";

	/** Property indicating the password of the deployer JMX control */
	String PROPERTY_MBEAN_SERVER_PASSWORD = ServerCorePlugin.PLUGIN_ID + "deployer.password";

	/** Property indicating the port of the deployer JMX control */
	String PROPERTY_MBEAN_SERVER_PORT = ServerCorePlugin.PLUGIN_ID + "deployer.port";

	/** Property indicating the port of the deployer JMX control */
	String PROPERTY_STATIC_FILENAMES = ServerCorePlugin.PLUGIN_ID + "filenames.static";
	
	/** Property if the server should tail log files */
	String PROPERTY_TAIL_LOG_FILES = ServerCorePlugin.PLUGIN_ID + "tail.log.files";
	
	String PROPERTY_CLEAN_STARTUP = ServerCorePlugin.PLUGIN_ID + "clean.startup";
	
	String PROPERTY_ARTEFACT_ORDER = ServerCorePlugin.PLUGIN_ID + "artefact.deploy.order";
	
	/**
	 * Returns the deploy directory.
	 */
	String getDeployDirectory();

	/**
	 * Returns the {@link IServerConfiguration}.
	 */
	IServerConfiguration getConfiguration();

	/**
	 * Returns the username of the JMX deployer control
	 */
	String getMBeanServerUsername();

	/**
	 * Returns the port of the JMX deployer control
	 */
	int getMBeanServerPort();

	/**
	 * Returns the password of the JMX deployer control
	 */
	String getMBeanServerPassword();
	
	/**
	 * Returns <code>true</code> if the server should tail log files 
	 */
	boolean shouldTailTraceFiles();

	boolean shouldCleanStartup();
	
	List<String> getArtefactOrder();
	
	String getStaticFilenamePatterns();
}
