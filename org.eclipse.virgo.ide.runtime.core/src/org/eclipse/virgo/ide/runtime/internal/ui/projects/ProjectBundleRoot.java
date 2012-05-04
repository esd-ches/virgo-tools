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

package org.eclipse.virgo.ide.runtime.internal.ui.projects;

import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.virgo.ide.runtime.core.artefacts.ILocalArtefact;

public class ProjectBundleRoot extends JarPackageFragmentRoot implements IServerProjectArtefact {

	private final ILocalArtefact artefact;

	private final ProjectBundleContainer container;

	//Override to allow constructor access for jar package
	public ProjectBundleRoot(ProjectBundleContainer container, ILocalArtefact artefact) {
		super(new Path(artefact.getFile().getAbsolutePath()), (JavaProject) container.getJavaProject());
		this.container = container;
		this.artefact = artefact;
	}

	public IServerProjectContainer getContainer() {
		return container;
	}

	public ILocalArtefact getArtefact() {
		return artefact;
	}
}