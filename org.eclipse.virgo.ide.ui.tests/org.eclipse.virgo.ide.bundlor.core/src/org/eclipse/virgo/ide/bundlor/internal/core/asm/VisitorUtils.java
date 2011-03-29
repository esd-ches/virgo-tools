/*******************************************************************************
 * Copyright (c) 2010 SpringSource, a divison of VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource, a division of VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.virgo.ide.bundlor.internal.core.asm;

import org.objectweb.asm.Type;

import org.eclipse.virgo.bundlor.support.partialmanifest.PartialManifest;

/**
 * @author Christian Dupuis
 * @author Rob Harrop
 */
final class VisitorUtils {

    /**
     * Gets the package name from the supplied {@link Type}.
     * 
     * @param type the <code>Type</code>.
     * @return the package name.
     */
    public static String getPackageName(Type type) {
        String name;
        if (type.getSort() == Type.OBJECT) {
            name = type.getClassName();
        } else if (type.getSort() == Type.ARRAY) {
            return getPackageName(type.getElementType());
        } else {
            return null;
        }
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex > -1) {
            return name.substring(0, dotIndex);
        } else {
            return null;
        }
    }

    public static String getFullyQualifiedTypeName(Type type) {
        if (type.getSort() == Type.OBJECT) {
            return type.getClassName();
        } else if (type.getSort() == Type.ARRAY) {
            return getFullyQualifiedTypeName(type.getElementType());
        } else {
            return null;
        }
    }

    /**
     * Records the supplied {@link Type Types} in the supplied {@link PartialManifest}.
     * 
     * @param partialManifest the <code>PartialManifest</code>.
     * @param types the <code>Types</code>.
     */
    public static void recordReferencedTypes(PartialManifest partialManifest, Type... types) {
        for (Type t : types) {
            partialManifest.recordReferencedType(getFullyQualifiedTypeName(t));
        }
    }

    /**
     * Records a uses of the packages for the supplied {@link Type Types} against the supplied
     * <code>exportingPackage</code>.
     * 
     * @param partialManifest the <code>PartialManifest</code>.
     * @param exportingType the exporting type.
     * @param types the <code>Types</code>.
     */
    public static void recordUses(PartialManifest partialManifest, Type exportingType, Type... types) {
        for (Type t : types) {
            String packageName = getPackageName(t);
            partialManifest.recordUsesPackage(getPackageName(exportingType), packageName);
        }
    }
}
