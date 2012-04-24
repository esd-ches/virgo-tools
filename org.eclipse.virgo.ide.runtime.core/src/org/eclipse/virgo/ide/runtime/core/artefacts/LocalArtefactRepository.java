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

import java.io.File;

import org.apache.commons.lang.ObjectUtils;

/**
 * 
 * @author Miles Parker
 *
 */
public class LocalArtefactRepository extends ArtefactRepository implements ILocalEntity {

	private final File file;

	public LocalArtefactRepository(File file) {
		this.file = file;
		bundles = createArtefactSet(ArtefactType.BUNDLE, file);
		libraries = createArtefactSet(ArtefactType.LIBRARY, file);
		allArtefacts = new LocalArtefactSet(this, ArtefactType.COMBINED, file);
	}

	protected ArtefactSet createArtefactSet(ArtefactType type, File file) {
		return new LocalArtefactSet(this, type, file) {
			@Override
			public boolean add(IArtefact artefact) {
				return super.add(artefact) && allArtefacts.add(artefact);
			}
		};
	}

	/**
	 * @see org.eclipse.virgo.ide.runtime.core.artefacts.ILocalEntity#getFile()
	 */
	public File getFile() {
		return file;
	}

	/**
	 * Assumes one and only one repository at each file.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object other) {
		if (other instanceof LocalArtefactRepository) {
			LocalArtefactRepository otherRepos = (LocalArtefactRepository) other;
			return ObjectUtils.equals(file, otherRepos.file);
		}
		return false;
	}
}
