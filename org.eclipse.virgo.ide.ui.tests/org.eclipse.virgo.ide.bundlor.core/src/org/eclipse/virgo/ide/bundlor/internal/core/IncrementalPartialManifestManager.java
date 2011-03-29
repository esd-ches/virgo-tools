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
package org.eclipse.virgo.ide.bundlor.internal.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.jdt.core.IJavaProject;

import org.eclipse.virgo.bundlor.support.partialmanifest.ReadablePartialManifest;

/**
 * Manages the {@link IncrementalReadablePartialManifest} instances by {@link IJavaProject}.
 * @author Christian Dupuis
 * @since 1.1.2
 */
public class IncrementalPartialManifestManager {

	/** Internal read write lock to protect the read and write operations of the internal caches */
	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

	/** Write lock to protect the model from concurrent write operations */
	private final Lock w = rwl.writeLock();

	/** Read lock to protect from reading while writing to the model resources */
	private final Lock r = rwl.readLock();

	/**
	 * Internal structure to associate {@link IncrementalReadablePartialManifest}s to {@link IJavaProject}s
	 */
	private Map<IJavaProject, ReadablePartialManifest> manifests = new ConcurrentHashMap<IJavaProject, ReadablePartialManifest>();

	/**
	 * Internal structure to associate {@link IncrementalReadablePartialManifest}s that represent the test manifests to
	 * {@link IJavaProject}s
	 */
	private Map<IJavaProject, ReadablePartialManifest> testManifests = new ConcurrentHashMap<IJavaProject, ReadablePartialManifest>();

	/**
	 * Returns an instance of {@link IncrementalReadablePartialManifest} for the given {@link IJavaProject}.
	 */
	public ReadablePartialManifest getPartialManifest(IJavaProject javaProject, boolean isTestManifest,
			boolean createNew) {
		if (!createNew) {
			try {
				r.lock();
				if (!isTestManifest) {
					if (manifests.containsKey(javaProject)) {
						return manifests.get(javaProject);
					}
				}
				else {
					if (testManifests.containsKey(javaProject)) {
						return testManifests.get(javaProject);
					}
				}
			}
			finally {
				r.unlock();
			}
		}
		try {
			w.lock();
			ReadablePartialManifest manifest = new IncrementalReadablePartialManifest();
			if (!isTestManifest) {
				manifests.put(javaProject, manifest);
			}
			else {
				testManifests.put(javaProject, manifest);
			}
			return manifest;
		}
		finally {
			w.unlock();
		}
	}

	/**
	 * Returns <code>true</code> if the given {@link IJavaProject} has {@link IncrementalReadablePartialManifest}
	 * associated with it.
	 */
	public boolean hasPartialManifest(IJavaProject javaProject) {
		try {
			r.lock();
			return manifests.containsKey(javaProject);
		}
		finally {
			r.unlock();
		}
	}

	/**
	 * Clears out existing manifest models.
	 * <p>
	 * Used form the UI to trigger a clean model creation on the next run.
	 */
	public void clearPartialManifest(IJavaProject javaProject) {
		try {
			w.lock();
			manifests.remove(javaProject);
			testManifests.remove(javaProject);
		}
		finally {
			w.unlock();
		}
	}

}
