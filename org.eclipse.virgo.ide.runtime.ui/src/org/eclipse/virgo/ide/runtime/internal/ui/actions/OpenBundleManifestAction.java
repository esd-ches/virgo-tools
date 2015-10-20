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

package org.eclipse.virgo.ide.runtime.internal.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.virgo.ide.runtime.core.artefacts.LocalBundleArtefact;
import org.eclipse.virgo.ide.ui.editors.BundleManifestEditor;
import org.eclipse.virgo.ide.ui.editors.model.BundleModelUtility;

/**
 * Open the manifest for the supplied bundle.
 *
 * @author Miles Parker
 * @since 1.0.0
 */
public class OpenBundleManifestAction implements IObjectActionDelegate {

    LocalBundleArtefact artefact;

    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        // nothing to do here
    }

    public void run(IAction action) {
        if (this.artefact != null) {
            BundleManifestEditor.openExternalPlugin(this.artefact.getFile(), BundleModelUtility.F_MANIFEST_FP);
        }
    }

    public void selectionChanged(IAction action, ISelection selection) {
        if (!selection.isEmpty()) {
            if (selection instanceof IStructuredSelection) {
                Object obj = ((IStructuredSelection) selection).getFirstElement();
                if (obj instanceof LocalBundleArtefact) {
                    this.artefact = (LocalBundleArtefact) obj;
                }
            }
        }
    }

}
