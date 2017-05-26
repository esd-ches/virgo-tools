/*******************************************************************************
 *  Copyright (c) 2016 GianMaria Romanato
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     GianMaria Romanato - initial API and implementation
 *******************************************************************************/

package org.eclipse.virgo.ide.ui.internal.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.virgo.ide.facet.core.FacetCorePlugin;
import org.eclipse.virgo.ide.pde.core.internal.Constants;
import org.eclipse.virgo.ide.ui.ServerIdeUiPlugin;

/**
 * ConvertPlugInProject is a contextual menu action in the Virgo submenu for migrating PDE Plug-in projects to the
 * official Virgo tools support for PDE.
 *
 * The migration consists in adding project natures.
 */
public class ConvertPlugInProject extends AbstractConvertAction implements IObjectActionDelegate {

    private static final String PDE_NATURE = "org.eclipse.pde.PluginNature"; //$NON-NLS-1$

    @Override
    protected String getNature() {
        return PDE_NATURE;
    }

    @Override
    protected void migrate(IProgressMonitor monitor, IProject project) {
        monitor.beginTask("", 1); //$NON-NLS-1$
        try {
            IProjectDescription description = project.getDescription();
            String[] natures = description.getNatureIds();

            String[] newNatures = new String[natures.length + 3];
            System.arraycopy(natures, 0, newNatures, 0, natures.length);
            newNatures[natures.length] = "org.eclipse.wst.common.project.facet.core.nature"; //$NON-NLS-1$
            newNatures[natures.length + 1] = FacetCorePlugin.BUNDLE_NATURE_ID;
            newNatures[natures.length + 2] = Constants.NATURE_ID;

            description.setNatureIds(newNatures);

            project.setDescription(description, monitor);
        } catch (CoreException e) {
            Status s = new Status(IStatus.ERROR, ServerIdeUiPlugin.PLUGIN_ID, e.getMessage());
            StatusManager.getManager().handle(s, StatusManager.SHOW);
        } finally {
            monitor.done();
        }
    }

    @Override
    protected boolean showConfirmationDialog() {
        return MessageDialog.openQuestion(this.part.getSite().getShell(), Messages.ConvertPlugInProject_title, Messages.ConvertPlugInProject_message);
    }

}
