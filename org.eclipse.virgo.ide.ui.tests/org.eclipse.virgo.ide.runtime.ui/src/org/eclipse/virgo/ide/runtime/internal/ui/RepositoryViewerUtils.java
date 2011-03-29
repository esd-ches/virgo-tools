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
package org.eclipse.virgo.ide.runtime.internal.ui;

import java.io.File;
import java.util.Set;

import org.eclipse.virgo.ide.bundlerepository.domain.BundleArtefact;
import org.eclipse.virgo.ide.bundlerepository.domain.LibraryArtefact;


/**
 * @author Christian Dupuis
 */
public interface RepositoryViewerUtils {
	
	public class Bundles {

		final Set<BundleArtefact> bundles;

		public Bundles(Set<BundleArtefact> bundles) {
			this.bundles = bundles;
		}

		public Set<BundleArtefact> getBundles() {
			return bundles;
		}

	}

	public class LocationAwareBundles extends Bundles {

		final File location;

		public LocationAwareBundles(Set<BundleArtefact> bundles, File location) {
			super(bundles);
			this.location = location;
		}

		public File getLocation() {
			return location;
		}

	}

	public class Libraries {

		final Set<LibraryArtefact> libraries;

		public Libraries(Set<LibraryArtefact> libraries) {
			this.libraries = libraries;
		}

		public Set<LibraryArtefact> getLibraries() {
			return libraries;
		}

	}

	public class LocationAwareLibraries extends Libraries {

		private final File location;

		public LocationAwareLibraries(Set<LibraryArtefact> libraries, File location) {
			super(libraries);
			this.location = location;
		}

		public File getLocation() {
			return location;
		}

	}

}
