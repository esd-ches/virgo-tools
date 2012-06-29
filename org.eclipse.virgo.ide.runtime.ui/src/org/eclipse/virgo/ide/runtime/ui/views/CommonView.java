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

import java.util.ArrayList;
import java.util.Collections;
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
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.INavigatorActivationService;
import org.eclipse.virgo.ide.runtime.internal.ui.actions.OpenServerProjectFileAction;
import org.eclipse.virgo.ide.runtime.internal.ui.editor.Messages;
import org.eclipse.virgo.ide.runtime.internal.ui.editor.VirgoEditorAdapterFactory;
import org.eclipse.virgo.ide.runtime.internal.ui.projects.ServerProject;
import org.eclipse.virgo.ide.runtime.internal.ui.projects.ServerProjectManager;
import org.eclipse.virgo.ide.runtime.internal.ui.providers.RuntimeContainersContentProvider;
import org.eclipse.virgo.ide.runtime.internal.ui.providers.RuntimeFullLabelProvider;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.ui.internal.cnf.ServersView2;
import org.eclipse.wst.server.ui.internal.editor.ServerEditor;

/**
 * 
 * @author Miles Parker
 * 
 */
public abstract class CommonView extends CommonNavigator implements ISelectionListener {

	public static final String SHOW_VIEW_LIST = "showViewList";

	private static final String TREE_ACTION_GROUP = "tree";

	protected IWorkbenchPart currentPart;

	protected final ILabelProvider titleLabelProvider = new RuntimeFullLabelProvider();

	RuntimeContainersContentProvider containerProvider = new RuntimeContainersContentProvider();

	private List<IServer> servers = Collections.EMPTY_LIST;

	private static final String REFRESH_ACTION_GROUP = "refresh";

	private RefreshArtefactsAction refreshAction;

	private boolean showList;

