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
package org.eclipse.virgo.ide.bundlor.ui;

import java.util.Collections;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.progress.IProgressConstants;
import org.eclipse.virgo.ide.bundlor.internal.core.BundlorCorePlugin;
import org.osgi.framework.BundleContext;

/**
 * UI Plugin for Bundlor UI.
 * 
 * @author Christian Dupuis
 * @since 1.1.2
 */
public class BundlorUiPlugin extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.eclipse.virgo.ide.bundlor.ui";

	public static final String JOB_FAMILY = "org.eclipse.virgo.ide.bundlor.ui.job.family";

	private static BundlorUiPlugin plugin;

	public static BundlorUiPlugin getDefault() {
		return plugin;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static Image getImage(String path) {
		ImageRegistry imageRegistry = getDefault().getImageRegistry();
		Image image = imageRegistry.get(path);
		if (image == null) {
			ImageDescriptor imageDescriptor = getImageDescriptor(path);
			if (imageDescriptor == null) {
				imageDescriptor = ImageDescriptor.getMissingImageDescriptor();
			}
			image = imageDescriptor.createImage(true);
			imageRegistry.put(path, image);
		}
		return image;
	}

	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, "icons/" + path);
	}

	public static void runBundlorOnProject(IJavaProject javaProject) {
		RunBundlorJob job = new RunBundlorJob(javaProject);
		job.schedule();
	}

	static class RunBundlorJob extends Job {

		private final IJavaProject javaProject;

		public RunBundlorJob(IJavaProject javaProject) {
			super("Generating MANIFEST.MF file for project '" + javaProject.getElementName() + "'");
			this.javaProject = javaProject;
			setPriority(Job.BUILD);
			setProperty(IProgressConstants.ICON_PROPERTY, BundlorUiPlugin.getImageDescriptor("full/obj16/osgi_obj.gif"));
			setProperty(IProgressConstants.KEEPONE_PROPERTY, true);
		}

		protected IStatus run(IProgressMonitor monitor) {
			try {
				javaProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, BundlorCorePlugin.BUILDER_ID,
						Collections.<String, String> emptyMap(), monitor);
			} catch (CoreException e) {
			}
			return Status.OK_STATUS;
		}

		@Override
		public boolean belongsTo(Object family) {
			return family instanceof RunBundlorJob;
		}
	}

	public static ICommand getBundlorBuilderCommand(IResource resource) {
		if (resource != null) {
			try {
				ICommand[] commands = resource.getProject().getDescription().getBuildSpec();
				for (ICommand command : commands) {
					if (command.getBuilderName().equals(BundlorCorePlugin.BUILDER_ID)) {
						return command;
					}
				}
			} catch (CoreException e) {
			}
		}
		return null;
	}

	public static boolean isBundlorBuilding(IResource resource) {
		if (resource != null) {
			ICommand command = getBundlorBuilderCommand(resource);
			return command != null && command.isBuilding(IncrementalProjectBuilder.FULL_BUILD);
		}
		return false;
	}
}
