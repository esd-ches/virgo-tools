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
package org.eclipse.virgo.ide.manifest.internal.core.validation;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.virgo.ide.facet.core.FacetUtils;
import org.eclipse.virgo.ide.manifest.core.BundleManifestUtils;
import org.eclipse.virgo.ide.manifest.internal.core.model.BundleManifest;
import org.eclipse.virgo.ide.manifest.internal.core.model.BundleManifestHeader;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.core.model.validation.AbstractValidator;
import org.springframework.ide.eclipse.core.model.validation.IValidationContext;
import org.springframework.ide.eclipse.core.model.validation.IValidationElementLifecycleManager;


/**
 * {@link AbstractValidator} that handles validation of MANIFEST.MF.
 * @author Christian Dupuis
 * @since 1.0.0
 */
public class BundleManifestValidator extends AbstractValidator {

	/**
	 * {@inheritDoc}
	 */
	public Set<IResource> deriveResources(Object object) {
		return Collections.emptySet();
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<IResource> getAffectedResources(IResource resource, int kind, int deltaKind) throws CoreException {
		IJavaProject javaProject = JavaCore.create(resource.getProject());
		IResource bundleManifest = BundleManifestUtils.locateManifest(javaProject, false);
		Set<IResource> resources = new LinkedHashSet<IResource>();
		if ((FacetUtils.isBundleProject(resource) && resource.equals(bundleManifest))
				|| (FacetUtils.isParProject(resource) && SpringCoreUtils.isManifest(resource))) {
			resources.add(resource);
		}
		else if (FacetUtils.isBundleProject(resource)
				&& resource.getProjectRelativePath().toString().equals("template.mf")) {
			resources.add(resource);
		}
		return resources;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected IValidationContext createContext(IResourceModelElement rootElement, IResourceModelElement contextElement) {
		return new BundleManifestValidationContext(rootElement, contextElement);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected IValidationElementLifecycleManager createValidationElementLifecycleManager() {
		return new BundleManifestElementLifecycleManager();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean supports(IModelElement element) {
		return element instanceof BundleManifest || element instanceof BundleManifestHeader;
	}

	static class BundleManifestElementLifecycleManager implements IValidationElementLifecycleManager {

		private IResourceModelElement rootElement = null;

		/**
		 * {@inheritDoc}
		 */
		public void destory() {
			// nothing to do
		}

		/**
		 * {@inheritDoc}
		 */
		public Set<IResourceModelElement> getContextElements() {
			Set<IResourceModelElement> contextElements = new HashSet<IResourceModelElement>();
			contextElements.add(getRootElement());
			return contextElements;
		}

		/**
		 * {@inheritDoc}
		 */
		public IResourceModelElement getRootElement() {
			return rootElement;
		}

		public void init(IResource resource) {
			if (resource instanceof IFile) {
				rootElement = new BundleManifest((IFile) resource);
			}
		}
	}

}
