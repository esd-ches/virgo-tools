/*******************************************************************************
 * Copyright (c) 2009 SpringSource, a divison of VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource, a division of VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.virgo.ide.manifest.core;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.virgo.ide.manifest.internal.core.BundleManifestManager;
import org.eclipse.virgo.kernel.osgi.provisioning.tools.NoOpEventLogger;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.util.osgi.ServiceRegistrationTracker;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * Activator for the manifest.core plugin.
 * @author Christian Dupuis
 * @since 1.0.0
 */
public class BundleManifestCorePlugin extends Plugin {

	/** The bundle symbolic name of this plugin */
	public static final String PLUGIN_ID = "org.eclipse.virgo.ide.manifest.core";

	/** Bundle manifest file name */
	public static final String MANIFEST_FILE_NAME = "MANIFEST.MF";

	/** Test bundle manifest file name */
	public static final String TEST_MANIFEST_FILE_NAME = "TEST.MF";

	/** Bundle manifest folder name */
	public static final String MANIFEST_FOLDER_NAME = "META-INF";

	/** Relative path to a bundle manifest */
	public static final String MANIFEST_FILE_LOCATION = new StringBuilder('/').append(
			MANIFEST_FOLDER_NAME).append('/').append(MANIFEST_FILE_NAME).toString();

	/** Relative path to a test bundle manifest */
	public static final String TEST_MANIFEST_FILE_LOCATION = new StringBuilder('/').append(
			MANIFEST_FOLDER_NAME).append('/').append(TEST_MANIFEST_FILE_NAME).toString();

	/** Module-Type manifest header */
	public static final String MODULE_TYPE_MANIFEST_HEADER = "Module-Type";

	/** The shared instance */
	private static BundleManifestCorePlugin plugin;

	/** The bundle manifest manager hosted with this plugin */
	private static BundleManifestManager bundleManifestManager;
	
    private final ServiceRegistrationTracker tracker = new ServiceRegistrationTracker();

    private volatile EventLogger eventLogger = null;

	public void start(BundleContext context) throws Exception {
		super.start(context);
		bundleManifestManager = new BundleManifestManager();
		bundleManifestManager.start();
		plugin = this;
		
		this.eventLogger = new NoOpEventLogger();
        this.tracker.track(context.registerService(EventLogger.class.getName(), this.eventLogger, null));
        
        // make sure that the repository bundle is started
        Bundle bundle = Platform.getBundle("org.eclipse.virgo.repository");
        if (bundle != null && bundle.getState() != Bundle.ACTIVE) {
        	try {
        		bundle.start();
        	}
        	catch (IllegalStateException e) {
        	}
        }
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		bundleManifestManager.stop();
		
		this.tracker.unregisterAll();
		
		super.stop(context);
	}

	public static BundleManifestCorePlugin getDefault() {
		return plugin;
	}

	/** Returns the bundle manifest model manager */
	public static IBundleManifestManager getBundleManifestManager() {
		return bundleManifestManager;
	}

}
