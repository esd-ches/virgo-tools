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
package org.eclipse.virgo.kernel.osgi.provisioning.tools;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Platform;
import org.eclipse.virgo.ide.manifest.core.dependencies.IDependencyLocator;
import org.eclipse.virgo.kernel.repository.BundleDefinition;
import org.eclipse.virgo.kernel.repository.LibraryDefinition;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;

/**
 * @author Christian Dupuis
 * @since 2.0.0
 */
public class DependencyLocator20 implements IDependencyLocator {
	
	private final DependencyLocator dependencyLocator;
	
	public DependencyLocator20(String serverHomePath, String[] additionalSearchPaths, String indexDirectoryPath,
			JavaVersion javaVersion) throws IOException {
		// Some platform dependent string matching
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			serverHomePath = serverHomePath.replace('/', '\\');
			indexDirectoryPath = indexDirectoryPath.replace('/', '\\');
			for (int i = 0; i < additionalSearchPaths.length; i++) {
				additionalSearchPaths[i] = additionalSearchPaths[i].replace('/', '\\');
			} 
		}
		dependencyLocator = new DependencyLocator(serverHomePath, additionalSearchPaths, indexDirectoryPath, new NoOpEventLogger());
	}
	
	public Map<File, List<String>> locateDependencies(BundleManifest manifest) throws DependencyLocationException {
		return dependencyLocator.locateDependencies(manifest);
	}

	public Set<? extends BundleDefinition> getBundles() {
		return dependencyLocator.getBundles();
	}

	public Set<? extends LibraryDefinition> getLibraries() {
		return dependencyLocator.getLibraries();
	}

	public void shutdown() {
		dependencyLocator.shutdown();
	}

}
