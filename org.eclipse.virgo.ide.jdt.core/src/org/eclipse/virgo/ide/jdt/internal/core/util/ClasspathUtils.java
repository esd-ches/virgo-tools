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
package org.eclipse.virgo.ide.jdt.internal.core.util;

import java.io.File;
import java.util.Arrays;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.virgo.ide.facet.core.FacetUtils;
import org.eclipse.virgo.ide.jdt.core.JdtCorePlugin;
import org.eclipse.virgo.ide.jdt.internal.core.classpath.ServerClasspathContainer;
import org.eclipse.virgo.ide.jdt.internal.core.classpath.ServerClasspathContainerUpdateJob;
import org.eclipse.virgo.ide.manifest.core.BundleManifestCorePlugin;
import org.eclipse.virgo.ide.manifest.core.BundleManifestUtils;
import org.eclipse.virgo.ide.manifest.core.IBundleManifestChangeListener;
import org.eclipse.virgo.ide.manifest.internal.core.BundleManifestManager;
import org.springframework.ide.eclipse.core.SpringCorePreferences;

import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.util.osgi.manifest.ImportedPackage;
import org.eclipse.virgo.util.osgi.manifest.RequiredBundle;

/**
 * Helper methods to be used to within the class path container infrastructure.
 * @author Christian Dupuis
 * @since 1.0.0
 */
public class ClasspathUtils {

	/**
	 * Adjusts the last modified timestamp on the source root and META-INF directory to reflect the
	 * same date as the MANIFEST.MF.
	 * <p>
	 * Note: this is a requirement for the dependency resolution.
	 */
	public static void adjustLastModifiedDate(IJavaProject javaProject, boolean testBundle) {
		File manifest = BundleManifestUtils.locateManifestFile(javaProject, testBundle);
		if (manifest != null && manifest.canRead()) {
			long lastmodified = manifest.lastModified();
			File metaInfFolder = manifest.getParentFile();
			if (metaInfFolder != null
					&& metaInfFolder.getName()
							.equals(BundleManifestCorePlugin.MANIFEST_FOLDER_NAME)
					&& metaInfFolder.canWrite()) {
				metaInfFolder.setLastModified(lastmodified);
				File srcFolder = metaInfFolder.getParentFile();
				if (srcFolder != null && srcFolder.canWrite()) {
					srcFolder.setLastModified(lastmodified);
				}
			}
		}
	}

	/**
	 * Returns <code>true</code> if the given project has the bundle dependency classpath container
	 * installed.
	 */
	public static boolean hasClasspathContainer(IJavaProject javaProject) {
		boolean hasContainer = false;
		try {
			for (IClasspathEntry entry : javaProject.getRawClasspath()) {
				if (entry.getPath().equals(ServerClasspathContainer.CLASSPATH_CONTAINER_PATH)) {
					hasContainer = true;
					break;
				}
			}
		}
		catch (JavaModelException e) {
			JdtCorePlugin.log(e);
		}
		return hasContainer;
	}

	/**
	 * Returns the {@link ServerClasspathContainer} for the given <code>javaProject</code>.
	 * <p>
	 * This method returns <code>null</code> if no appropriate class path container could be found
	 * on the given project.
	 */
	public static ServerClasspathContainer getClasspathContainer(IJavaProject javaProject) {
		try {
			if (hasClasspathContainer(javaProject)) {
				IClasspathContainer container = JavaCore.getClasspathContainer(
						ServerClasspathContainer.CLASSPATH_CONTAINER_PATH, javaProject);
				if (container instanceof ServerClasspathContainer) {
					return (ServerClasspathContainer) container;
				}
			}
		}
		catch (JavaModelException e) {
			JdtCorePlugin.log(e);
		}
		return null;
	}

	/**
	 * Returns configured source attachment paths for a given jar resource path.
	 * @param project the java project which preferences needs to be checked.
	 * @param file the jar that needs a source attachment
	 */
	public static IPath getSourceAttachment(IJavaProject project, File file) {
		SpringCorePreferences prefs = SpringCorePreferences.getProjectPreferences(project
				.getProject(), JdtCorePlugin.PLUGIN_ID);
		String value = prefs.getString("source.attachment-" + file.getName(), null);
		if (value != null) {
			return new Path(value);
		}
		return null;
	}

