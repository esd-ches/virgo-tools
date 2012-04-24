/*******************************************************************************
 * Copyright (c) 2009, 2012 SpringSource, a divison of VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource, a division of VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.virgo.ide.ui.editors;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.pde.internal.core.ibundle.IBundle;
import org.eclipse.pde.internal.core.ibundle.IBundleModel;
import org.eclipse.pde.internal.core.text.bundle.ImportPackageHeader;
import org.eclipse.pde.internal.core.text.bundle.ImportPackageObject;
import org.eclipse.pde.internal.ui.PDELabelProvider;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.PDEPluginImages;
import org.eclipse.pde.internal.ui.editor.PDEFormEditor;
import org.eclipse.pde.internal.ui.editor.PDEFormPage;
import org.eclipse.pde.internal.ui.editor.context.InputContextManager;
import org.eclipse.pde.internal.ui.editor.plugin.BundleInputContext;
import org.eclipse.pde.internal.ui.editor.plugin.ManifestOutlinePage;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.virgo.ide.manifest.core.IHeaderConstants;
import org.eclipse.virgo.ide.manifest.core.editor.model.ImportBundleHeader;
import org.eclipse.virgo.ide.manifest.core.editor.model.ImportBundleObject;
import org.eclipse.virgo.ide.manifest.core.editor.model.ImportLibraryHeader;
import org.eclipse.virgo.ide.manifest.core.editor.model.ImportLibraryObject;
import org.osgi.framework.Constants;

/**
 * @author Christian Dupuis
 * @author Steffen Pingel
 * @author Leo Dos Santos
 */
public class BundleManifestOutlinePage extends ManifestOutlinePage {

	public BundleManifestOutlinePage(PDEFormEditor editor) {
		super(editor);
	}

	@Override
	protected Object[] getChildren(Object parent) {
		if (parent instanceof PDEFormPage) {
			PDEFormPage page = (PDEFormPage) parent;
			IBundleModel model = getBundleModel(page);
			if (model != null && model.isValid()) {
				IBundle bundle = model.getBundle();
				if (page.getId().equals(BundleDependenciesPage.PAGE_ID)) {
					ArrayList<Object> list = new ArrayList<Object>();
					ImportPackageHeader packageHeader = (ImportPackageHeader) bundle.getManifestHeader(Constants.IMPORT_PACKAGE);
					ImportBundleHeader bundleHeader = (ImportBundleHeader) bundle.getManifestHeader(IHeaderConstants.IMPORT_BUNDLE);
					ImportLibraryHeader libraryHeader = (ImportLibraryHeader) bundle.getManifestHeader(IHeaderConstants.IMPORT_LIBRARY);

					if (packageHeader != null && !packageHeader.isEmpty()) {
						list.addAll(Arrays.asList(packageHeader.getPackages()));
					}
					if (bundleHeader != null && !bundleHeader.isEmpty()) {
						list.addAll(Arrays.asList(bundleHeader.getImportedBundles()));
					}
					if (libraryHeader != null && !libraryHeader.isEmpty()) {
						list.addAll(Arrays.asList(libraryHeader.getImportedLibraries()));
					}
					return list.toArray();
				}
			}
		}
		return super.getChildren(parent);
	}

	@Override
	protected String getParentPageId(Object item) {
		String pageId = null;
		if (item instanceof ImportPackageObject || item instanceof ImportBundleObject
				|| item instanceof ImportLibraryObject) {
			pageId = BundleDependenciesPage.PAGE_ID;
		}
		if (pageId != null) {
			return pageId;
		}
		return super.getParentPageId(item);
	}

	private IBundleModel getBundleModel(PDEFormPage page) {
		InputContextManager manager = page.getPDEEditor().getContextManager();
		if (manager != null) {
			BundleInputContext context = (BundleInputContext) manager.findContext(BundleInputContext.CONTEXT_ID);
			if (context != null) {
				return (IBundleModel) context.getModel();
			}
		}
		return null;
	}

	@Override
	public ILabelProvider createLabelProvider() {
		return new BasicLabelProvider() {
			@Override
			public String getText(Object obj) {
				if (obj instanceof ImportLibraryObject) {
					return ((ImportLibraryObject) obj).getId();
				} else if (obj instanceof ImportBundleObject) {
					return ((ImportBundleObject) obj).getId();
				} else {
					return super.getText(obj);
				}
			}

			@Override
			public Image getImage(Object obj) {
				PDELabelProvider labelProvider = PDEPlugin.getDefault().getLabelProvider();
				if (obj instanceof ImportLibraryObject) {
					return labelProvider.get(PDEPluginImages.DESC_JAR_LIB_OBJ);
				} else if (obj instanceof ImportBundleObject) {
					return labelProvider.get(PDEPluginImages.DESC_BUNDLE_OBJ);
				} else {
					return super.getImage(obj);
				}
			}

		};
	}

	public class BasicLabelProvider extends LabelProvider {
		public String getText(Object obj) {
			if (obj instanceof IFormPage) {
				return ((IFormPage) obj).getTitle();
			}
			return PDEPlugin.getDefault().getLabelProvider().getText(obj);
		}

		public Image getImage(Object obj) {
			if (obj instanceof IFormPage) {
				return PDEPlugin.getDefault().getLabelProvider().get(PDEPluginImages.DESC_PAGE_OBJ);
			}
			return PDEPlugin.getDefault().getLabelProvider().getImage(obj);
		}
	}

}
