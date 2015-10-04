/// *******************************************************************************
// * Copyright (c) 2009, 2012 SpringSource, a divison of VMware, Inc.
// * All rights reserved. This program and the accompanying materials
// * are made available under the terms of the Eclipse Public License v1.0
// * which accompanies this distribution, and is available at
// * http://www.eclipse.org/legal/epl-v10.html
// *
// * Contributors:
// * SpringSource, a division of VMware, Inc. - initial API and implementation
// *******************************************************************************/
// package org.eclipse.virgo.ide.manifest.internal.core.validation.rules;
//
//
/// **
// * @author Christian Dupuis
// * @since 1.0.0
// */
/// **
// * TODO CD add comments
// */
// public abstract class AbstractManifestHeaderRule implements
// IValidationRule<BundleManifest, BundleManifestValidationContext> {
//
// public boolean supports(IModelElement element, IValidationContext context) {
// return element instanceof BundleManifest && context instanceof BundleManifestValidationContext
// && ((BundleManifest) element).getProblems().size() == 0
// && supportsContext((BundleManifestValidationContext) context);
// }
//
// public final void validate(BundleManifest element, BundleManifestValidationContext context, IProgressMonitor monitor)
/// {
// IResource resource = context.getRootElement().getElementResource();
//
// for (String headerName : getHeaderName()) {
// BundleManifestHeader header = null;
// if (isRequiredHeader(resource)) {
// header = context.validateRequiredHeader(headerName, getMissingRequiredHeaderErrorId());
// }
// else {
// header = context.getBundleManifest().getHeader(headerName);
// }
// if (header != null) {
// validateHeader(header, context);
// }
// }
// }
//
// /**
// * Intended to be overridden by validation rules to provide a unique error id that will be added to the marker in
// * the case that the header is required and missing.
// */
// protected String getMissingRequiredHeaderErrorId() {
// return "MISSING_REQUIRED_HEADER";
// }
//
// protected abstract String[] getHeaderName();
//
// protected boolean isRequiredHeader(IResource resource) {
// return false;
// }
//
// protected abstract boolean supportsContext(BundleManifestValidationContext context);
//
// protected abstract void validateHeader(BundleManifestHeader header, BundleManifestValidationContext context);
// }
