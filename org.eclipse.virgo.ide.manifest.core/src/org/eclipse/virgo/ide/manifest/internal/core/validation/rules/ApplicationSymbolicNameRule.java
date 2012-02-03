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
//import org.eclipse.pde.internal.core.util.IdUtil;
//import org.eclipse.virgo.ide.manifest.core.BundleManifestCoreMessages;
//import org.eclipse.virgo.ide.manifest.core.BundleManifestUtils;
//import org.eclipse.virgo.ide.manifest.internal.core.model.BundleManifestHeader;
//import org.eclipse.virgo.ide.manifest.internal.core.model.BundleManifestHeaderElement;
//import org.eclipse.virgo.ide.manifest.internal.core.validation.BundleManifestValidationContext;
//import org.springframework.util.StringUtils;
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
//public class ApplicationSymbolicNameRule extends AbstractApplicationManifestHeaderRule {
//
//	private static final String[] APPLICATION_SYMBOLIC_NAME_MANIFEST_HEADER = 
//		new String[] { "Application-SymbolicName" };
//
//	@Override
//	protected String[] getHeaderName() {
//		return APPLICATION_SYMBOLIC_NAME_MANIFEST_HEADER;
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
//
//		BundleManifestHeaderElement[] elements = header.getBundleManifestHeaderElements();
//		String id = elements.length > 0 ? elements[0].getManifestElement().getValue() : null;
//		if (id == null || id.length() == 0) {
//			context.error("NO_SYMBOLIC_NAME",
//					BundleManifestCoreMessages.BundleErrorReporter_NoSymbolicName, header
//							.getLineNumber() + 1);
//		}
//
//		if (StringUtils.hasText(id) && !IdUtil.isValidCompositeID3_0(id)) {
//			context.error("ILLAGEL_SYMBOLIC_NAME",
//					BundleManifestCoreMessages.BundleErrorReporter_InvalidSymbolicName,
//					BundleManifestUtils.getLineNumber(context.getBundleManifest().getDocument(),
//							header, id));
//		}
//
//	}
//
//}
