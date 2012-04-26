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
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.virgo.ide.runtime.core.artefacts.LocalBundleArtefact;
import org.eclipse.virgo.ide.runtime.internal.ui.projects.ServerProject;
import org.eclipse.virgo.ide.runtime.internal.ui.projects.ServerProjectManager;
import org.eclipse.wst.server.core.IServer;

public class BundleContentProvider implements ITreeContentProvider {
	public class BundlePackageFragmentRoot extends JarPackageFragmentRoot {

		public BundlePackageFragmentRoot(IPath externalJarPath, JavaProject project) {
			super(externalJarPath, project);
			// ignore
		}

	}

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof IServer) {
			ServerProject project = ServerProjectManager.getInstance().getProject((IServer) inputElement);
			project.refresh();
			return project.getContainers().toArray(new Object[0]);
		}
		if (inputElement instanceof LocalBundleArtefact) {
			LocalBundleArtefact artefact = (LocalBundleArtefact) inputElement;
			File file = artefact.getFile();
			try {
				IWorkspace ws = ResourcesPlugin.getWorkspace();
				String name = artefact.getRepository().getServer().getName();
				IProject project = ws.getRoot().getProject(name + " Bundles");
				if (!project.exists()) {
					project.create(null);
				}
				if (!project.isOpen()) {
					project.open(null);
				}
				IProjectDescription description = project.getDescription();
				description.setNatureIds(new String[] { JavaCore.NATURE_ID });
				project.setDescription(description, null);
				JavaProject javaProject = (JavaProject) JavaCore.create(project);

				List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();
				IVMInstall vmInstall = JavaRuntime.getDefaultVMInstall();
				LibraryLocation[] locations = JavaRuntime.getLibraryLocations(vmInstall);
				IPath location = new Path(file.getAbsolutePath());
				entries.add(JavaCore.newLibraryEntry(location, null, null));
				//add libs to project class path
				javaProject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), null);

//				javaProject.ad
//				IFile projectFile = project.getFile(location.lastSegment());
//				if (!projectFile.exists()) {
//					projectFile.createLink(location, IResource.NONE, null);
//				}
				IPackageFragmentRoot packageFragmentRoot = new BundlePackageFragmentRoot(new Path(
						file.getAbsolutePath()), javaProject);//javaProject.getPackageFragmentRoot(file.getAbsolutePath());
				packageFragmentRoot.isArchive();
				if (packageFragmentRoot != null) {
					return new Object[] { packageFragmentRoot };
				}
			} catch (CoreException e) {
				throw new RuntimeException(e);
			}

		}
		return new Object[0];
	}

	public Object[] getChildren(Object parentElement) {
		return getElements(parentElement);
	}

	public Object getParent(Object element) {
		return null;
	}

	public boolean hasChildren(Object element) {
		//TODO fix for performance
		return getElements(element).length > 0;
	}

}
