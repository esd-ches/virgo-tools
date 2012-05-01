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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.MessagePage;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;
import org.eclipse.virgo.ide.runtime.internal.ui.ServerUiPlugin;
import org.eclipse.virgo.ide.runtime.internal.ui.editor.VirgoEditorAdapterFactory;
import org.eclipse.virgo.ide.runtime.internal.ui.providers.RuntimeFullLabelProvider;
import org.eclipse.wst.server.ui.internal.editor.ServerEditor;

/**
 * 
 * @see org.eclipse.pde.internal.ui.views.dependencies.DependenciesView
 * @author Miles Parker
 * 
 */
public class ArtefactView extends PageBookView implements ISelectionListener {

	private final Map fPagesToParts;

	private final Map fPartsToPages;

	private ISelection currentSelection;

	private IWorkbenchPart currentPart;

	private final ILabelProvider titleLabelProvider = new RuntimeFullLabelProvider();

	public ArtefactView() {
		fPartsToPages = new HashMap();
		fPagesToParts = new HashMap();
	}

	/**
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
//		getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.PageBookView#doCreatePage(org.eclipse.ui.IWorkbenchPart)
	 */
	@Override
	protected PageRec doCreatePage(IWorkbenchPart part) {
		IPageBookViewPage page = (IPageBookViewPage) fPartsToPages.get(part);
		if (page == null && !fPartsToPages.containsKey(part)) {
			page = createPage(part);
		}
		if (page != null) {
			return new PageRec(part, page);
		}
		return null;
	}

	private IPageBookViewPage createPage(IWorkbenchPart part) {
		IPageBookViewPage page = new ArtefactViewPage();

		initPage(page);
		page.createControl(getPageBook());
		fPartsToPages.put(part, page);
		fPagesToParts.put(page, part);
		return page;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.PageBookView#doDestroyPage(org.eclipse.ui.IWorkbenchPart,
	 *      org.eclipse.ui.part.PageBookView.PageRec)
	 */
	@Override
	protected void doDestroyPage(IWorkbenchPart part, PageRec pageRecord) {
		IPage page = pageRecord.page;
		page.dispose();
		pageRecord.dispose();

		// empty cross-reference cache
		fPartsToPages.remove(part);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.PageBookView#createDefaultPage(org.eclipse.ui.part.PageBook)
	 */
	@Override
	protected IPage createDefaultPage(PageBook book) {
		MessagePage page = new MessagePage();
		page.createControl(getPageBook());
		initPage(page);
		return page;
	}

	/**
	 * @see org.eclipse.ui.part.PageBookView#isImportant(org.eclipse.ui.IWorkbenchPart)
	 */
	@Override
	protected boolean isImportant(IWorkbenchPart part) {
		return part.getSite().getId().equals(ServerUiPlugin.WST_SERVER_VIEW_ID)
				|| (part instanceof ServerEditor && VirgoEditorAdapterFactory.getVirgoServer((IEditorPart) part) != null);
	}

	@Override
	public void partActivated(IWorkbenchPart part) {
		if (isImportant(part)) {
			super.partActivated(part);

			if (isImportant(part)) {
				currentPart = part;
				// reset the selection (to allow selectionChanged() accept part change for empty selections)
				currentSelection = null;
			}

			if (getCurrentPage() instanceof ArtefactViewPage) {
				ArtefactViewPage page = (ArtefactViewPage) getCurrentPage();
				if (page != null) {
					page.selectionChanged(part, part.getSite().getSelectionProvider().getSelection());
				}
			}
		}
		updateContentDescription();
	}

	private IEditorPart getActiveEditor(IEditorPart part) {
		try {
			Method method = MultiPageEditorPart.class.getDeclaredMethod("getActiveEditor", new Class[] {});
			method.setAccessible(true);
			Object result = method.invoke(part, new Object[] {});
			return (IEditorPart) result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see org.eclipse.ui.part.PageBookView#partDeactivated(org.eclipse.ui.IWorkbenchPart)
	 */
	@Override
	public void partDeactivated(IWorkbenchPart part) {
		super.partDeactivated(part);
		updateContentDescription();
	}

	protected void updateContentDescription() {
		String title = "(No Selection)";
		if (currentPart instanceof ServerEditor) {
			title = ((ServerEditor) currentPart).getTitle();
		} else if (getCurrentPage() instanceof ArtefactViewPage) {
			ArtefactViewPage page = (ArtefactViewPage) getCurrentPage();
			Object input = page.getViewer().getInput();
			if (input != null) {
				title = titleLabelProvider.getText(input);
			}
		}
		setContentDescription(title);
	}

	/* (non-Javadoc)
	 * Method declared on ISelectionListener.
	 * Notify the current page that the selection has changed.
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection sel) {
		// we ignore null selection, or if we are pinned, or our own selection or same selection
		if (sel == null || !isImportant(part) || sel.equals(currentSelection)) {
			return;
		}

		// we ignore selection if we are hidden OR selection is coming from another source as the last one
		if (part == null || !part.equals(currentPart)) {
			return;
		}

		currentPart = part;
		currentSelection = sel;

		// pass the selection to the page
		ArtefactViewPage page = (ArtefactViewPage) getCurrentPage();
		if (page != null) {
			page.selectionChanged(currentPart, currentSelection);
		}
		updateContentDescription();
	}

	/* (non-Javadoc)
	 * Method declared on IViewPart.
	 */
	@Override
	public void init(IViewSite site) throws PartInitException {
		site.getPage().addPostSelectionListener(this);
		super.init(site);
	}

	/* (non-Javadoc)
	 * Method declared on IWorkbenchPart.
	 */
	@Override
	public void dispose() {
		super.dispose();
		getSite().getPage().removePostSelectionListener(this);
		currentPart = null;
		currentSelection = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.PageBookView#getBootstrapPart()
	 */
	@Override
	protected IWorkbenchPart getBootstrapPart() {
		for (IViewReference reference : getViewSite().getPage().getViewReferences()) {
			if (reference.getId().equals(ServerUiPlugin.WST_SERVER_VIEW_ID)) {
				return reference.getView(false);
			}
		}
		return null;
	}

}