	/**
	 * This is a bit of a hack to determine the last view that was activated by the user in order to determine a
	 * sensible input for any newly activated views.
	 */
	private IWorkbenchPart lastPartHint;

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
			refreshAll();
		}
	}

	@Override
	protected CommonViewer createCommonViewerObject(Composite aParent) {
		return new CommonViewer(getViewId(), aParent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
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

	/**
	 * @see org.eclipse.ui.navigator.CommonNavigator#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite aParent) {
		for (IViewReference viewReference : getViewSite().getPage().getViewReferences()) {
			IWorkbenchPart part = viewReference.getPart(false);
			if (part instanceof ServersView2 && part != this) {
				lastPartHint = part;
				break;
			}
		}
		if (lastPartHint == null) {
			IEditorPart editor = getViewSite().getPage().getActiveEditor();
			if (editor instanceof ServerEditor) {
				lastPartHint = editor;
			}
		}

		IActionBars actionBars = getViewSite().getActionBars();
		IToolBarManager manager = actionBars.getToolBarManager();

		if (isSupportsListTree()) {
			showList = false;
			if (getMemento() != null) {
				Boolean value = getMemento().getBoolean(SHOW_VIEW_LIST);
				if (value != null) {
					showList = value;
				}
			}
			ShowTreeAction showTreeAction = new ShowTreeAction();
			showTreeAction.setChecked(!showList);
			ShowListAction showListAction = new ShowListAction();
			showListAction.setChecked(showList);
			manager.add(new Separator(TREE_ACTION_GROUP));
			manager.add(new Separator("presentation")); //$NON-NLS-1$
			manager.appendToGroup("presentation", showTreeAction); //$NON-NLS-1$
			manager.appendToGroup("presentation", showListAction); //$NON-NLS-1$
		}

		super.createPartControl(aParent);

		manager.add(new Separator(REFRESH_ACTION_GROUP));
		refreshAction = new RefreshArtefactsAction();
		refreshAction.setEnabled(false);
		manager.appendToGroup(REFRESH_ACTION_GROUP, refreshAction);

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

		getViewSite().getPage().addPartListener(new IPartListener() {

			public void partOpened(IWorkbenchPart part) {
			}

			public void partDeactivated(IWorkbenchPart part) {
			}

			public void partClosed(IWorkbenchPart part) {
			}

			public void partBroughtToTop(IWorkbenchPart part) {
			}

			public void partActivated(IWorkbenchPart part) {
				if (part == CommonView.this) {
					activated();
				}
			}
		});

		activated();
	}

	protected void updateContentDescription() {
		String title = "(No Selection)";
		Object input = getCommonViewer().getInput();
		if (input != null && !servers.isEmpty()) {
			title = titleLabelProvider.getText(input);
		}
		setContentDescription(title);
	}

	/* (non-Javadoc)
	 * Method declared on ISelectionListener.
	 * Notify the current page that the selection has changed.
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection sel) {
		if (part instanceof ServersView2 && part != this) {
			if (sel instanceof StructuredSelection) {
				lastPartHint = part;
				Iterator<Object> items = ((StructuredSelection) sel).iterator();
				List<IServer> lastServers = servers;
				servers = new ArrayList<IServer>();
				while (items.hasNext()) {
					Object next = items.next();
					if (next instanceof IServer) {
						IServer server = (IServer) next;
						if (ServerProject.isVirgo(server)) {
							servers.add(server);
						}
					}
				}
				if (!servers.equals(lastServers)) {
					update();
				}
			}
		} else if (part instanceof IEditorPart) {
			IServer virgoServer = VirgoEditorAdapterFactory.getVirgoServer((IEditorPart) part);
			if (virgoServer != null) {
				servers = Collections.singletonList(virgoServer);
				lastPartHint = part;
				getCommonViewer().setInput(virgoServer);
				update();
			}
		}
		updateContentDescription();
		refreshAction.setEnabled(!servers.isEmpty());
	}

	protected void update() {
		getCommonViewer().setInput(servers);
		getCommonViewer().refresh();
		refreshAction.setEnabled(!getServers().isEmpty());
	}

	/**
	 * @see org.eclipse.ui.navigator.CommonNavigator#saveState(org.eclipse.ui.IMemento)
	 */
	@Override
	public void saveState(IMemento aMemento) {
		super.saveState(aMemento);
		if (isSupportsListTree()) {
			aMemento.putBoolean(SHOW_VIEW_LIST, showList);
		}
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
		lastPartHint = null;
	}

	public List<IServer> getServers() {
		return servers;
	}

	protected void refreshAll() {
		for (IServer server : getServers()) {
			ServerProject project = ServerProjectManager.getInstance().getProject(server);
			if (project != null) {
				project.refreshDirectories();
			}
		}
		refreshView();
	}

	public boolean isShowList() {
		return isSupportsListTree() && showList;
	}

	protected abstract String getTreeContentId();

	/**
	 * May return null in which case we won't show list.
	 */
	protected String getListContentId() {
		return null;
	}

	protected abstract String getViewId();

	protected final boolean isSupportsListTree() {
		return getListContentId() != null;
	}

	protected void updateActivations() {
		INavigatorActivationService activationService = getCommonViewer().getNavigatorContentService()
				.getActivationService();
		if (!isSupportsListTree()) {
			activationService.activateExtensions(new String[] { getTreeContentId() }, false);
		} else {
			if (isShowList()) {
				activationService.deactivateExtensions(new String[] { getTreeContentId() }, false);
				activationService.activateExtensions(new String[] { getListContentId() }, false);
			} else {
				activationService.deactivateExtensions(new String[] { getListContentId() }, false);
				activationService.activateExtensions(new String[] { getTreeContentId() }, false);
			}
		}
	}

	protected void activated() {
		if (lastPartHint instanceof ServersView2) {
			selectionChanged(lastPartHint, ((ServersView2) lastPartHint).getCommonViewer().getSelection());
		} else if (lastPartHint instanceof IEditorPart) {
			IServer virgoServer = VirgoEditorAdapterFactory.getVirgoServer((IEditorPart) lastPartHint);
			if (virgoServer != null) {
				selectionChanged(lastPartHint, StructuredSelection.EMPTY);
			}
		}
	}
}