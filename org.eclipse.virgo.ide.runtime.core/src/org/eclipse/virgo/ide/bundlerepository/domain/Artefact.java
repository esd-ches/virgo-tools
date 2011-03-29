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
 * Represents an artefact in the repository :- bundle or library
 * @author adriancolyer
 * 
 */
public abstract class Artefact {

	private byte[] name; // human readable name of the artefact

	private byte[] symbolicName; // identifying name

	private OsgiVersion version; // artefact version

	private byte[] organisationName; // organisation name as defined in ivy.xml and used in artefact path

	private byte[] moduleName; // module name as defined in ivy.xml and used in artefact path

	private boolean sourceAvailable = false; // indicates if source code is available

	/** for persistence use only */
	protected Artefact() {
	}

	/**
	 * Construct a new artefact
	 * @param name human readable artefact name (e.g. "Spring Framework")
	 * @param symbolicName uniquely identifying name (e.g. "org.springframework")
	 * @param version version of the artefact, following OSGi Version semantics
	 * @param organisationName organisation name as defined in ivy.xml
	 * @param moduleName module name as defined in ivy.xml
	 */
	public Artefact(String name, String symbolicName, OsgiVersion version, String organisationName, String moduleName) {
		this.name = ArtefactRepositoryManager.convert(name);
		this.symbolicName = ArtefactRepositoryManager.convert(symbolicName);
		this.version = version;
		this.organisationName = ArtefactRepositoryManager.convert(organisationName);
		this.moduleName = ArtefactRepositoryManager.convert(moduleName);
	}

	/**
	 * The human-readable name of the artefact
	 */
	public String getName() {
		return (name != null ? new String(name) : null);
	}

	/**
	 * The symbolic name of the artefact
	 */
	public String getSymbolicName() {
		return (symbolicName != null ? new String(symbolicName) : null);
	}

	/**
	 * The version of the artefact
	 */
	public OsgiVersion getVersion() {
		return version;
	}

	/**
	 * The organisation name as used in ivy.xml (groupId name for maven)
	 */
	public String getOrganisationName() {
		return (organisationName != null ? new String(organisationName) : null);
	}

	/**
	 * The module name as used in ivy.xml (artefactId for maven)
	 */
	public String getModuleName() {
		return (moduleName != null ? new String(moduleName) : null);
	}

	/**
	 * Is source code available
	 */
	public boolean isSourceAvailable() {
		return this.sourceAvailable;
	}

	/**
	 * Set if source code is available
	 */
	public void setSourceAvailable(boolean sourceAvailable) {
		this.sourceAvailable = sourceAvailable;
	}

	/**
	 * Get the relative URL path for downloading this artefact from S3
	 */
	public abstract String getRelativeUrlPath();

	/**
	 * Get the relative URL path for displaying the license file for this artefact
	 */
	public String getRelativeLicenseUrlPath() {
		return "/" + getOrganisationName() + "/" + getModuleName() + "/" + getVersion() + "/license-" + getVersion()
				+ ".txt";
	}

}