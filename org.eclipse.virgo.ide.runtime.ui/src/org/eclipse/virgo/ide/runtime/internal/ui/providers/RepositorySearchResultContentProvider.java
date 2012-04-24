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
import org.eclipse.virgo.ide.runtime.core.artefacts.ArtefactRepository;
import org.eclipse.virgo.ide.runtime.core.artefacts.ArtefactSet;

/**
 * @author Christian Dupuis
 * @author Miles Parker
 */
public class RepositorySearchResultContentProvider implements ITreeContentProvider {

	public void dispose() {
	}

	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof ArtefactSet) {
			ArtefactSet artefacts = (ArtefactSet) parentElement;
			return artefacts.toArray();
		}
		return new Object[0];
	}

	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof ArtefactRepository) {
			ArtefactRepository artefactRepository = (ArtefactRepository) inputElement;
			return new Object[] { artefactRepository.getBundleSet(), artefactRepository.getLibrarySet() };
		}
		return new Object[0];
	}

	public Object getParent(Object element) {
		if (element instanceof ArtefactSet) {
			return ((ArtefactSet) element).getRepository();
		}
		return null;
	}

	public boolean hasChildren(Object element) {
		return getChildren(element).length > 0;
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

}
