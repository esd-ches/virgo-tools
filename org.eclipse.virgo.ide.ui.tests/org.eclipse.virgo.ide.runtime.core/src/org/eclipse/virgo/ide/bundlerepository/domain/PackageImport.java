/*******************************************************************************
 * Copyright (c) 2007, 2009 SpringSource
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     SpringSource - initial API and implementation
 *******************************************************************************/
package org.eclipse.virgo.ide.bundlerepository.domain;

import org.eclipse.virgo.ide.runtime.core.provisioning.ArtefactRepositoryManager;

/**
 * Represent an import-package declaration for an OSGi bundle. Ignores matching attributes and only worries about
 * version range and optionality.
 * @author acolyer
 */
public class PackageImport {

	private byte[] name; // name of the package to be imported

	private boolean isOptional; // whether or not this import is optional

	private VersionRange importRange; // the range of version that are acceptable

	/** for persistence use only */
	protected PackageImport() {
	}

	/**
	 * Create a new package import.
	 * @param name the name of the package to be imported
	 * @param isOptional true if this is an optional import
	 * @param range the range of versions that are acceptable
	 */
	public PackageImport(String name, boolean isOptional, VersionRange range) {
		this.name = ArtefactRepositoryManager.convert(name);
		this.isOptional = isOptional;
		this.importRange = range;
	}

	public String getName() {
		return (name != null ? new String(name) : null);
	}

	public boolean isOptional() {
		return isOptional;
	}

	public VersionRange getImportRange() {
		return importRange;
	}

	/**
	 * Return true if this import can be satisfied by the given exported package
	 * @param pkg the candidate package for satisfying this import
	 */
	public boolean isSatisfiedBy(PackageExport pkg) {
		return (this.name.equals(pkg.getName()) && this.importRange.contains(pkg.getVersion()));
	}

}
