/*******************************************************************************
 * Copyright (c) 2009, 2010 SpringSource, a divison of VMware, Inc.
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

import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.maven.ide.eclipse.MavenPlugin;
import org.maven.ide.eclipse.project.IMavenProjectFacade;
import org.maven.ide.eclipse.project.MavenProjectManager;

import org.eclipse.virgo.bundlor.support.properties.PropertiesSource;

/**
 * {@link PropertiesSource} implementation that reads properties from a Maven project pom.xml hiearchy.
 * @author Christian Dupuis
 * @since 2.2.0
 */
public class MavenPropertiesSource implements PropertiesSource {

	private static final Properties EMPTY_STANDARD = new Properties();

	private final IProject project;

	public MavenPropertiesSource(IProject project) {
		this.project = project;
	}

	public int getPriority() {
		return Integer.MAX_VALUE - 1;
	}

	public Properties getProperties() {
		try {
			MavenProjectManager manager = MavenPlugin.getDefault().getMavenProjectManager();
			IMavenProjectFacade facade = manager.create(project, new NullProgressMonitor());
			MavenProject project = facade.getMavenProject(new NullProgressMonitor());
			Properties props = project.getProperties();
			
			// add in some special maven properties
			addPropertyIfNotNull(props, "project.artifactId", project.getArtifactId());
			addPropertyIfNotNull(props, "project.groupId", project.getGroupId());
			addPropertyIfNotNull(props, "project.description", project.getDescription());
			addPropertyIfNotNull(props, "project.name", project.getName());
			addPropertyIfNotNull(props, "project.version", project.getVersion());
			addPropertyIfNotNull(props, "pom.artifactId", project.getArtifactId());
			addPropertyIfNotNull(props, "pom.groupId", project.getGroupId());
			addPropertyIfNotNull(props, "pom.description", project.getDescription());
			addPropertyIfNotNull(props, "pom.name", project.getName());
			addPropertyIfNotNull(props, "pom.version", project.getVersion());

			return props;
		}
		catch (Exception e) {
			// this exception will be reported later on as the properties can't be reported.
		}
		return EMPTY_STANDARD;
	}
	
	private void addPropertyIfNotNull(Properties props, String key, String value) {
		if (value != null && key != null) {
			props.put(key, value);
		}
	}
}
