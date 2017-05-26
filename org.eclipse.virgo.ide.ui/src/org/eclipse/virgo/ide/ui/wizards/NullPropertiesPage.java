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

package org.eclipse.virgo.ide.ui.wizards;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.widgets.Composite;

/**
 * @author Christian Dupuis
 */
public class NullPropertiesPage extends AbstractPropertiesPage {

    protected static String ID_PAGE = "null.properties"; //$NON-NLS-1$

    public NullPropertiesPage() {
        super(ID_PAGE);
    }

    @Override
    protected void createPropertiesGroup(Composite container) {
        // ignore
    }

    @Override
    public String getModuleType() {
        return ""; //$NON-NLS-1$
    }

    @Override
    public Map<String, String> getProperties() {
        return new HashMap<String, String>();
    }

}
