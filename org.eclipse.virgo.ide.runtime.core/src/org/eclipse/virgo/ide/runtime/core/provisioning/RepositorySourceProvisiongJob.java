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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.virgo.ide.bundlerepository.domain.Artefact;
import org.eclipse.virgo.ide.runtime.internal.core.utils.WebDownloadUtils;
import org.eclipse.virgo.util.io.FileCopyUtils;
import org.eclipse.wst.server.core.IRuntime;


/**
 * {@link Job} that provisions the missing source jars of the installed bundles in the dm Server's
 * repository.
 * @author Christian Dupuis
 * @since 1.0.0
 */
public class RepositorySourceProvisiongJob extends RepositoryProvisioningJob {

	private Map<Artefact, File> outputFileMapping = new HashMap<Artefact, File>();

	public RepositorySourceProvisiongJob(Set<IRuntime> runtimes, Set<Artefact> artifactsToDownload) {
		super(runtimes, artifactsToDownload, false, true);
	}

	@Override
	protected void downloadSource(IProgressMonitor monitor, File bundlesFile, Artefact artifact) {
		if (artifact instanceof LocalBundleArtefact) {
			LocalBundleArtefact bundle = (LocalBundleArtefact) artifact;
			String url = new StringBuilder().append(
					"http://www.springsource.com/repository/app/bundle/version/download?name=").append(
					bundle.getSymbolicName()).append("&version=").append(bundle.getVersion())
					.append("&type=source").toString();
			File outputFile = WebDownloadUtils.downloadFile(url, bundlesFile, monitor);
			if (outputFile != null) {
				outputFileMapping.put(artifact, outputFile);
			}
		}
	}

	@Override
	protected void copyDownloadedArtifactsIntoServer(File bundlesFile, File libraryFile) {
		for (Map.Entry<Artefact, File> entry : outputFileMapping.entrySet()) {
			if (entry.getKey() instanceof LocalBundleArtefact) {
				File folder = ((LocalBundleArtefact) entry.getKey()).getFile().getParentFile();
				File outputFile = new File(folder, entry.getValue().getName());
				try {
					outputFile.createNewFile();
					FileCopyUtils.copy(entry.getValue(), outputFile);
				}
				catch (IOException e) {
				}
			}
		}
	}
}
