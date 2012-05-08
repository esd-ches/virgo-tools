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

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.virgo.ide.runtime.internal.ui.projects.IServerProjectArtefact;
import org.eclipse.virgo.ide.runtime.internal.ui.projects.IServerProjectContainer;
import org.eclipse.virgo.ide.runtime.internal.ui.projects.ServerProject;
import org.eclipse.virgo.ide.runtime.internal.ui.projects.ServerProjectManager;
import org.eclipse.wst.server.core.IServer;

/**
 * Common content provider for repository installation nodes.
 * 
 * @author Miles Parker
 */
public class FlattenedArtefactsContentProvider extends RuntimeContainersContentProvider {

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof IServer) {
			ServerProject project = ServerProjectManager.getInstance().getProject((IServer) inputElement);
			if (project != null) {
				IServerProjectContainer[] containers = project.getContainers().toArray(new IServerProjectContainer[0]);
				Object[] members = new Object[0];
				for (IServerProjectContainer container : containers) {
					Object[] roots = container.getMembers();
					members = ArrayUtils.addAll(members, roots);
				}
				return members;
			}
		}
		return super.getElements(inputElement);
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof IServerProjectArtefact) {
			return ((IServerProjectArtefact) element).getContainer().getServer();
		}
		return super.getParent(element);
	}

	/**
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	@Override
	public void dispose() {
	}

	/**
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object,
	 *      java.lang.Object)
	 */
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	@Override
	public Object[] getChildren(Object parentElement) {
		return getElements(parentElement);
	}

	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	@Override
	public boolean hasChildren(Object element) {
		return getElements(element).length > 0;
	}
}