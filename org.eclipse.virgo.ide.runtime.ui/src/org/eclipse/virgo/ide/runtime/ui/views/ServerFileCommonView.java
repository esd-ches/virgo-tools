/*******************************************************************************
 * Copyright (c) 2009, 2012 SpringSource, a divison of VMware, Inc.
 * Copyright (c) IBM Corporation (code cribbed from pde and navigator.)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource, a division of VMware, Inc. - initial API and implementation
 *******************************************************************************/

package org.eclipse.virgo.ide.runtime.ui.views;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.virgo.ide.runtime.internal.ui.projects.ServerProject;
import org.eclipse.virgo.ide.runtime.internal.ui.projects.ServerProjectManager;
import org.eclipse.virgo.ide.runtime.internal.ui.providers.ServerFileSelection;
import org.eclipse.wst.server.core.IServer;

/**
 * 
 * @see org.eclipse.pde.internal.ui.views.dependencies.DependenciesView
 * @author Miles Parker
 * 
 */
@SuppressWarnings("restriction")
public abstract class ServerFileCommonView extends CommonView implements ISelectionListener {

	private Collection<IFile> currentFiles;

	protected IResourceChangeListener resourceListener;

	private final class DeltaVisitor implements IResourceDeltaVisitor {
		boolean change;

		public boolean visit(IResourceDelta delta) {
			if (change) {
				return false;
			}
			//only interested in changed resources (not added or removed)
			if (delta.getKind() != IResourceDelta.CHANGED) {
				return true;
			}
			//only interested in content changes
			if ((delta.getFlags() & IResourceDelta.CONTENT) == 0) {
				return true;
			}
			IResource resource = delta.getResource();
			if (currentFiles.contains(resource)) {
				change = true;
			}
			return true;
		}
	}

	/**
	 * @see org.eclipse.ui.navigator.CommonNavigator#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite aParent) {
		super.createPartControl(aParent);
		resourceListener = new IResourceChangeListener() {
			public void resourceChanged(IResourceChangeEvent event) {
				//we are only interested in POST_CHANGE events
				if (getServers().size() > 0) {
					if (event.getType() != IResourceChangeEvent.POST_CHANGE) {
						return;
					}
					IResourceDelta rootDelta = event.getDelta();
					boolean refresh = false;
					//get the delta, if any, for the documentation directory
					for (IServer server : getServers()) {
						ServerProject project = ServerProjectManager.getInstance().getProject(server);
						if (project != null) {
							for (String dir : getManagedDirs()) {
								IFolder folder = project.getWorkspaceProject().getFolder(dir);
								IResourceDelta docDelta = rootDelta.findMember(folder.getFullPath());
								if (docDelta == null) {
									return;
								}
								DeltaVisitor visitor = new DeltaVisitor();
								try {
									docDelta.accept(visitor);
								} catch (CoreException e) {
								}
								if (visitor.change) {
									refresh = true;
									break;
								}
							}
						}
					}
					if (refresh) {
						refreshView();
					}
				}
			}
		};
		ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceListener);
		currentFiles = new HashSet<IFile>();
	}

	/**
	 * @see org.eclipse.virgo.ide.runtime.ui.views.CommonView#update()
	 */
	@Override
	protected void update() {
		currentFiles = new HashSet<IFile>();
		for (IServer server : getServers()) {
			Object[] elements = ((ITreeContentProvider) getCommonViewer().getContentProvider()).getElements(server);
			for (Object object : elements) {
				if (object instanceof ServerFileSelection) {
					currentFiles.add(((ServerFileSelection) object).getFile());
				}
				if (object instanceof IFile) {
					currentFiles.add((IFile) object);
				}
			}
		}
		super.update();
	}

	/**
	 * @see org.eclipse.virgo.ide.runtime.ui.views.CommonView#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceListener);
	}

	public abstract String[] getManagedDirs();
}
