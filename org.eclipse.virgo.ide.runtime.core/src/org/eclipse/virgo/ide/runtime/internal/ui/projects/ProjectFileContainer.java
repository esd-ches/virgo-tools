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

package org.eclipse.virgo.ide.runtime.internal.ui.projects;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.virgo.ide.runtime.core.ServerCorePlugin;
import org.eclipse.wst.server.core.IServer;

/**
 * @author Miles Parker
 */
public abstract class ProjectFileContainer implements IServerProjectContainer {

    private final ServerProject serverProject;

    protected ProjectFileReference[] fileReferences;

    protected ProjectFileContainer(ServerProject serverProject) {
        this.serverProject = serverProject;
    }

    public static void createFolder(IFolder folder) {
        IContainer parent = folder.getParent();
        if (parent instanceof IFolder) {
            createFolder((IFolder) parent);
        }
        if (!folder.exists()) {
            try {
                folder.create(true, true, null);
            } catch (CoreException e) {
                StatusManager.getManager().handle(
                    new Status(IStatus.ERROR, ServerCorePlugin.PLUGIN_ID, "Problem occurred while managing server project.", e));
            }
        }
    }

    /**
     * @see org.eclipse.virgo.ide.runtime.internal.ui.projects.IServerProjectContainer#getMembers()
     */
    public Object[] getMembers() {
        return getFileReferences();
    }

    /**
     * @see org.eclipse.virgo.ide.runtime.internal.ui.projects.IServerProjectContainer#getServer()
     */
    public IServer getServer() {
        return getServerProject().getServer();
    }

    public ProjectFileReference[] getFileReferences() {
        if (this.fileReferences == null) {
            this.fileReferences = createFileReferences();
        }
        return this.fileReferences;
    }

    protected abstract ProjectFileReference[] createFileReferences();

    protected abstract IFolder getRootFolder();

    public ServerProject getServerProject() {
        return this.serverProject;
    }
}
