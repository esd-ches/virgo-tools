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
package org.eclipse.virgo.ide.ui.editors.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.pde.core.IBaseModel;
import org.eclipse.pde.internal.ui.util.ModelModification;

/**
 * Adapted from ModelModification for use when modifying SpringBundleModels.
 * This essentially provides access to needed private final methods.
 * @author Christian Dupuis
 * @author Leo Dos Santos
 */
public abstract class BundleModelModification extends ModelModification {

	private IFile modelFile;

	/**
	 * Create a single model modification - used for modifying single
	 * AbstractEditingModels
	 * @param modelFile the basic underlying file for the model you wish to
	 * modify.
	 */
	public BundleModelModification(IFile modelFile) {
		super(modelFile);
		this.modelFile = modelFile;
	}

	/**
	 * Create a full IBundlePluginModelBase modification
	 * @param bundleFile the MANIFEST.MF file
	 * @param xmlFile the plugin.xml/fragment.xml file for this modification
	 * (optional - can be null)
	 * @pre bundleFile must not be <code>null</code>
	 */
	public BundleModelModification(IFile bundleFile, IFile xmlFile) {
		super(bundleFile, xmlFile);
	}

	/**
	 * Create a ModelModification based on the contents of the project ie. if
	 * the project contains a MANIFEST.MF this will be tagged as a
	 * fullBundleModification, otherwise (this project is an old-style plugin)
	 * this will be a PluginModel/FragmentModel modification.
	 * @param project
	 */
	public BundleModelModification(IProject project) {
		super(project);
	}

	public IFile getIfile() {
		if (this.getFile() != null) {
			return this.getFile();
		}
		return this.modelFile;
	}

	public void modifySpringBundle(IBaseModel model, IProgressMonitor monitor) throws CoreException {
		this.modifyModel(model, monitor);
	}

}
