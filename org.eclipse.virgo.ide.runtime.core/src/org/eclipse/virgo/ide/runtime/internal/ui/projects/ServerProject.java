/*******************************************************************************
 * Copyright (c) 2009 - 2013 SpringSource, a divison of VMware, Inc.
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.internal.resources.ProjectDescription;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider;
import org.eclipse.virgo.ide.runtime.core.ServerCorePlugin;
import org.eclipse.virgo.ide.runtime.core.ServerUtils;
import org.eclipse.virgo.ide.runtime.core.artefacts.ArtefactRepository;
import org.eclipse.virgo.ide.runtime.core.artefacts.ArtefactSet;
import org.eclipse.virgo.ide.runtime.core.artefacts.ArtefactType;
import org.eclipse.virgo.ide.runtime.core.artefacts.IArtefact;
import org.eclipse.virgo.ide.runtime.core.artefacts.ILocalArtefact;
import org.eclipse.virgo.ide.runtime.core.artefacts.LocalArtefactRepository;
import org.eclipse.virgo.ide.runtime.core.artefacts.LocalArtefactSet;
import org.eclipse.virgo.ide.runtime.core.provisioning.RepositoryUtils;
import org.eclipse.virgo.ide.runtime.internal.core.runtimes.RuntimeProviders;
import org.eclipse.wst.server.core.IServer;

/**
 * Manages a server project for a Virgo server. The server project acts as a kind of proxy server, allowing us to
 * transparently support UI -- and eventually other features -- leveraging existing Eclipse IDE capabilities such as JDT
 * and PDE support. There should be one and only one server project for a given server.
 *
 * @author Miles Parker
 * @author Leo Dos Santos
 */
public class ServerProject {

    public static final String SERVER_PROJECT_ID = "org.eclipse.virgo.ide.runtime.managedProject";

    final IServer server;

    JavaProject javaProject;

    private IProject project;

    private List<IServerProjectContainer> containers;

    private List<IClasspathEntry> libraryEntries;

    private List<ArtefactSet> artefactSets;

    private Map<ArtefactSet, IServerProjectContainer> projectContainerForArtefactSet;

    public static final String LOG_WORKSPACE_DIR = "logs";

    public static final String PROPERTIES_DIR = "properties";

    public ServerProject(IServer server) {
        this.server = server;
        updateWorkspaceProject();
    }

    public void refresh() {
        updateWorkspaceProject();

        clearArtefacts();

        if (isRuntimeExists()) {
            // TODO we need a more efficient way of updating this
            clearFiles();

            refreshArtefacts();

            refreshDirectories();

            try {
                this.javaProject.setRawClasspath(getLibraryEntries().toArray(new IClasspathEntry[getLibraryEntries().size()]), null);
            } catch (JavaModelException e) {
                ServerProjectManager.handleException(e);
            }
        } else {

        }
    }

    public void refreshArtefacts() {
        ArtefactRepository repository = RepositoryUtils.getRepositoryContents(this.server.getRuntime());
        repository.setServer(this.server);
        Map<File, ArtefactRepository> setForFile = new HashMap<File, ArtefactRepository>();

        for (IArtefact bundle : repository.getAllArtefacts().getArtefacts()) {
            if (bundle instanceof ILocalArtefact) {
                File file = ((ILocalArtefact) bundle).getFile().getParentFile();
                if (file.getParentFile().getName().equals("subsystems")) {
                    file = file.getParentFile();
                }
                if (setForFile.containsKey(file)) {
                    setForFile.get(file).add(bundle);
                } else {
                    ArtefactRepository localRepository = new LocalArtefactRepository(file);
                    localRepository.setServer(this.server);
                    localRepository.add(bundle);
                    setForFile.put(file, localRepository);
                }
            }
        }
        for (ArtefactRepository repos : setForFile.values()) {
            if (repos.getBundleSet().getArtefacts().iterator().hasNext()) {
                this.artefactSets.add(repos.getBundleSet());
            }
            if (repos.getLibrarySet().getArtefacts().iterator().hasNext()) {
                this.artefactSets.add(repos.getLibrarySet());
            }
        }

        for (ArtefactSet artefactSet : this.artefactSets) {
            if (artefactSet instanceof LocalArtefactSet) {
                LocalArtefactSet localSet = (LocalArtefactSet) artefactSet;
                if (artefactSet.getArtefactType() == ArtefactType.BUNDLE) {
                    ProjectBundleContainer container = new ProjectBundleContainer(this, localSet);
                    this.containers.add(container);
                    this.projectContainerForArtefactSet.put(localSet, container);
                } else if (artefactSet.getArtefactType() == ArtefactType.LIBRARY) {
                    ProjectFileContainer container = new ArtefactProjectFileContainer(this, localSet);
                    this.containers.add(container);
                    this.projectContainerForArtefactSet.put(localSet, container);
                }
            }
        }
    }

