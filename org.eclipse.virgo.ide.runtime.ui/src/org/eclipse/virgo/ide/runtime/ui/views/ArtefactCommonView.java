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
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.pde.internal.ui.PDEPluginImages;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.INavigatorActivationService;
import org.eclipse.virgo.ide.runtime.core.ServerCorePlugin;
import org.eclipse.virgo.ide.runtime.core.artefacts.ArtefactSet;
import org.eclipse.virgo.ide.runtime.core.artefacts.IArtefact;
import org.eclipse.virgo.ide.runtime.core.provisioning.IBundleRepositoryChangeListener;
import org.eclipse.virgo.ide.runtime.internal.ui.ServerUiPlugin;
import org.eclipse.virgo.ide.runtime.internal.ui.actions.OpenServerProjectFileAction;
import org.eclipse.virgo.ide.runtime.internal.ui.editor.Messages;
import org.eclipse.virgo.ide.runtime.internal.ui.editor.VirgoEditorAdapterFactory;
import org.eclipse.virgo.ide.runtime.internal.ui.filters.FilterAction;
import org.eclipse.virgo.ide.runtime.internal.ui.projects.IServerProjectArtefact;
import org.eclipse.virgo.ide.runtime.internal.ui.projects.IServerProjectContainer;
import org.eclipse.virgo.ide.runtime.internal.ui.providers.LibrariesNode;
import org.eclipse.virgo.ide.runtime.internal.ui.providers.RuntimeContainersContentProvider;
import org.eclipse.virgo.ide.runtime.internal.ui.providers.RuntimeFullLabelProvider;
import org.eclipse.virgo.ide.runtime.internal.ui.repository.RefreshBundleJob;
import org.eclipse.wst.server.core.IRuntime;
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

	private static final String TREE_ACTION_GROUP = "tree";

	private static final String FILTER_ACTION_GROUP = "filters";

	private static final String REFRESH_ACTION_GROUP = "refresh";

	private IWorkbenchPart currentPart;

	private final ILabelProvider titleLabelProvider = new RuntimeFullLabelProvider();

	RuntimeContainersContentProvider containerProvider = new RuntimeContainersContentProvider();

	private boolean showList;

	private IBundleRepositoryChangeListener repositoryListener;

	private RefreshArtefactsAction refreshArtefactsAction;

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

	class RefreshArtefactsAction extends Action {

		public RefreshArtefactsAction() {
			super("", AS_PUSH_BUTTON); //$NON-NLS-1$
			setText(Messages.RepositoryBrowserEditorPage_Refresh);
			setDescription(Messages.RepositoryBrowserEditorPage_RefreshMessage);
			setToolTipText(Messages.RepositoryBrowserEditorPage_RefreshMessage);
			setImageDescriptor(PDEPluginImages.DESC_REFRESH);
			setDisabledImageDescriptor(PDEPluginImages.DESC_REFRESH_DISABLED);
		}

		/*
		 * @see Action#actionPerformed
		 */
		@Override
		public void run() {
			IServer serverInput = getServerInput();
			if (serverInput != null) {
				RefreshBundleJob.execute(getSite().getShell(), serverInput.getRuntime());
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

		manager.add(new Separator(REFRESH_ACTION_GROUP));
		refreshArtefactsAction = new RefreshArtefactsAction();
		refreshArtefactsAction.setEnabled(false);
		manager.appendToGroup(REFRESH_ACTION_GROUP, refreshArtefactsAction);

		getCommonViewer().addDoubleClickListener(new IDoubleClickListener() {

			public void doubleClick(DoubleClickEvent event) {
				if (event.getSelection() instanceof StructuredSelection) {
					final StructuredSelection sel = (StructuredSelection) event.getSelection();
					OpenServerProjectFileAction fileAction = new OpenServerProjectFileAction(getSite().getPage()) {
						@Override
						public org.eclipse.jface.viewers.IStructuredSelection getStructuredSelection() {
							return sel;
						}
					};
					if (fileAction.updateSelection(sel)) {
						fileAction.run();
					}
				}
			}
		});
		updateActivations();

		repositoryListener = new IBundleRepositoryChangeListener() {
			public void bundleRepositoryChanged(IRuntime runtime) {
				refreshView();
			}
		};
		ServerCorePlugin.getArtefactRepositoryManager().addBundleRepositoryChangeListener(repositoryListener);
	}

	@Override
	protected CommonViewer createCommonViewerObject(Composite aParent) {
		return new CommonViewer(ServerUiPlugin.ARTEFACTS_DETAIL_VIEW_ID, aParent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL);
	}

	protected void refreshView() {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				ISelection selection = getCommonViewer().getSelection();
				getCommonViewer().setInput(getCommonViewer().getInput());
				getCommonViewer().setSelection(selection, true);
			}
		});
	}

	protected void updateContentDescription() {
		String title = "(No Selection)";
		if (currentPart instanceof ServerEditor) {
			title = ((ServerEditor) currentPart).getTitle();
		} else {
			Object input = getCommonViewer().getInput();
			if (input != null && getServerInput() != null) {
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
				Iterator<Object> items = ((StructuredSelection) sel).iterator();
				List<IServer> servers = new ArrayList<IServer>();
				List<Object> containers = new ArrayList<Object>();
				List<Object> artifacts = new ArrayList<Object>();

				while (items.hasNext()) {
					Object next = items.next();
					if (next instanceof IServer) {
						servers.add((IServer) next);
					}
					if (next instanceof LibrariesNode) {
						servers.add(((LibrariesNode) next).getServer());
					}
					if (next instanceof IServerProjectContainer || next instanceof ArtefactSet) {
						containers.add(next);
					}
					if (next instanceof IArtefact || next instanceof IServerProjectArtefact) {
						artifacts.add(next);
					}
				}
				List<Object> input = new ArrayList<Object>(servers);
				if (input.size() == 0) {
					input = new ArrayList<Object>(containers);
				}
				if (input.size() == 0) {
					input = new ArrayList<Object>(artifacts);
				}
				if (input.size() == 1) {
					getCommonViewer().setInput(input.get(0));
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
		refreshArtefactsAction.setEnabled(getServerInput() != null);
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
		ServerCorePlugin.getArtefactRepositoryManager().removeBundleRepositoryChangeListener(repositoryListener);
		currentPart = null;
	}

	/**
	 * @see org.eclipse.ui.navigator.CommonNavigator#getMemento()
	 */
	@Override
	public IMemento getMemento() {
		return super.getMemento();
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

	public IServer getServerInput() {
		return containerProvider.getServer(getCommonViewer().getInput());
	}
}