	/**
	 * Stores the configured source attachments paths in the projects settings area.
	 * @param project the java project to store the preferences for
	 * @param containerSuggestion the configured classpath container entries
	 */
	public static void storeSourceAttachments(IJavaProject project,
			IClasspathContainer containerSuggestion) {
		SpringCorePreferences prefs = SpringCorePreferences.getProjectPreferences(project
				.getProject(), JdtCorePlugin.PLUGIN_ID);
		for (IClasspathEntry entry : containerSuggestion.getClasspathEntries()) {
			IPath path = entry.getPath();
			IPath sourcePath = entry.getSourceAttachmentPath();
			if (sourcePath != null) {
				prefs.putString("source.attachment-" + path.lastSegment().toString(), sourcePath
						.toString());
			}
		}
	}

	/**
	 * Updates the class path container on the given <code>javaProject</code>.
	 */
	public static IStatus updateClasspathContainer(IJavaProject javaProject,
			Set<IBundleManifestChangeListener.Type> types, IProgressMonitor monitor) {
		try {

			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}

			// only update if something relevant happened
			if (types.contains(IBundleManifestChangeListener.Type.IMPORT_BUNDLE)
					|| types.contains(IBundleManifestChangeListener.Type.IMPORT_LIBRARY)
					|| types.contains(IBundleManifestChangeListener.Type.IMPORT_PACKAGE)
					|| types.contains(IBundleManifestChangeListener.Type.REQUIRE_BUNDLE)) {

				IClasspathContainer oldContainer = ClasspathUtils
						.getClasspathContainer(javaProject);
				if (oldContainer != null) {
					ServerClasspathContainer container = new ServerClasspathContainer(javaProject);

					container.refreshClasspathEntries();
					if (!Arrays.deepEquals(oldContainer.getClasspathEntries(), container
							.getClasspathEntries())) {
						JavaCore.setClasspathContainer(
								ServerClasspathContainer.CLASSPATH_CONTAINER_PATH,
								new IJavaProject[] { javaProject },
								new IClasspathContainer[] { container }, monitor);
					}
				}
			}

			if (types.contains(IBundleManifestChangeListener.Type.EXPORT_PACKAGE)) {
				// Always try to update the depending projects because MANIFEST might have changed
				// without changes to the project's own class path
				updateClasspathContainerForDependingBundles(javaProject);
			}

		}
		catch (JavaModelException e) {
			JdtCorePlugin.log(e);
		}
		return Status.OK_STATUS;
	}

	/**
	 * Triggers an class path container update on depending {@link IJavaProject}s.
	 */
	private static void updateClasspathContainerForDependingBundles(IJavaProject javaProject) {

		BundleManifest updatedBundleManifest = BundleManifestCorePlugin.getBundleManifestManager()
				.getBundleManifest(javaProject);
		Set<String> updatedPackageExports = BundleManifestCorePlugin.getBundleManifestManager()
				.getPackageExports(javaProject);

		if (updatedBundleManifest != null && updatedBundleManifest.getBundleSymbolicName() != null) {
			String bundleSymbolicName = updatedBundleManifest.getBundleSymbolicName()
					.getSymbolicName();
			for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
				try {

					// Only dm server bundle projects are of interest
					if (!javaProject.getProject().equals(project)
							&& FacetUtils.isBundleProject(project)) {

						IJavaProject jp = JavaCore.create(project);
						BundleManifest manifest = BundleManifestCorePlugin
								.getBundleManifestManager().getBundleManifest(jp);

						boolean refreshClasspath = false;
						if (manifest != null) {

							// Check for Require-Bundle dependency
							if (manifest.getRequireBundle() != null) {
								for (RequiredBundle requiredBundle : manifest.getRequireBundle()
										.getRequiredBundles()) {
									if (bundleSymbolicName != null
											&& bundleSymbolicName.equals(requiredBundle
													.getBundleSymbolicName())) {
										refreshClasspath = true;
										break;
									}
								}
							}

							// Check for export -> import package dependency
							if (manifest.getImportPackage() != null) {
								for (ImportedPackage packageImport : manifest.getImportPackage()
										.getImportedPackages()) {
									if (updatedPackageExports.contains(packageImport
											.getPackageName())) {
										refreshClasspath = true;
										break;
									}
								}
							}

							// Check for explicit project dependencies
							for (String requiredProjectName : jp.getRequiredProjectNames()) {
								if (requiredProjectName.equals(javaProject.getElementName())) {
									refreshClasspath = true;
									break;
								}
							}

							// Schedule class path container update
							if (refreshClasspath) {
								ServerClasspathContainerUpdateJob
										.scheduleClasspathContainerUpdateJob(jp,
												BundleManifestManager.IMPORTS_CHANGED);
							}
						}
					}
				}
				catch (Exception e) {
					JdtCorePlugin.log(e);
				}
			}
		}
	}
}