    public void refreshDirectories() {
        try {
            IServerRuntimeProvider provider = RuntimeProviders.getRuntimeProvider(getServer().getRuntime());
            for (String runtimeDir : provider.getServerPropertiesDirectories()) {
                synchronizeRuntimeDirectory(PROPERTIES_DIR, runtimeDir);
            }
            for (String runtimeDir : provider.getServerLogDirectories()) {
                synchronizeRuntimeDirectory(LOG_WORKSPACE_DIR, runtimeDir);
            }
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    void synchronizeRuntimeDirectory(String workspaceDirectory, String runtimeDirectory) throws CoreException {
        IFolder folder = getWorkspaceProject().getFolder(workspaceDirectory + "/" + runtimeDirectory);
        ProjectFileContainer.createFolder(folder);
        File runtimeFolder = getServer().getRuntime().getLocation().append(runtimeDirectory).toFile();
        File[] files = runtimeFolder.listFiles();
        if (files != null) {
            for (File runtimeFile : files) {
                linkFile(folder, runtimeFile, runtimeDirectory);
            }
        }
    }

    protected void linkFile(IFolder folder, File runtimeFile, String runtimeDirectory) {
        if (!runtimeFile.isDirectory()) {
            IFile workspaceFile = folder.getFile(runtimeFile.getName());
            try {
                workspaceFile.createLink(new Path(runtimeFile.getAbsolutePath()), IResource.REPLACE, null);
            } catch (CoreException e) {
                ServerProjectManager.handleException(e);
            }
        }
    }

    protected boolean isRuntimeExists() {
        String home = ServerUtils.getServerHome(this.server.getRuntime());
        if (home == null) {
            return false;
        }
        return new File(home).exists();
    }

    protected void clearFiles() {
        if (this.project != null) {
            try {
                for (IResource resource : this.project.members()) {
                    if (resource instanceof IFolder) {
                        resource.setReadOnly(false);
                        resource.delete(true, null);
                    }
                }
            } catch (CoreException e) {
                ServerProjectManager.handleException(e);
            }
        }
    }

    protected void clearArtefacts() {
        this.libraryEntries = new ArrayList<IClasspathEntry>();
        this.containers = new ArrayList<IServerProjectContainer>();
        this.artefactSets = new ArrayList<ArtefactSet>();
        this.projectContainerForArtefactSet = new HashMap<ArtefactSet, IServerProjectContainer>();
    }

    protected void updateProject() {
        if (isRuntimeExists()) {
            updateWorkspaceProject();
        } else {
            deleteWorkspaceProject();
        }
    }

    void deleteWorkspaceProject() {
        IWorkspace ws = ResourcesPlugin.getWorkspace();
        String projectName = getWorkspaceProjectName();
        this.project = ws.getRoot().getProject(projectName);
        if (this.project != null && this.project.exists()) {
            try {
                this.project.delete(false, null);
            } catch (CoreException e) {
                ServerProjectManager.handleException(e);
            }
        }
        this.project = null;
        clearArtefacts();
    }

    void updateWorkspaceProject() {
        if (isRuntimeExists()) {
            try {
                IWorkspace ws = ResourcesPlugin.getWorkspace();
                String projectName = getWorkspaceProjectName();
                this.project = ws.getRoot().getProject(projectName);
                if (!this.project.exists()) {
                    IProjectDescription description = new ProjectDescription();
                    description.setNatureIds(new String[] { JavaCore.NATURE_ID, SERVER_PROJECT_ID });
                    description.setComment("Created and managed by Virgo Tooling.");
                    description.setName(projectName);
                    this.project.create(description, null);
                }
                if (!this.project.isOpen()) {
                    this.project.open(null);
                }

                this.javaProject = (JavaProject) JavaCore.create(this.project);
            } catch (CoreException e) {
                ServerProjectManager.handleException(e);
            }
        }
    }

    String getWorkspaceProjectName() {
        return this.server.getName() + " Server";
    }

    public IProject getWorkspaceProject() {
        return this.project;
    }

    @SuppressWarnings("restriction")
    public JavaProject getJavaProject() {
        return this.javaProject;
    }

    public List<IServerProjectContainer> getContainers() {
        return this.containers;
    }

    public IServerProjectContainer getContainer(ArtefactSet set) {
        return this.projectContainerForArtefactSet.get(set);
    }

    public IServer getServer() {
        return this.server;
    }

    public List<ArtefactSet> getArtefactSets() {
        return this.artefactSets;
    }

    public List<IClasspathEntry> getLibraryEntries() {
        return this.libraryEntries;
    }

    /**
     * Checks server type for virgo. Null safe.
     */
    public static boolean isVirgo(IServer server) {
        return server != null && server.getServerType().getId().equals(ServerCorePlugin.VIRGO_SERVER_ID);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ServerProject) {
            ServerProject other = (ServerProject) obj;
            return other.project.equals(this.project);
        }
        return false;
    }
}
