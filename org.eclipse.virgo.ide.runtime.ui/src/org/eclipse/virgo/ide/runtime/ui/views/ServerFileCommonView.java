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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.pde.internal.ui.PDEPluginImages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
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
import org.eclipse.virgo.ide.runtime.internal.ui.providers.ServerFileSelection;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.ui.internal.editor.ServerEditor;

/**
 * 
 * @see org.eclipse.pde.internal.ui.views.dependencies.DependenciesView
 * @author Miles Parker
 * 
 */
@SuppressWarnings("restriction")
public abstract class ServerFileCommonView extends CommonNavigator implements ISelectionListener {

//	private static final String FILTER_ACTION_GROUP = "filters";

	private static final String REFRESH_ACTION_GROUP = "refresh";

	private IWorkbenchPart currentPart;

	private final ILabelProvider titleLabelProvider = new RuntimeFullLabelProvider();

	RuntimeContainersContentProvider containerProvider = new RuntimeContainersContentProvider();

	private RefreshArtefactsAction refreshArtefactsAction;

	private Collection<IFile> currentFiles;

	private IResourceChangeListener resourceListener;

	private final String managedFolder;

	private final String viewId;

	private final String contentId;

	private List<IServer> servers;

	public ServerFileCommonView(String viewId, String contentId, String managedFolder) {
		this.viewId = viewId;
		this.contentId = contentId;
		this.managedFolder = managedFolder;
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
				ServerProjectManager.getInstance().getProject(serverInput).refreshDirectories();
				refreshView();
			}
		}
	}

	private final class DeltaVisitor implements IResourceDeltaVisitor {
		boolean change;

		public boolean visit(IResourceDelta delta) {
			if (change) {
				return false;
			}
			//only interested in changed resources (not added or removed)
			if (delta.getKind() != IResourceDelta.CHANGED) {
				return true;
			}
			//only interested in content changes
			if ((delta.getFlags() & IResourceDelta.CONTENT) == 0) {
				return true;
			}
			IResource resource = delta.getResource();
			if (currentFiles.contains(resource)) {
				change = true;
			}
			return true;
		}
	}

	/**
	 * @see org.eclipse.ui.navigator.CommonNavigator#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite aParent) {
		resourceListener = new IResourceChangeListener() {
			public void resourceChanged(IResourceChangeEvent event) {
				//we are only interested in POST_CHANGE events
				if (servers.size() > 0) {
					if (event.getType() != IResourceChangeEvent.POST_CHANGE) {
						return;
					}
					IResourceDelta rootDelta = event.getDelta();
					boolean refresh = false;
					//get the delta, if any, for the documentation directory
					for (IServer server : servers) {
						ServerProject project = ServerProjectManager.getInstance().getProject(server);
						IFolder folder = project.getWorkspaceProject().getFolder(managedFolder);
						IResourceDelta docDelta = rootDelta.findMember(folder.getFullPath());
						if (docDelta == null) {
							return;
						}
						DeltaVisitor visitor = new DeltaVisitor();
						try {
							docDelta.accept(visitor);
						} catch (CoreException e) {
						}
						if (visitor.change) {
							refresh = true;
							break;
						}
					}
					if (refresh) {
						refreshView();
					}
				}
			}
		};
		ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceListener);
		currentFiles = new HashSet<IFile>();

		IActionBars actionBars = getViewSite().getActionBars();
		IToolBarManager manager = actionBars.getToolBarManager();

		super.createPartControl(aParent);

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
				if (part == ServerFileCommonView.this) {
					ISelection selection = getViewSite().getSelectionProvider().getSelection();
					selectionChanged(part, selection);
				}
			}
		});
	}

	@Override
	protected CommonViewer createCommonViewerObject(Composite aParent) {
		return new CommonViewer(viewId, aParent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
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
				servers = new ArrayList<IServer>();
				while (items.hasNext()) {
					Object next = items.next();
					if (next instanceof IServer) {
						servers.add((IServer) next);
					}
				}
				getCommonViewer().setInput(servers);
				getCommonViewer().refresh();
				currentFiles = new HashSet<IFile>();
				for (IServer server : servers) {
					Object[] elements = ((ITreeContentProvider) getCommonViewer().getContentProvider()).getElements(server);
					for (Object object : elements) {
						if (object instanceof ServerFileSelection) {
							currentFiles.add(((ServerFileSelection) object).getFile());
						}
						if (object instanceof IFile) {
							currentFiles.add((IFile) object);
						}
					}
				}
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
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceListener);
		getSite().getPage().removePostSelectionListener(this);
		currentPart = null;
	}

	protected void updateActivations() {
		INavigatorActivationService activationService = getCommonViewer().getNavigatorContentService()
				.getActivationService();
		activationService.activateExtensions(new String[] { contentId }, false);
	}

	public IServer getServerInput() {
		return containerProvider.getServer(getCommonViewer().getInput());
	}
}
