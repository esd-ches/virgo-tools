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
package org.eclipse.virgo.ide.export;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.jarpackager.IJarExportRunnable;
import org.eclipse.jdt.ui.jarpackager.JarPackageData;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jst.j2ee.datamodel.properties.IJ2EEComponentExportDataModelProperties;
import org.eclipse.jst.j2ee.internal.web.archive.operations.WebComponentExportDataModelProvider;
import org.eclipse.jst.j2ee.internal.web.archive.operations.WebComponentExportOperation;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.virgo.ide.manifest.core.BundleManifestCorePlugin;
import org.eclipse.virgo.ide.module.core.ServerModuleDelegate;
import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;


/**
 * Utility class for exporting bundle projects
 * @author Christian Dupuis
 * @author Terry Hon
 */
public class BundleExportUtils {

	/**
	 * Find manifest file given a java project
	 * @param project
	 * @return
	 */
	public static IPath locateManifestFile(IJavaProject project) {
		try {

			Set<IPath> outputPaths = new HashSet<IPath>();
			outputPaths.add(project.getOutputLocation());
			IPackageFragmentRoot[] roots = project.getPackageFragmentRoots();
			for (IPackageFragmentRoot root : roots) {
				if (root != null) {
					IClasspathEntry cpEntry = root.getRawClasspathEntry();
					if (cpEntry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
						IPath location = cpEntry.getOutputLocation();
						if (location != null) {
							outputPaths.add(location);
						}
					}
				}
			}

			for (IPath outputPath : outputPaths) {
				if (outputPath != null) {
					IPath manifestPath = getManifesFile(outputPath, project.getProject());
					if (manifestPath != null) {
						return manifestPath;
					}
				}
			}
		}
		catch (JavaModelException e) {
		}
		catch (MalformedURLException e) {
		}
		return null;
	}

	private static IPath getManifesFile(IPath outputLocation, IProject project) throws MalformedURLException {
		IPath path = outputLocation.append(BundleManifestCorePlugin.MANIFEST_FILE_LOCATION);
		IPath projectPath = project.getFullPath();
		path = path.removeFirstSegments(path.matchingFirstSegments(projectPath));
		IResource manifest = project.findMember(path);
		if (manifest != null) {
			return manifest.getFullPath();
		}
		return null;
	}

	/**
	 * Create export operation given project to be exported and location of the JAR.
	 * @param project
	 * @param jarLocation
	 * @param shell
	 * @param warningMessages 
	 * @return
	 */
	public static IJarExportRunnable createExportOperation(IJavaProject project, IPath jarLocation, Shell shell, List<IStatus> warnings) {
		JarPackageData jarPackage = new JarPackageData();
		jarPackage.setJarLocation(jarLocation);
		jarPackage.setExportClassFiles(true);
		jarPackage.setExportJavaFiles(false);
		jarPackage.setOverwrite(true);
		jarPackage.setExportErrors(true);
		jarPackage.setExportWarnings(true);
		jarPackage.setBuildIfNeeded(true);

		Set<Object> outputFiles = new LinkedHashSet<Object>();

		try {
			Set<IClasspathEntry> entries = ServerModuleDelegate.getSourceClasspathEntries(project.getProject(), false);
			for (IPackageFragmentRoot root : project.getPackageFragmentRoots()) {
				if (entries.contains(root.getRawClasspathEntry())) {
					outputFiles.add(root);
				}
			}
		}
		catch (JavaModelException e) {
			// TODO add error handling
		}
		jarPackage.setElements(outputFiles.toArray());

		setManifestFile(project, jarPackage, shell, warnings);

		return jarPackage.createJarExportRunnable(shell);
	}

	private static void setManifestFile(IJavaProject project, JarPackageData jarPackage, Shell shell, List<IStatus> warnings) {
		IPath manifestPath = locateManifestFile(project);
		if (manifestPath != null) {
			File manifestFile = ResourcesPlugin.getWorkspace().getRoot().getLocation().append(manifestPath).toFile();
			BufferedReader reader;
			try {
				reader = new BufferedReader(new FileReader(manifestFile));
				char charactor = 'a';
				while(reader.ready()) {
					charactor = (char) reader.read();
				}
				
				if (charactor != '\n') {
					warnings.add(new Status(Status.WARNING, ServerExportPlugin.PLUGIN_ID, "Manifest file for project " + project.getElementName() + " is missing a '\\n' at the end of file. The exported bundle might not work properly."));
				}
			} catch (FileNotFoundException e) {
				jarPackage.setGenerateManifest(true);
				return;
			} catch (IOException e) {
				jarPackage.setGenerateManifest(true);
				return;
			}
			jarPackage.setGenerateManifest(false);
			jarPackage.setManifestLocation(manifestPath);
		}
		else {
			jarPackage.setGenerateManifest(true);
		}	
	}
	
	/**
	 * Util method for running IJarExportRunnable operation.
	 * @param op export JAR operation
	 * @param reportStatus true if export errors are displayed to the user
	 * @param context runnable context
	 * @param shell
	 * @param warningMessages 
	 * @return if operation was performed successfully
	 */
	public static boolean executeExportOperation(IJarExportRunnable op, boolean reportStatus, IRunnableContext context,
			Shell shell, List<IStatus> warnings) {
		try {
			context.run(true, true, op);
		}
		catch (InterruptedException e) {
			return false;
		}
		catch (InvocationTargetException ex) {
			if (ex.getTargetException() != null) {
				return false;
			}
		}
		IStatus status = op.getStatus();
		if ((warnings.size() > 0 || !status.isOK()) && reportStatus) {
			List<IStatus> children = new ArrayList<IStatus>();
			for(IStatus child: status.getChildren()) {
				children.add(child);
			}
			children.addAll(warnings);
			
			MultiStatus multiStatus = new MultiStatus(ServerExportPlugin.PLUGIN_ID, !status.isOK() ? status.getCode() : Status.WARNING, children.toArray(new Status[0]), !status.isOK() ? status.getMessage() : "There were warnings while exporting bundle project. Click Details to see more...", null);
			ErrorDialog.openError(shell, "Export Warning", null, multiStatus);
			return !(status.matches(IStatus.ERROR));
		}
		return true;
	}

	@SuppressWarnings("restriction")
	public static boolean executeWarExportOperation(IProject bundleProject, String jarName, IFolder settingsFolder) {
		WebComponentExportDataModelProvider provider = new WebComponentExportDataModelProvider();
		IDataModel dataModel = DataModelFactory.createDataModel(provider);
		provider.propertySet(IJ2EEComponentExportDataModelProperties.PROJECT_NAME, bundleProject.getName());
		dataModel.setBooleanProperty(IJ2EEComponentExportDataModelProperties.OPTIMIZE_FOR_SPECIFIC_RUNTIME, false);
		dataModel.setBooleanProperty(IJ2EEComponentExportDataModelProperties.EXPORT_SOURCE_FILES, false);
		dataModel.setBooleanProperty(IJ2EEComponentExportDataModelProperties.OVERWRITE_EXISTING, true);
		dataModel.setProperty(IJ2EEComponentExportDataModelProperties.ARCHIVE_DESTINATION, settingsFolder
				.getRawLocation().append(jarName).toString());
		WebComponentExportOperation operation = new WebComponentExportOperation(dataModel);

		try {
			operation.execute(new NullProgressMonitor(), null);
			settingsFolder.refreshLocal(IResource.DEPTH_ONE, new NullProgressMonitor());
		}
		catch (ExecutionException e) {
			return false;
		}
		catch (CoreException e) {
			return false;
		}
		
		return true;
	}

}
