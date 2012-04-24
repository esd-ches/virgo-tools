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
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.jarpackager.IJarExportRunnable;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

/**
 * Export wizard for exporting bundle project
 * 
 * @author Christian Dupuis
 * @author Terry Hon
 */
public class BundleExportWizard extends Wizard implements IExportWizard {

	private BundleExportWizardPage wizardPage;

	private IStructuredSelection selection;

	private static final String TITLE = "Bundle Export Wizard";

	@Override
	public void addPages() {
		wizardPage = new BundleExportWizardPage(selection);
		addPage(wizardPage);
	}

	@Override
	public boolean performFinish() {
		IJavaProject project = JavaCore.create(wizardPage.getSelectedProject());
		IPath jarLocation = wizardPage.getJarLocation();

		if (jarLocation.toFile().exists() && !wizardPage.getOverwrite()) {
			boolean overwrite = MessageDialog.openQuestion(getShell(), "Overwrite File",
					"The file " + jarLocation.toOSString()
							+ " already exists. Do you want to overwrite the existing file?");
			if (!overwrite) {
				return false;
			}
		}

		List<IStatus> warnings = new ArrayList<IStatus>();
		IJarExportRunnable op = BundleExportUtils.createExportOperation(project, jarLocation, getShell(), warnings);

		return BundleExportUtils.executeExportOperation(op, true, getContainer(), getShell(), warnings);
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
		setWindowTitle(TITLE);
		setDefaultPageImageDescriptor(ServerExportPlugin.getImageDescriptor("full/wizban/wizban-bundle.png"));
		setNeedsProgressMonitor(true);
	}

}
