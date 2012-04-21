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
package org.eclipse.virgo.ide.runtime.internal.ui;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.virgo.ide.bundlerepository.domain.ArtefactRepository;
import org.eclipse.virgo.ide.bundlerepository.domain.ArtefactSet;


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
			Object[] children = new Object[2];
			ArtefactRepository artefactRepository = (ArtefactRepository) inputElement;
			children[0] = artefactRepository.getBundleSet();
			children[1] = artefactRepository.getLibrarySet();
			return children;
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
