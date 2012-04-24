/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.virgo.ide.eclipse.wizards;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.ide.undo.CreateProjectOperation;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.StatusUtil;
import org.eclipse.ui.internal.wizards.newresource.ResourceMessages;
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

/**
 * SpringSource Tool Suite Team - Portions of this class were copied from BasicNewProjectResourceWizard. This allows us
 * to create our own wizard around the required functions instead of extending and overriding the entire
 * BasicNewProjectResourceWizard.
 */
@SuppressWarnings("restriction")
public abstract class AbstractNewParProjectWizard extends BasicNewResourceWizard {

	private WizardNewProjectCreationPage mainPage;

	private IProject newProject;

	/**
	 * @see BasicNewProjectResourceWizard
	 */
	protected IProject createNewProject() {
		if (newProject != null) {
			return newProject;
		}

		// get a project handle
		final IProject newProjectHandle = mainPage.getProjectHandle();

		// get a project descriptor
		URI location = null;
		if (!mainPage.useDefaults()) {
			location = mainPage.getLocationURI();
		}

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IProjectDescription description = workspace.newProjectDescription(newProjectHandle.getName());
		description.setLocationURI(location);

		// STS - replaced with associateProjectsToPar()

		// update the referenced project if provided
		// if (referencePage != null) {
		// IProject[] refProjects = referencePage.getReferencedProjects();
		// if (refProjects.length > 0) {
		// description.setReferencedProjects(refProjects);
		// }
		// }

		// create the new project operation
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				CreateProjectOperation op = new CreateProjectOperation(description,
						ResourceMessages.NewProject_windowTitle);
				try {
					// see bug
					// https://bugs.eclipse.org/bugs/show_bug.cgi?id=219901
					// directly execute the operation so that the undo state is
					// not preserved. Making this undoable resulted in too many
					// accidental file deletions.
					op.execute(monitor, WorkspaceUndoUtil.getUIInfoAdapter(getShell()));
				} catch (ExecutionException e) {
					throw new InvocationTargetException(e);
				}
			}
		};

		// run the new project creation operation
		try {
			getContainer().run(true, true, op);
		} catch (InterruptedException e) {
			return null;
		} catch (InvocationTargetException e) {
			Throwable t = e.getTargetException();
			if (t instanceof ExecutionException && t.getCause() instanceof CoreException) {
				CoreException cause = (CoreException) t.getCause();
				StatusAdapter status;
				if (cause.getStatus().getCode() == IResourceStatus.CASE_VARIANT_EXISTS) {
					status = new StatusAdapter(StatusUtil.newStatus(IStatus.WARNING,
							NLS.bind(ResourceMessages.NewProject_caseVariantExistsError, newProjectHandle.getName()),
							cause));
				} else {
					status = new StatusAdapter(StatusUtil.newStatus(cause.getStatus().getSeverity(),
							ResourceMessages.NewProject_errorMessage, cause));
				}
				status.setProperty(StatusAdapter.TITLE_PROPERTY, ResourceMessages.NewProject_errorMessage);
				StatusManager.getManager().handle(status, StatusManager.BLOCK);
			} else {
				StatusAdapter status = new StatusAdapter(new Status(IStatus.WARNING, IDEWorkbenchPlugin.IDE_WORKBENCH,
						0, NLS.bind(ResourceMessages.NewProject_internalError, t.getMessage()), t));
				status.setProperty(StatusAdapter.TITLE_PROPERTY, ResourceMessages.NewProject_errorMessage);
				StatusManager.getManager().handle(status, StatusManager.LOG | StatusManager.BLOCK);
			}
			return null;
		}

		newProject = newProjectHandle;

		return newProject;
	}

	protected WizardNewProjectCreationPage getMainPage() {
		return mainPage;
	}

	protected IProject getNewProject() {
		return newProject;
	}

	protected void setMainPage(WizardNewProjectCreationPage page) {
		mainPage = page;
	}

}
