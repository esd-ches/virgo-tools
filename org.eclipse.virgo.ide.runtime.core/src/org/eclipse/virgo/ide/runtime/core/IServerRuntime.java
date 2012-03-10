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

import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jst.server.core.IJavaRuntime;

/**
 * Extension to {@link IJavaRuntime} that has specific dm Server methods.
 * @author Christian Dupuis
 * @since 1.0.0
 */
public interface IServerRuntime extends IJavaRuntime {

	String PROPERTY_VM_INSTALL_ID = "vm-install-id";

	String PROPERTY_VM_INSTALL_TYPE_ID = "vm-install-type-id";

	String PROPERTY_SERVER_ADDRESS = ServerCorePlugin.PLUGIN_ID + ".serverAddressProperty";

	String PROPERTY_VIRGO_VERSION_TYPE_ID = "virgo-version-type-id";

	/**
	 * Returns the runtime classpath of the dm server runtime
	 */
	List<IRuntimeClasspathEntry> getRuntimeClasspath();

	/**
	 * Returns the path to the server.profile file
	 */
	String getProfilePath();

	/**
	 * Returns the path to the server.config file
	 */
	String getConfigPath();

	/**
	 * Returns the user level bundle repository path
	 */
	String getUserLevelBundleRepositoryPath();

	/**
	 * Returns the user level library path
	 */
	String getUserLevelLibraryRepositoryPath();

	/**
	 * Returns the main class to launch the dm Server with.
	 */
	String getRuntimeClass();

}
