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

import org.eclipse.core.runtime.Assert;
import org.eclipse.virgo.ide.bundlerepository.domain.OsgiVersion;
import org.eclipse.virgo.ide.bundlerepository.domain.PackageExport;
import org.eclipse.virgo.ide.bundlerepository.domain.PackageImport;

/**
 * Central element in the domain model - captures everything we know about a
 * bundle. This includes meta-data directly available in the bundle, as well as
 * additional information such as the license and any notes we want to display
 * in the BRITS web application
 * 
 * @author Christian Dupuis
 * @author Miles Parker
 */
public class BundleArtefact extends Artefact {

	/**
	 * The packages exported by the bundle
	 */
	private List<PackageExport> exports = new ArrayList<PackageExport>();

	/**
	 * The imports declared by the bundle
	 */
	private List<PackageImport> imports = new ArrayList<PackageImport>();

	/** for persistence use only */
	protected BundleArtefact() {
	}

	/**
	 * Construct a new BundleArtefact, for parameter descriptions see Artefact
	 * constructor javadoc
	 * 
	 * @param name
	 * @param symbolicName
	 * @param version
	 * @param organisationName
	 * @param moduleName
	 * @see Artefact
	 */
	public BundleArtefact(String name, String symbolicName, OsgiVersion version, String organisationName,
		String moduleName) {
		super(name, symbolicName, version, organisationName, moduleName);
	}

	/**
	 * Return the list of packages exported by this bundle
	 */
	public List<PackageExport> getExports() {
		return exports;
	}

	/**
	 * Add an export to the set of packages exported by this bundle
	 */
	public void addExport(PackageExport export) {
		Assert.isNotNull(export, "Tried to add null export");
		exports.add(export);
	}

	/**
	 * Set the list of packages exported by this bundle
	 */
	public void setExports(List<PackageExport> exports) {
		this.exports = exports;
	}

	/**
	 * Return the list of imports declared by this bundle
	 */
	public List<PackageImport> getImports() {
		return imports;
	}

	/**
	 * Add an import to the set of import declaration for this bundle
	 */
	public void addImport(PackageImport imp) {
		Assert.isNotNull(imp, "Tried to add null import");
		imports.add(imp);
	}

	/**
	 * Set the list of packages imported by this bundle
	 */
	public void setImports(List<PackageImport> imports) {
		this.imports = imports;
	}

	/**
	 * Return the relative URL path for downloading this bundle from S3
	 */
	@Override
	public String getRelativeUrlPath() {
		return ("/" + getOrganisationName() + "/" + getModuleName() + "/" + getVersion() + "/" + getModuleName() + "-"
			+ getVersion() + ".jar");
	}

	/**
	 * Return the relative URL path for downloading the source for this bundle
	 * from S3
	 */
	public String getRelativeSourceUrlPath() {
		return ("/" + getOrganisationName() + "/" + getModuleName() + "/" + getVersion() + "/" + getModuleName()
			+ "-sources-" + getVersion() + ".jar");
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Bundle-Name: ");
		builder.append(getName());
		builder.append("\n");
		builder.append("Bundle-SymbolicName: ");
		builder.append(getSymbolicName());
		builder.append("\n");
		builder.append("Bundle-Version: ");
		builder.append(getVersion());
		builder.append("\n");
		// including exports can cause a Hibernate lazy-load exception
		// builder.append("Export-Package: ");
		// builder.append("\n");
		// for (PackageExport export : this.exports) {
		// builder.append("  ");
		// builder.append(export);
		// builder.append("\n");
		// }
		return builder.toString();
	}

	/**
	 * Returns BUNDLE;
	 * 
	 * @see org.eclipse.virgo.ide.runtime.core.artefacts.IArtefact#getType()
	 */
	public ArtefactType getArtefactType() {
		return ArtefactType.BUNDLE;
	}
}
