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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.virgo.ide.jdt.core.JdtCorePlugin;
import org.eclipse.virgo.ide.manifest.core.BundleManifestUtils;
import org.eclipse.virgo.ide.manifest.internal.core.model.BundleManifest;
import org.eclipse.virgo.kernel.osgi.provisioning.tools.DependencyLocationException;
import org.eclipse.virgo.kernel.osgi.provisioning.tools.ImportDescriptor;

 
/**
 * Helper class that handles creation and deletion of {@link IMarker} instances.
 * @author Christian Dupuis
 * @since 1.0.0
 */
public class MarkerUtils {

	/**
	 * Removes all bundle resolution error markers from a given <code>javaProject</code>.
	 */
	public static void removeErrorMarkers(IJavaProject javaProject, boolean testBundle) {
		IResource manifest = BundleManifestUtils.locateManifest(javaProject, testBundle);
		org.springframework.ide.eclipse.core.MarkerUtils.deleteMarkers(manifest,
				JdtCorePlugin.DEPENDENCY_PROBLEM_MARKER_ID);
	}

	/**
	 * Creates bundle resolution dependency error based on the resolution results stored in the
	 * {@link DependencyLocationException}.
	 */
	public static void createErrorMarkers(final DependencyLocationException e,
			final IJavaProject javaProject, final boolean testBundle) {
		final IResource manifest = BundleManifestUtils.locateManifest(javaProject, testBundle);

		// Some sanity check in case this error is reported too early
		if (manifest == null) {
			return;
		}

		final BundleManifest bundleManifest = new BundleManifest((IFile) manifest);
		final IDocument document = bundleManifest.getDocument();

		// Schedule marker creation in different job as it requires workspace lock
		Job markerCreationJob = new Job("Managing markers on resource '"
				+ manifest.getFullPath().toString() + "'") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {

				// Clear out old error markers
				removeErrorMarkers(javaProject, testBundle);

				if (e == null) {
					return Status.OK_STATUS;
				}

				if (e.getUnsatisfiablePackageImports() != null) {
					for (ImportDescriptor desc : e.getUnsatisfiablePackageImports()) {
						int lineNumber = BundleManifestUtils.getLineNumber(document, bundleManifest
								.getHeader("Import-Package"), desc.getName());
						createProblemMarker(manifest,
								MarkerConstants.MISSING_DEPENDENCY_KIND_IMPORT_PACKAGE, desc
										.getName(), desc.getParseVersion(), new StringBuilder(
										"Import-Package: ").append(desc.getName()).append(" ")
										.append(desc.getVersion()).append(" could not be resolved")
										.toString(), lineNumber, IMarker.SEVERITY_ERROR);
					}
				}
				if (e.getUnsatisfiableLibraryImports() != null) {
					for (ImportDescriptor desc : e.getUnsatisfiableLibraryImports()) {
						int lineNumber = BundleManifestUtils.getLineNumber(document, bundleManifest
								.getHeader("Import-Library"), desc.getName());
						createProblemMarker(manifest,
								MarkerConstants.MISSING_DEPENDENCY_KIND_IMPORT_LIBRARY, desc
										.getName(), desc.getParseVersion(), new StringBuilder(
										"Import-Library: ").append(desc.getName()).append(" ")
										.append(desc.getVersion()).append(" could not be resolved")
										.toString(), lineNumber, IMarker.SEVERITY_ERROR);
					}
				}
				if (e.getUnsatisfiableRequireBundle() != null) {
					for (ImportDescriptor desc : e.getUnsatisfiableRequireBundle()) {
						int lineNumber = BundleManifestUtils.getLineNumber(document, bundleManifest
								.getHeader("Require-Bundle"), desc.getName());
						createProblemMarker(manifest,
								MarkerConstants.MISSING_DEPENDENCY_KIND_REQUIRE_BUNDLE, desc
										.getName(), desc.getParseVersion(), new StringBuilder(
										"Require-Bundle: ").append(desc.getName()).append(" ")
										.append(desc.getVersion()).append(" could not be resolved")
										.toString(), lineNumber, IMarker.SEVERITY_ERROR);
					}
				}
				if (e.getUnsatisfiableBundleImports() != null) {
					for (ImportDescriptor desc : e.getUnsatisfiableBundleImports()) {
						int lineNumber = BundleManifestUtils.getLineNumber(document, bundleManifest
								.getHeader("Import-Bundle"), desc.getName());
						createProblemMarker(manifest,
								MarkerConstants.MISSING_DEPENDENCY_KIND_IMPORT_BUNDLE, desc
										.getName(), desc.getParseVersion(), new StringBuilder(
										"Import-Bundle: ").append(desc.getName()).append(" ")
										.append(desc.getVersion()).append(" could not be resolved")
										.toString(), lineNumber, IMarker.SEVERITY_ERROR);
					}
				}
				return Status.OK_STATUS;
			}

		};

		markerCreationJob.setPriority(Job.BUILD);
		markerCreationJob.setRule(ResourcesPlugin.getWorkspace().getRuleFactory().buildRule());
		markerCreationJob.schedule();
	}

	/**
	 * Creates a single problem marker.
	 */
	public static void createProblemMarker(IResource resource, String message, int lineNumber,
			int severity) {
		createProblemMarker(resource, null, null, null, message, lineNumber, severity);
	}

	public static void createProblemMarker(IResource resource, String missingDependencyKind,
			String missingDependency, String missingDependencyVersion, String message,
			int lineNumber, int severity) {

		if (resource != null && resource.isAccessible()) {
			try {

				// First check if specified marker already exists
				IMarker[] markers = resource.findMarkers(
						JdtCorePlugin.DEPENDENCY_PROBLEM_MARKER_ID, false, IResource.DEPTH_ZERO);
				for (IMarker marker : markers) {
					int line = marker.getAttribute(IMarker.LINE_NUMBER, -1);
					if (line == lineNumber) {
						String msg = marker.getAttribute(IMarker.MESSAGE, "");
						if (msg.equals(message)) {
							return;
						}
					}
				}

				// Create new marker
				IMarker marker = resource.createMarker(JdtCorePlugin.DEPENDENCY_PROBLEM_MARKER_ID);
				Map<String, Object> attributes = new HashMap<String, Object>();
				attributes.put(IMarker.MESSAGE, message);
				attributes.put(IMarker.SEVERITY, severity);
				attributes.put(IMarker.LINE_NUMBER, lineNumber);
				if (null != missingDependency) {
					attributes.put(MarkerConstants.MISSING_DEPENDENCY_KEY, missingDependency);
				}
				if (null != missingDependencyVersion) {
					attributes.put(MarkerConstants.MISSING_DEPENDENCY_VERSION_KEY,
							missingDependencyVersion);
				}
				if (null != missingDependencyKind) {
					attributes.put(MarkerConstants.MISSING_DEPENDENCY_KIND_KEY,
							missingDependencyKind);
				}
				marker.setAttributes(attributes);
			}
			catch (CoreException e) {
				//TODO Should we rethrow this?
				StatusManager.getManager()
				.handle(new Status(IStatus.ERROR, JdtCorePlugin.PLUGIN_ID, "Couldn't create problem markers", e));
			}
		}

	}

}
