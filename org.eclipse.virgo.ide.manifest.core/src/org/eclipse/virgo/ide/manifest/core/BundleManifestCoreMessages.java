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

package org.eclipse.virgo.ide.manifest.core;

import org.eclipse.osgi.util.NLS;

/**
 * @author Christian Dupuis
 */
public class BundleManifestCoreMessages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.virgo.ide.manifest.core.messages";

    static {
        // load message values from bundle file
        NLS.initializeMessages(BUNDLE_NAME, BundleManifestCoreMessages.class);
    }

    public static String BundleErrorReporter_lineTooLong;

    public static String BundleErrorReporter_noMainSection;

    public static String BundleErrorReporter_duplicateHeader;

    public static String BundleErrorReporter_noColon;

    public static String BundleErrorReporter_noSpaceValue;

    public static String BundleErrorReporter_nameHeaderInMain;

    public static String BundleErrorReporter_noNameHeader;

    public static String BundleErrorReporter_invalidHeaderName;

    public static String BundleErrorReporter_noLineTermination;

    public static String BundleErrorReporter_parseHeader;

    public static String BundleErrorReporter_att_value;

    public static String BundleErrorReporter_dir_value;

    public static String BundleErrorReporter_illegal_value;

    public static String BundleErrorReporter_deprecated_attribute_optional;

    public static String BundleErrorReporter_deprecated_attribute_reprovide;

    public static String BundleErrorReporter_deprecated_attribute_singleton;

    public static String BundleErrorReporter_deprecated_attribute_specification_version;

    public static String BundleErrorReporter_directive_hasNoEffectWith_;

    public static String BundleErrorReporter_singletonAttrRequired;

    public static String BundleErrorReporter_singletonRequired;

    public static String BundleErrorReporter_headerMissing;

    public static String BundleErrorReporter_NoSymbolicName;

    public static String BundleErrorReporter_illegalManifestVersion;

    public static String BundleErrorReporter_ClasspathNotEmpty;

    public static String BundleErrorReporter_fragmentActivator;

    public static String BundleErrorReporter_NoExist;

    public static String BundleErrorReporter_InvalidFormatInBundleVersion;

    public static String BundleErrorReporter_NotExistInProject;

    public static String BundleErrorReporter_BundleRangeInvalidInBundleVersion;

    public static String BundleErrorReporter_invalidVersionRangeFormat;

    public static String BundleErrorReporter_NotExistPDE;

    public static String BundleErrorReporter_HostNotExistPDE;

    public static String BundleErrorReporter_HostNeeded;

    public static String BundleErrorReporter_PackageNotExported;

    public static String BundleErrorReporter_InvalidSymbolicName;

    public static String BundleErrorReporter_invalidFilterSyntax;

    public static String BundleErrorReporter_importexport_servicesDeprecated;

    public static String BundleErrorReporter_unecessaryDependencyDueToFragmentHost;

    public static String BundleErrorReporter_missingPackagesInProject;

    public static String BundleErrorReporter_noExecutionEnvironmentSet;

    public static String BundleErrorReporter_missingBundleClassPathEntry;

    public static String BundleErrorReporter_startHeader_autoStartDeprecated;

    public static String BundleErrorReporter_exportNoJRE;

    public static String BundleErrorReporter_importNoJRE;

    public static String BundleErrorReporter_lazyStart_unsupported;

}
