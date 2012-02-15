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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IMarker;
import org.eclipse.osgi.util.ManifestElement;
import org.eclipse.osgi.util.NLS;
import org.eclipse.virgo.ide.manifest.core.BundleManifestCoreMessages;
import org.osgi.framework.BundleException;


/**
 * @author Christian Dupuis
 * @since 1.0.0
 */
/**
 * TODO CD add comments
 */
public class BundleManifestHeader extends AbstractManifestElement {

	private static final int BUNDLE_MANIFEST_HEADER_TYPE = 1;

	private int lineNumber;

	private int lines;

	private BundleManifestHeaderElement[] manifestElements;

	private String value;

	protected BundleManifestHeader(BundleManifest parent, String name, String value, int lineNumber) {
		super(parent, name);
		this.value = value;
		this.lineNumber = lineNumber;
		this.lines = 1;
	}

	public void append(String value) {
		this.value += value;
		lines++;
	}

	public BundleManifestHeaderElement[] getBundleManifestHeaderElements() {
		if (this.manifestElements == null) {
			init();
		}
		return this.manifestElements;
	}

	@Override
	public AbstractManifestElement[] getChildren() {
		return getBundleManifestHeaderElements();
	}

	public int getElementType() {
		return BUNDLE_MANIFEST_HEADER_TYPE;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public int getLinesSpan() {
		return lines;
	}

	public String getValue() {
		return value;
	}

	public void init() {
		if (this.manifestElements == null) {
			if (getValue().trim().length() > 0) {
				try {

					// Make sure the every " is closed
					if (StringUtils.countMatches(getValue(), "\"") % 2 != 0) {
						throw new BundleException("");
					}
					
					List<BundleManifestHeaderElement> headerElements = new ArrayList<BundleManifestHeaderElement>();
					ManifestElement[] elements = ManifestElement.parseHeader(getName(),
							getValue());
					for (ManifestElement element : elements) {
						headerElements.add(new BundleManifestHeaderElement(this, element));
					}
					this.manifestElements = headerElements
							.toArray(new BundleManifestHeaderElement[headerElements.size()]);
				}
				catch (BundleException be) {
					String message = NLS.bind(
							BundleManifestCoreMessages.BundleErrorReporter_parseHeader,
							getName());
					((BundleManifest) getParent()).error(IMarker.SEVERITY_ERROR, message,
							getLineNumber() + 1);
					this.manifestElements = new BundleManifestHeaderElement[0];
				}
			}
			else {
				this.manifestElements = new BundleManifestHeaderElement[0];
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[").append(lineNumber).append("] ").append(getName()).append(" <")
				.append(lines).append("> :\n");
		for (AbstractManifestElement element : getChildren()) {
			builder.append("   ").append(element.toString());
		}
		return builder.toString();
	}
}
