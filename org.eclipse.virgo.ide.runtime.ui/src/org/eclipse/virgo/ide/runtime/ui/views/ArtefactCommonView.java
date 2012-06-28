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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.pde.internal.ui.PDEPluginImages;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.INavigatorActivationService;
import org.eclipse.virgo.ide.runtime.core.ServerCorePlugin;
import org.eclipse.virgo.ide.runtime.core.provisioning.IBundleRepositoryChangeListener;
import org.eclipse.virgo.ide.runtime.internal.ui.ServerUiPlugin;
import org.eclipse.virgo.ide.runtime.internal.ui.filters.FilterAction;
import org.eclipse.virgo.ide.runtime.internal.ui.projects.ServerProjectManager;
import org.eclipse.virgo.ide.runtime.internal.ui.repository.RefreshBundleJob;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;

/**
 * 
 * @see org.eclipse.pde.internal.ui.views.dependencies.DependenciesView
 * @author Miles Parker
 * 
 */
@SuppressWarnings("restriction")
public class ArtefactCommonView extends CommonView {

	public static final String SHOW_VIEW_LIST = "showViewList";

	private static final String TREE_ACTION_GROUP = "tree";

	private static final String FILTER_ACTION_GROUP = "filters";

	private boolean showList;

	private IBundleRepositoryChangeListener repositoryListener;

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
				if (memento != null) {
					memento.putBoolean(SHOW_VIEW_LIST, true);
				}
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
				if (memento != null) {
					memento.putBoolean(SHOW_VIEW_LIST, false);
				}
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
		if (getMemento() != null) {
			Boolean value = getMemento().getBoolean(SHOW_VIEW_LIST);
			if (value != null) {
				showList = value;
			}
		}
		IActionBars actionBars = getViewSite().getActionBars();
		IToolBarManager manager = actionBars.getToolBarManager();
		ShowTreeAction showTreeAction = new ShowTreeAction();
		showTreeAction.setChecked(!showList);
		ShowListAction showListAction = new ShowListAction();
		showListAction.setChecked(showList);
		manager.add(new Separator(TREE_ACTION_GROUP));
		manager.add(new Separator("presentation")); //$NON-NLS-1$
		manager.appendToGroup("presentation", showTreeAction); //$NON-NLS-1$
		manager.appendToGroup("presentation", showListAction); //$NON-NLS-1$

		super.createPartControl(aParent);

		manager.add(new Separator(FILTER_ACTION_GROUP));
		FilterAction[] filterActions = FilterAction.createSet(this);
		for (FilterAction action : filterActions) {
			manager.appendToGroup(FILTER_ACTION_GROUP, action);
		}

		repositoryListener = new IBundleRepositoryChangeListener() {
			public void bundleRepositoryChanged(IRuntime runtime) {
				refreshView();
			}
		};
		ServerCorePlugin.getArtefactRepositoryManager().addBundleRepositoryChangeListener(repositoryListener);
	}

	/**
	 * @see org.eclipse.virgo.ide.runtime.ui.views.CommonView#refreshAll()
	 */
	@Override
	protected void refreshAll() {
		for (IServer server : getServers()) {
			RefreshBundleJob.execute(getSite().getShell(), server.getRuntime());
			ServerProjectManager.getInstance().getProject(server).refresh();
		}
		super.refreshAll();
	}

	/**
	 * @see org.eclipse.ui.navigator.CommonNavigator#saveState(org.eclipse.ui.IMemento)
	 */
	@Override
	public void saveState(IMemento aMemento) {
		super.saveState(aMemento);
		aMemento.putBoolean(SHOW_VIEW_LIST, showList);
	}

	@Override
	protected void updateActivations() {
		INavigatorActivationService activationService = getCommonViewer().getNavigatorContentService()
				.getActivationService();
		String contentId = getContentId();
		if (contentId == ServerUiPlugin.RUNTIME_FLATTENED_ARTEFACTS_CONTENT_ID) {
			activationService.deactivateExtensions(new String[] { ServerUiPlugin.RUNTIME_ARTEFACTS_CONTENT_ID }, false);
			activationService.activateExtensions(
					new String[] { ServerUiPlugin.RUNTIME_FLATTENED_ARTEFACTS_CONTENT_ID }, false);
		} else {
			activationService.deactivateExtensions(
					new String[] { ServerUiPlugin.RUNTIME_FLATTENED_ARTEFACTS_CONTENT_ID }, false);
			activationService.activateExtensions(new String[] { ServerUiPlugin.RUNTIME_ARTEFACTS_CONTENT_ID }, false);
		}
	}

	/**
	 * @see org.eclipse.virgo.ide.runtime.ui.views.CommonView#getContentId()
	 */
	@Override
	protected String getContentId() {
		if (showList) {
			return ServerUiPlugin.RUNTIME_FLATTENED_ARTEFACTS_CONTENT_ID;
		} else {
			return ServerUiPlugin.RUNTIME_ARTEFACTS_CONTENT_ID;
		}
	}

	/**
	 * @see org.eclipse.virgo.ide.runtime.ui.views.CommonView#getViewId()
	 */
	@Override
	protected String getViewId() {
		return ServerUiPlugin.ARTEFACTS_DETAIL_VIEW_ID;
	}
}
