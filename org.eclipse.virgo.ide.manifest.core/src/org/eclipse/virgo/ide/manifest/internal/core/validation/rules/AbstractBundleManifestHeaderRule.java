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
package org.eclipse.virgo.ide.manifest.internal.core.validation.rules;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.virgo.ide.facet.core.FacetUtils;
import org.eclipse.virgo.ide.manifest.core.BundleManifestUtils;
import org.eclipse.virgo.ide.manifest.internal.core.validation.BundleManifestValidationContext;
import org.osgi.framework.Version;


/**
 * @author Christian Dupuis
 * @since 1.0.0
 */
/**
 * TODO CD add comments
 */
public abstract class AbstractBundleManifestHeaderRule extends AbstractManifestHeaderRule {

	protected static final Version MAX_VERSION = new Version(Integer.MAX_VALUE, 
			Integer.MAX_VALUE, Integer.MAX_VALUE);

	@Override
	protected boolean supportsContext(BundleManifestValidationContext context) {
		IResource resource = context.getRootElement().getElementResource();
		IJavaProject javaProject = JavaCore.create(resource.getProject());
		
		return FacetUtils.isBundleProject(context.getRootElement().getElementResource())
				&& (resource.equals(BundleManifestUtils.locateManifest(javaProject, false)) || resource
						.equals(BundleManifestUtils.locateManifest(javaProject, true)));
	}
}
