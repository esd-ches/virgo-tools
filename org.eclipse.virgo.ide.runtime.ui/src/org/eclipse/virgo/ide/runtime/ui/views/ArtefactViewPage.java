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

package org.eclipse.virgo.ide.runtime.ui.views;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.part.Page;
import org.eclipse.virgo.ide.runtime.internal.ui.ServerUiPlugin;
import org.eclipse.virgo.ide.runtime.internal.ui.projects.IServerProjectContainer;
import org.eclipse.virgo.ide.runtime.internal.ui.providers.LibrariesNode;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.ui.editor.ServerEditorPart;
import org.eclipse.wst.server.ui.internal.editor.ServerEditor;

/**
 * 
 * @author Miles Parker
 * 
 */
public class ArtefactViewPage extends Page implements ISelectionListener {

	private CommonViewer viewer;

	@Override
	public void createControl(Composite parent) {
		viewer = new CommonViewer(ServerUiPlugin.ARTEFACTS_DETAIL_VIEW_ID, parent, SWT.NONE);
	}

	@Override
	public Control getControl() {
		return viewer.getControl();
	}

	@Override
	public void setFocus() {
	}

	/**
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (part instanceof IViewPart) {
			if (selection instanceof StructuredSelection) {
				List<Object> serverSelection = new ArrayList<Object>();
				Iterator<Object> items = ((StructuredSelection) selection).iterator();
				while (items.hasNext()) {
					Object next = items.next();
					if (next instanceof IServer) {
						serverSelection.add(next);
					}
					if (next instanceof LibrariesNode) {
						serverSelection.add(((LibrariesNode) next).getServer());
					}
					if (next instanceof IServerProjectContainer) {
						//Don't add if we already have a server selected at top level.
						if (serverSelection.isEmpty()) {
							serverSelection.add(next);
						}
					}
				}
				if (serverSelection.size() == 1) {
					viewer.setInput(serverSelection.get(0));
				} else {
					viewer.setInput(null);
				}
				viewer.refresh();
			}
		} else if (part instanceof ServerEditor) {
			viewer.setInput(getServer((ServerEditor) part));
		}
	}

	static IServer getServer(ServerEditor part) {
		try {
			Method method = MultiPageEditorPart.class.getDeclaredMethod("getActiveEditor", new Class[] {});
			method.setAccessible(true);
			Object result = method.invoke(part, new Object[] {});
			if (result instanceof ServerEditorPart) {
				return ((ServerEditorPart) result).getServer().getOriginal();
			} else {
				return null;
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Object getAdapter(Class adapter) {
		if (adapter == ISelectionListener.class) {
			return this;
		}
		return null;
	}

	public CommonViewer getViewer() {
		return viewer;
	}
}
