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
package org.eclipse.virgo.ide.runtime.core.artefacts;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.virgo.ide.bundlerepository.domain.BundleImport;
import org.eclipse.virgo.ide.bundlerepository.domain.OsgiVersion;

/**
 * A library stored in BRITS. A library is a collection of bundles grouped under a common name, id, and version.
 * @author acolyer
 */
public class LibraryArtefact extends Artefact implements Comparable<LibraryArtefact> {

	/**
	 * The bundle imports declared by the library
	 */
	private List<BundleImport> bundleImports;

	/** for persistence use only */
	protected LibraryArtefact() {
	}

	/**
	 * Create a new LibraryArtefact. This will *not* be stored in the database unless explicitly stored using the
	 * ArtefactRepository. Storing a library artefact does *not* the bundles it contains - these must be stored in the
	 * database independently first.
	 * @param name human readable name of the library (e.g. "Spring Framework")
	 * @param symbolicName identifying name of the library (e.g. "org.springframework")
	 * @param version the library version, following normal OSGi conventions
	 * @param organisationName the organisation name as found in ivy.xml
	 * @param moduleName the module name as found in ivy.xml
	 * @param bundles the bundles that are part of this library
	 */
	public LibraryArtefact(String name, String symbolicName, OsgiVersion version, String organisationName,
			String moduleName) {
		super(name, symbolicName, version, organisationName, moduleName);
		this.bundleImports = new ArrayList<BundleImport>();
	}

	/**
	 * Add a new import to this library
	 * @param imp the import to add
	 */
	public void addBundleImport(BundleImport imp) {
		this.bundleImports.add(imp);
	}

	/**
	 * The set of bundles contained in this library.
	 */
	public List<BundleImport> getBundleImports() {
		return this.bundleImports;
	}

	/**
	 * The relative URL needed to download this library from S3
	 */
	@Override
	public String getRelativeUrlPath() {
		return ("/" + getOrganisationName() + "/" + getModuleName() + "/" + getVersion() + "/" + getModuleName() + "-"
				+ getVersion() + ".libd");
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Library-Name: ");
		builder.append(this.getName());
		builder.append("\n");
		builder.append("Library-SymbolicName: ");
		builder.append(this.getSymbolicName());
		builder.append("\n");
		builder.append("Library-Version: ");
		builder.append(this.getVersion());
		builder.append("\n");
		builder.append("Library bundles: \n");
		if (this.bundleImports != null) {
			for (BundleImport b : this.bundleImports) {
				builder.append("  ");
				builder.append(b.getSymbolicName());
				builder.append(" v.");
				builder.append(b.getVersionRange());
				builder.append("\n");
			}
		}
		return builder.toString();
	}

	/**
	 * Sort by symbolic name, and then reverse version. See the javadoc for the TreeSet class to understand the
	 * implications of this wrt. Set semantics. Since equality is based on id, and compareTo is based on symbolicName
	 * and version, full Set semantics when using LibraryArtefacts in a TreeSet are not guaranteed.
	 */
	public int compareTo(LibraryArtefact other) {
		if (this == other)
			return 0;
		if (other == null)
			return 0;
		if (this.getSymbolicName().equals(other.getSymbolicName())) {
			return other.getVersion().compareTo(this.getVersion());
		}
		else {
			return this.getSymbolicName().compareTo(other.getSymbolicName());
		}
	}

	/**
	 * Returns LIBRARY;
	 * @see org.eclipse.virgo.ide.runtime.core.artefacts.IArtefact#getType()
	 */
	public ArtefactType getArtefactType() {
		return ArtefactType.LIBRARY;
	}
}
