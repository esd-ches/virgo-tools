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
package org.eclipse.virgo.ide.jdt.internal.core.classpath;

import java.util.Arrays;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.virgo.ide.facet.core.FacetUtils;
import org.eclipse.virgo.ide.manifest.core.IBundleManifestChangeListener;
import org.eclipse.virgo.ide.manifest.internal.core.BundleManifestManager;
import org.eclipse.virgo.ide.runtime.core.ServerUtils;
import org.eclipse.virgo.ide.runtime.core.provisioning.IBundleRepositoryChangeListener;
import org.eclipse.wst.server.core.IRuntime;
import org.springframework.ide.eclipse.core.java.JdtUtils;

import org.eclipse.virgo.util.osgi.manifest.BundleManifest;

/**
 * {@link IBundleManifestChangeListener} that triggers refreshing of the bundle class path container
 * through scheduling a new workspace job.
 * @author Christian Dupuis
 * @since 1.0.0
 */
public class ServerClasspathContainerBundleManifestChangeListener implements
		IBundleManifestChangeListener, IBundleRepositoryChangeListener {

	/**
	 * Gets notified for every change to a given <code>bundleManifest</code> and delegates the
	 * refreshment of the class path container to the {@link ServerClasspathContainerUpdateJob}.
	 */
	public void bundleManifestChanged(BundleManifest newBundleManifest,
			BundleManifest oldBundleManifest, BundleManifest newTestBundleManifest,
			BundleManifest oldTestBundleManifest, Set<Type> types, IJavaProject javaProject) {
		if (newBundleManifest != null || newTestBundleManifest != null) {
			ServerClasspathContainerUpdateJob.scheduleClasspathContainerUpdateJob(javaProject, types);
		}
	}

	/**
	 * Gets notified for every change of the given <code>runtime</code>'s {@link BundleRepository}.
	 * On every notification it will update <i>all</i> bundle project's {@link BundleManifest}.
	 */
	public void bundleRepositoryChanged(IRuntime runtime) {
		for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			if (FacetUtils.isBundleProject(project)
					&& Arrays.asList(ServerUtils.getTargettedRuntimes(project)).contains(runtime)) {
				ServerClasspathContainerUpdateJob.scheduleClasspathContainerUpdateJob(JavaCore.create(project), BundleManifestManager.IMPORTS_CHANGED);
			}
		}
	}

}
