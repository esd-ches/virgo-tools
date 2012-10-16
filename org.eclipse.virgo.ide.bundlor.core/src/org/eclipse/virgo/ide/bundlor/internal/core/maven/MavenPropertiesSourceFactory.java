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
package org.eclipse.virgo.ide.bundlor.internal.core.maven;

import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.virgo.bundlor.support.properties.PropertiesSource;
import org.eclipse.virgo.ide.facet.core.FacetUtils;

/**
 * Factory implementation to create instances of {@link MavenPropertiesSource}.
 * <p>
 * This implementation will make sure not to depend on M2E until it is actually installed.
 * 
 * @author Christian Dupuis
 * @author Leo Dos Santos
 */
public class MavenPropertiesSourceFactory {

	private static final String M2E_CLASS_NAME = "org.eclipse.m2e.core.project.IMavenProjectRegistry"; //$NON-NLS-1$

	private static final String M2E_PROJECT_NATURE = "org.eclipse.m2e.core.maven2Nature"; //$NON-NLS-1$

	private static final boolean IS_M2E_PRESENT = isM2ePresent();

	private static final PropertiesSource EMPTY_PROPERTIES_SOURCE = new NoOpPropertiesSource();

	public static boolean shouldCreate(IProject project) {
		return IS_M2E_PRESENT && FacetUtils.hasNature(project, M2E_PROJECT_NATURE);
	}

	public static PropertiesSource createPropertiesSource(IProject project) {
		if (shouldCreate(project)) {
			return new MavenPropertiesSource(project);
		}
		return EMPTY_PROPERTIES_SOURCE;
	}

	private static boolean isM2ePresent() {
		try {
			MavenPropertiesSourceFactory.class.getClassLoader().loadClass(M2E_CLASS_NAME);
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	private static class NoOpPropertiesSource implements PropertiesSource {

		public int getPriority() {
			return Integer.MIN_VALUE;
		}

		public Properties getProperties() {
			return new Properties();
		}

	}

}
