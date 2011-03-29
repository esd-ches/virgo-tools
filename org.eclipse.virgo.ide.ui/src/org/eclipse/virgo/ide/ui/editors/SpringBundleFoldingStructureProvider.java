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
package org.eclipse.virgo.ide.ui.editors;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.pde.internal.core.ibundle.IBundle;
import org.eclipse.pde.internal.core.ibundle.IManifestHeader;
import org.eclipse.pde.internal.core.text.IEditingModel;
import org.eclipse.pde.internal.core.text.bundle.BundleModel;
import org.eclipse.pde.internal.ui.editor.PDESourcePage;
import org.eclipse.pde.internal.ui.editor.plugin.BundleFoldingStructureProvider;
import org.eclipse.virgo.ide.manifest.core.IHeaderConstants;
import org.osgi.framework.Constants;


/**
 * @author Christian Dupuis
 * @author Leo Dos Santos
 */
public class SpringBundleFoldingStructureProvider extends BundleFoldingStructureProvider {

	private final Map fPositionToElement = new HashMap();

	public SpringBundleFoldingStructureProvider(PDESourcePage editor, IEditingModel model) {
		super(editor, model);
	}

	@Override
	public void addFoldingRegions(Set currentRegions, IEditingModel model) throws BadLocationException {
		IBundle bundle = ((BundleModel) model).getBundle();

		IManifestHeader importPackageHeader = bundle.getManifestHeader(Constants.IMPORT_PACKAGE);
		IManifestHeader exportPackageHeader = bundle.getManifestHeader(Constants.EXPORT_PACKAGE);
		IManifestHeader requireBundleHeader = bundle.getManifestHeader(Constants.REQUIRE_BUNDLE);
		IManifestHeader importLibraryHeader = bundle.getManifestHeader(IHeaderConstants.IMPORT_LIBRARY);
		IManifestHeader importBundleHeader = bundle.getManifestHeader(IHeaderConstants.IMPORT_BUNDLE);
		IManifestHeader importTemplateBundleHeader = bundle.getManifestHeader(IHeaderConstants.IMPORT_TEMPLATE);
		IManifestHeader exportTemplateBundleHeader = bundle.getManifestHeader(IHeaderConstants.EXPORT_TEMPLATE);

		try {
			addFoldingRegions(currentRegions, importPackageHeader, model.getDocument());
			addFoldingRegions(currentRegions, exportPackageHeader, model.getDocument());
			addFoldingRegions(currentRegions, requireBundleHeader, model.getDocument());
			addFoldingRegions(currentRegions, importLibraryHeader, model.getDocument());
			addFoldingRegions(currentRegions, importBundleHeader, model.getDocument());
			addFoldingRegions(currentRegions, importTemplateBundleHeader, model.getDocument());
			addFoldingRegions(currentRegions, exportTemplateBundleHeader, model.getDocument());
		}
		catch (BadLocationException e) {
		}
	}

	private void addFoldingRegions(Set regions, IManifestHeader header, IDocument document) throws BadLocationException {
		if (header == null) {
			return;
		}
		int startLine = document.getLineOfOffset(header.getOffset());
		int endLine = document.getLineOfOffset(header.getOffset() + header.getLength() - 1);
		if (startLine < endLine) {
			int start = document.getLineOffset(startLine);
			int end = document.getLineOffset(endLine) + document.getLineLength(endLine);
			Position position = new Position(start, end - start);
			regions.add(position);
			fPositionToElement.put(position, header);
		}
	}

}
