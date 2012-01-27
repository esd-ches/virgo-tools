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
package org.eclipse.virgo.ide.runtime.internal.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourcePathComputerDelegate;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.virgo.ide.facet.core.FacetCorePlugin;
import org.eclipse.virgo.ide.module.core.ServerModuleDelegate;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerUtil;


/**
 * {@link ISourcePathComputerDelegate} that sets up the source folder of the dm server runtime
 * server.
 * @author Christian Dupuis
 * @since 1.0.0
 */
public class ServerSourcePathComputerDelegate implements ISourcePathComputerDelegate {

	public ISourceContainer[] computeSourceContainers(ILaunchConfiguration configuration,
			IProgressMonitor monitor) throws CoreException {

		List<IRuntimeClasspathEntry> runtimeClasspath = new ArrayList<IRuntimeClasspathEntry>();
		IServer server = ServerUtil.getServer(configuration);

		IModule[] modules = server.getModules();
		for (IModule module : modules) {
			if (module.getModuleType().getId().equals(FacetCorePlugin.BUNDLE_FACET_ID)) {
				addRuntimeClasspathForJavaProject(runtimeClasspath, module);
			}
			else if (module.getModuleType().getId().equals(FacetCorePlugin.PAR_FACET_ID)) {
				ServerModuleDelegate moduleDelegate = (ServerModuleDelegate) module
						.loadAdapter(ServerModuleDelegate.class, null);
				if (moduleDelegate != null) {
					IModule[] children = moduleDelegate.getChildModules();
					for (IModule child : children) {
						addRuntimeClasspathForJavaProject(runtimeClasspath, child);
					}
				}
			}
			else if (module.getModuleType().getId().equals(FacetCorePlugin.WEB_FACET_ID)) {
				addRuntimeClasspathForJavaProject(runtimeClasspath, module);
			}
		}

		runtimeClasspath.addAll(Arrays.asList(JavaRuntime
				.computeUnresolvedSourceLookupPath(configuration)));
		IRuntimeClasspathEntry[] entries = (IRuntimeClasspathEntry[]) runtimeClasspath
				.toArray(new IRuntimeClasspathEntry[runtimeClasspath.size()]);
		IRuntimeClasspathEntry[] resolved = JavaRuntime.resolveSourceLookupPath(entries,
				configuration);
		return JavaRuntime.getSourceContainers(resolved);
	}

	private void addRuntimeClasspathForJavaProject(List<IRuntimeClasspathEntry> runtimeClasspath,
			IModule module) {
		IJavaProject javaProject = JavaCore.create(module.getProject());
		runtimeClasspath.add(JavaRuntime.newDefaultProjectClasspathEntry(javaProject));
	}

}
