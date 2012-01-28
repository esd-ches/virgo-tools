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
 * A {@link IBundleManifestManager} implementation manages the life-cycle of {@link BundleManifest}instances and provides read access to manifest instances.
 * @author Christian Dupuis
 * @since 1.0.0
 */
public interface IBundleManifestManager {

	/**
	 * Returns a {@link BundleManifest} for the given {@link IJavaProject}.
	 * <p>
	 * If the {@link BundleManifest} has not already been loaded this method will try to locate a
	 * MANIFEST.MF file in the {@link IJavaProject}'s source folders and create the
	 * {@link BundleManifest} instance.
	 */
	BundleManifest getBundleManifest(IJavaProject javaProject);
	
	/**
	 * Returns a {@link BundleManifest} for the given {@link IJavaProject}'s test dependencies.
	 * <p>
	 * If the {@link BundleManifest} has not already been loaded this method will try to locate a
	 * TEST.MF file in the {@link IJavaProject}'s source folders and create the
	 * {@link BundleManifest} instance.
	 */
	BundleManifest getTestBundleManifest(IJavaProject javaProject);

	/**
	 * Returns the resolved package imports.
	 * <p>
	 * Note: the resolved package imports do not need to be same as the Import-Package headers as
	 * this method returns transitively imported packages (Require-Bundle, Library-Import) as well.
	 */
	Set<String> getResolvedPackageImports(IJavaProject javaProject);
	
	/**
	 * Returns the exported packages of the given <code>javaProject</code>.
	 */
	Set<String> getPackageExports(IJavaProject javaProject);
	
	/**
	 * Add a {@link IBundleManifestChangeListener} with the model manager. 
	 */
	void addBundleManifestChangeListener(IBundleManifestChangeListener bundleManifestChangeListener);
	
	/**
	 * Remove a registered {@link IBundleManifestChangeListener} from the model manager.
	 */
	void removeBundleManifestChangeListener(
			IBundleManifestChangeListener bundleManifestChangeListener);

}
