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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.internal.resources.ProjectDescription;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.virgo.ide.runtime.core.ServerUtils;
import org.eclipse.virgo.ide.runtime.core.artefacts.ArtefactRepository;
import org.eclipse.virgo.ide.runtime.core.artefacts.ArtefactSet;
import org.eclipse.virgo.ide.runtime.core.artefacts.ArtefactType;
import org.eclipse.virgo.ide.runtime.core.artefacts.IArtefact;
import org.eclipse.virgo.ide.runtime.core.artefacts.ILocalArtefact;
import org.eclipse.virgo.ide.runtime.core.artefacts.LocalArtefactRepository;
import org.eclipse.virgo.ide.runtime.core.artefacts.LocalArtefactSet;
import org.eclipse.virgo.ide.runtime.core.provisioning.RepositoryUtils;
import org.eclipse.wst.server.core.IServer;

/**
 * Manages a server project for a Virgo server. The server project acts as a kind of proxy server, allowing us to
 * transparently support UI -- and eventually other features -- leveraging existing Eclipse IDE capabilities such as JDT
 * and PDE support. There should be one and only one server project for a given server.
 * 
 * @author Miles Parker
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

	public ServerProject(IServer server) {
		this.server = server;
		updateWorkspaceProject();
	}

	public void refresh() {
		updateWorkspaceProject();

		clearArtefacts();

		if (isRuntimeExists()) {
			//TODO we need a more efficient way of updating this
			clearFiles();

			ArtefactRepository repository = RepositoryUtils.getRepositoryContents(server.getRuntime());
			repository.setServer(server);
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
						localRepository.setServer(server);
						localRepository.add(bundle);
						setForFile.put(file, localRepository);
					}
				}
			}
			for (ArtefactRepository repos : setForFile.values()) {
				if (repos.getBundleSet().getArtefacts().iterator().hasNext()) {
					artefactSets.add(repos.getBundleSet());
				}
				if (repos.getLibrarySet().getArtefacts().iterator().hasNext()) {
					artefactSets.add(repos.getLibrarySet());
				}
			}

			for (ArtefactSet artefactSet : artefactSets) {
				if (artefactSet instanceof LocalArtefactSet) {
					LocalArtefactSet localSet = (LocalArtefactSet) artefactSet;
					if (artefactSet.getArtefactType() == ArtefactType.BUNDLE) {
						ProjectBundleContainer container = new ProjectBundleContainer(this, localSet);
						containers.add(container);
						projectContainerForArtefactSet.put(localSet, container);
					} else if (artefactSet.getArtefactType() == ArtefactType.LIBRARY) {
						ProjectFileContainer container = new ArtefactProjectFileContainer(this, localSet);
						containers.add(container);
						projectContainerForArtefactSet.put(localSet, container);
					}
				}
			}

			try {
				javaProject.setRawClasspath(
						getLibraryEntries().toArray(new IClasspathEntry[getLibraryEntries().size()]), null);
			} catch (JavaModelException e) {
				ServerProjectManager.handleException(e);
			}
		} else {

		}
	}

	protected boolean isRuntimeExists() {
		return ServerUtils.createDependencyLocator(server.getRuntime()) != null;
	}

	protected void clearFiles() {
		if (project != null) {
			try {
				for (IResource resource : project.members()) {
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
		libraryEntries = new ArrayList<IClasspathEntry>();
		containers = new ArrayList<IServerProjectContainer>();
		artefactSets = new ArrayList<ArtefactSet>();
		projectContainerForArtefactSet = new HashMap<ArtefactSet, IServerProjectContainer>();
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
		project = ws.getRoot().getProject(projectName);
		if (project != null && project.exists()) {
			try {
				project.delete(false, null);
			} catch (CoreException e) {
				ServerProjectManager.handleException(e);
			}
		}
		project = null;
		clearArtefacts();
	}

	void updateWorkspaceProject() {
		if (isRuntimeExists()) {
			try {
				IWorkspace ws = ResourcesPlugin.getWorkspace();
				String projectName = getWorkspaceProjectName();
				project = ws.getRoot().getProject(projectName);
				if (!project.exists()) {
					IProjectDescription description = new ProjectDescription();
					description.setNatureIds(new String[] { JavaCore.NATURE_ID, SERVER_PROJECT_ID });
					description.setComment("Created and managed by Virgo Tooling.");
					description.setName(projectName);
					project.create(description, null);
				}
				if (!project.isOpen()) {
					project.open(null);
				}

				javaProject = (JavaProject) JavaCore.create(project);
			} catch (CoreException e) {
				ServerProjectManager.handleException(e);
			}
		}
	}

	String getWorkspaceProjectName() {
		return server.getName() + " Server";
	}

	public IProject getWorkspaceProject() {
		return project;
	}

	public JavaProject getJavaProject() {
		return javaProject;
	}

	public List<IServerProjectContainer> getContainers() {
		return containers;
	}

	public IServerProjectContainer getContainer(ArtefactSet set) {
		return projectContainerForArtefactSet.get(set);
	}

	public IServer getServer() {
		return server;
	}

	public List<ArtefactSet> getArtefactSets() {
		return artefactSets;
	}

	public List<IClasspathEntry> getLibraryEntries() {
		return libraryEntries;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ServerProject) {
			ServerProject other = (ServerProject) obj;
			return other.project.equals(project);
		}
		return false;
	}
}
