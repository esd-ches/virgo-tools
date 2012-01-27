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
package org.eclipse.virgo.ide.manifest.internal.core.validation.rules;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.virgo.ide.manifest.core.BundleManifestCoreMessages;
import org.eclipse.virgo.ide.manifest.internal.core.model.BundleManifestHeader;
import org.eclipse.virgo.ide.manifest.internal.core.validation.BundleManifestValidationContext;
import org.osgi.framework.Constants;
import org.springframework.ide.eclipse.core.java.JdtUtils;


/**
 * @author Christian Dupuis
 * @since 1.0.0
 */
/**
 * TODO CD add comments
 */
public class BundleActivatorRule extends AbstractBundleManifestHeaderRule {

	@Override
	protected String[] getHeaderName() {
		return new String[] { Constants.BUNDLE_ACTIVATOR };
	}

	@Override
	protected void validateHeader(BundleManifestHeader header,
			BundleManifestValidationContext context) {
		String activatorClass = header.getValue();
		//TODO -- not sure if this logic is ok, it is much simpler than old logic, but assuming that the class needs to be in the same bundle as Manifest
		IProject project = context.getRootElement().getElementResource().getProject();
		IJavaProject javaProject = JavaCore.create(project);
		IType findType;
		try {
			findType = javaProject.findType(activatorClass, new NullProgressMonitor());
			if (findType == null) {
				context.error("ILLEGAL_ACTIVATOR_CLASS", NLS.bind(
						BundleManifestCoreMessages.BundleErrorReporter_NoExist, activatorClass), header
						.getLineNumber() + 1);
			}
		} catch (JavaModelException e) {
			context.error("ILLEGAL_ACTIVATOR_CLASS", NLS.bind(
			                              					BundleManifestCoreMessages.BundleErrorReporter_NoExist, activatorClass), header
			                              					.getLineNumber() + 1);
		}
	}

}
