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
package org.eclipse.virgo.ide.jdt.internal.core.classpath;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.virgo.ide.jdt.core.JdtCorePlugin;
import org.eclipse.virgo.ide.jdt.internal.core.util.ClasspathUtils;
import org.eclipse.virgo.ide.manifest.core.BundleManifestUtils;
import org.eclipse.virgo.ide.manifest.internal.core.BundleManifestManager;


/**
 * {@link ClasspathContainerInitializer} that creates and restores {@link ServerClasspathContainer}.
 * @author Christian Dupuis
 * @since 1.0.0
 */
public class ServerClasspathContainerInitializer extends ClasspathContainerInitializer {

	/**
	 * Initializes a new {@link ServerClasspathContainer} and install the container with the given
	 * <code>javaProject</code>.
	 * <p>
	 * Schedules a container update by calling
	 * {@link ServerClasspathContainerUpdateJob#scheduleClasspathContainerUpdateJob()}
	 */
	@Override
	public void initialize(IPath containerPath, IJavaProject javaProject) throws CoreException {
		// Only responsible for our own class path container
		if (containerPath.equals(ServerClasspathContainer.CLASSPATH_CONTAINER_PATH)) {

			// Retrieve persisted class path container
			IClasspathContainer oldContainer = ClasspathUtils.getClasspathContainer(javaProject);

			ServerClasspathContainer newContainer = null;
			IClasspathEntry[] oldEntries = null;

			if (oldContainer == null) {

				// Try to read previously persisted classpath container settings
				oldEntries = ServerClasspathUtils.readPersistedClasspathEntries(javaProject);

				if (oldEntries != null) {
					newContainer = new ServerClasspathContainer(javaProject, oldEntries);
				}
				else {
					newContainer = new ServerClasspathContainer(javaProject);
				}
			}
			else {
				newContainer = new ServerClasspathContainer(javaProject, oldContainer
						.getClasspathEntries());
			}

			// Install the class path container with the project
			JavaCore.setClasspathContainer(containerPath, new IJavaProject[] { javaProject },
					new IClasspathContainer[] { newContainer }, new NullProgressMonitor());

			if (oldEntries == null || hasManifestChanged(javaProject, false)
					|| hasManifestChanged(javaProject, true)) {
				// Schedule refresh of class path container
				ServerClasspathContainerUpdateJob.scheduleClasspathContainerUpdateJob(javaProject,
						BundleManifestManager.IMPORTS_CHANGED);
			}
		}
	}

	private boolean hasManifestChanged(IJavaProject javaProject, boolean testManifest) {
		IFile file = BundleManifestUtils.locateManifest(javaProject, testManifest);
		if (file != null && file.exists()) {
			try {
				String lastmodified = file.getPersistentProperty(new QualifiedName(
						JdtCorePlugin.PLUGIN_ID, ServerClasspathContainer.MANIFEST_TIMESTAMP));
				return lastmodified == null
						|| file.getLocalTimeStamp() != Long.valueOf(lastmodified).longValue();
			}
			catch (CoreException e) {
				JdtCorePlugin.log(e);
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getComparisonID(IPath containerPath, IJavaProject project) {
		if (containerPath == null || project == null)
			return null;

		return containerPath.segment(0) + "/" + project.getPath().segment(0); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDescription(IPath containerPath, IJavaProject project) {
		return ServerClasspathContainer.CLASSPATH_CONTAINER_DESCRIPTION;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canUpdateClasspathContainer(IPath containerPath, IJavaProject project) {
		// always ok to modify the class path container
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void requestClasspathContainerUpdate(IPath containerPath, IJavaProject javaProject,
			IClasspathContainer containerSuggestion) throws CoreException {
		// Store source attachments and dismiss any other changes to the container
		ClasspathUtils.storeSourceAttachments(javaProject, containerSuggestion);

		// Schedule refresh of class path container
		ServerClasspathContainerUpdateJob.scheduleClasspathContainerUpdateJob(javaProject,
				BundleManifestManager.IMPORTS_CHANGED);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IClasspathContainer getFailureContainer(IPath containerPath, IJavaProject project) {
		// re-try in case something went wrong
		return null;
	}

}
