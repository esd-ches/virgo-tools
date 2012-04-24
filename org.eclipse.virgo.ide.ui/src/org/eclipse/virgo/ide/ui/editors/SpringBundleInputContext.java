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

import java.io.File;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.IDocument;
import org.eclipse.pde.core.IBaseModel;
import org.eclipse.pde.internal.ui.editor.JarEntryEditorInput;
import org.eclipse.pde.internal.ui.editor.PDEFormEditor;
import org.eclipse.pde.internal.ui.editor.plugin.BundleInputContext;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.virgo.ide.manifest.core.editor.model.SpringBundleModel;

/**
 * @author Christian Dupuis
 * @author Steffen Pingel
 * @author Leo Dos Santos
 */
public class SpringBundleInputContext extends BundleInputContext {

	public SpringBundleInputContext(PDEFormEditor editor, IEditorInput input, boolean primary) {
		super(editor, input, primary);
	}

	@Override
	protected IBaseModel createModel(IEditorInput input) throws CoreException {
		SpringBundleModel model = null;
		boolean isReconciling = input instanceof IFileEditorInput;
		IDocument document = getDocumentProvider().getDocument(input);
		model = new SpringBundleModel(document, isReconciling);
		if (input instanceof IFileEditorInput) {
			IFile file = ((IFileEditorInput) input).getFile();
			model.setUnderlyingResource(file);
			model.setCharset(file.getCharset());
		} else if (PdeCompatibilityUtil.isSystemFileEditorInput(input)) {
			File file = (File) (input).getAdapter(File.class);
			IPath path = new Path(file.getAbsolutePath()).removeLastSegments(2);
			model.setInstallLocation(path.addTrailingSeparator().toString());
			model.setCharset(getDefaultCharset());
		} else if (input instanceof IURIEditorInput) {
			IFileStore store = EFS.getStore(((IURIEditorInput) input).getURI());
			model.setInstallLocation(store.getParent().getParent().toString());
			model.setCharset(getDefaultCharset());
		} else if (input instanceof JarEntryEditorInput) {
			File file = (File) ((JarEntryEditorInput) input).getAdapter(File.class);
			model.setInstallLocation(file.toString());
			model.setCharset(getDefaultCharset());
		} else {
			model.setCharset(getDefaultCharset());
		}
		model.load();
		return model;
	}

}
