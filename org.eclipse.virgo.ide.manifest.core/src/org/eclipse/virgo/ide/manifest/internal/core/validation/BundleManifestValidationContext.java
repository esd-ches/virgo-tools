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
//package org.eclipse.virgo.ide.manifest.internal.core.validation;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//
//
///**
// * @author Christian Dupuis
// * @since 1.0.0
// */
///**
// * TODO CD add comments
// */
//public class BundleManifestValidationContext extends AbstractValidationContext {
//
//	public static final String[] BOOLEAN_VALUES = new String[] { "true", "false" };
//
//	public BundleManifestValidationContext(IResourceModelElement rootElement,
//			IResourceModelElement contextElement) {
//		super(rootElement, contextElement);
//	}
//
//	public void error(String problemId, String msg, int line, ValidationProblemAttribute... attributes) {
//		error(getRootElement(), problemId, msg, mergeAttributes(line, attributes));
//	}
//
//	public void errorIllegalAttributeValue(BundleManifestHeader header, ManifestElement element,
//			String key, String value) {
//		String msg = NLS.bind(BundleManifestCoreMessages.BundleErrorReporter_att_value,
//				(new String[] { value, key }));
//		error(getRootElement(), "ILLEGAL_ATTRIBUTE_VALUE", msg, new ValidationProblemAttribute(
//				IMarker.LINE_NUMBER, BundleManifestUtils.getLineNumber(getDocument(), header, key
//						+ "=")));
//	}
//
//	public void errorIllegalDirectiveValue(BundleManifestHeader header, ManifestElement element,
//			String key, String value) {
//		String msg = NLS.bind(BundleManifestCoreMessages.BundleErrorReporter_dir_value,
//				(new String[] { value, key }));
//		error(getRootElement(), "ILLEGAL_DIRECTIVE_VALUE", msg, new ValidationProblemAttribute(
//				IMarker.LINE_NUMBER, BundleManifestUtils.getLineNumber(getDocument(), header, key
//						+ ":=")));
//	}
//
//	public void errorIllegalValue(BundleManifestHeader header, ManifestElement element, String illegalValue, String errorId) {
//		String msg = NLS.bind(BundleManifestCoreMessages.BundleErrorReporter_illegal_value, illegalValue);
//		error(getRootElement(), errorId, msg, new ValidationProblemAttribute(
//				IMarker.LINE_NUMBER, BundleManifestUtils
//						.getLineNumber(getDocument(), header, illegalValue)));
//	}
//
//	public BundleManifest getBundleManifest() {
//		return (BundleManifest) getRootElement();
//	}
//
//	public void info(String problemId, String msg, int line, ValidationProblemAttribute... attributes) {
//		info(getRootElement(), problemId, msg, mergeAttributes(line, attributes));
//	}
//
//	public void validateAttributeValue(BundleManifestHeader header, ManifestElement element,
//			String key, String[] allowedValues) {
//		String value = element.getAttribute(key);
//		if (value == null) {
//			return;
//		}
//		for (int i = 0; i < allowedValues.length; i++) {
//			if (allowedValues[i].equals(value)) {
//				return;
//			}
//		}
//		errorIllegalAttributeValue(header, element, key, value);
//	}
//
//	public void validateBooleanAttributeValue(BundleManifestHeader header, ManifestElement element,
//			String key) {
//		validateAttributeValue(header, element, key, BOOLEAN_VALUES);
//	}
//
//	public void validateBooleanDirectiveValue(BundleManifestHeader header, ManifestElement element,
//			String key) {
//		validateDirectiveValue(header, element, key, BOOLEAN_VALUES);
//	}
//
//	public void validateBooleanValue(BundleManifestHeader header) {
//		validateHeaderValue(header, BOOLEAN_VALUES, "ILLEGAL_VALUE");
//	}
//
//	public void validateDirectiveValue(BundleManifestHeader header, ManifestElement element,
//			String key, String[] allowedValues) {
//		String value = element.getDirective(key);
//		if (value == null) {
//			return;
//		}
//		for (int i = 0; i < allowedValues.length; i++) {
//			if (allowedValues[i].equals(value)) {
//				return;
//			}
//		}
//		errorIllegalDirectiveValue(header, element, key, value);
//	}
//
//	public void validateHeaderValue(BundleManifestHeader header, String[] allowedValues, String errorId) {
//		BundleManifestHeaderElement[] elements = header.getBundleManifestHeaderElements();
//		if (elements != null && elements.length > 0) {
//			for (int i = 0; i < allowedValues.length; i++) {
//				if (allowedValues[i].equals(elements[0].getManifestElement().getValue())) {
//					return;
//				}
//			}
//			errorIllegalValue(header, elements[0].getManifestElement(), elements[0]
//					.getManifestElement().getValue(), errorId);
//		}
//	}
//
//	public BundleManifestHeader validateRequiredHeader(String name, String missingRequiredHeaderErrorId) {
//		BundleManifestHeader header = getBundleManifest().getHeader(name);
//		if (header == null) {
//			String msg = NLS.bind(BundleManifestCoreMessages.BundleErrorReporter_headerMissing,
//					name);
//			error(getRootElement(), missingRequiredHeaderErrorId, msg, new ValidationProblemAttribute(
//					IMarker.LINE_NUMBER, new Integer(1)));
//		}
//		return header;
//	}
//
//	public void warning(String problemId, String msg, int line, ValidationProblemAttribute... attributes) {
//		warning(getRootElement(), problemId, msg, mergeAttributes(line, attributes));
//	}
//
//	private ValidationProblemAttribute[] mergeAttributes(int line, ValidationProblemAttribute... attributes) {
//		List<ValidationProblemAttribute> attributeList = new ArrayList<ValidationProblemAttribute>();
//		if (attributes != null && attributes.length > 0) {
//			 attributeList.addAll(Arrays.asList(attributes));
//		}
//		attributeList.add(new ValidationProblemAttribute(IMarker.LINE_NUMBER, line));
//		return attributeList.toArray(new ValidationProblemAttribute[attributeList.size()]);
//	}
//
//	private IDocument getDocument() {
//		return getBundleManifest().getDocument();
//	}
//	
//}
