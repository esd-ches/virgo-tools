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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.pde.internal.core.util.VersionUtil;
import org.eclipse.virgo.ide.facet.core.FacetCorePlugin;
import org.eclipse.virgo.ide.facet.core.FacetUtils;
import org.eclipse.virgo.ide.manifest.internal.core.model.BundleManifestHeader;
import org.eclipse.virgo.ide.manifest.internal.core.validation.BundleManifestValidationContext;
import org.osgi.framework.Constants;


/**
 * @author Christian Dupuis
 * @since 1.0.0
 */
/**
 * TODO CD add comments
 */
@SuppressWarnings("restriction")
public class BundleVersionRule extends AbstractBundleManifestHeaderRule {

	@Override
	protected String[] getHeaderName() {
		return new String[] { Constants.BUNDLE_VERSION };
	}

	@Override
	protected boolean isRequiredHeader(IResource resource) {
		return !FacetUtils.hasProjectFacet(resource, FacetCorePlugin.WEB_FACET_ID);
	}

	@Override
	protected void validateHeader(BundleManifestHeader header, BundleManifestValidationContext context) {
		IStatus status = VersionUtil.validateVersion(header.getValue());
		if (!status.isOK()) {
			context.error("ILLEGAL_BUNDLE_VERSION", status.getMessage(), header.getLineNumber() + 1);
		}
	}

	@Override
	protected String getMissingRequiredHeaderErrorId() {
		return ManifestValidationRuleConstants.MISSING_BUNDLE_VERSION;
	}

}
