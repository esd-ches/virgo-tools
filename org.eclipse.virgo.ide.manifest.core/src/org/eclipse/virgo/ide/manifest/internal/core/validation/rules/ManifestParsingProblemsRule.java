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
//
///**
// * @author Christian Dupuis
// * @since 1.0.0
// */
///**
// * TODO CD add comments
// */
//public class ManifestParsingProblemsRule implements
//		IValidationRule<BundleManifest, BundleManifestValidationContext> {
//
//	public boolean supports(IModelElement element, IValidationContext context) {
//		return element instanceof BundleManifest
//				&& context instanceof BundleManifestValidationContext;
//	}
//
//	public void validate(BundleManifest element, BundleManifestValidationContext context,
//			IProgressMonitor monitor) {
//		context.addProblems(element.getProblems().toArray(new ValidationProblem[element.getProblems().size()]));
//	}
//}
