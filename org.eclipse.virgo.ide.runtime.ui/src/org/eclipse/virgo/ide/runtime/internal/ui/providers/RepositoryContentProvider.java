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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.virgo.ide.runtime.core.artefacts.Artefact;
import org.eclipse.virgo.ide.runtime.core.artefacts.ArtefactRepository;
import org.eclipse.virgo.ide.runtime.core.artefacts.ArtefactSet;
import org.eclipse.virgo.ide.runtime.core.artefacts.IArtefact;
import org.eclipse.virgo.ide.runtime.core.artefacts.ILocalArtefact;
import org.eclipse.virgo.ide.runtime.core.artefacts.LocalArtefactRepository;
import org.eclipse.virgo.ide.runtime.core.provisioning.RepositoryUtils;
import org.eclipse.wst.server.core.IServer;

/**
 * Common content provider for repository installation nodes. (Not currently used, but could be used in cases where we
 * don't want to create server side projects.)
 *
 * @author Miles Parker
 * @author Christian Dupuis
 */
public class RepositoryContentProvider implements ITreeContentProvider {

    private ArtefactRepository repository;

    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof IServer) {
            IServer server = (IServer) inputElement;
            this.repository = RepositoryUtils.getRepositoryContents(server.getRuntime());
            this.repository.setServer(server);
            List<Object> children = new ArrayList<Object>();
            Map<File, ArtefactRepository> setForFile = new HashMap<File, ArtefactRepository>();

            for (IArtefact bundle : this.repository.getAllArtefacts().getArtefacts()) {
                if (bundle instanceof ILocalArtefact) {
                    File file = ((ILocalArtefact) bundle).getFile().getParentFile();
                    if (file.getParentFile().getName().equals("subsystems")) {
                        file = file.getParentFile();
                    }
                    if (setForFile.containsKey(file)) {
                        setForFile.get(file).add(bundle);
                    } else {
                        ArtefactRepository localRepository = new LocalArtefactRepository(file);
                        localRepository.setServer(server);
                        localRepository.add(bundle);
                        setForFile.put(file, localRepository);
                    }
                }
            }
            for (ArtefactRepository repos : setForFile.values()) {
                if (repos.getBundleSet().getArtefacts().iterator().hasNext()) {
                    children.add(repos.getBundleSet());
                }
                if (repos.getLibrarySet().getArtefacts().iterator().hasNext()) {
                    children.add(repos.getLibrarySet());
                }
            }
            return children.toArray();
        }
        return new Object[0];
    }

    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof ArtefactSet) {
            return ((ArtefactSet) parentElement).toArray();
        }
        return new Object[0];
    }

    public Object getParent(Object element) {
        if (element instanceof Artefact) {
            Artefact artefact = (Artefact) element;
            return artefact.getSet();
        }
        return null;
    }

    public boolean hasChildren(Object element) {
        return getChildren(element).length > 0;
    }

    public void dispose() {
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    public ArtefactRepository getRepository() {
        return this.repository;
    }
}