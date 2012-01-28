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
package org.eclipse.virgo.ide.runtime.core.provisioning;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.virgo.ide.bundlerepository.domain.Artefact;
import org.eclipse.virgo.ide.bundlerepository.domain.BundleArtefact;
import org.eclipse.virgo.ide.bundlerepository.domain.LibraryArtefact;
import org.eclipse.virgo.ide.runtime.core.ServerCorePlugin;
import org.eclipse.virgo.ide.runtime.internal.core.ServerRuntime;
import org.eclipse.virgo.ide.runtime.internal.core.utils.WebDownloadUtils;
import org.eclipse.virgo.util.io.FileCopyUtils;
import org.eclipse.wst.server.core.IRuntime;


/**
 * Eclipse background job that downloads selected bundles and libraries from the remote enterprise
 * bundle repository.
 * @author Christian Dupuis
 * @since 1.0.0
 */
public class RepositoryProvisioningJob extends Job {

	private static final Object CONTENT_FAMILY = new Object();

	private Set<Artefact> artifactsToDownload;

	private boolean downloadSources = false;

	private boolean downloadBinary = true;

	protected Set<IRuntime> runtimes;

	public RepositoryProvisioningJob(Set<IRuntime> runtimes, Set<Artefact> artifactsToDownload,
			boolean downloadSources) {
		this(runtimes, artifactsToDownload, true, downloadSources);
	}

	public RepositoryProvisioningJob(Set<IRuntime> runtimes, Set<Artefact> artifactsToDownload,
			boolean downloadBinary, boolean downloadSources) {
		super("Downloading bundles and libraries from Enterprise Bundle Repository");
		this.runtimes = runtimes;
		this.artifactsToDownload = artifactsToDownload;
		this.downloadSources = downloadSources;
		this.downloadBinary = downloadBinary;
		setPriority(Job.LONG);
	}

	public Set<IRuntime> getRuntimes() {
		return runtimes;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean belongsTo(Object family) {
		return CONTENT_FAMILY == family;
	}

	protected void copyDirectoryContent(File from, File to) {
		for (File file : from.listFiles()) {
			if (file.isFile()) {
				String fileName = file.getName();
				File newFile = new File(to, fileName);
				try {
					newFile.createNewFile();
					FileCopyUtils.copy(file, newFile);
				}
				catch (IOException e) {
				}
			}
		}
	}

	protected void copyDownloadedArtifactsIntoServer(File bundlesFile, File libraryFile) {
		for (IRuntime runtime : runtimes) {
			ServerRuntime serverRuntime = (ServerRuntime) runtime.loadAdapter(ServerRuntime.class,
					new NullProgressMonitor());
			copyDirectoryContent(bundlesFile, new File(serverRuntime
					.getUserLevelBundleRepositoryPath()));
			copyDirectoryContent(libraryFile, new File(serverRuntime
					.getUserLevelLibraryRepositoryPath()));
		}

	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {

		if (downloadSources) {
			monitor.beginTask("Downloading selected bundles and libraries", (artifactsToDownload
					.size() * 2));
		}
		else {
			monitor.beginTask("Downloading selected bundles and libraries", artifactsToDownload
					.size());
		}

		IPath outputPath = ServerCorePlugin.getDefault().getStateLocation().append(
				"repository-downloads-" + System.currentTimeMillis());
		File bundlesFile = outputPath.append("bundles").toFile();
		if (!bundlesFile.exists()) {
			bundlesFile.mkdirs();
		}
		File libraryFile = outputPath.append("libraries").toFile();
		if (!libraryFile.exists()) {
			libraryFile.mkdirs();
		}

		for (Artefact artifact : this.artifactsToDownload) {
			if (monitor.isCanceled()) {
				continue;
			}
			if (downloadBinary) {
				downloadBinary(monitor, bundlesFile, libraryFile, artifact);
			}
			monitor.worked(1);
			if (downloadSources) {
				if (monitor.isCanceled()) {
					continue;
				}

				downloadSource(monitor, bundlesFile, artifact);
				monitor.worked(1);
			}
		}

		if (monitor.isCanceled()) {
			return Status.OK_STATUS;
		}
		copyDownloadedArtifactsIntoServer(bundlesFile, libraryFile);

		// refresh the local file system cache of bundles and libraries
		for (IRuntime runtime : runtimes) {
			ServerCorePlugin.getArtefactRepositoryManager().refreshBundleRepository(runtime);
		}

		monitor.done();
		return Status.OK_STATUS;
	}

	protected void downloadBinary(IProgressMonitor monitor, File bundlesFile, File libraryFile,
			Artefact artifact) {
		if (artifact instanceof BundleArtefact) {
			WebDownloadUtils.downloadFile(RepositoryUtils.getResourceUrl((BundleArtefact) artifact,
					RepositoryUtils.DOWNLOAD_TYPE_BINARY), bundlesFile, monitor);
		}
		else {
			WebDownloadUtils.downloadFile(RepositoryUtils.getResourceUrl(
					(LibraryArtefact) artifact, RepositoryUtils.DOWNLOAD_TYPE_LIBRARY),
					libraryFile, monitor);
		}
	}

	protected void downloadSource(IProgressMonitor monitor, File bundlesFile, Artefact artifact) {
		if (artifact instanceof BundleArtefact && artifact.isSourceAvailable()) {
			WebDownloadUtils.downloadFile(RepositoryUtils.getResourceUrl((BundleArtefact) artifact,
					RepositoryUtils.DOWNLOAD_TYPE_SOURCE), bundlesFile, monitor);
		}
	}
}
