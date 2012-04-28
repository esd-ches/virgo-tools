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
import org.eclipse.virgo.ide.runtime.internal.ui.editor.RepositoryBrowserEditorPage;
import org.eclipse.wst.server.core.IServer;

/**
 * Common content provider for repository installation nodes.
 * 
 * @author Miles Parker
 */
public class RepositoryPageContentProvider implements ITreeContentProvider {

	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof RepositoryBrowserEditorPage) {
			IServer server = ((RepositoryBrowserEditorPage) inputElement).getServer().getOriginal();
			return new Object[] { server };
		}
		return new Object[0];
	}

	public Object getParent(Object element) {
		return null;
	}

	/**
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
	}

	/**
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object,
	 *      java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parentElement) {
		return getElements(parentElement);
	}

	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object element) {
		return element instanceof RepositoryBrowserEditorPage;
	}
}