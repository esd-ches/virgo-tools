/*******************************************************************************
 *  Copyright (c) 2016 GianMaria Romanato
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     GianMaria Romanato - initial API and implementation
 *******************************************************************************/

package org.eclipse.virgo.ide.facet.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Base class for runnables used for creating/changing projects
 */
public abstract class AbstractOperation implements IWorkspaceRunnable {

    public AbstractOperation() {
        super();
    }

    protected String readResourceFromClassPath(String path, String charset) throws CoreException {
        InputStream is = getClass().getResourceAsStream(path);
        if (is != null) {
            try {
                InputStreamReader r = new InputStreamReader(is, charset);
                StringBuilder sb = new StringBuilder();
                int c;
                while ((c = r.read()) != -1) {
                    sb.append((char) c);
                }
                return sb.toString();
            } catch (IOException e) {
                throw new CoreException(new Status(IStatus.ERROR, FacetCorePlugin.PLUGIN_ID, e.getMessage(), e));
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        } else {
            throw new CoreException(new Status(IStatus.ERROR, FacetCorePlugin.PLUGIN_ID, "Template file missing " + path)); //$NON-NLS-1$
        }

    }

}