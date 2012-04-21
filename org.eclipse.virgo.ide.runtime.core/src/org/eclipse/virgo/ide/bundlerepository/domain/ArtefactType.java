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

/**
 * Types of artefacts that might exist within a repository.
 * 
 * @author Miles Parker
 * 
 */
public enum ArtefactType {
	BUNDLE("Bundle", "Bundles", 2), LIBRARY("Library", "Libraries", 3), COMBINED("Library Or Bundle",
			"Libraries and Bundles", 1);

	private final String label;
	private final int ordering;
	private final String pluralLabel;

	ArtefactType(String label, String pluralLabel, int ordering) {
		this.label = label;
		this.pluralLabel = pluralLabel;
		this.ordering = ordering;
	}

	public String getLabel() {
		return label;
	}

	public String getPluralLabel() {
		return pluralLabel;
	}

	public int getOrdering() {
		return ordering;
	}
}
