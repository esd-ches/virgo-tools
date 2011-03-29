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

import org.eclipse.osgi.util.ManifestElement;
import org.eclipse.pde.internal.core.util.VersionUtil;
import org.eclipse.virgo.ide.manifest.core.BundleManifestCoreMessages;
import org.eclipse.virgo.ide.manifest.core.BundleManifestUtils;
import org.eclipse.virgo.ide.manifest.internal.core.model.BundleManifestHeader;
import org.eclipse.virgo.ide.manifest.internal.core.model.BundleManifestHeaderElement;
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
public class RequireBundleRule extends AbstractBundleManifestHeaderRule {

	private void validateBundleVersionAttribute(BundleManifestHeader header,
			ManifestElement element, BundleManifestValidationContext context) {
		String versionRange = element.getAttribute(Constants.BUNDLE_VERSION_ATTRIBUTE);
		if (versionRange != null && !VersionUtil.validateVersionRange(versionRange).isOK()) {
			context.error("ILLEGAL_BUNDLE_VERSION",
					BundleManifestCoreMessages.BundleErrorReporter_InvalidFormatInBundleVersion,
					BundleManifestUtils.getPackageLineNumber(context.getBundleManifest()
							.getDocument(), header, element));
		}
	}

	private void validateResolutionDirective(BundleManifestHeader header, ManifestElement element,
			BundleManifestValidationContext context) {
		String resolution = element.getDirective(Constants.RESOLUTION_DIRECTIVE);
		if (resolution != null) {
			context.validateDirectiveValue(header, element, Constants.RESOLUTION_DIRECTIVE,
					new String[] { Constants.RESOLUTION_MANDATORY, Constants.RESOLUTION_OPTIONAL });
		}
	}

	private void validateVisibilityDirective(BundleManifestHeader header, ManifestElement element,
			BundleManifestValidationContext context) {
		String visibility = element.getDirective(Constants.VISIBILITY_DIRECTIVE);
		if (visibility != null) {
			context.validateDirectiveValue(header, element, Constants.VISIBILITY_DIRECTIVE,
					new String[] { Constants.VISIBILITY_PRIVATE, Constants.VISIBILITY_REEXPORT });
		}
	}

	@Override
	protected String[] getHeaderName() {
		return new String[] { Constants.REQUIRE_BUNDLE };
	}

	@Override
	protected void validateHeader(BundleManifestHeader header,
			BundleManifestValidationContext context) {
		for (BundleManifestHeaderElement element : header.getBundleManifestHeaderElements()) {
			validateBundleVersionAttribute(header, element.getManifestElement(), context);
			validateVisibilityDirective(header, element.getManifestElement(), context);
			validateResolutionDirective(header, element.getManifestElement(), context);
		}
	}

}
