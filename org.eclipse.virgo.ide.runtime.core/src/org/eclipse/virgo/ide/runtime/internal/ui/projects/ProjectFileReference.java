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

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.virgo.ide.runtime.core.artefacts.ILocalArtefact;
import org.eclipse.virgo.ide.runtime.core.artefacts.LocalArtefactSet;

/**
 * @author Miles Parker
 */
public class ProjectFileReference implements IServerProjectArtefact, IAdaptable {

    ProjectFileContainer container;

    ILocalArtefact library;

    private final File runtimeFile;

    private final IFile workspaceFile;

    public ProjectFileReference(ProjectFileContainer container, ILocalArtefact artefact) {
        super();
        this.container = container;
        this.library = artefact;

        this.runtimeFile = artefact.getFile();
        String artefactRelative = this.runtimeFile.getAbsolutePath().replace(((LocalArtefactSet) artefact.getSet()).getFile().getAbsolutePath(), "");
        this.workspaceFile = container.getRootFolder().getFile(artefactRelative);
        try {
            this.runtimeFile.setReadOnly();
            this.workspaceFile.createLink(new Path(this.runtimeFile.getAbsolutePath()), IResource.REPLACE, null);
        } catch (CoreException e) {
            ServerProjectManager.handleException(e);
        }

    }

    /**
     * @see org.eclipse.virgo.ide.runtime.internal.ui.projects.IServerProjectArtefact#getContainer()
     */
    public IServerProjectContainer getContainer() {
        return this.container;
    }

    /**
     * @see org.eclipse.virgo.ide.runtime.internal.ui.projects.IServerProjectArtefact#getArtefact()
     */
    public ILocalArtefact getArtefact() {
        return this.library;
    }

    public File getRuntimeFile() {
        return this.runtimeFile;
    }

    public IFile getWorkspaceFile() {
        return this.workspaceFile;
    }

    /**
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    public Object getAdapter(Class adapter) {
        if (adapter == IFile.class) {
            return this.workspaceFile;
        }
        return null;
    }
}
