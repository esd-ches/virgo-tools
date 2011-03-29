/*******************************************************************************
 * Copyright (c) 2009, 2010 SpringSource, a divison of VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource, a division of VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.virgo.ide.module.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.virgo.ide.facet.core.FacetCorePlugin;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IModuleArtifact;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.model.ModuleArtifactAdapterDelegate;


/**
 * {@link ModuleArtifactAdapterDelegate} implementation for dm Server specific project types.
 * @author Christian Dupuis
 * @author Terry Hon
 * @since 2.0.0
 */
public class ServerModuleArtifactAdapterDelegate extends ModuleArtifactAdapterDelegate {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IModuleArtifact getModuleArtifact(Object obj) {
		IResource resource = null;
		if (obj instanceof IAdaptable) {
			resource = (IResource) ((IAdaptable) obj).getAdapter(IResource.class);
		}
		
		if (resource != null) {
			IProject project = resource.getProject();
			for (IModule module : ServerUtil.getModules(FacetCorePlugin.WEB_FACET_ID)) {
				if (module.getProject().equals(project)) {
					return new ServerModuleArtifact(module);
				}
			}
			for (IModule module : ServerUtil.getModules(FacetCorePlugin.BUNDLE_FACET_ID)) {
				if (module.getProject().equals(project)) {
					return new ServerModuleArtifact(module);
				}
			}
			for (IModule module : ServerUtil.getModules(FacetCorePlugin.PAR_FACET_ID)) {
				if (module.getProject().equals(project)) {
					return new ServerModuleArtifact(module);
				}
			}
			for (IModule module : ServerUtil.getModules(FacetCorePlugin.PLAN_FACET_ID)) {
				if (module.getProject().equals(project)) {
					return new ServerModuleArtifact(module);
				}
			}
		}
		System.out.println(obj);
		return null;
	}
	
	/**
	 * {@link IModuleArtifact} implementation carrying {@link IModule} instances. 
	 */
	class ServerModuleArtifact implements IModuleArtifact {
		private final IModule module;
		
		public ServerModuleArtifact(IModule module) {
			this.module = module;
		}
		
		public IModule getModule() {
			return module;
		}
	}
}
