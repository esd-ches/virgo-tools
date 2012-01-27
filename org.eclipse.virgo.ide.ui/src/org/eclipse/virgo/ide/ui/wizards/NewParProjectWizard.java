/*******************************************************************************
 * Copyright (c) 2009 SpringSource, a divison of VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource, a division of VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.virgo.ide.ui.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jst.common.project.facet.JavaProjectFacetCreationDataModelProvider;
import org.eclipse.pde.internal.ui.wizards.IProjectProvider;
import org.eclipse.pde.internal.ui.wizards.plugin.AbstractFieldData;
import org.eclipse.pde.internal.ui.wizards.plugin.PluginFieldData;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.dialogs.WizardNewProjectReferencePage;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.ISetSelectionTarget;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.virgo.ide.eclipse.wizards.AbstractNewParProjectWizard;
import org.eclipse.virgo.ide.facet.core.FacetCorePlugin;
import org.eclipse.virgo.ide.manifest.core.BundleManifestUtils;
import org.eclipse.virgo.ide.par.Bundle;
import org.eclipse.virgo.ide.par.Par;
import org.eclipse.virgo.ide.par.ParFactory;
import org.eclipse.virgo.ide.par.ParPackage;
import org.eclipse.virgo.ide.ui.ServerIdeUiPlugin;
import org.eclipse.virgo.ide.ui.editors.ParManifestEditor;
import org.eclipse.virgo.ide.ui.editors.ParUtils;
import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetProjectCreationDataModelProperties;
import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.eclipse.wst.common.project.facet.core.runtime.IRuntime;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.SpringCoreUtils;

/**
 * @author Christian Dupuis
 * @author Leo Dos Santos
 */
public class NewParProjectWizard extends AbstractNewParProjectWizard implements INewWizard {

	private static final String PAR_FILE_NAME = ".settings/org.eclipse.virgo.ide.runtime.core.par.xml";

	private static final String ENCODING_UTF8 = "UTF-8";

	private WizardNewProjectCreationPage mainPage;

	private NewParInformationPage bundlePage;

	private WizardNewProjectReferencePage referencePage;

	private IProjectProvider projectProvider;

	private final AbstractFieldData bundleData;

	private final IDataModel model;

	private final String title = "New PAR Project";

	protected ParPackage parPackage = ParPackage.eINSTANCE;

	protected ParFactory parFactory = parPackage.getParFactory();

	public NewParProjectWizard() {
		super();
		setWindowTitle(title);
		setNeedsProgressMonitor(true);
		bundleData = new PluginFieldData();
		model = DataModelFactory.createDataModel(new JavaProjectFacetCreationDataModelProvider());
	}

	private void addFacetsToProject(final IProject project) {
		WorkspaceModifyOperation oper = new WorkspaceModifyOperation() {
			@Override
			protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
					InterruptedException {
				SpringCoreUtils.addProjectNature(project.getProject(), SpringCore.NATURE_ID, monitor);
				IFacetedProject fProject = ProjectFacetsManager.create(project.getProject(), true, monitor);

				// WST 3.0 only
				// fProject.createWorkingCopy().addProjectFacet(
				// ProjectFacetsManager.getProjectFacet("jst.java").
				// getLatestVersion());
				// fProject.createWorkingCopy().addProjectFacet(
				// ProjectFacetsManager.getProjectFacet(FacetCorePlugin.
				// BUNDLE_FACET_ID).getLatestVersion());

				fProject.installProjectFacet(ProjectFacetsManager.getProjectFacet(FacetCorePlugin.PAR_FACET_ID)
						.getDefaultVersion(), null, monitor);
				IRuntime runtime = (IRuntime) model.getProperty(IFacetProjectCreationDataModelProperties.FACET_RUNTIME);
				if (runtime != null
						&& runtime.supports(ProjectFacetsManager.getProjectFacet(FacetCorePlugin.PAR_FACET_ID))) {
					fProject.setTargetedRuntimes(Collections.singleton(runtime), monitor);
				}
			}
		};

		try {
			getContainer().run(true, true, oper);
		}
		catch (InvocationTargetException e) {
			StatusManager.getManager()
					.handle(new Status(IStatus.ERROR, ServerIdeUiPlugin.PLUGIN_ID,
							"Exception while adding project facets.", e));
		}
		catch (InterruptedException e) {
			StatusManager.getManager().handle(
					new Status(IStatus.WARNING, ServerIdeUiPlugin.PLUGIN_ID,
							"Interruption while adding project facets.", e));
		}
	}

