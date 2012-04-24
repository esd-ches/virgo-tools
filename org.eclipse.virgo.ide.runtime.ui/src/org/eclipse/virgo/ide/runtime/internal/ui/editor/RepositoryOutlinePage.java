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
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.eclipse.virgo.ide.runtime.internal.ui.providers.RepositoryLabelProvider;
import org.eclipse.virgo.ide.runtime.internal.ui.providers.ServerEditorContentLabelProvider;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.ui.internal.editor.ServerEditor;
import org.eclipse.wst.server.ui.internal.editor.ServerEditorInput;

/**
 * @author Miles Parker
 */
@SuppressWarnings("restriction")
public class RepositoryOutlinePage extends ContentOutlinePage {

	private TreeViewer contentOutlineViewer;

	protected Object input;

	final ServerEditor editor;

	public RepositoryOutlinePage(ServerEditor editor) {
		this.editor = editor;
	}

	private void setEditorPage(int page) {
		try {
			Method setPageMethod = MultiPageEditorPart.class.getDeclaredMethod("setActivePage",
					new Class[] { Integer.TYPE });
			setPageMethod.setAccessible(true);
			setPageMethod.invoke(editor, page);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private IEditorPart getActiveEditor() {
		try {
			Method setPageMethod = MultiPageEditorPart.class.getDeclaredMethod("getActiveEditor", new Class[] {});
			setPageMethod.setAccessible(true);
			Object result = setPageMethod.invoke(editor, new Object[] {});
			return (IEditorPart) result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void createControl(Composite parent) {
		super.createControl(parent);

		contentOutlineViewer = getTreeViewer();
		contentOutlineViewer.addSelectionChangedListener(this);

		ServerEditorInput editorInput = (ServerEditorInput) editor.getEditorInput();
		final IServer server = ServerCore.findServer(editorInput.getServerId());
		final ServerEditorContentLabelProvider provider = new ServerEditorContentLabelProvider(server);
		contentOutlineViewer.setContentProvider(provider);
		contentOutlineViewer.setLabelProvider(new RepositoryLabelProvider());
		contentOutlineViewer.setInput(server);

		contentOutlineViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				Object firstElement = sel.getFirstElement();
				int pageNumber = provider.getPageNumber(firstElement);
				if (pageNumber >= 0) {
					setEditorPage(pageNumber);
					IEditorPart activeEditor = getActiveEditor();
					ISelectionChangedListener pageListener = (ISelectionChangedListener) activeEditor.getAdapter(ISelectionChangedListener.class);
					if (pageListener != null) {
						ISelection selection = getTreeViewer().getSelection();
						if (selection instanceof TreeSelection) {
							TreeSelection treeSel = (TreeSelection) selection;
							List<Object> leafList = new ArrayList<Object>();
							for (TreePath path : treeSel.getPaths()) {
								leafList.add(path.getLastSegment());
							}
							if (leafList.size() > 0) {
								pageListener.selectionChanged(new SelectionChangedEvent(event.getSelectionProvider(),
										new StructuredSelection(leafList)));
							}
						}
					}
				}
			}
		});
		registerContextMenu(contentOutlineViewer);
	}

	protected void registerContextMenu(StructuredViewer viewer) {
		MenuManager searchResultManager = new MenuManager();
		Menu searchResultPopup = searchResultManager.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(searchResultPopup);
		getSite().registerContextMenu("Something", searchResultManager, viewer);
	}
}
