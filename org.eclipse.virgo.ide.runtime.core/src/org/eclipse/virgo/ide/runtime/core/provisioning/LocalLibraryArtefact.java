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
package org.eclipse.virgo.ide.runtime.core.provisioning;

import java.io.File;
import java.net.URI;

import org.eclipse.virgo.ide.bundlerepository.domain.BundleArtefact;
import org.eclipse.virgo.ide.bundlerepository.domain.ILocalArtefact;
import org.eclipse.virgo.ide.bundlerepository.domain.LibraryArtefact;
import org.eclipse.virgo.ide.bundlerepository.domain.OsgiVersion;


/**
 * An extension to {@link BundleArtefact} to take some more information of local bundles.
 * @author Christian Dupuis
 * @since 1.0.0
 */
public class LocalLibraryArtefact extends LibraryArtefact implements ILocalArtefact  {

	private static final long serialVersionUID = 2752279714525304374L;

	private final File file;

	public LocalLibraryArtefact(String name, String symbolicName, OsgiVersion version, URI file) {
		super(name, symbolicName, version, symbolicName, symbolicName);
		this.file = new File(file);
	}

	public File getFile() {
		return file;
	}

}