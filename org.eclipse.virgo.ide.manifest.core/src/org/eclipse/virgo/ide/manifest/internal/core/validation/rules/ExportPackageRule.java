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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.osgi.util.ManifestElement;
import org.eclipse.pde.internal.core.util.VersionUtil;
import org.eclipse.virgo.ide.manifest.core.BundleManifestUtils;
import org.eclipse.virgo.ide.manifest.internal.core.model.BundleManifestHeader;
import org.eclipse.virgo.ide.manifest.internal.core.model.BundleManifestHeaderElement;
import org.eclipse.virgo.ide.manifest.internal.core.validation.BundleManifestValidationContext;
import org.osgi.framework.Constants;


/**
 * @author Christian Dupuis
 * @since 1.0.0
 */
/**
 * TODO CD add comments
 */
@SuppressWarnings("restriction")
public class ExportPackageRule extends AbstractBundleManifestHeaderRule {

	private void validateExportPackage(BundleManifestHeader header, ManifestElement element,
			BundleManifestValidationContext context) {
		boolean found = false;
		try {
			IPackageFragmentRoot[] roots = JavaCore.create(
					context.getRootElement().getElementResource().getProject()).getPackageFragmentRoots();

			for (IPackageFragmentRoot root : roots) {
				int kind = root.getKind();
				if (kind == IPackageFragmentRoot.K_SOURCE
						|| (kind == IPackageFragmentRoot.K_BINARY && !root.isExternal())) {
					IJavaElement[] javaElements = root.getChildren();
					for (int j = 0; j < javaElements.length; j++)
						if (javaElements[j] instanceof IPackageFragment) {
							IPackageFragment fragment = (IPackageFragment) javaElements[j];
							String name = fragment.getElementName();
							if (name.length() == 0) {
								name = ".";
							}
							if (fragment.containsJavaResources()
									|| fragment.getNonJavaResources().length > 0) {
								if (name.equals(element.getValue())) {
									found = true;
								}
							}
						}
				}
			}
		}
		catch (JavaModelException e) {
		}

		// if we actually have packages to add
		if (!found) {
			context.warning("MISSING_PACKAGE_EXPORT", "Bundle does export non-existing package", header
							.getLineNumber() + 1);
		}
	}

	private void validateVersionAttribute(BundleManifestHeader header, ManifestElement element,
			BundleManifestValidationContext context) {
		String version = element.getAttribute(Constants.VERSION_ATTRIBUTE);
		if (version == null)
			return;
		IStatus status = VersionUtil.validateVersion(version);
		if (!status.isOK()) {
			context.error("ILLEGAL_VERSION_RANGE", status.getMessage(), BundleManifestUtils
					.getPackageLineNumber(context.getBundleManifest().getDocument(), header,
							element));
		}
	}

	@Override
	protected String[] getHeaderName() {
		return new String[] { Constants.EXPORT_PACKAGE };
	}

	@Override
	protected void validateHeader(BundleManifestHeader header,
			BundleManifestValidationContext context) {
		for (BundleManifestHeaderElement element : header.getBundleManifestHeaderElements()) {
			validateVersionAttribute(header, element.getManifestElement(), context);
			validateExportPackage(header, element.getManifestElement(), context);
		}
	}

}
