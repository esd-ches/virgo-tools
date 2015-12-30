/*******************************************************************************
 *  Copyright (c) 2015 GianMaria Romanato
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     GianMaria Romanato - initial API and implementation
 *******************************************************************************/

package org.eclipse.virgo.ide.pde.core.internal;

import org.eclipse.osgi.util.NLS;

/**
 * Localization support.
 */
public class Messages extends NLS {

    public static String Builder_copy_nativecode;

    public static String Builder_IncrementalBuildMessage;

    public static String Builder_FullBuildMessage;

    public static String Builder_copy_libraries;

    public static String Builder_CopyContent;

    public static String Helper_BinFolderError;

    public static String Helper_ManifestParsingError;

    static {
        // initialize resource bundle
        NLS.initializeMessages(Messages.class.getName(), Messages.class);
    }

    private Messages() {
    }
}
