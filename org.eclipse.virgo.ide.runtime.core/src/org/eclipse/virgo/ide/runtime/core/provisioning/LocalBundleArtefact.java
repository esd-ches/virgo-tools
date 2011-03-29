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
import java.net.URI;

import org.eclipse.virgo.ide.bundlerepository.domain.BundleArtefact;
import org.eclipse.virgo.ide.bundlerepository.domain.OsgiVersion;


/**
 * An extension to {@link BundleArtefact} to take some more information of local bundles.
 * @author Christian Dupuis
 * @since 1.0.0
 */
public class LocalBundleArtefact extends BundleArtefact {

	private static final long serialVersionUID = -6694481554205917720L;

	private final boolean hasDownloadedSources;

	private final File file;

	public LocalBundleArtefact(String name, String symbolicName, OsgiVersion version,
			boolean hasDownloadedSources, URI file) {
		super(name, symbolicName, version, symbolicName, symbolicName);
		this.hasDownloadedSources = hasDownloadedSources;
		this.file = (file != null ? new File(file) : null);
	}

	public boolean hasDownloadedSource() {
		return this.hasDownloadedSources;
	}

	public File getFile() {
		return file;
	}

}