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
package org.eclipse.virgo.ide.runtime.ui;

import java.lang.reflect.Method;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.wst.server.ui.internal.editor.ServerEditor;

/**
 * Generic support for server pages. Should work with all WTP servers.
 * 
 * @author Miles Parker
 */
@SuppressWarnings("restriction")
public class ServerEditorPageContentProvider implements ITreeContentProvider {

	private IEditorPart[] pageParts;

	private ServerEditor editor;

	public ServerEditorPageContentProvider() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (newInput instanceof ServerEditor) {
			editor = (ServerEditor) newInput;
			pageParts = new IEditorPart[getPageCount()];
			for (int i = 0; i < pageParts.length; i++) {
				pageParts[i] = getEditor(i);
			}
		}
	}

	public void dispose() {
	}

	public boolean hasChildren(Object element) {
		return element == editor && pageParts.length > 0;
	}

	public Object getParent(Object element) {
		for (IEditorPart part : pageParts) {
			if (part == element) {
				return editor;
			}
		}
		return null;
	}

	public Object[] getElements(Object inputElement) {
		if (inputElement == editor) {
			return pageParts;
		}
		return new Object[0];
	}

	public Object[] getChildren(Object parentElement) {
		if (parentElement == editor) {
			return pageParts;
		}
		return new Object[0];
	}

	public void removeListener(ILabelProviderListener listener) {
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void addListener(ILabelProviderListener listener) {
	}

	private IEditorPart getEditor(int index) {
		try {
			Method method = MultiPageEditorPart.class.getDeclaredMethod("getEditor", new Class[] { Integer.TYPE });
			method.setAccessible(true);
			Object result = method.invoke(editor, new Object[] { index });
			return (IEditorPart) result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private int getPageCount() {
		try {
			Method method = MultiPageEditorPart.class.getDeclaredMethod("getPageCount", new Class[] {});
			method.setAccessible(true);
			Object result = method.invoke(editor, new Object[] {});
			return (Integer) result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}