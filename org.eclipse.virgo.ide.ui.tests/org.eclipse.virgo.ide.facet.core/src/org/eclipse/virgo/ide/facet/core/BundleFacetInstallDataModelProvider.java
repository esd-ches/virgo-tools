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
package org.eclipse.virgo.ide.facet.core;

import java.util.Set;

import org.eclipse.jst.common.project.facet.JavaProjectFacetCreationDataModelProvider;

/**
 * @author Christian Dupuis
 * @since 1.1.3
 */
public class BundleFacetInstallDataModelProvider extends JavaProjectFacetCreationDataModelProvider	 {

	public static String ENABLE_SERVER_CLASSPATH_CONTAINER = FacetCorePlugin.PLUGIN_ID
			+ ".ENABLE_BUNDLE_CLASSPATH_CONTAINER";

	@SuppressWarnings("unchecked")
	public Set getPropertyNames() {
		Set propertyNames = super.getPropertyNames();
		propertyNames.add(ENABLE_SERVER_CLASSPATH_CONTAINER);
		return propertyNames;
	}

	public Object getDefaultProperty(String propertyName) {
		if (ENABLE_SERVER_CLASSPATH_CONTAINER.equals(propertyName)) {
			return Boolean.TRUE;
		}
		return super.getDefaultProperty(propertyName);
	}

}
