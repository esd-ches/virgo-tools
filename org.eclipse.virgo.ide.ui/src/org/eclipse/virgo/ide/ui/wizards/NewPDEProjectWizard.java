/*******************************************************************************
 *  Copyright (c) 2015 GianMaria Romanato
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     GianMaria Romanato - initial API and implementation
 *******************************************************************************/

package org.eclipse.virgo.ide.ui.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.pde.core.project.IBundleProjectService;
import org.eclipse.pde.ui.templates.NewPluginProjectFromTemplateWizard;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.virgo.ide.pde.core.internal.cmd.SetupProjectOperation;
import org.eclipse.virgo.ide.ui.ServerIdeUiPlugin;

/**
 * A PDE template wizard used to create PDE projects for Virgo tools.
 * <p />
 *
 */
public class NewPDEProjectWizard extends NewPluginProjectFromTemplateWizard {

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getTemplateID() {
        return "org.eclipse.virgo.ide.ui.pdetemplate"; //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean performFinish() {
        boolean result = super.performFinish();
        if (result) {
            // on finish modify the newly created PDE project
            WizardNewProjectCreationPage newProjectCreationPage = (WizardNewProjectCreationPage) getPage("main"); //$NON-NLS-1$
            IProject project = newProjectCreationPage.getProjectHandle();
            IBundleProjectService service = PlatformUI.getWorkbench().getService(IBundleProjectService.class);
            String contextRoot = getContextRoot();

            final SetupProjectOperation operation = new SetupProjectOperation(project, contextRoot, service);

            try {
                getContainer().run(true, false, new IRunnableWithProgress() {

                    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                        try {
                            ResourcesPlugin.getWorkspace().run(operation, monitor);
                        } catch (CoreException e) {
                            StatusManager.getManager().handle(e.getStatus(), StatusManager.LOG | StatusManager.SHOW);
                        }
                    }
                });
            } catch (Exception e) {
                IStatus s = new Status(IStatus.ERROR, ServerIdeUiPlugin.PLUGIN_ID, e.getMessage(), e);
                StatusManager.getManager().handle(s, StatusManager.LOG | StatusManager.SHOW);
            }
        }
        return true;
    }

    private String getContextRoot() {
        NewPDEProjectWABPage wabPage = (NewPDEProjectWABPage) getPage(NewPDEProjectWABPage.class.getSimpleName());
        String contextRoot = wabPage.getContextRoot();
        return contextRoot;
    }
}
