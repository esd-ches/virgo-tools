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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.packageview.PackageFragmentRootContainer;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.virgo.ide.runtime.core.artefacts.ArtefactRepository;
import org.eclipse.virgo.ide.runtime.core.artefacts.ArtefactSet;
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

	class ArtefactSetContainer extends PackageFragmentRootContainer implements IClasspathContainer {

		private final LocalArtefactSet artefactSet;

		private final List<IClasspathEntry> entries;

		private final List<IPackageFragmentRoot> roots;

		public ArtefactSetContainer(IJavaProject project, LocalArtefactSet artefactSet) {
			super(project);
			this.artefactSet = artefactSet;

			entries = new ArrayList<IClasspathEntry>();
			roots = new ArrayList<IPackageFragmentRoot>();
			for (IArtefact artefact : artefactSet.getArtefacts()) {
				if (artefact instanceof ILocalArtefact) {
					ILocalArtefact localArtefact = (ILocalArtefact) artefact;
					IPath location = new Path(localArtefact.getFile().getAbsolutePath());
					IClasspathEntry entry = JavaCore.newLibraryEntry(location, null, null);
					entries.add(entry);
					IPackageFragmentRoot packageFragmentRoot = new BundlePackageFragmentRoot(new Path(
							localArtefact.getFile().getAbsolutePath()), javaProject);//javaProject.getPackageFragmentRoot(file.getAbsolutePath());
					roots.add(packageFragmentRoot);
				}
			}
			try {
				JavaCore.setClasspathContainer(getPath(), new IJavaProject[] { javaProject },
						new IClasspathContainer[] { ArtefactSetContainer.this }, null);
			} catch (JavaModelException e) {
				throw new RuntimeException(e);
			}
			libraryEntries.add(JavaCore.newContainerEntry(getPath()));
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof ArtefactSetContainer) {
				ArtefactSetContainer other = (ArtefactSetContainer) obj;
				return artefactSet.equals(other.artefactSet);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return getJavaProject().hashCode();
		}

		@Override
		public IAdaptable[] getChildren() {
			return getPackageFragmentRoots();
		}

		@Override
		public ImageDescriptor getImageDescriptor() {
			return JavaPluginImages.DESC_OBJS_LIBRARY;
		}

		@Override
		public String getLabel() {
			return artefactSet.getShortLabel();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jdt.internal.ui.packageview.PackageFragmentRootContainer#getPackageFragmentRoots()
		 */
		@Override
		public IPackageFragmentRoot[] getPackageFragmentRoots() {
			return roots.toArray(new IPackageFragmentRoot[roots.size()]);
		}

		/**
		 * @see org.eclipse.jdt.core.IClasspathContainer#getClasspathEntries()
		 */
		public IClasspathEntry[] getClasspathEntries() {
			return entries.toArray(new IClasspathEntry[entries.size()]);
		}

		/**
		 * @see org.eclipse.jdt.core.IClasspathContainer#getDescription()
		 */
		public String getDescription() {
			return artefactSet.getShortLabel();
		}

		/**
		 * @see org.eclipse.jdt.core.IClasspathContainer#getKind()
		 */
		public int getKind() {
			return IClasspathContainer.K_APPLICATION;
		}

		/**
		 * @see org.eclipse.jdt.core.IClasspathContainer#getPath()
		 */
		public IPath getPath() {
			return new Path(artefactSet.getFile().getAbsolutePath() + "/"
					+ artefactSet.getArtefactType().getPluralLabel());
		}
	}

	public class BundlePackageFragmentRoot extends JarPackageFragmentRoot {

		public BundlePackageFragmentRoot(IPath externalJarPath, JavaProject project) {
			super(externalJarPath, project);
			// ignore
		}
	}

	final IServer server;

	private JavaProject javaProject;

	private IProject project;

	private List<ArtefactSetContainer> containers;

	private List<IClasspathEntry> libraryEntries;

	public ServerProject(IServer server) {
		this.server = server;
		createProject();
	}

	public void refresh() {
		libraryEntries = new ArrayList<IClasspathEntry>();
		containers = new ArrayList<ServerProject.ArtefactSetContainer>();
		ArtefactRepository repository = RepositoryUtils.getRepositoryContents(server.getRuntime());
		repository.setServer(server);
		List<ArtefactSet> children = new ArrayList<ArtefactSet>();
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
				children.add(repos.getBundleSet());
			}
			if (repos.getLibrarySet().getArtefacts().iterator().hasNext()) {
				children.add(repos.getLibrarySet());
			}
		}

		for (ArtefactSet artefactSet : children) {
			if (artefactSet instanceof LocalArtefactSet) {
				LocalArtefactSet localSet = (LocalArtefactSet) artefactSet;
				ArtefactSetContainer container = new ArtefactSetContainer(javaProject, localSet);
				containers.add(container);
			}
		}
		try {
			javaProject.setRawClasspath(libraryEntries.toArray(new IClasspathEntry[libraryEntries.size()]), null);
		} catch (JavaModelException e) {
			throw new RuntimeException(e);
		}
	}

	protected void createProject() {
		try {
			IWorkspace ws = ResourcesPlugin.getWorkspace();
			String name = server.getName();
			String projectName = name + " Server";
			project = ws.getRoot().getProject(projectName);
			if (!project.exists()) {
				IProjectDescription description = new ProjectDescription();
				description.setNatureIds(new String[] { JavaCore.NATURE_ID });
				description.setComment("Created and managed by Virgo Tooling.");
				description.setName(projectName);
				project.create(description, null);
			}
			if (!project.isOpen()) {
				project.open(null);
			}
			javaProject = (JavaProject) JavaCore.create(project);
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}

	public JavaProject getJavaProject() {
		return javaProject;
	}

	public List<ArtefactSetContainer> getContainers() {
		return containers;
	}
}
