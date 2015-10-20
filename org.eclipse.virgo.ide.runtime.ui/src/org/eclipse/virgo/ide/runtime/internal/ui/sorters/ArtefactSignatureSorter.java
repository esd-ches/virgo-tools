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

package org.eclipse.virgo.ide.runtime.internal.ui.sorters;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.virgo.ide.runtime.core.artefacts.Artefact;
import org.eclipse.virgo.ide.runtime.internal.ui.projects.ProjectBundleRoot;

/**
 *
 * @author Miles Parker
 *
 */
public class ArtefactSignatureSorter extends ViewerSorter {

    @Override
    public int category(Object element) {
        return 0;
    }

    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {
        if (e1 instanceof ProjectBundleRoot && e2 instanceof ProjectBundleRoot) {
            return compare(viewer, ((ProjectBundleRoot) e1).getArtefact(), ((ProjectBundleRoot) e2).getArtefact());
        }
        if (e1 instanceof Artefact && e2 instanceof Artefact) {
            return ((Artefact) e1).getSignature().compareTo(((Artefact) e2).getSignature());
        }
        return super.compare(viewer, e1, e2);
    }

}
