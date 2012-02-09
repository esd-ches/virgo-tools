/*******************************************************************************
 * Copyright (c) 2007, 2012 SpringSource
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     SpringSource - initial API and implementation
 *******************************************************************************/
package org.eclipse.virgo.ide.bundlerepository.domain;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Christian Dupuis
 * @since 2.2.7
 */
public class ArtefactRepository {

	private Set<BundleArtefact> bundles = new HashSet<BundleArtefact>();

	private Set<LibraryArtefact> libraries = new HashSet<LibraryArtefact>();

	public Set<BundleArtefact> getBundles() {
		return bundles;
	}

	public void addBundle(BundleArtefact bundle) {
		this.bundles.add(bundle);
	}

	public Set<LibraryArtefact> getLibraries() {
		return libraries;
	}

	public void addLibrary(LibraryArtefact library) {
		this.libraries.add(library);
	}

}
