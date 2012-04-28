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
package org.eclipse.virgo.ide.runtime.internal.ui.editor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.eclipse.wst.server.ui.internal.editor.ServerEditor;

/**
 * @author Miles Parker
 */
@SuppressWarnings("restriction")
public class ServerOutlinePage extends ContentOutlinePage {

	private CommonViewer contentOutlineViewer;

	protected Object input;

	final ServerEditor editor;

	public ServerOutlinePage(ServerEditor editor) {
		this.editor = editor;
	}

	private void setEditorPage(int page) {
		try {
			Method method = MultiPageEditorPart.class.getDeclaredMethod("setActivePage", new Class[] { Integer.TYPE });
			method.setAccessible(true);
			method.invoke(editor, page);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private IEditorPart getActiveEditor() {
		try {
			Method method = MultiPageEditorPart.class.getDeclaredMethod("getActiveEditor", new Class[] {});
			method.setAccessible(true);
			Object result = method.invoke(editor, new Object[] {});
			return (IEditorPart) result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see org.eclipse.ui.views.contentoutline.ContentOutlinePage#getControl()
	 */
	@Override
	public Control getControl() {
		return contentOutlineViewer.getControl();
	}

	/**
	 * @see org.eclipse.ui.views.contentoutline.ContentOutlinePage#getTreeViewer()
	 */
	@Override
	protected TreeViewer getTreeViewer() {
		return contentOutlineViewer;
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);

		contentOutlineViewer = new CommonViewer("org.eclipse.virgo.ide.runtime.ui.OutlineView", parent, getTreeStyle());
		//contentOutlineViewer.addSelectionChangedListener(this);
		contentOutlineViewer.setInput(editor);

		contentOutlineViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				ISelection selection = getTreeViewer().getSelection();
				if (selection instanceof TreeSelection) {
					TreeSelection treeSel = (TreeSelection) selection;
					if (treeSel.getPaths().length > 0) {
						TreePath firstPath = treeSel.getPaths()[0];
						if (firstPath.getFirstSegment() instanceof IEditorPart) {
							IEditorPart newPart = (IEditorPart) firstPath.getFirstSegment();
							if (getActiveEditor() != newPart) {
								editor.setActiveEditor(newPart);
							}
							List<Object> leafList = new ArrayList<Object>();
							for (TreePath path : treeSel.getPaths()) {
								leafList.add(path.getLastSegment());
							}
							if (leafList.size() > 0) {
								ISelectionChangedListener pageListener = (ISelectionChangedListener) newPart.getAdapter(ISelectionChangedListener.class);
								if (pageListener != null) {
									pageListener.selectionChanged(new SelectionChangedEvent(
											event.getSelectionProvider(), new StructuredSelection(leafList)));
								}
							}
						}
					} else if (sel.getFirstElement() instanceof IEditorPart) {
						editor.setActiveEditor((IEditorPart) sel.getFirstElement());
					}
				}
			}
		});
		//registerContextMenu(contentOutlineViewer);
	}

	protected void registerContextMenu(StructuredViewer viewer) {
		MenuManager searchResultManager = new MenuManager();
		Menu searchResultPopup = searchResultManager.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(searchResultPopup);
		getSite().registerContextMenu("Something", searchResultManager, viewer);
	}
}
