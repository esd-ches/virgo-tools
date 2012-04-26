/*******************************************************************************
 * Copyright (c) 2009, 2012 SpringSource, a divison of VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource, a division of VMware, Inc. - initial API and implementation
 *******************************************************************************/

package org.eclipse.virgo.ide.runtime.core.artefacts;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.eclipse.core.runtime.Assert;

/**
 * Safely encapsulates artefact interactions.
 * 
 * @author Miles Parker
 * 
 */
public class ArtefactSet implements IArtefactTyped {

	private final ArtefactType artefactType;

	private final Set<IArtefact> artefacts = new HashSet<IArtefact>();

	private final ArtefactRepository repository;

	public ArtefactSet(ArtefactRepository repository, ArtefactType artefactType) {
		this.repository = repository;
		this.artefactType = artefactType;
	}

	public Iterable<IArtefact> getArtefacts() {
		return artefacts;
	}

	public IArtefact[] toArray() {
		return artefacts.toArray(new IArtefact[] {});
	}

	public boolean add(IArtefact artefact) {
		Assert.isNotNull(artefact, "Tried to add null artefact.");
		if (artefact.getArtefactType() == artefactType || artefactType == ArtefactType.COMBINED) {
			return artefacts.add(artefact);
		}
		artefact.setSet(this);
		throw new RuntimeException("Tried to add non-matching artefact to " + artefactType.name() + ": " + artefact);
	}

	public ArtefactType getArtefactType() {
		return artefactType;
	}

	public ArtefactRepository getRepository() {
		return repository;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return artefactType.getPluralLabel();
	}

	/**
	 * Assumes that there is one and only one artefact set of a given type for
	 * each set.
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ArtefactSet) {
			ArtefactSet set = (ArtefactSet) obj;
			return (getArtefactType() == set.getArtefactType() && ObjectUtils.equals(	getRepository(),
																						set.getRepository()));
		}
		return super.equals(obj);
	}
}
