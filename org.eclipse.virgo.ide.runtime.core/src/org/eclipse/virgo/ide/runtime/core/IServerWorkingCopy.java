/*******************************************************************************
 * Copyright (c) 2009 - 2013 SpringSource, a divison of VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource, a division of VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.virgo.ide.runtime.core;

import java.beans.PropertyChangeListener;
import java.util.List;

/**
 * Extension to {@link IServer}.
 * 
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @since 1.0.0
 */
public interface IServerWorkingCopy extends IServer {

	/** Default deploy directory name */
	String DEFAULT_DEPLOYDIR = "stage";

	/** Default deploy timeout */
	int DEFAULT_DEPLOY_TIMEOUT = 30;

	/** Default deployer control username */
	String DEFAULT_MBEAN_SERVER_USERNAME = "admin";

	/** Default deployer control port */
	String DEFAULT_MBEAN_SERVER_PORT = "9753";

	/** Default deployer control password */
	String DEFAULT_MBEAN_SERVER_PASSWORD = "springsource";

	/** Default filename patters that should not trigger a bundle refresh */
	String DEFAULT_STATIC_FILENAMES = "*.html,*.xhtml,*.css,*.js,*.jspx,*.jsp,*.gif,*.jpg,*.png,*.swf,*-flow.xml,*.properties,*.xml,!tiles.xml,!web.xml";

	/** Default -XX:MaxPermSize value based on Virgo 3.6 dmk.sh */
	String DEFAULT_MAX_PERM_SIZE = "512m";

	/** Default setting if the server should tail trace files */
	boolean DEFAULT_TAIL_LOG_FILES = false;

	boolean DEFAULT_CLEAN_STARTUP = false;

	/**
	 * Sets the deploy directory
	 */
	void setDeployDirectory(String deployDir);

	/**
	 * Adds a {@link PropertyChangeListener} to the configuration backing the server.
	 */
	void addConfigurationChangeListener(PropertyChangeListener listener);

	/**
	 * Removes an installed {@link PropertyChangeListener}.
	 */
	void removeConfigurationChangeListener(PropertyChangeListener listener);

	/**
	 * Sets the username of the JMX deployer control
	 */
	void setMBeanServerUsername(String username);

	/**
	 * Sets the port of the JMX deployer control
	 */
	void setMBeanServerPort(int port);

	/**
	 * Sets the timeout of the JMX deploy command
	 */
	void setDeployTimeout(int timeout);

	/**
	 * Sets the password of the JMX deployer control
	 */
	void setMBeanServerPassword(String password);

	/**
	 * Sets the flag to tail application trace files
	 */
	void shouldTailTraceFiles(boolean shouldTailTraceFiles);

	void shouldCleanStartup(boolean shouldCleanStartup);

	void setArtefactOrder(List<String> artefactOrder);

	void setStaticFilenamePatterns(String filenamePatterns);

	void setMaxPermSize(String maxPermSize);

}
