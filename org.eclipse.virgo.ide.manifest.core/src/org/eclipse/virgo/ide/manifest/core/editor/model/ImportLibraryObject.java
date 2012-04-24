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
package org.eclipse.virgo.ide.manifest.core.editor.model;

import org.eclipse.osgi.util.ManifestElement;
import org.eclipse.pde.internal.core.ICoreConstants;
import org.eclipse.pde.internal.core.text.bundle.ManifestHeader;
import org.eclipse.pde.internal.core.text.bundle.PDEManifestElement;
import org.osgi.framework.Constants;

/**
 * @author Leo Dos Santos
 */
public class ImportLibraryObject extends PDEManifestElement {

	private static final long serialVersionUID = 1L;

	public ImportLibraryObject(ManifestHeader header, ManifestElement manifestElement) {
		super(header, manifestElement);
	}

	public ImportLibraryObject(ManifestHeader header, String value) {
		super(header, value);
	}

	public void setId(String id) {
		String old = getId();
		setValue(id);
		fHeader.update();
		firePropertyChanged(this, fHeader.getName(), old, id);
	}

	public String getId() {
		return getValue();
	}

	public void setVersion(String version) {
		String old = getVersion();
		// Reset the previous value
		setAttribute(Constants.VERSION_ATTRIBUTE, null);
		// Parse the version String into segments
		String[] values = ManifestElement.getArrayFromList(version);
		// If there are values, add them
		if ((values != null) && (values.length > 0)) {
			for (String element : values) {
				addAttribute(Constants.VERSION_ATTRIBUTE, element);
			}
		}
		fHeader.update();
		firePropertyChanged(this, Constants.VERSION_ATTRIBUTE, old, version);
	}

	public String getVersion() {
		String[] versionSegments = getAttributes(Constants.VERSION_ATTRIBUTE);
		StringBuffer version = new StringBuffer();
		if (versionSegments == null) {
			return null;
		} else if (versionSegments.length == 0) {
			return null;
		} else if (versionSegments.length == 1) {
			version.append(versionSegments[0]);
		} else if (versionSegments.length == 2) {
			version.append(versionSegments[0]);
			version.append(',');
			version.append(versionSegments[1]);
		}
		return version.toString();
	}

	public boolean isOptional() {
		return Constants.RESOLUTION_OPTIONAL.equals(getDirective(Constants.RESOLUTION_DIRECTIVE));

	}

	public void setOptional(boolean optional) {
		boolean old = isOptional();
		if (optional) {
			setDirective(Constants.RESOLUTION_DIRECTIVE, Constants.RESOLUTION_OPTIONAL);
		} else {
			setDirective(Constants.RESOLUTION_DIRECTIVE, null);
			setAttribute(ICoreConstants.OPTIONAL_ATTRIBUTE, null);
		}
		fHeader.update();
		firePropertyChanged(this, Constants.RESOLUTION_DIRECTIVE, Boolean.toString(old), Boolean.toString(optional));
	}

}
