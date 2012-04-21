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

package org.eclipse.virgo.ide.bundlerepository.domain;

import java.io.File;

/**
 * 
 * @author Miles Parker
 *
 */
public class LocalArtefactSet extends ArtefactSet implements ILocalEntity {

	final File location;

	public LocalArtefactSet(ArtefactRepository artefactRepository, ArtefactType artefactType, File location) {
		super(artefactRepository, artefactType);
		this.location = location;
	}

	public File getFile() {
		return location;
	}
	
	/**
	 * @see org.eclipse.virgo.ide.bundlerepository.domain.ArtefactSet#toString()
	 */
	public String toString() {
		return location + " [" + super.toString() + "]";
	}
}
