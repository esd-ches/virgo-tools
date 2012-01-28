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

import java.util.Set;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;

/**
 * A listener to get notification if a {@link BundleManifest} and {@link IJavaProject} has been
 * changed in the bundle manifest model.
 * <p>
 * Register and un-register implementations of this interface with the
 * {@link IBundleManifestManager#addBundleManifestChangeListener()} and
 * {@link IBundleManifestManager#removeBundleManifestChangeListener()} method
 * @author Christian Dupuis
 * @since 1.0.0
 * @see IBundleManifestManager
 * @see BundleManifestCorePlugin#getBundleManifestManager()
 */
public interface IBundleManifestChangeListener {
	
	enum Type {
		IMPORT_PACKAGE, IMPORT_BUNDLE, IMPORT_LIBRARY, EXPORT_PACKAGE, REQUIRE_BUNDLE
	}

	/**
	 * Notified if in case of a change to a {@link BundleManifest} within the {@link IJavaProject}.
	 */
	void bundleManifestChanged(BundleManifest newBundleManfest, BundleManifest oldBundleManifest,
			BundleManifest newTestBundleManifest, BundleManifest oldTestBundleManifest,
			Set<Type> type, IJavaProject javaProject);

}
