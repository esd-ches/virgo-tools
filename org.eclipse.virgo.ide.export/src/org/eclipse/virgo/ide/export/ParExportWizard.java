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
package org.eclipse.virgo.ide.export;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.jarpackager.IJarBuilder;
import org.eclipse.jdt.ui.jarpackager.IJarExportRunnable;
import org.eclipse.jdt.ui.jarpackager.JarPackageData;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.virgo.ide.facet.core.FacetCorePlugin;
import org.eclipse.virgo.ide.facet.core.FacetUtils;
import org.eclipse.virgo.ide.manifest.core.BundleManifestCorePlugin;
import org.eclipse.virgo.ide.par.Bundle;
import org.eclipse.virgo.ide.par.Par;

import org.eclipse.virgo.util.osgi.manifest.BundleManifest;

/**
 * Export wizard for exporting par project
 * @author Christian Dupuis
 * @author Terry Hon
 */
public class ParExportWizard extends Wizard implements IExportWizard {

	private ParExportWizardPage wizardPage;

	private IStructuredSelection selection;

	private static final String TITLE = "Par Export Wizard";

	private static final String PAR_FILE_NAME = ".settings/org.eclipse.virgo.ide.runtime.core.par.xml";

	@Override
	public void addPages() {
		wizardPage = new ParExportWizardPage(selection);
		addPage(wizardPage);
	}

	@Override
	public boolean performFinish() {
		IProject project = wizardPage.getSelectedProject();
		IPath jarLocation = wizardPage.getJarLocation();
		
		if (jarLocation.toFile().exists() && ! wizardPage.getOverwrite()) {
			boolean overwrite = MessageDialog.openQuestion(getShell(), "Overwrite File", "The file " + jarLocation.toOSString() + " already exists. Do you want to overwrite the existing file?");
			if (! overwrite) {
				return false;
			}
		}

		return exportPar(project, jarLocation, getContainer(), getShell());
	}

	// public for testing
	static public boolean exportPar(IProject project, IPath parLocation, IRunnableContext context, Shell shell) {
		IFile parFile = project.getFile(PAR_FILE_NAME);
		IFolder settingsFolder = project.getFolder(".settings");
		IPath settingsPath = settingsFolder.getLocation();

		List<IStatus> warnings = new ArrayList<IStatus>();
		
		URI fileURI = URI.createPlatformResourceURI(parFile.getFullPath().toString(), true);
		ResourceSet resourceSet = new ResourceSetImpl();
		Resource resource = resourceSet.getResource(fileURI, true);
		EList<EObject> contents = resource.getContents();
		Set<IResource> parElements = new HashSet<IResource>();
		for (EObject content : contents) {
			if (content instanceof Par) {
				Par par = (Par) content;
				EList<Bundle> bundles = par.getBundle();
				for (Bundle bundle : bundles) {
					String bundleName = bundle.getSymbolicName();
					IProject bundleProject = ResourcesPlugin.getWorkspace().getRoot().getProject(bundleName);
					BundleManifest manifest = BundleManifestCorePlugin.getBundleManifestManager().getBundleManifest(
							JavaCore.create(bundleProject));
					String jarName = null;
					if (manifest != null && manifest.getBundleVersion() != null
							&& manifest.getBundleSymbolicName() != null) {
						String version = manifest.getBundleVersion().toString();
						String name = (manifest.getBundleSymbolicName() != null
								&& manifest.getBundleSymbolicName().getSymbolicName() != null ? manifest
								.getBundleSymbolicName().getSymbolicName() : bundleProject.getName());
						jarName = name + "-" + version;
					}
					else {
						jarName = bundleProject.getProject().toString();
					}

					// Support for nested wars
					if (FacetUtils.hasProjectFacet(bundleProject, FacetCorePlugin.WEB_FACET_ID)) {
						jarName = jarName + ".war";
						if (BundleExportUtils.executeWarExportOperation(bundleProject, jarName, settingsFolder)) {
							IResource bundleJar = settingsFolder.getFile(jarName);
							if (bundleJar.exists()) {
								parElements.add(bundleJar);
							}
						}
					}
					else {
						IJavaProject javaBundleProject = JavaCore.create(bundleProject);
						jarName = jarName + ".jar";
						IPath jarPath = settingsPath.append(jarName);
						
						IJarExportRunnable op = BundleExportUtils.createExportOperation(javaBundleProject, jarPath,
								shell, warnings);
						if (BundleExportUtils.executeExportOperation(op, false, context, shell, warnings)) {
							IResource bundleJar = settingsFolder.getFile(jarName);
							parElements.add(bundleJar);
						}
					}
				}
			}
		}

		Set<IResource> jarElements = new HashSet<IResource>();
		jarElements.addAll(parElements);
		try {
			IResource[] rootMembers = project.members();
			for (IResource rootMember : rootMembers) {
				String fileExtension = rootMember.getFileExtension();
				if (fileExtension != null && fileExtension.equals("jar")) {
					jarElements.add(rootMember);
				}
			}
		}
		catch (CoreException e1) {
		}

		JarPackageData parPackage = new JarPackageData();
		parPackage.setJarLocation(parLocation);
		parPackage.setExportClassFiles(true);
		parPackage.setExportOutputFolders(false);
		parPackage.setExportJavaFiles(false);
		parPackage.setElements(jarElements.toArray());
		parPackage.setOverwrite(true);

		IPath manifestPath = locateManifestFile(project);
		if (manifestPath != null) {
			parPackage.setGenerateManifest(false);
			parPackage.setManifestLocation(manifestPath);
		}
		else {
			parPackage.setGenerateManifest(true);
		}

		IJarBuilder builder = parPackage.getJarBuilder();
		try {
			builder.open(parPackage, shell, null);
			for (IResource elem : jarElements) {
				if (elem instanceof IFile) {
					try {
						builder.writeFile((IFile) elem, new Path(elem.getName()));
					}
					catch (CoreException e) {
					}
				}
			}
			builder.close();
		}
		catch (CoreException e1) {
		}

		for (IResource parElement : parElements) {
			try {
				parElement.delete(true, new NullProgressMonitor());
			}
			catch (CoreException e) {
			}
		}
		
		if (warnings.size() > 0) {
			ErrorDialog.openError(shell, "PAR export warnings", null, new MultiStatus(ServerExportPlugin.PLUGIN_ID, Status.WARNING, warnings.toArray(new IStatus[0]), "There were warnings while export the PAR project. Click Details to see more...", null));
		}
		return true;
	}

	private static IPath locateManifestFile(IProject project) {
		Path path = new Path("META-INF/MANIFEST.MF");
		IResource manifestFile = project.findMember(path);
		if (manifestFile != null)
			return new Path(project.getName()).append(path);
		return null;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
		setWindowTitle(TITLE);
		setDefaultPageImageDescriptor(ServerExportPlugin.getImageDescriptor("full/wizban/wizban-par.png"));
	}

}
