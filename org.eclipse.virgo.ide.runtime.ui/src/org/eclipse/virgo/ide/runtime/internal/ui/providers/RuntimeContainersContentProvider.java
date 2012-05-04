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

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.virgo.ide.runtime.core.artefacts.ArtefactSet;
import org.eclipse.virgo.ide.runtime.internal.ui.projects.IServerProjectContainer;
import org.eclipse.virgo.ide.runtime.internal.ui.projects.ServerProject;
import org.eclipse.virgo.ide.runtime.internal.ui.projects.ServerProjectManager;
import org.eclipse.wst.server.core.IServer;

public class RuntimeContainersContentProvider implements ITreeContentProvider {

	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof IServer) {
			IServer server = (IServer) inputElement;
			ServerProject project = ServerProjectManager.getInstance().getProject(server);
			if (project != null) {
				return project.getArtefactSets().toArray(new Object[0]);
			}
//			return project.getContainers().toArray(new Object[0]);
		}
		if (inputElement instanceof IServerProjectContainer) {
			return ((IServerProjectContainer) inputElement).getMembers();
		}
		if (inputElement instanceof ArtefactSet) {
			ArtefactSet artefactSet = (ArtefactSet) inputElement;
			ServerProject project = ServerProjectManager.getInstance().getProject(
					artefactSet.getRepository().getServer());
			if (project != null) {
				IServerProjectContainer container = project.getContainer(artefactSet);
				if (container != null) {
					return container.getMembers();
				}
				return artefactSet.toArray();
			}
		}
		return new Object[0];
	}

	public Object[] getChildren(Object parentElement) {
		return getElements(parentElement);
	}

	public Object getParent(Object element) {
		if (element instanceof IServerProjectContainer) {
			return ((IServerProjectContainer) element).getServer();
		}
		return null;
	}

	public boolean hasChildren(Object element) {
		//TODO fix for performance
		return getChildren(element).length > 0;
	}

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
}
