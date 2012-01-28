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
package org.eclipse.virgo.ide.manifest.core.dependencies;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.virgo.kernel.osgi.provisioning.tools.DependencyLocationException;
import org.eclipse.virgo.kernel.repository.BundleDefinition;
import org.eclipse.virgo.kernel.repository.LibraryDefinition;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;

/**
 * Implementors of this interface can calculate dependencies for a given {@link BundleManifest}.
 * @author Christian Dupuis
 * @since 2.0.0
 */
public interface IDependencyLocator {

	enum JavaVersion {
		Java5, Java6;
	}
	
	Map<File, List<String>> locateDependencies(BundleManifest manifest) throws DependencyLocationException;
	
	Set<? extends BundleDefinition> getBundles();
	
    Set<? extends LibraryDefinition> getLibraries();
    
    void shutdown();
}
