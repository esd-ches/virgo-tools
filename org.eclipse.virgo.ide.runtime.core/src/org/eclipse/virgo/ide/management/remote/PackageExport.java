/*******************************************************************************
 * Copyright (c) 2009, 2011 SpringSource, a divison of VMware, Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource, a division of VMware, Inc. - initial API and implementation
 *     SAP AG - moving to Eclipse Libra project and enhancements
 *******************************************************************************/

package org.eclipse.virgo.ide.management.remote;

import java.io.Serializable;

import org.eclipse.libra.framework.editor.core.model.IPackageExport;

/**
 * @author Christian Dupuis
 * @author Kaloyan Raev
 */
public class PackageExport implements IPackageExport, Serializable {

    private static final long serialVersionUID = -4798012781542524159L;

    private final String name;

    private final String version;

    public PackageExport(String name, String version) {
        this.name = name;
        this.version = version;
    }

    public String getName() {
        return this.name;
    }

    public String getVersion() {
        return this.version;
    }

}