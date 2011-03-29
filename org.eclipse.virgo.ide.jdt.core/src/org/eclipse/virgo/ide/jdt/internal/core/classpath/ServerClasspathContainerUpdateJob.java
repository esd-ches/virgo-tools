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
package org.eclipse.virgo.ide.jdt.internal.core.classpath;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.virgo.ide.jdt.core.JdtCorePlugin;
import org.eclipse.virgo.ide.jdt.internal.core.util.ClasspathUtils;
import org.eclipse.virgo.ide.manifest.core.IBundleManifestChangeListener.Type;


/**
 * {@link WorkspaceJob} that triggers the class path container refresh.
 * @author Christian Dupuis
 * @since 1.0.0
 */
public class ServerClasspathContainerUpdateJob extends WorkspaceJob {

	/** Internal cache of scheduled and <b>unfinished</b> update jobs */
	private static final Queue<IJavaProject> SCHEDULED_PROJECTS = new ConcurrentLinkedQueue<IJavaProject>();

	/** The {@link IJavaProject} this jobs should refresh the class path container for */
	private final IJavaProject javaProject;
	
	private Set<Type> types;

	/**
	 * Private constructor to create an instance
	 * @param javaProject the {@link IJavaProject} the class path container should be updated for
	 * @param types the change types happened to the manifest
	 */
	private ServerClasspathContainerUpdateJob(IJavaProject javaProject, Set<Type> types) {
		super("Updating bundle classpath container for project '"
				+ javaProject.getElementName() + "'");
		this.javaProject = javaProject;
		this.types = types;
	}

	/**
	 * Returns the internal {@link IJavaProject}
	 */
	public IJavaProject getJavaProject() {
		return javaProject;
	}

	/**
	 * Runs the job in the context of the workspace. Simply delegates refreshing of the class path
	 * container to {@link ClasspathUtils#updateClasspathContainer(IJavaProject, IProgressMonitor)}.
	 */
	@Override
	public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
		if (!javaProject.getProject().isOpen() || monitor.isCanceled()) {
			return Status.CANCEL_STATUS;
		}
		try {
			ClasspathUtils.updateClasspathContainer(javaProject, types, monitor);
		}
		catch (Exception e) {
			return Status.CANCEL_STATUS;
		}

		return new Status(IStatus.OK, JdtCorePlugin.PLUGIN_ID,
				"Updated SpringSource dm Server classpath container");
	}

	/**
	 * Helper method to schedule a new {@link ServerClasspathContainerUpdateJob}.
	 * @param javaProject the {@link IJavaProject} the class path container should be updated for
	 * @param types the change types of the manifest
	 */
	public static void scheduleClasspathContainerUpdateJob(IJavaProject javaProject, Set<Type> types) {
		if (javaProject != null && !SCHEDULED_PROJECTS.contains(javaProject) && types.size() > 0
				&& ClasspathUtils.hasClasspathContainer(javaProject)) {
			newClasspathContainerUpdateJob(javaProject, types);
		}
	}

	/**
	 * Creates a new instance of {@link ServerClasspathContainerUpdateJob} and configures required
	 * properties and schedules it to the workbench.
	 */
	private static ServerClasspathContainerUpdateJob newClasspathContainerUpdateJob(
			IJavaProject javaProject, Set<Type> types) {
		ServerClasspathContainerUpdateJob job = new ServerClasspathContainerUpdateJob(
				javaProject, types);
//		job.setRule(ResourcesPlugin.getWorkspace().getRuleFactory().buildRule());
		job.setPriority(Job.BUILD);
		job.setSystem(true);
		job.addJobChangeListener(new DuplicateJobListener());
		job.schedule();
		return job;
	}

	/**
	 * Internal {@link IJobChangeListener} to detect duplicates in the scheduled list of
	 * {@link ServerClasspathContainerUpdateJob Jobs}.
	 */
	private static class DuplicateJobListener extends JobChangeAdapter implements
			IJobChangeListener {

		@Override
		public void done(IJobChangeEvent event) {
			SCHEDULED_PROJECTS.remove(((ServerClasspathContainerUpdateJob) event.getJob())
					.getJavaProject());
		}

		@Override
		public void scheduled(IJobChangeEvent event) {
			SCHEDULED_PROJECTS.add(((ServerClasspathContainerUpdateJob) event.getJob())
					.getJavaProject());
		}

	}

}
