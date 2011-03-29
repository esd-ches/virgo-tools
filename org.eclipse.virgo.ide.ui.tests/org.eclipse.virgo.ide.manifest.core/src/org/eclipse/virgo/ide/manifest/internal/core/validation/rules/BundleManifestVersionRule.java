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
import org.eclipse.virgo.ide.facet.core.FacetCorePlugin;
import org.eclipse.virgo.ide.facet.core.FacetUtils;
import org.eclipse.virgo.ide.manifest.core.BundleManifestCoreMessages;
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
public class BundleManifestVersionRule extends AbstractBundleManifestHeaderRule {

	@Override
	protected boolean supportsContext(BundleManifestValidationContext context) {
		if (super.supportsContext(context)) {
			IResource resource = context.getRootElement().getElementResource();
			return !FacetUtils.hasProjectFacet(resource, FacetCorePlugin.WEB_FACET_ID);
		}
		return false;
	}
	
	@Override
	protected String[] getHeaderName() {
		return new String[] { Constants.BUNDLE_MANIFESTVERSION };
	}

	@Override
	protected boolean isRequiredHeader(IResource resource) {
		return true;
	}

	@Override
	protected void validateHeader(BundleManifestHeader header,
			BundleManifestValidationContext context) {
		String version = header.getValue();
		if (!"2".equals(version)) {
			context.warning(ManifestValidationRuleConstants.ILLEGAL_BUNDLE_MANIFEST_VERSION,
					BundleManifestCoreMessages.BundleErrorReporter_illegalManifestVersion, header
							.getLineNumber() + 1);
		}
	}
	
	@Override
	protected String getMissingRequiredHeaderErrorId() {
		return ManifestValidationRuleConstants.MISSING_BUNDLE_MANIFEST_VERSION;
	}

}
