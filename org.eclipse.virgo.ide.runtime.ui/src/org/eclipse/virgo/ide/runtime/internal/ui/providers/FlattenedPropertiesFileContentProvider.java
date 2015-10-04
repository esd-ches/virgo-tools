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

package org.eclipse.virgo.ide.runtime.internal.ui.providers;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.wst.server.core.IServer;

/**
 * Common content provider for views on server content.
 *
 * @author Miles Parker
 */
public class FlattenedPropertiesFileContentProvider extends PropertiesFileContentProvider {

    @Override
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof IServer) {
            Object[] elements = super.getElements(inputElement);
            Object[] returned = new Object[0];
            for (Object object : elements) {
                Object[] fileSelection = super.getElements(object);
                returned = ArrayUtils.addAll(returned, fileSelection);
            }
            return returned;
        }
        return super.getElements(inputElement);
    }
}