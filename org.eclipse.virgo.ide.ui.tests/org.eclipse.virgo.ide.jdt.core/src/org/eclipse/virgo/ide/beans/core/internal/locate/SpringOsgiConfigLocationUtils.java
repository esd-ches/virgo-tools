/*******************************************************************************
 * Copyright (c) 2009 SpringSource, a divison of VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource, a division of VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.virgo.ide.beans.core.internal.locate;

import java.util.Dictionary;

import org.springframework.util.StringUtils;

/**
 * Utility class for dealing with the extender configuration and OSGi bundle manifest headers. Defines Spring/OSGi
 * constants and methods for configuring Spring application context.
 * @author Christian Dupuis
 * @author Costin Leau
 */
public abstract class SpringOsgiConfigLocationUtils {

	public static final String CONFIG_WILDCARD = "*";

	/**
	 * Manifest entry name for configuring Spring application context.
	 */
	public static final String SPRING_CONTEXT_HEADER = "Spring-Context";

	public static final String DIRECTIVE_SEPARATOR = ";";

	public static final String CONTEXT_LOCATION_SEPARATOR = ",";

	/**
	 * Return the {@value #SPRING_CONTEXT_HEADER} if present from the given dictionary.
	 */
	public static String getSpringContextHeader(Dictionary<String, String> headers) {
		Object header = null;
		if (headers != null)
			header = headers.get(SPRING_CONTEXT_HEADER);
		return (header != null ? header.toString().trim() : null);
	}

	/**
	 * Returns the location headers (if any) specified by the Spring-Context header (if available). The returned Strings
	 * can be sent to a {@link org.springframework.core.io.ResourceLoader} for loading the configurations.
	 */
	public static String[] getHeaderLocations(Dictionary<String, String> headers) {
		return getLocationsFromHeader(getSpringContextHeader(headers), "/META-INF/spring/*.xml");

	}

	/**
	 * Similar to {@link #getHeaderLocations(Dictionary)} but looks at a specified header directly.
	 */
	public static String[] getLocationsFromHeader(String header, String defaultValue) {

		String[] ctxEntries;
		if (StringUtils.hasText(header) && !(';' == header.charAt(0))) {
			// get the config locations
			String locations = StringUtils.tokenizeToStringArray(header, DIRECTIVE_SEPARATOR)[0];
			// parse it into individual token
			ctxEntries = StringUtils.tokenizeToStringArray(locations, CONTEXT_LOCATION_SEPARATOR);

			// replace * with a 'digestable' location
			for (int i = 0; i < ctxEntries.length; i++) {
				if (CONFIG_WILDCARD.equals(ctxEntries[i]))
					ctxEntries[i] = defaultValue;
			}
		}
		else {
			ctxEntries = new String[] { defaultValue };
		}

		return ctxEntries;
	}
}
