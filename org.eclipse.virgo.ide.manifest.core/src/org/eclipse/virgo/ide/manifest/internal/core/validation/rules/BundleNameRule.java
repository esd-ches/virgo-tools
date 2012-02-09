///*******************************************************************************
// * Copyright (c) 2009, 2012 SpringSource, a divison of VMware, Inc.
// * All rights reserved. This program and the accompanying materials
// * are made available under the terms of the Eclipse Public License v1.0
// * which accompanies this distribution, and is available at
// * http://www.eclipse.org/legal/epl-v10.html
// *
// * Contributors:
// *     SpringSource, a division of VMware, Inc. - initial API and implementation
// *******************************************************************************/
//package org.eclipse.virgo.ide.manifest.internal.core.validation.rules;
//
//import org.eclipse.core.resources.IResource;
//import org.eclipse.virgo.ide.manifest.internal.core.model.BundleManifestHeader;
//import org.eclipse.virgo.ide.manifest.internal.core.validation.BundleManifestValidationContext;
//import org.osgi.framework.Constants;
//
//
///**
// * @author Christian Dupuis
// * @since 1.0.0
// */
///**
// * TODO CD add comments
// */
//public class BundleNameRule extends AbstractBundleManifestHeaderRule {
//
//	@Override
//	protected String[] getHeaderName() {
//		return new String[] { Constants.BUNDLE_NAME };
//	}
//
//	@Override
//	protected boolean isRequiredHeader(IResource resource) {
//		return true;
//	}
//
//	@Override
//	protected void validateHeader(BundleManifestHeader header,
//			BundleManifestValidationContext context) {
//		// nothing to validate on the bundle name
//	}
//	
//	@Override
//	protected String getMissingRequiredHeaderErrorId() {
//		return ManifestValidationRuleConstants.MISSING_BUNDLE_NAME;
//	}
//
//}
