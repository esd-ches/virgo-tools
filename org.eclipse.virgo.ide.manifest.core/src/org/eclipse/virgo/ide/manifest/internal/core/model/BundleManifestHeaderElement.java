/*******************************************************************************
 * Copyright (c) 2009 - 2012 SpringSource, a divison of VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource, a division of VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.virgo.ide.manifest.internal.core.model;

import org.eclipse.osgi.util.ManifestElement;

/**
 * @author Christian Dupuis
 * @since 1.0.0
 */
/**
 * TODO CD add comments
 */
public class BundleManifestHeaderElement extends AbstractManifestElement {

	private static final int BUNDLE_MANIFEST_HEADER_ELEMENT_TYPE = 2;

	private ManifestElement manifestElement;
	
	public BundleManifestHeaderElement(BundleManifestHeader parent, ManifestElement manifestElement) {
		super(parent, manifestElement.toString());
		this.manifestElement = manifestElement;
	}

	public int getElementType() {
		return BUNDLE_MANIFEST_HEADER_ELEMENT_TYPE;
	}
	
	public ManifestElement getManifestElement() {
		return manifestElement;
	}
	
	@Override
	public String toString() {
		return new StringBuilder().append(manifestElement.getValue()).toString();
	}
	
}
