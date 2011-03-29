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
package org.eclipse.virgo.ide.beans.core.internal.locate;

import org.eclipse.virgo.ide.facet.core.FacetUtils;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.process.IBeansConfigPostProcessingContext;
import org.springframework.ide.eclipse.beans.core.model.process.IBeansConfigPostProcessor;


/**
 * Automatically adds the <code>bundleContext</code> bean or type {@link BundleContext} to the
 * {@link IBeansConfig}.
 * <p>
 * This is convenience since Spring DM exposes this in every application context.
 * @author Christian Dupuis
 * @since 1.0.1
 */
public class SpringOsgiBeansConfigPostProcessor implements IBeansConfigPostProcessor {
	
	/** Name of the {@link BundleContext} bean */
	private static final String BUNDLE_CONTEXT_BEAN_NAME = "bundleContext";
	
	/** {@link BundleContext} class name */
	private static final String BUNDLE_CONTEXT_CLASS_NAME = BundleContext.class.getName();
	
	/**
	 * {@inheritDoc}
	 */
	public void postProcess(IBeansConfigPostProcessingContext postProcessingContext) {
		IBeansConfig config = postProcessingContext.getBeansConfig();
		if (IBeansConfig.Type.AUTO_DETECTED == config.getType()
				&& FacetUtils.isBundleProject(config.getElementResource())) {

			// Check if there is already a bean called bundleContext
			for (IBean bean : postProcessingContext.getBeansConfigRegistrySupport().getBeans()) {
				if (BUNDLE_CONTEXT_BEAN_NAME.equals(bean.getElementName())) {
					return;
				}
			}

			AbstractBeanDefinition beanDefinition = new RootBeanDefinition();
			beanDefinition.setBeanClassName(BUNDLE_CONTEXT_CLASS_NAME);
			beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
			BeanComponentDefinition componentDefinition = new BeanComponentDefinition(
					beanDefinition, BUNDLE_CONTEXT_BEAN_NAME);

			postProcessingContext.getBeansConfigRegistrySupport().registerComponent(
					componentDefinition);
		}
	}

}
