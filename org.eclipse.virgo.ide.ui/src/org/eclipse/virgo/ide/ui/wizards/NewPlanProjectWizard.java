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

package org.eclipse.virgo.ide.ui.wizards;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.pde.internal.ui.wizards.NewWizard;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.virgo.ide.facet.core.CreatePlanProjectOperation;
import org.eclipse.virgo.ide.ui.ServerIdeUiPlugin;

public class NewPlanProjectWizard extends NewWizard {

    WizardNewProjectCreationPage mainPage;

    private NewPlanProjectFilePage planPage;

    public NewPlanProjectWizard() {
        super();
        setWindowTitle(Messages.NewPlanProjectWizard_title);
        setDefaultPageImageDescriptor(ServerIdeUiPlugin.getImageDescriptor("full/wizban/wizban-par.png")); //$NON-NLS-1$
        setNeedsProgressMonitor(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addPages() {
        super.addPages();
        mainPage = new WizardNewProjectCreationPage("planPage");
        mainPage.setTitle(Messages.NewPlanProjectNamePage_title);
        mainPage.setDescription(Messages.NewPlanProjectNamePage_description);
        addPage(mainPage);
        planPage = new NewPlanProjectFilePage();
        addPage(planPage);
    }

    @Override
    public boolean performFinish() {

        URI location = null;
        if (!mainPage.useDefaults()) {
            location = mainPage.getLocationURI();
        }

        String name = planPage.getPlanName();
        boolean atomic = planPage.isAtomic();
        boolean scoped = planPage.isScoped();

        final CreatePlanProjectOperation operation = new CreatePlanProjectOperation(mainPage.getProjectHandle(), location, name, scoped, atomic,
            getShell());

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

        
        IFile file = operation.getPlanFile();
        if (file!=null) {
            IEditorDescriptor editorDesc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(file.getName());
            try {
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(new FileEditorInput(file), editorDesc.getId());
            } catch (PartInitException e) {
                // ignore
            }
        }
        return true;
    }

}
