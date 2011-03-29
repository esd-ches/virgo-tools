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

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * RFC124-version of {@link ConfigUtils} class. Basically a small util class that handles the retrieval of relevant
 * headers from the any given bundle.
 * @author Christian Dupuis
 * @author Costin Leau
 */
public class BlueprintConfigUtils {

	private static final String EQUALS = "=";

	private static final String SEMI_COLON = ";";

	private static final String COMMA = ",";

	/** Manifest entry name for configuring Blueprint modules */
	public static final String BLUEPRINT_HEADER = "Bundle-Blueprint";

	/**
	 * Returns the {@value #BLUEPRINT_HEADER} if present from the given dictionary.
	 */
	public static String getBlueprintHeader(Dictionary<String, String> headers) {
		Object header = null;
		if (headers != null)
			header = headers.get(BLUEPRINT_HEADER);
		return (header != null ? header.toString().trim() : null);
	}

	/**
	 * Returns the location headers (if any) specified by the Blueprint-Bundle header (if available). The returned
	 * Strings can be sent to a {@link org.springframework.core.io.ResourceLoader} for loading the configurations.
	 * 
	 * Different from {@link ConfigUtils#getLocationsFromHeader(String, String)} since "," is used for separating
	 * clauses while ; is used inside a clause to allow parameters or directives besides paths.
	 * 
	 * Since the presence of the header, disables any processing this method will return null if the header is not
	 * specified, an empty array if it's empty (disabled) or a populated array otherwise.
	 */
	public static String[] getBlueprintHeaderLocations(Dictionary<String, String> headers) {
		String header = getBlueprintHeader(headers);

		// no header specified
		if (header == null) {
			return null;
		}

		// empty header specified
		if (header.length() == 0) {
			return new String[0];
		}

		List<String> ctxEntries = new ArrayList<String>(4);
		if (StringUtils.hasText(header)) {
			String[] clauses = header.split(COMMA);
			for (String clause : clauses) {
				// split into directives
				String[] directives = clause.split(SEMI_COLON);
				if (!ObjectUtils.isEmpty(directives)) {
					// check if it's a path or not
					for (String directive : directives) {
						if (!directive.contains(EQUALS)) {
							ctxEntries.add(directive.trim());
						}
					}
				}
			}
		}

		// replace * with a 'digestable' location
		for (int i = 0; i < ctxEntries.size(); i++) {
			String ctxEntry = ctxEntries.get(i);
			if (SpringOsgiConfigLocationUtils.CONFIG_WILDCARD.equals(ctxEntry))
				ctxEntry =  "/META-INF/spring/*.xml";
		}

		return (String[]) ctxEntries.toArray(new String[ctxEntries.size()]);
	}
}