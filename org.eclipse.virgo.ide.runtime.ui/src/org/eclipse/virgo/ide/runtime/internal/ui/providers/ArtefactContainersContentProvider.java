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

import org.eclipse.virgo.ide.runtime.internal.ui.projects.IServerProjectArtefact;
import org.eclipse.virgo.ide.runtime.internal.ui.projects.IServerProjectContainer;
import org.eclipse.virgo.ide.runtime.internal.ui.projects.ServerProject;
import org.eclipse.virgo.ide.runtime.internal.ui.projects.ServerProjectManager;
import org.eclipse.wst.server.core.IServer;

/**
 *
 * @author Miles Parker
 *
 */
public class ArtefactContainersContentProvider extends GenericTreeProvider {

    /**
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getElements(java.lang.Object)
     */
    @Override
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof IServer) {
            ServerProject project = ServerProjectManager.getInstance().getProject((IServer) inputElement, true, true);
            if (project != null) {
                Object[] containers = project.getContainers().toArray(new Object[0]);
                return containers;
            }
        }
        if (inputElement instanceof IServerProjectContainer) {
            return ((IServerProjectContainer) inputElement).getMembers();
        }
        return super.getElements(inputElement);
    }

    /**
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
     */
    @Override
    public Object getParent(Object element) {
        if (element instanceof IServerProjectArtefact) {
            return ((IServerProjectArtefact) element).getContainer();
        }
        if (element instanceof IServerProjectContainer) {
            return ((IServerProjectContainer) element).getServer();
        }
        return null;
    }
}
