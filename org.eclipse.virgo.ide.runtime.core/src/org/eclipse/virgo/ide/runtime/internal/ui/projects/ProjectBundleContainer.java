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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.packageview.PackageFragmentRootContainer;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.virgo.ide.runtime.core.ServerCorePlugin;
import org.eclipse.virgo.ide.runtime.core.artefacts.IArtefact;
import org.eclipse.virgo.ide.runtime.core.artefacts.ILocalArtefact;
import org.eclipse.virgo.ide.runtime.core.artefacts.LocalArtefactSet;
import org.eclipse.wst.server.core.IServer;

/**
 * Provides a wrapper for an artefact set that acts as a Library. TODO we may not need artefact sets at all.
 * 
 * @author Miles Parker
 */
@SuppressWarnings("restriction")
public class ProjectBundleContainer extends PackageFragmentRootContainer implements IClasspathContainer,
		IServerProjectContainer {

	private final LocalArtefactSet artefactSet;

	private final List<IClasspathEntry> entries;

	private final List<IPackageFragmentRoot> roots;

	protected ProjectBundleContainer(final ServerProject serverProject, LocalArtefactSet artefactSet) {
		super(serverProject.getJavaProject());
		this.artefactSet = artefactSet;

		entries = new ArrayList<IClasspathEntry>();
		roots = new ArrayList<IPackageFragmentRoot>();
		for (IArtefact artefact : artefactSet.getArtefacts()) {
			if (artefact instanceof ILocalArtefact) {
				ILocalArtefact localArtefact = (ILocalArtefact) artefact;
				IPath location = new Path(localArtefact.getFile().getAbsolutePath());
				IClasspathEntry entry = JavaCore.newLibraryEntry(location, null, null);
				entries.add(entry);
				IPackageFragmentRoot packageFragmentRoot = new ProjectBundleRoot(this, localArtefact);
				roots.add(packageFragmentRoot);
			}
		}
		try {
			JavaCore.setClasspathContainer(getPath(), new IJavaProject[] { serverProject.javaProject },
					new IClasspathContainer[] { ProjectBundleContainer.this }, null);
			serverProject.getLibraryEntries().add(JavaCore.newContainerEntry(getPath()));
		} catch (JavaModelException e) {
			StatusManager.getManager().handle(
					new Status(IStatus.ERROR, ServerCorePlugin.PLUGIN_ID, "Problem occurred in bundle manager.", e));
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ProjectBundleContainer) {
			ProjectBundleContainer other = (ProjectBundleContainer) obj;
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
		return new Path(artefactSet.getFile().getAbsolutePath() + "/" + artefactSet.getArtefactType().getPluralLabel());
	}

	public IServer getServer() {
		return artefactSet.getRepository().getServer();
	}

	public LocalArtefactSet getArtefactSet() {
		return artefactSet;
	}

	/**
	 * @see org.eclipse.virgo.ide.runtime.internal.ui.projects.IServerProjectContainer#getMembers()
	 */
	public Object[] getMembers() {
		return getPackageFragmentRoots();
	}
}