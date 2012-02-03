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
//import org.eclipse.core.resources.IResource;
//import org.eclipse.core.runtime.IStatus;
//import org.eclipse.pde.internal.core.util.VersionUtil;
//import org.eclipse.virgo.ide.manifest.internal.core.model.BundleManifestHeader;
//import org.eclipse.virgo.ide.manifest.internal.core.validation.BundleManifestValidationContext;
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
//public class ApplicationVersionRule extends AbstractApplicationManifestHeaderRule {
//
//	private static final String[] APPLICATION_VERSION_MANIFEST_HEADER = 
//		new String[] { "Application-Version" };
//
//	@Override
//	protected String[] getHeaderName() {
//		return APPLICATION_VERSION_MANIFEST_HEADER;
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
//		IStatus status = VersionUtil.validateVersion(header.getValue());
//		if (!status.isOK()) {
//			context.error("ILLEGAL_BUNDLE_VERSION", status.getMessage(),
//				header.getLineNumber() + 1);
//		}
//	}
//
//}
