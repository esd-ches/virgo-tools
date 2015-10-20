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

import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.IMavenProjectRegistry;
import org.eclipse.virgo.bundlor.support.properties.PropertiesSource;

/**
 * {@link PropertiesSource} implementation that reads properties from a Maven project pom.xml hierarchy.
 *
 * @author Christian Dupuis
 * @author Leo Dos Santos
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
            IMavenProjectRegistry registry = MavenPlugin.getMavenProjectRegistry();
            IMavenProjectFacade facade = registry.create(this.project, new NullProgressMonitor());
            if (facade != null) {
                MavenProject mavenProj = facade.getMavenProject(new NullProgressMonitor());
                Properties props = mavenProj.getProperties();

                // add in some special maven properties
                addPropertyIfNotNull(props, "project.artifactId", mavenProj.getArtifactId()); //$NON-NLS-1$
                addPropertyIfNotNull(props, "project.groupId", mavenProj.getGroupId()); //$NON-NLS-1$
                addPropertyIfNotNull(props, "project.description", mavenProj.getDescription()); //$NON-NLS-1$
                addPropertyIfNotNull(props, "project.name", mavenProj.getName()); //$NON-NLS-1$
                addPropertyIfNotNull(props, "project.version", mavenProj.getVersion()); //$NON-NLS-1$
                addPropertyIfNotNull(props, "pom.artifactId", mavenProj.getArtifactId()); //$NON-NLS-1$
                addPropertyIfNotNull(props, "pom.groupId", mavenProj.getGroupId()); //$NON-NLS-1$
                addPropertyIfNotNull(props, "pom.description", mavenProj.getDescription()); //$NON-NLS-1$
                addPropertyIfNotNull(props, "pom.name", mavenProj.getName()); //$NON-NLS-1$
                addPropertyIfNotNull(props, "pom.version", mavenProj.getVersion()); //$NON-NLS-1$

                return props;
            }
        } catch (CoreException e) {
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
