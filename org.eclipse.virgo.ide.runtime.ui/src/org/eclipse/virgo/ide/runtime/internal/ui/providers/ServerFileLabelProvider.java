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

package org.eclipse.virgo.ide.runtime.internal.ui.providers;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * 
 * @author Miles Parker
 * 
 */
public class ServerFileLabelProvider extends LabelProvider {
	private static final String REMOVE_REGEXP = "org\\.eclipse\\.virgo\\.|\\.properties";

	WorkbenchLabelProvider delegate = new WorkbenchLabelProvider();

	/**
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element) {
		if (element instanceof IFile) {
			IFile file = (IFile) element;
			return file.getName().replaceAll(REMOVE_REGEXP, "");
		}
		if (element instanceof File) {
			File file = (File) element;
			return file.getName().replaceAll(REMOVE_REGEXP, "");
		}
		if (element instanceof ServerFileSelection) {
			return ((ServerFileSelection) element).getLine();
		}
		return delegate.getText(element);
	}

	/**
	 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
	 */
	@Override
	public Image getImage(Object element) {
		if (element instanceof ServerFileSelection) {
			return null;
		}
		return delegate.getImage(element);
//		if (element instanceof IFile || element instanceof File) {
//			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
//		}
//		return super.getImage(element);
	}
}
