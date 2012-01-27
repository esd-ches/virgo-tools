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
package org.eclipse.virgo.ide.manifest.internal.core;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.virgo.ide.facet.core.FacetCorePlugin;
import org.eclipse.virgo.ide.facet.core.FacetUtils;
import org.eclipse.virgo.ide.manifest.core.BundleManifestCorePlugin;
import org.eclipse.virgo.ide.manifest.core.BundleManifestUtils;
import org.eclipse.virgo.ide.manifest.core.IBundleManifestChangeListener;
import org.eclipse.virgo.ide.manifest.core.IBundleManifestManager;
import org.eclipse.virgo.ide.manifest.core.IBundleManifestMangerWorkingCopy;
import org.eclipse.virgo.ide.par.Bundle;
import org.eclipse.virgo.ide.par.Par;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.util.osgi.manifest.ExportedPackage;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.java.JdtUtils;

/**
 * Default {@link IBundleManifestManager} implementation used to manage the life-cycle of the
 * {@link BundleManifest} instances.
 * @author Christian Dupuis
 * @since 1.0.0
 */
public class BundleManifestManager implements IBundleManifestMangerWorkingCopy {

	public static final Set<IBundleManifestChangeListener.Type> IMPORTS_CHANGED;

	static {
		IMPORTS_CHANGED = new HashSet<IBundleManifestChangeListener.Type>();
		IMPORTS_CHANGED.add(IBundleManifestChangeListener.Type.IMPORT_PACKAGE);
		IMPORTS_CHANGED.add(IBundleManifestChangeListener.Type.IMPORT_BUNDLE);
		IMPORTS_CHANGED.add(IBundleManifestChangeListener.Type.IMPORT_LIBRARY);
		IMPORTS_CHANGED.add(IBundleManifestChangeListener.Type.REQUIRE_BUNDLE);
	}

	/** Internal read write lock to protect the read and write operations of the internal caches */
	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

	/** Write lock to protect the model from concurrent write operations */
	private final Lock w = rwl.writeLock();

	/** Read lock to protect from reading while writing to the model resources */
	private final Lock r = rwl.readLock();

	/** Internal cache of {@link BundleManifest} keyed by {@link IJavaProject} */
	private Map<IJavaProject, BundleManifest> bundles = new ConcurrentHashMap<IJavaProject, BundleManifest>();

	/** Internal cache of {@link BundleManifest} keyed by {@link IJavaProject}; these represent the */
	private Map<IJavaProject, BundleManifest> testBundles = new ConcurrentHashMap<IJavaProject, BundleManifest>();

	/** Internal cache of resolved package imports keyed by {@link IJavaProject} */
	private Map<IJavaProject, Set<String>> packageImports = new ConcurrentHashMap<IJavaProject, Set<String>>();

	/** Internal listener list */
	private List<IBundleManifestChangeListener> bundleManifestChangeListeners = new CopyOnWriteArrayList<IBundleManifestChangeListener>();

	private Map<IJavaProject, Long> bundleTimestamps = new ConcurrentHashMap<IJavaProject, Long>();

	private Map<IJavaProject, Long> testBundleTimestamps = new ConcurrentHashMap<IJavaProject, Long>();

	/**
	 * Resource change listener that listens to changes of Eclipse file resources and triggers a
	 * refresh for corresponding class path container
	 */
	private IResourceChangeListener resourceChangeListener = null;

	/**
	 * {@inheritDoc}
	 */
	public BundleManifest getBundleManifest(IJavaProject javaProject) {
		try {
			r.lock();
			if (bundles.containsKey(javaProject)) {
				return bundles.get(javaProject);
			}
		}
		finally {
			r.unlock();
		}
		// Load and store the bundle if not found; loadBundleManifest will acquire write lock.
		return loadBundleManifest(javaProject, false);
	}

