///*******************************************************************************
// * Copyright (c) 2009 SpringSource, a divison of VMware, Inc.
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
//
///**
// * @author Christian Dupuis
// * @since 1.0.0
// */
///**
// * TODO CD add comments
// */
//@SuppressWarnings("restriction")
//public class ImportPackageBundleAndLibraryRule extends AbstractBundleManifestHeaderRule {
//
//	private void validateResolutionDirective(BundleManifestHeader header, ManifestElement element,
//			BundleManifestValidationContext context) {
//		String resolution = element.getDirective(Constants.RESOLUTION_DIRECTIVE);
//		if (resolution != null) {
//			context.validateDirectiveValue(header, element, Constants.RESOLUTION_DIRECTIVE,
//					new String[] { Constants.RESOLUTION_MANDATORY, Constants.RESOLUTION_OPTIONAL });
//		}
//	}
//
//	private void validateVersionAttribute(BundleManifestHeader header, ManifestElement element,
//			BundleManifestValidationContext context) {
//		String version = null;
//		if (Constants.REQUIRE_BUNDLE.equals(header.getElementName())) {
//			version = element.getAttribute(Constants.BUNDLE_VERSION_ATTRIBUTE);
//		} else {
//			version = element.getAttribute(Constants.VERSION_ATTRIBUTE);
//		}
//		
//		if (version == null)
//			return;
//		IStatus status = VersionUtil.validateVersionRange(version);
//		if (!status.isOK()) {
//			String errorId = getIllegalVersionErrorId(header);
//			String artifactName = element.getValue();
//			context.error(errorId, status.getMessage(), BundleManifestUtils
//					.getPackageLineNumber(context.getBundleManifest().getDocument(), header,
//							element), new ValidationProblemAttribute(ManifestValidationRuleConstants.IMPORT_ARTIFACT_NAME, artifactName));
//		}
//	}
//	
//	protected String getIllegalVersionErrorId(BundleManifestHeader header) {
//		if (header.getElementName().equals(Constants.IMPORT_PACKAGE)) {
//			return ManifestValidationRuleConstants.ILLEGAL_IMPORT_PACKAGE_VERSION;
//		} else if (header.getElementName().equals("Import-Bundle")) {
//			return ManifestValidationRuleConstants.ILLEGAL_IMPORT_BUNDLE_VERSION;
//		} else if (header.getElementName().equals("Require-Bundle")) {
//			return ManifestValidationRuleConstants.ILLEGAL_REQUIRE_BUNDLE_VERSION;
//		} else if (header.getElementName().equals("Import-Library")) {
//			return ManifestValidationRuleConstants.ILLEGAL_IMPORT_LIBRARY_VERSION;
//		}
//		return "";
//	}
//
//	@Override
//	protected String[] getHeaderName() {
//		return new String[] { Constants.IMPORT_PACKAGE, "Import-Bundle", "Import-Library", Constants.REQUIRE_BUNDLE };
//	}
//
//	@Override
//	protected void validateHeader(BundleManifestHeader header,
//			BundleManifestValidationContext context) {
//		for (BundleManifestHeaderElement element : header.getBundleManifestHeaderElements()) {
//			validateResolutionDirective(header, element.getManifestElement(), context);
//			validateVersionAttribute(header, element.getManifestElement(), context);
//		}
//	}
//
//}
