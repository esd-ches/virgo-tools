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

import java.util.Arrays;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.virgo.ide.facet.core.FacetUtils;
import org.eclipse.virgo.ide.manifest.core.BundleManifestCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.locate.AbstractJavaProjectPathMatchingBeansConfigLocator;
import org.springframework.ide.eclipse.beans.core.model.locate.IBeansConfigLocator;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.java.JdtUtils;

import org.eclipse.virgo.util.osgi.manifest.BundleManifest;

/**
 * {@link IBeansConfigLocator} that discovers spring configuration files that are placed in the
 * META-INF/spring directory.
 * @author Christian Dupuis
 * @since 1.0.0
 */
public class SpringOsgiBeansConfigLocator extends AbstractJavaProjectPathMatchingBeansConfigLocator {

	/** The default context location with Spring DM */
	private static final String DEFAULT_CONTEXT_LOCATION = "/META-INF/spring/*.xml";

	/** The default context locations as list */
	private static final List<String> DEFAULT_CONTEXT_LOCATION_PATTERN = Arrays
			.asList(new String[] { DEFAULT_CONTEXT_LOCATION });

	/** Internal list of context locations */
	private List<String> contextLocations = DEFAULT_CONTEXT_LOCATION_PATTERN;

	/**
	 * This implementation only supports {@link IJavaProject} and SpringSource bundle projects.
	 * <p>
	 * This method calculates the {@link #contextLocations} for this particular bundle.
	 */
	@Override
	protected boolean canLocateInProject(IProject project) {
		if (!super.canLocateInProject(project) || !FacetUtils.isBundleProject(project)) {
			return false;
		}
		BundleManifest bundleManifest = BundleManifestCorePlugin.getBundleManifestManager()
				.getBundleManifest(JdtUtils.getJavaProject(project));
		if (bundleManifest != null) {
			Dictionary<String, String> manifest = bundleManifest.toDictionary();
			contextLocations = getConfigLocationPattern(manifest);
			return true;
		}
		return false;
	}
	
	protected List<String> getConfigLocationPattern(Dictionary<String, String> header) {
		return Arrays.asList(SpringOsgiConfigLocationUtils.getHeaderLocations(header));
	}

	@Override
	protected List<String> getAllowedFilePatterns() {
		return contextLocations;
	}

	/**
	 * Returns <code>true</code> if the given <code>project</code> is SpringSource AP bundle project
	 * and has the Spring IDE nature.
	 */
	public boolean supports(IProject project) {
		return FacetUtils.isBundleProject(project)
				&& SpringCoreUtils.isSpringProject(project);
	}

	/**
	 * Returns <code>true</code> if the given <code>file</code> is a MANIFEST.MF.
	 */
	public boolean requiresRefresh(IFile file) {
		return SpringCoreUtils.isManifest(file)
				|| (file != null && file.getName().equals("template.mf"))
				|| (file != null && ".settings/org.eclipse.wst.common.project.facet.core.xml"
						.equals(file.getProjectRelativePath().toString()));
	}

	/**
	 * Returns Spring DM-specific config set name.
	 */
	public String getBeansConfigSetName(Set<IFile> files) {
		Iterator<IFile> iterator = files.iterator();
		if (iterator.hasNext()) {
			BundleManifest bundleManifest = BundleManifestCorePlugin.getBundleManifestManager()
					.getBundleManifest(JdtUtils.getJavaProject(iterator.next().getProject()));
			if (bundleManifest != null && bundleManifest.getBundleSymbolicName() != null
					&& bundleManifest.getBundleSymbolicName().getSymbolicName() != null) {
				return bundleManifest.getBundleSymbolicName().getSymbolicName() + "-context " + getBeansConfigSetNameSuffix();
			}
		}
		return "module-context " + getBeansConfigSetNameSuffix();
	}
	
	protected String getBeansConfigSetNameSuffix() {
		return "[Spring DM]";
	}

	/**
	 * Enable bean overriding as we add the implicit <code>bundleContext</code> bean.
	 */
	@Override
	public void configureBeansConfigSet(IBeansConfigSet configSet) {
		if (configSet instanceof BeansConfigSet) {
			((BeansConfigSet) configSet).setAllowBeanDefinitionOverriding(true);
		}
	}
}
