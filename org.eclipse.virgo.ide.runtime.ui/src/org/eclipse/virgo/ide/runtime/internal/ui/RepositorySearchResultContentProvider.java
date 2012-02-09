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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.virgo.ide.bundlerepository.domain.ArtefactRepository;
import org.eclipse.virgo.ide.bundlerepository.domain.BundleArtefact;
import org.eclipse.virgo.ide.bundlerepository.domain.LibraryArtefact;
import org.eclipse.virgo.ide.runtime.internal.ui.RepositoryViewerUtils.Bundles;
import org.eclipse.virgo.ide.runtime.internal.ui.RepositoryViewerUtils.Libraries;


/**
 * @author Christian Dupuis
 */
public class RepositorySearchResultContentProvider implements ITreeContentProvider {

	private Bundles bundles = null;

	private Libraries libraries = null;

	public void dispose() {
	}

	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof Bundles) {
			bundles = ((Bundles) parentElement);
			List<Object> children = new ArrayList<Object>();
			children.addAll(bundles.getBundles());
			return children.toArray();
		}
		else if (parentElement instanceof Libraries) {
			libraries = ((Libraries) parentElement);
			List<Object> children = new ArrayList<Object>();
			children.addAll(libraries.getLibraries());
			return children.toArray();
		}
		return new Object[0];
	}

	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof ArtefactRepository) {
			Object[] children = new Object[2];
			children[0] = new Bundles(((ArtefactRepository) inputElement).getBundles());
			children[1] = new Libraries(((ArtefactRepository) inputElement).getLibraries());
			return children;
		}
		return new Object[0];
	}

	public Object getParent(Object element) {
		if (element instanceof BundleArtefact) {
			return bundles;
		}
		else if (element instanceof LibraryArtefact) {
			return libraries;
		}
		return null;
	}

	public boolean hasChildren(Object element) {
		return getChildren(element).length > 0;
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
	
}
