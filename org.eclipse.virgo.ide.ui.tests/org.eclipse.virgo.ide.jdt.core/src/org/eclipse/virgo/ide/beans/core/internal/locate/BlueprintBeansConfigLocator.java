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
import java.util.Arrays;
import java.util.Dictionary;
import java.util.List;

import org.springframework.ide.eclipse.beans.core.model.locate.IBeansConfigLocator;

/**
 * {@link IBeansConfigLocator} that discovers spring configuration files that are placed in the OSGI-INF/blueprint
 * directory.
 * @author Christian Dupuis
 * @since 2.2.0
 */
public class BlueprintBeansConfigLocator extends SpringOsgiBeansConfigLocator {

	/** The default context location with Spring DM */
	private static final String DEFAULT_CONTEXT_LOCATION = "/OSGI-INF/blueprint/*.xml";

	/** The default context locations as list */
	private static final List<String> DEFAULT_CONTEXT_LOCATION_PATTERN = Arrays
			.asList(new String[] { DEFAULT_CONTEXT_LOCATION });

	@Override
	protected List<String> getConfigLocationPattern(Dictionary<String, String> header) {
		List<String> contextLocations = DEFAULT_CONTEXT_LOCATION_PATTERN;
		
		String[] locations = BlueprintConfigUtils.getBlueprintHeaderLocations(header);
		if (locations != null) {
			contextLocations = new ArrayList<String>();
			for (String location : locations) {
				if (isAbsolute(location)) {
					contextLocations.add(location);
				}
				// resolve the location to check if it's present
				else {
					String loc = location;
					if (loc.endsWith("/")) {
						loc = loc + "*.xml";
					}
					contextLocations.add(loc);
				}
			}
		}
		return contextLocations;
	}
	
	protected String getBeansConfigSetNameSuffix() {
		return "[Blueprint]";
	}

	private boolean isAbsolute(String location) {
		return !(location.endsWith("/") || location.contains("*"));
	}
}
