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

import org.eclipse.virgo.ide.bundlerepository.domain.OsgiVersion;

/**
 * Represents an artefact in the repository :- bundle or library
 * 
 * @author adriancolyer
 * 
 */
public abstract class Artefact implements IArtefact {

	private byte[] name; // human readable name of the artefact

	private byte[] symbolicName; // identifying name

	private OsgiVersion version; // artefact version

	private byte[] organisationName; // organisation name as defined in ivy.xml
										// and used in artefact path

	private byte[] moduleName; // module name as defined in ivy.xml and used in
								// artefact path

	private boolean sourceAvailable = false; // indicates if source code is
												// available

	private ArtefactRepository repository;

	private ArtefactSet set;

	/** for persistence use only */
	protected Artefact() {
	}

	/**
	 * Construct a new artefact
	 * 
	 * @param name human readable artefact name (e.g. "Spring Framework")
	 * @param symbolicName uniquely identifying name (e.g.
	 *        "org.springframework")
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
	 * @see org.eclipse.virgo.ide.runtime.core.artefacts.IArtefact#getName()
	 */
	public String getName() {
		return (name != null ? new String(name) : null);
	}

	/**
	 * @see org.eclipse.virgo.ide.runtime.core.artefacts.IArtefact#getSymbolicName()
	 */
	public String getSymbolicName() {
		return (symbolicName != null ? new String(symbolicName) : null);
	}

	/**
	 * @see org.eclipse.virgo.ide.runtime.core.artefacts.IArtefact#getVersion()
	 */
	public OsgiVersion getVersion() {
		return version;
	}

	/**
	 * @see org.eclipse.virgo.ide.runtime.core.artefacts.IArtefact#getOrganisationName()
	 */
	public String getOrganisationName() {
		return (organisationName != null ? new String(organisationName) : null);
	}

	/**
	 * @see org.eclipse.virgo.ide.runtime.core.artefacts.IArtefact#getModuleName()
	 */
	public String getModuleName() {
		return (moduleName != null ? new String(moduleName) : null);
	}

	/**
	 * @see org.eclipse.virgo.ide.runtime.core.artefacts.IArtefact#isSourceAvailable()
	 */
	public boolean isSourceAvailable() {
		return this.sourceAvailable;
	}

	/**
	 * @see org.eclipse.virgo.ide.runtime.core.artefacts.IArtefact#setSourceAvailable(boolean)
	 */
	public void setSourceAvailable(boolean sourceAvailable) {
		this.sourceAvailable = sourceAvailable;
	}

	/**
	 * @see org.eclipse.virgo.ide.runtime.core.artefacts.IArtefact#getRelativeUrlPath()
	 */
	public abstract String getRelativeUrlPath();

	/**
	 * @see org.eclipse.virgo.ide.runtime.core.artefacts.IArtefact#getRelativeLicenseUrlPath()
	 */
	public String getRelativeLicenseUrlPath() {
		return "/" + getOrganisationName() + "/" + getModuleName() + "/" + getVersion() + "/license-" + getVersion()
			+ ".txt";
	}

	public String getSignature() {
		return getSymbolicName() + ";" + getVersion() + ";" + getArtefactType().getLabel();
	}

	/**
	 * @see org.eclipse.virgo.ide.runtime.core.artefacts.IArtefact#isMatch(org.eclipse.virgo.ide.runtime.core.artefacts.IArtefact)
	 */
	public boolean isMatch(IArtefact artefact) {
		return getSignature().equals(artefact.getSignature());
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object other) {
		if (other instanceof Artefact) {
			Artefact artefact = (Artefact) other;
			return isMatch(artefact) && sourceAvailable == artefact.sourceAvailable;
		}
		return false;
	}

	public ArtefactRepository getRepository() {
		return repository;
	}

	public void setRepository(ArtefactRepository repository) {
		this.repository = repository;
	}

	public ArtefactSet getSet() {
		return set;
	}

	public void setSet(ArtefactSet set) {
		this.set = set;
	}
}