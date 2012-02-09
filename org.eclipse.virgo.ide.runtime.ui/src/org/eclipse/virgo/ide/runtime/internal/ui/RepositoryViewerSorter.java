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

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.virgo.ide.bundlerepository.domain.BundleArtefact;
import org.eclipse.virgo.ide.bundlerepository.domain.LibraryArtefact;
import org.eclipse.virgo.ide.runtime.internal.ui.RepositoryViewerUtils.Bundles;
import org.eclipse.virgo.ide.runtime.internal.ui.RepositoryViewerUtils.Libraries;
import org.eclipse.virgo.ide.runtime.internal.ui.RepositoryViewerUtils.LocationAwareBundles;
import org.eclipse.virgo.ide.runtime.internal.ui.RepositoryViewerUtils.LocationAwareLibraries;


/**
 * @author Christian Dupuis
 */
public class RepositoryViewerSorter extends ViewerSorter {

	@Override
	public int category(Object element) {
		if (element instanceof Bundles) {
			return 1;
		}
		else if (element instanceof Libraries) {
			return 2;
		}
		else if (element instanceof BundleArtefact) {
			return 3;
		}
		else if (element instanceof LibraryArtefact) {
			return 4;
		}
		return super.category(element);
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		if (e1 instanceof LocationAwareBundles && e2 instanceof LocationAwareBundles) {
			return ((LocationAwareBundles) e1).getLocation().compareTo(
					((LocationAwareBundles) e2).getLocation());
		}
		else if (e1 instanceof LocationAwareLibraries && e2 instanceof LocationAwareLibraries) {
			return ((LocationAwareLibraries) e1).getLocation().compareTo(
					((LocationAwareLibraries) e2).getLocation());
		}
		else if (e1 instanceof BundleArtefact && e2 instanceof BundleArtefact) {
			String st1 = ((BundleArtefact) e1).getSymbolicName() + ";"
					+ ((BundleArtefact) e1).getVersion();
			String st2 = ((BundleArtefact) e2).getSymbolicName() + ";"
					+ ((BundleArtefact) e2).getVersion();
			return super.compare(viewer, st1, st2);
		}
		else if (e1 instanceof LibraryArtefact && e2 instanceof LibraryArtefact) {
			String st1 = ((LibraryArtefact) e1).getSymbolicName() + ";"
					+ ((LibraryArtefact) e1).getVersion();
			String st2 = ((LibraryArtefact) e2).getSymbolicName() + ";"
					+ ((LibraryArtefact) e2).getVersion();
			return super.compare(viewer, st1, st2);
		}
		return super.compare(viewer, e1, e2);
	}
	
}
