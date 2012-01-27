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

import org.eclipse.virgo.kernel.repository.BundleRepository;
import org.eclipse.wst.server.core.IRuntime;

/**
 * Implementation of this interface get notified if a {@link BundleRepository} stored in the
 * {@link ArtefactRepositoryManager} is changed.
 * @author Christian Dupuis
 * @since 1.0.1
 * @see ArtefactRepositoryManager#addBundleRepositoryChangeListener(IBundleRepositoryChangeListener)
 * @see ArtefactRepositoryManager#removeBundleRepositoryChangeListener(IBundleRepositoryChangeListener)
 */
public interface IBundleRepositoryChangeListener {

	/**
	 * Notifies changes to the {@link BundleRepository} of the supplied {@link IRuntime}.
	 * @param runtime the runtime instance which {@link BundleRepository} has changed
	 */
	void bundleRepositoryChanged(IRuntime runtime);

}
