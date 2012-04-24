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
package org.eclipse.virgo.ide.jdt.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.virgo.ide.jdt.internal.core.classpath.ServerClasspathContainerBundleManifestChangeListener;
import org.eclipse.virgo.ide.manifest.core.BundleManifestCorePlugin;
import org.eclipse.virgo.ide.runtime.core.ServerCorePlugin;
import org.osgi.framework.BundleContext;

/**
 * Bundle {@link JdtCorePlugin} for the jdt.core plug-in.
 * 
 * @author Christian Dupuis
 * @since 1.0.0
 */
public class JdtCorePlugin extends Plugin {

	/** The bundle-symbolic name */
	public static final String PLUGIN_ID = "org.eclipse.virgo.ide.jdt.core";

	/**
	 * The marker id used for creating markers that indicate un-resolved dependencies on bundle manifests
	 */
	public static final String DEPENDENCY_PROBLEM_MARKER_ID = PLUGIN_ID + ".dependencyproblemmarker";

	// The shared instance
	private static JdtCorePlugin plugin;

	private ServerClasspathContainerBundleManifestChangeListener bundleManifestChangeListener;

	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		bundleManifestChangeListener = new ServerClasspathContainerBundleManifestChangeListener();
		BundleManifestCorePlugin.getBundleManifestManager().addBundleManifestChangeListener(
				bundleManifestChangeListener);
		ServerCorePlugin.getArtefactRepositoryManager().addBundleRepositoryChangeListener(bundleManifestChangeListener);
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		BundleManifestCorePlugin.getBundleManifestManager().removeBundleManifestChangeListener(
				bundleManifestChangeListener);
		ServerCorePlugin.getArtefactRepositoryManager().removeBundleRepositoryChangeListener(
				bundleManifestChangeListener);
		super.stop(context);
	}

	public IEclipsePreferences getProjectPreferences(IProject project) {
		IScopeContext context = new ProjectScope(project);
		IEclipsePreferences node = context.getNode(PLUGIN_ID);
		return node;
	}

	public static JdtCorePlugin getDefault() {
		return plugin;
	}

	public static IStatus createErrorStatus(String message, Throwable exception) {
		if (message == null) {
			message = "";
		}
		return new Status(IStatus.ERROR, PLUGIN_ID, 0, message, exception);
	}

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	public static void log(String message, Throwable exception) {
		getDefault().getLog().log(createErrorStatus(message, exception));
	}

	public static void log(Throwable exception) {
		getDefault().getLog().log(createErrorStatus(exception.getMessage(), exception));
	}

}