	/**
	 * {@inheritDoc}
	 */
	public BundleManifest getTestBundleManifest(IJavaProject javaProject) {
		try {
			r.lock();
			if (testBundles.containsKey(javaProject)) {
				return testBundles.get(javaProject);
			}
		}
		finally {
			r.unlock();
		}
		// Load and store the bundle if not found; loadBundleManifest will acquire write lock.
		return loadBundleManifest(javaProject, true);
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<String> getPackageExports(IJavaProject javaProject) {
		try {
			r.lock();
			if (bundles.containsKey(javaProject)) {
				BundleManifest bundleManifest = bundles.get(javaProject);
				if (bundleManifest.getExportPackage() != null
						&& bundleManifest.getExportPackage().getExportedPackages() != null) {
					Set<String> packageExports = new LinkedHashSet<String>();
					for (ExportedPackage packageExport : bundleManifest.getExportPackage()
							.getExportedPackages()) {
						packageExports.add(packageExport.getPackageName());
					}
					return packageExports;
				}
			}
			return Collections.emptySet();
		}
		finally {
			r.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<String> getResolvedPackageImports(IJavaProject javaProject) {
		try {
			r.lock();
			if (packageImports.containsKey(javaProject)) {
				return packageImports.get(javaProject);
			}
			return Collections.emptySet();
		}
		finally {
			r.unlock();
		}
	}

	/**
	 * Starts the model.
	 */
	public void start() {
		this.resourceChangeListener = new ManifestResourceChangeListener();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceChangeListener);
	}

	/**
	 * Stops the model.
	 */
	public void stop() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceChangeListener);
		bundles = null;
		packageImports = null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void updateResolvedPackageImports(IJavaProject javaProject,
			Set<String> resolvedPackageImports) {
		try {
			w.lock();
			packageImports.put(javaProject, resolvedPackageImports);
		}
		finally {
			w.unlock();
		}
	}

	/**
	 * Internal method to locate and load {@link BundleManifest}s for given {@link IJavaProject}.
	 * @param testBundle
	 */
	private BundleManifest loadBundleManifest(IJavaProject javaProject, boolean testBundle) {
		IFile manifestFile = BundleManifestUtils.locateManifest(javaProject, testBundle);
		BundleManifest oldBundleManifest = null;

		Map<IJavaProject, BundleManifest> manifests = (testBundle ? testBundles : bundles);
		Map<IJavaProject, Long> timestamps = (testBundle ? testBundleTimestamps : bundleTimestamps);

		if (manifestFile == null) {
			try {
				w.lock();
				manifests.remove(javaProject);
				timestamps.remove(javaProject);
				bundleManifestChanged(null, null, null, null, IMPORTS_CHANGED, javaProject);
			}
			finally {
				w.unlock();
			}
		}

		if (manifestFile != null) {
			r.lock();
			try {
				if (timestamps.containsKey(javaProject)) {
					Long oldTimestamp = timestamps.get(javaProject);
					oldBundleManifest = manifests.get(javaProject);
					if (oldTimestamp.longValue() >= manifestFile.getLocalTimeStamp()) {
						return oldBundleManifest;
					}
				}
			}
			finally {
				r.unlock();
			}
			BundleManifest bundleManifest = BundleManifestUtils.getBundleManifest(javaProject,
					testBundle);
			try {
				w.lock();
				if (bundleManifest != null) {
					manifests.put(javaProject, bundleManifest);
				}
				else {
					manifests.remove(javaProject);
				}
				timestamps.put(javaProject, manifestFile.getLocalTimeStamp());
			}
			finally {
				w.unlock();
			}
			if (testBundle) {
				bundleManifestChanged(null, null, bundleManifest, oldBundleManifest, javaProject);
			}
			else {
				bundleManifestChanged(bundleManifest, oldBundleManifest, null, null, javaProject);
			}
			return bundleManifest;
		}
		return null;
	}

	/**
	 * Triggers an update to the saved {@link BundleManifest} for the given <code>javaProject</code>
	 */
	private void updateBundleManifest(IJavaProject javaProject, boolean testBundle) {
		// Re-load the bundle manifest
		loadBundleManifest(javaProject, testBundle);
	}

	/**
	 * {@inheritDoc}
	 */
	public void addBundleManifestChangeListener(
			IBundleManifestChangeListener bundleManifestChangeListener) {
		this.bundleManifestChangeListeners.add(bundleManifestChangeListener);
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeBundleManifestChangeListener(
			IBundleManifestChangeListener bundleManifestChangeListener) {
		if (bundleManifestChangeListeners.contains(bundleManifestChangeListener)) {
			bundleManifestChangeListeners.remove(bundleManifestChangeListener);
		}
	}

	/**
	 * Notifies {@link IBundleManifestChangeListener} of changes.
	 */
	private void bundleManifestChanged(BundleManifest newBundleManifest,
			BundleManifest oldBundleManifest, BundleManifest newTestBundleManifest,
			BundleManifest oldTestBundleManifest, IJavaProject javaProject) {
		Set<IBundleManifestChangeListener.Type> types = new HashSet<IBundleManifestChangeListener.Type>();
		types.addAll(BundleManifestDiffer.diff(newBundleManifest, oldBundleManifest));
		types.addAll(BundleManifestDiffer.diff(newTestBundleManifest, oldTestBundleManifest));
		bundleManifestChanged(newBundleManifest, oldBundleManifest, newTestBundleManifest,
				oldTestBundleManifest, types, javaProject);
	}

	/**
	 * Notifies {@link IBundleManifestChangeListener} of changes.
	 */
	private void bundleManifestChanged(BundleManifest newBundleManifest,
			BundleManifest oldBundleManifest, BundleManifest newTestBundleManifest,
			BundleManifest oldTestBundleManifest, Set<IBundleManifestChangeListener.Type> types,
			IJavaProject javaProject) {
		for (IBundleManifestChangeListener listener : bundleManifestChangeListeners) {
			listener.bundleManifestChanged(newBundleManifest, oldBundleManifest,
					newTestBundleManifest, oldTestBundleManifest, types, javaProject);
		}
	}

	/**
	 * {@link IResourceChangeListener} that listens to changes to the MANIFEST.MF resources.
	 */
	class ManifestResourceChangeListener implements IResourceChangeListener {

		/**
		 * Internal resource delta visitor.
		 */
		protected class ManifestResourceVisitor implements IResourceDeltaVisitor {

			protected int eventType;

			public ManifestResourceVisitor(int eventType) {
				this.eventType = eventType;
			}

			public final boolean visit(IResourceDelta delta) throws CoreException {
				IResource resource = delta.getResource();
				switch (delta.getKind()) {
				case IResourceDelta.ADDED:
					return resourceAdded(resource);

				case IResourceDelta.OPEN:
					return resourceOpened(resource);

				case IResourceDelta.CHANGED:
					return resourceChanged(resource, delta.getFlags());

				case IResourceDelta.REMOVED:
					return resourceRemoved(resource);
				}
				return true;
			}

			protected boolean resourceAdded(IResource resource) {
				return true;
			}

			protected boolean resourceChanged(IResource resource, int flags) {
				if (resource instanceof IFile) {
					if ((flags & IResourceDelta.CONTENT) != 0) {
						if ((FacetUtils.isBundleProject(resource) || SpringCoreUtils
								.hasProjectFacet(resource, FacetCorePlugin.WEB_FACET_ID))) {
							if (SpringCoreUtils.isManifest(resource)) {
								updateBundleManifest(JavaCore.create(resource.getProject()), false);
							}
							else if (isTestManifest(resource)) {
								updateBundleManifest(JavaCore.create(resource.getProject()), true);
							}
							else if (resource.getName().equals(".classpath") && resource.getParent() instanceof IProject) {
								updateBundleManifest(JavaCore.create(resource.getProject()), false);
								updateBundleManifest(JavaCore.create(resource.getProject()), true);
							}
						}

						if (resource.getName().equals(
								"org.eclipse.wst.common.project.facet.core.xml")
								|| resource.getName().equals(
										"org.eclipse.virgo.ide.runtime.core.par.xml")) {
							// target of a par project or par bundles has changed
							if (FacetUtils.isParProject(resource)) {
								Par par = FacetUtils.getParDefinition(resource.getProject());
								if (par != null && par.getBundle() != null) {
									for (Bundle bundle : par.getBundle()) {
										IProject bundleProject = ResourcesPlugin.getWorkspace()
												.getRoot().getProject(bundle.getSymbolicName());
										if (FacetUtils.isBundleProject(bundleProject)) {
											updateBundleManifestForResource(bundleProject);
										}
									}
								}
							}
							// target of bundle project could have changed
							else if (FacetUtils.isBundleProject(resource)) {
								updateBundleManifestForResource(resource);
							}
						}
					}
					return false;
				}
				return true;
			}

			private void updateBundleManifestForResource(IResource resource) {
				IJavaProject javaProject = JavaCore.create(resource.getProject());
				BundleManifest bundleManifest = getBundleManifest(javaProject);
				BundleManifest testBundleManifest = getTestBundleManifest(javaProject);
				bundleManifestChanged(bundleManifest, bundleManifest, testBundleManifest,
						testBundleManifest, IMPORTS_CHANGED, javaProject);
			}

			protected boolean resourceOpened(IResource resource) {
				return true;
			}

			protected boolean resourceRemoved(IResource resource) {
				if (SpringCoreUtils.isManifest(resource)) {
					updateBundleManifest(JavaCore.create(resource.getProject()), false);
				}
				else if (isTestManifest(resource)) {
					updateBundleManifest(JavaCore.create(resource.getProject()), true);
				}
				return true;
			}
		}

		public static final int LISTENER_FLAGS = IResourceChangeEvent.PRE_CLOSE
				| IResourceChangeEvent.PRE_DELETE | IResourceChangeEvent.PRE_BUILD;

		private static final int VISITOR_FLAGS = IResourceDelta.ADDED | IResourceDelta.CHANGED
				| IResourceDelta.REMOVED;

		public void resourceChanged(IResourceChangeEvent event) {
			if (event.getSource() instanceof IWorkspace) {
				int eventType = event.getType();
				switch (eventType) {
				case IResourceChangeEvent.POST_CHANGE:
					IResourceDelta delta = event.getDelta();
					if (delta != null) {
						try {
							delta.accept(getVisitor(eventType), VISITOR_FLAGS);
						}
						catch (CoreException e) {
							StatusManager.getManager().handle(new Status(IStatus.ERROR, BundleManifestCorePlugin.PLUGIN_ID, "Error while traversing resource change delta", e));
						}
					}
					break;
				}
			}
			else if (event.getSource() instanceof IProject) {
				int eventType = event.getType();
				switch (eventType) {
				case IResourceChangeEvent.POST_CHANGE:
					IResourceDelta delta = event.getDelta();
					if (delta != null) {
						try {
							delta.accept(getVisitor(eventType), VISITOR_FLAGS);
						}
						catch (CoreException e) {
							StatusManager.getManager().handle(new Status(IStatus.ERROR, BundleManifestCorePlugin.PLUGIN_ID, "Error while traversing resource change delta", e));
						}
					}
					break;
				}
			}

		}

		protected IResourceDeltaVisitor getVisitor(int eventType) {
			return new ManifestResourceVisitor(eventType);
		}

		public boolean isTestManifest(IResource resource) {
			return resource != null
					&& resource.isAccessible()
					&& resource.getType() == IResource.FILE
					&& resource.getName().equals("TEST.MF")
					&& resource.getParent() != null
					&& resource.getParent().getProjectRelativePath().lastSegment().equals(
							"META-INF");
		}

	}

}
