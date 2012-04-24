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

import org.eclipse.virgo.ide.runtime.core.artefacts.ArtefactRepositoryManager;
import org.eclipse.virgo.ide.runtime.core.artefacts.BundleArtefact;

/**
 * A package exported by one or more bundles
 */
public class PackageExport {

	private byte[] name; // the fully-qualified package name

	private OsgiVersion version; // the version of the package as exported by
									// the owning bundle

	private Set<PackageMember> exports; // the contents of the exported package

	private BundleArtefact bundle; // the bundle exporting this package

	/* for persistence only */
	protected PackageExport() {
	}

	/**
	 * Create a new package exported by the given bundle, with the given name
	 * and version
	 * 
	 * @param bundle bundle exporting the package
	 * @param name the fully-qualified name of the package
	 * @param version the exported version of the package
	 */
	public PackageExport(BundleArtefact bundle, String name, OsgiVersion version) {
		this.bundle = bundle;
		this.name = ArtefactRepositoryManager.convert(name);
		this.version = version;
		this.exports = new HashSet<PackageMember>();
	}

	/**
	 * Return the bundle exporting this package
	 */
	public BundleArtefact getBundle() {
		return this.bundle;
	}

	/**
	 * Return the bundle exporting this package
	 */
	public void setBundle(BundleArtefact bundle) {
		this.bundle = bundle;
	}

	/**
	 * The fully-qualified name of the package
	 */
	public String getName() {
		return (name != null ? new String(name) : null);
	}

	/**
	 * The version of the package as exported by the owning bundle
	 */
	public OsgiVersion getVersion() {
		return this.version;
	}

	/**
	 * The set of classes and resources exported by the package
	 */
	public Set<PackageMember> getExports() {
		return this.exports;
	}

	/**
	 * Add a new class to the set of classes exported by this package
	 * 
	 * @param className the fully-qualified name of the class to add
	 */
	public void addClassExport(String className) {
		this.exports.add(new PackageMember(className, PackageMemberType.CLASS, this));
	}

	/**
	 * Add a new resource to the set of resources exported by this package
	 * 
	 * @param resourceName the full path to the resource in the exported package
	 */
	public void addResourceExport(String resourceName) {
		this.exports.add(new PackageMember(resourceName, PackageMemberType.RESOURCE, this));
	}
}
