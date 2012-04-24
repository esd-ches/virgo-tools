/*******************************************************************************
 * Copyright (c) 2009, 2012 SpringSource, a divison of VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource, a division of VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.virgo.ide.manifest.internal.core.validation.rules;

/**
 * @author Christian Dupuis
 */
public interface ManifestValidationRuleConstants {

	String MISSING_BUNDLE_SYMBOLIC_NAME = "MISSING_BUNDLE_SYMBOLIC_NAME";

	String MISSING_BUNDLE_NAME = "MISSING_BUNDLE_NAME";

	String MISSING_BUNDLE_VERSION = "MISSING_BUNDLE_VERSION";

	String MISSING_BUNDLE_MANIFEST_VERSION = "MISSING_BUNDLE_MANIFEST_VERSION";

	String ILLEGAL_BUNDLE_MANIFEST_VERSION = "ILLEGAL_BUNDLE_MANIFEST_VERSION";

	String ILLEGAL_BUNDLE_ACTIVATION_POLICY = "ILLEGAL_BUNDLE_ACTIVATION_POLICY";

	String ILLEGAL_IMPORT_BUNDLE_VERSION = "ILLEGAL_IMPORT_BUNDLE_VERSION";

	String ILLEGAL_REQUIRE_BUNDLE_VERSION = "ILLEGAL_REQUIRE_BUNDLE_VERSION";

	String ILLEGAL_IMPORT_LIBRARY_VERSION = "ILLEGAL_IMPORT_LIBRARY_VERSION";

	String ILLEGAL_IMPORT_PACKAGE_VERSION = "ILLEGAL_IMPORT_PACKAGE_VERSION";

	String IMPORT_ARTIFACT_NAME = "IMPORT_ARTIFACT_NAME";

}
