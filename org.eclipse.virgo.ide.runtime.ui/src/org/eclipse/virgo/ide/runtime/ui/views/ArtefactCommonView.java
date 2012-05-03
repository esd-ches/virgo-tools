/*******************************************************************************
 * Copyright (c) 2009, 2012 SpringSource, a divison of VMware, Inc.
 * Copyright (c) IBM Corporation (code cribbed from pde and navigator.)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource, a division of VMware, Inc. - initial API and implementation
 *******************************************************************************/

package org.eclipse.virgo.ide.runtime.ui.views;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.pde.internal.ui.PDEPluginImages;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.INavigatorActivationService;
import org.eclipse.virgo.ide.runtime.internal.ui.ServerUiPlugin;
import org.eclipse.virgo.ide.runtime.internal.ui.editor.VirgoEditorAdapterFactory;
import org.eclipse.virgo.ide.runtime.internal.ui.projects.IServerProjectContainer;
import org.eclipse.virgo.ide.runtime.internal.ui.providers.LibrariesNode;
import org.eclipse.virgo.ide.runtime.internal.ui.providers.RuntimeFullLabelProvider;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.ui.internal.cnf.ServersView2;
import org.eclipse.wst.server.ui.internal.editor.ServerEditor;

/**
 * 
 * @see org.eclipse.pde.internal.ui.views.dependencies.DependenciesView
 * @author Miles Parker
 * 
 */
@SuppressWarnings("restriction")
public class ArtefactCommonView extends CommonNavigator implements ISelectionListener {

	public static final String SHOW_VIEW_LIST = "showViewList";

	public static final String SHOW_LIBRARIES = "showLibraries";

	public static final String SHOW_BUNDLES = "showBundles";

	private static final String TREE_ACTION_GROUP = "tree";

	private IWorkbenchPart currentPart;

	private final ILabelProvider titleLabelProvider = new RuntimeFullLabelProvider();

	private ShowTreeAction showTreeAction;

	private ShowListAction showListAction;

	private boolean showList;

	class ShowListAction extends Action {
		public ShowListAction() {
			super("", AS_RADIO_BUTTON); //$NON-NLS-1$
			setText(PDEUIMessages.DependenciesView_ShowListAction_label);
			setDescription(PDEUIMessages.DependenciesView_ShowListAction_description);
			setToolTipText(PDEUIMessages.DependenciesView_ShowListAction_tooltip);
			setImageDescriptor(PDEPluginImages.DESC_FLAT_LAYOUT);
			setDisabledImageDescriptor(PDEPluginImages.DESC_FLAT_LAYOUT_DISABLED);
		}

		/*
		 * @see Action#actionPerformed
		 */
		@Override
		public void run() {
			if (isChecked()) {
				memento.putBoolean(SHOW_VIEW_LIST, true);
				showList = true;
				updateActivations();
			}
		}
	}

	class ShowTreeAction extends Action {

		public ShowTreeAction() {
			super("", AS_RADIO_BUTTON); //$NON-NLS-1$
			setText(PDEUIMessages.DependenciesView_ShowTreeAction_label);
			setDescription(PDEUIMessages.DependenciesView_ShowTreeAction_description);
			setToolTipText(PDEUIMessages.DependenciesView_ShowTreeAction_tooltip);
			setImageDescriptor(PDEPluginImages.DESC_HIERARCHICAL_LAYOUT);
			setDisabledImageDescriptor(PDEPluginImages.DESC_HIERARCHICAL_LAYOUT_DISABLED);
		}

		/*
		 * @see Action#actionPerformed
		 */
		@Override
		public void run() {
			if (isChecked()) {
				memento.putBoolean(SHOW_VIEW_LIST, false);
				showList = false;
				updateActivations();
			}
		}
	}

	/**
	 * @see org.eclipse.ui.navigator.CommonNavigator#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite aParent) {
		showList = false;
		if (memento != null) {
			Boolean value = memento.getBoolean(SHOW_VIEW_LIST);
			if (value != null) {
				showList = value;
			}
		}
		showTreeAction = new ShowTreeAction();
		showTreeAction.setChecked(!showList);
		showListAction = new ShowListAction();
		showListAction.setChecked(showList);
		IActionBars actionBars = getViewSite().getActionBars();
		IToolBarManager manager = actionBars.getToolBarManager();
		manager.add(new Separator(TREE_ACTION_GROUP));
		manager.add(new Separator("presentation")); //$NON-NLS-1$
		manager.appendToGroup("presentation", showTreeAction); //$NON-NLS-1$
		manager.appendToGroup("presentation", showListAction); //$NON-NLS-1$

		super.createPartControl(aParent);

		updateActivations();
	}

	@Override
	protected CommonViewer createCommonViewerObject(Composite aParent) {
		return new CommonViewer(ServerUiPlugin.ARTEFACTS_DETAIL_VIEW_ID, aParent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL);
	}

	protected void updateContentDescription() {
		String title = "(No Selection)";
		if (currentPart instanceof ServerEditor) {
			title = ((ServerEditor) currentPart).getTitle();
		} else {
			Object input = getCommonViewer().getInput();
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
		if (part instanceof IViewPart && part != this) {
			if (sel instanceof StructuredSelection) {
				List<Object> serverSelection = new ArrayList<Object>();
				Iterator<Object> items = ((StructuredSelection) sel).iterator();
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
					getCommonViewer().setInput(serverSelection.get(0));
				} else if (part instanceof ServersView2) {
					getCommonViewer().setInput(null);
				}
				getCommonViewer().refresh();
			}
		} else if (part instanceof IEditorPart) {
			IServer virgoServer = VirgoEditorAdapterFactory.getVirgoServer((IEditorPart) part);
			if (virgoServer != null) {
				getCommonViewer().setInput(virgoServer);
			}
		}
		updateContentDescription();
	}

	/**
	 * @see org.eclipse.ui.navigator.CommonNavigator#createCommonActionGroup()
	 */
	@Override
	protected ActionGroup createCommonActionGroup() {
		return super.createCommonActionGroup();
	}

	/**
	 * @see org.eclipse.ui.navigator.CommonNavigator#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
	 */
	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		site.getPage().addPostSelectionListener(this);
		super.init(site, memento);
	}

	/* (non-Javadoc)
	 * Method declared on IWorkbenchPart.
	 */
	@Override
	public void dispose() {
		super.dispose();
		getSite().getPage().removePostSelectionListener(this);
		currentPart = null;
	}

	/**
	 * @see org.eclipse.ui.navigator.CommonNavigator#saveState(org.eclipse.ui.IMemento)
	 */
	@Override
	public void saveState(IMemento aMemento) {
		super.saveState(aMemento);
		aMemento.putBoolean(SHOW_VIEW_LIST, showList);
	}

	protected void updateActivations() {
		INavigatorActivationService activationService = getCommonViewer().getNavigatorContentService()
				.getActivationService();
		if (showList) {
			activationService.deactivateExtensions(new String[] { ServerUiPlugin.RUNTIME_ARTEFACTS_CONTENT_ID }, false);
			activationService.activateExtensions(
					new String[] { ServerUiPlugin.RUNTIME_FLATTENED_ARTEFACTS_CONTENT_ID }, false);
		} else {
			activationService.deactivateExtensions(
					new String[] { ServerUiPlugin.RUNTIME_FLATTENED_ARTEFACTS_CONTENT_ID }, false);
			activationService.activateExtensions(new String[] { ServerUiPlugin.RUNTIME_ARTEFACTS_CONTENT_ID }, false);
		}
	}
}