	@Override
	public void addPages() {
		mainPage = new NewParProjectSettingsPage("basicNewProjectPage", getSelection());
		setMainPage(mainPage);
		addPage(mainPage);

		projectProvider = new IProjectProvider() {
			public IPath getLocationPath() {
				return getProject().getLocation();
			}

			public IProject getProject() {
				return mainPage.getProjectHandle();
			}

			public String getProjectName() {
				return mainPage.getProjectName();
			}
		};

		bundlePage = new NewParInformationPage(title, projectProvider, bundleData, model);
		addPage(bundlePage);

		// only add page if there are already projects in the workspace
		if (ResourcesPlugin.getWorkspace().getRoot().getProjects().length > 0) {
			referencePage = new NewParProjectReferencePage("basicReferenceProjectPage");
			addPage(referencePage);
		}
	}

	private IFile associateProjectsToPar(IProject[] references, final IFile parFile) {
		// Add the initial model object to the contents.
		//
		final Par par = parFactory.createPar();
		for (IProject workspaceProject : references) {
			Bundle bundle = parFactory.createBundle();
			bundle.setSymbolicName(ParUtils.getSymbolicName(workspaceProject));
			par.getBundle().add(bundle);
		}

		try {
			WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {
				@Override
				protected void execute(IProgressMonitor progressMonitor) {
					try {
						// Create a resource set
						//
						ResourceSet resourceSet = new ResourceSetImpl();

						// Get the URI of the model file.
						//
						org.eclipse.emf.common.util.URI fileURI = org.eclipse.emf.common.util.URI
								.createPlatformResourceURI(parFile.getFullPath().toString(), true);

						// Create a resource for this file.
						//
						Resource resource = resourceSet.createResource(fileURI);
						resource.getContents().add(par);

						// Save the contents of the resource to the file
						// system.
						//
						Map<Object, Object> options = new HashMap<Object, Object>();
						options.put(XMLResource.OPTION_ENCODING, ENCODING_UTF8);
						resource.save(options);
					}
					catch (Exception exception) {
						ServerIdeUiPlugin.getDefault().log(exception);
					}
					finally {
						progressMonitor.done();
					}
				}
			};
			getContainer().run(false, false, operation);
			return parFile;
		}
		catch (Exception exception) {
			ServerIdeUiPlugin.getDefault().log(exception);
			return null;
		}
	}

	@Override
	public boolean canFinish() {
		IWizardPage page = getContainer().getCurrentPage();
		return super.canFinish() && page != getMainPage();
	}

	@Override
	public boolean performFinish() {
		createNewProject();
		if (getNewProject() == null) {
			return false;
		}

		bundlePage.performPageFinish();
		addFacetsToProject(getNewProject());
		writeBundleData(getNewProject());

		if (referencePage != null) {
			IProject[] references = referencePage.getReferencedProjects();
			associateProjectsToPar(references, getNewProject().getFile(PAR_FILE_NAME));
		}

		IWorkingSet[] workingSets = mainPage.getSelectedWorkingSets();
		if (workingSets.length > 0) {
			getWorkbench().getWorkingSetManager().addToWorkingSets(getNewProject(), workingSets);
		}

		IFile manifestFile = (IFile) getNewProject().findMember("META-INF/MANIFEST.MF");
		if (manifestFile != null) {
			// Select the new file resource in the current view.
			//
			IWorkbenchWindow workbenchWindow = getWorkbench().getActiveWorkbenchWindow();
			IWorkbenchPage page = workbenchWindow.getActivePage();
			final IWorkbenchPart activePart = page.getActivePart();
			if (activePart instanceof ISetSelectionTarget) {
				final ISelection targetSelection = new StructuredSelection(manifestFile);
				getShell().getDisplay().asyncExec(new Runnable() {
					public void run() {
						((ISetSelectionTarget) activePart).selectReveal(targetSelection);
					}
				});
			}

			// Open an editor on the new file.
			//
			try {
				page.openEditor(new FileEditorInput(manifestFile), ParManifestEditor.ID_EDITOR);
			}
			catch (PartInitException exception) {
				MessageDialog.openError(workbenchWindow.getShell(), "Error opening editor", exception.getMessage());
			}
			return true;
		}
		return false;
	}

	private void writeBundleData(final IProject project) {
		WorkspaceModifyOperation oper = new WorkspaceModifyOperation() {
			@Override
			protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
					InterruptedException {
				BundleManifestUtils.createNewParManifest(project, bundleData.getId(), bundleData.getVersion(),
						bundleData.getName(), bundleData.getProvider());
				project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
			}
		};

		try {
			getContainer().run(true, true, oper);
		}
		catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void initializeDefaultPageImageDescriptor() {
		setDefaultPageImageDescriptor(ServerIdeUiPlugin.getImageDescriptor("full/wizban/wizban-par.png"));
	}

}
