/*******************************************************************************
 * Copyright (c) 2009, 2010 SpringSource, a divison of VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource, a division of VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.virgo.ide.runtime.internal.ui.dependencies;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonImages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.internal.browser.ImageResource;
import org.eclipse.virgo.ide.management.remote.Bundle;
import org.eclipse.virgo.ide.runtime.internal.ui.AbstractBundleEditorPage;
import org.eclipse.virgo.ide.runtime.internal.ui.SearchControl;
import org.eclipse.virgo.ide.runtime.internal.ui.SearchTextHistory;
import org.eclipse.virgo.ide.runtime.internal.ui.ServerUiImages;
import org.eclipse.virgo.ide.runtime.internal.ui.model.ManagementConnectorClient;
import org.eclipse.virgo.ide.runtime.internal.ui.overview.BundleInformationEditorPage;
import org.eclipse.wst.server.ui.internal.editor.ServerEditorPartInput;
import org.eclipse.wst.server.ui.internal.editor.ServerResourceCommandManager;
import org.eclipse.zest.core.viewers.AbstractZoomableViewer;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.viewers.IZoomableWorkbenchPart;
import org.eclipse.zest.core.viewers.ZoomContributionViewItem;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.eclipse.zest.layouts.LayoutAlgorithm;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.CompositeLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.HorizontalShift;
import org.eclipse.zest.layouts.algorithms.RadialLayoutAlgorithm;


/**
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public class BundleDependencyEditorPage extends AbstractBundleEditorPage implements IZoomableWorkbenchPart {

	private GraphViewer viewer;

	private BundleDependencyContentProvider contentProvider;

	private BundleDependencyLabelProvider labelProvider;

	private ZoomContributionViewItem zoomContributionItem;

	private SearchControl searchControl;

	private Action refreshAction;

	private IToolBarManager toolBarManager;

	private final SearchTextHistory history = new SearchTextHistory();

	private Action forwardAction;

	private Action backAction;

	private ServerResourceCommandManager commandManager;

	protected void createBundleContent(Composite parent) {
		mform = new ManagedForm(parent);
		setManagedForm(mform);
		sform = mform.getForm();
		FormToolkit toolkit = mform.getToolkit();
		sform.setText("Bundle Dependency Graph");
		sform.setImage(ServerUiImages.getImage(ServerUiImages.IMG_OBJ_SPRINGSOURCE));
		sform.setExpandHorizontal(true);
		sform.setExpandVertical(true);
		toolkit.decorateFormHeading(sform.getForm());

		Composite body = sform.getBody();
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		body.setLayout(layout);

		Composite configComposite = toolkit.createComposite(body);
		layout.marginLeft = 6;
		layout.marginTop = 6;
		layout.numColumns = 2;
		configComposite.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		configComposite.setLayout(layout);

		Section expandableComposite = toolkit.createSection(configComposite, ExpandableComposite.TITLE_BAR
				| ExpandableComposite.FOCUS_TITLE);
		layout = new GridLayout();
		expandableComposite.setLayout(layout);
		expandableComposite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		expandableComposite.setFont(body.getFont());
		expandableComposite.setBackground(body.getBackground());
		expandableComposite.setText("Configuration");

		Composite composite = toolkit.createComposite(expandableComposite, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginHeight = 0;
		layout.marginRight = 0;
		composite.setLayout(layout);
		expandableComposite.setClient(composite);

		final Button servicesButton = toolkit.createButton(composite, "Services", SWT.RADIO);
		servicesButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				contentProvider.setShowServices(servicesButton.getSelection());
				new BundleDependencyUpdateJob(false).schedule(200);
			}
		});

		final Button packagesButton = toolkit.createButton(composite, "Packages", SWT.RADIO);
		packagesButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				contentProvider.setShowPackage(packagesButton.getSelection());
				new BundleDependencyUpdateJob(false).schedule(200);
			}
		});
		packagesButton.setSelection(true);

		new Label(composite, SWT.NONE);
		Label dependenciesLabel = toolkit.createLabel(composite, "Dependencies");
		dependenciesLabel.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));

		toolkit.createLabel(composite, "Incoming");

		final Spinner incomingSpinner = new Spinner(composite, SWT.NONE);
		incomingSpinner.setMinimum(0);
		incomingSpinner.setIncrement(1);
		incomingSpinner.setMaximum(20);
		incomingSpinner.setSelection(1);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_END);
//		data.widthHint = 20;
		incomingSpinner.setLayoutData(data);
		incomingSpinner.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				contentProvider.setIncomingDependencyDegree(incomingSpinner.getSelection());
				new BundleDependencyUpdateJob(false).schedule(200);
			}
		});

		toolkit.createLabel(composite, "Outgoing");

		final Spinner outgoingSpinner = new Spinner(composite, SWT.NONE);
		outgoingSpinner.setMinimum(0);
		outgoingSpinner.setIncrement(1);
		outgoingSpinner.setMaximum(20);
		outgoingSpinner.setSelection(1);
		data = new GridData(GridData.HORIZONTAL_ALIGN_END);
//		data.widthHint = 20;
		outgoingSpinner.setLayoutData(data);
		outgoingSpinner.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				contentProvider.setOutgoingDependencyDegree(outgoingSpinner.getSelection());
				new BundleDependencyUpdateJob(false).schedule(200);
			}
		});

		Label separator = new Label(configComposite, SWT.NONE);
		// separator.setBackground(toolkit.getColors().getColor(IFormColors.TB_BG));
		separator.setForeground(toolkit.getColors().getColor(IFormColors.H_BOTTOM_KEYLINE1));
		data = new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.FILL_VERTICAL);
		data.widthHint = 1;
		separator.setLayoutData(data);

		toolkit.paintBordersFor(expandableComposite);

		layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		body.setLayout(layout);

		viewer = new GraphViewer(body, SWT.NONE);
		viewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		viewer.setConnectionStyle(ZestStyles.CONNECTIONS_DIRECTED);

		CompositeLayoutAlgorithm layoutAlgorithm = new CompositeLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING
				| LayoutStyles.ENFORCE_BOUNDS, //
				new LayoutAlgorithm[] { // 
				new RadialLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING | LayoutStyles.ENFORCE_BOUNDS), //
						new HorizontalShift(LayoutStyles.NO_LAYOUT_NODE_RESIZING | LayoutStyles.ENFORCE_BOUNDS) });
		searchControl = new SearchControl("Find", getManagedForm());
		contentProvider = new BundleDependencyContentProvider(viewer, searchControl);

		FocusedBundleDependencyLayoutAlgorithm la = new FocusedBundleDependencyLayoutAlgorithm(
				LayoutStyles.NO_LAYOUT_NODE_RESIZING | LayoutStyles.ENFORCE_BOUNDS, layoutAlgorithm, contentProvider);
		viewer.setLayoutAlgorithm(la);

		viewer.setContentProvider(contentProvider);
		viewer.addSelectionChangedListener(contentProvider);
		viewer.setFilters(new ViewerFilter[] {});
		labelProvider = new BundleDependencyLabelProvider(contentProvider, toolkit);
		viewer.setLabelProvider(labelProvider);
		viewer.addDoubleClickListener(new IDoubleClickListener() {

			public void doubleClick(DoubleClickEvent event) {
				Object selection = ((IStructuredSelection) event.getSelection()).getFirstElement();
				if (selection instanceof Bundle) {
					Bundle bundle = (Bundle) selection;
					searchControl.getSearchText().setText(bundle.getSymbolicName() + " (" + bundle.getVersion() + ")");
					history.add(bundle.getSymbolicName() + " (" + bundle.getVersion() + ")");
					forwardAction.setEnabled(history.canForward());
					backAction.setEnabled(history.canBack());
					toolBarManager.update(true);
					new BundleDependencyUpdateJob(true).schedule();
				}
			}
		});

		toolBarManager = sform.getToolBarManager();
		toolBarManager.add(searchControl);

		backAction = new Action("Back") {
			@Override
			public void run() {
				searchControl.getSearchText().setText(history.back());
				setEnabled(history.canBack());
				forwardAction.setEnabled(history.canForward());
				toolBarManager.update(true);
				new BundleDependencyUpdateJob(true).schedule();
			}
		};
		backAction.setImageDescriptor(ImageResource
				.getImageDescriptor(org.eclipse.ui.internal.browser.ImageResource.IMG_ELCL_NAV_BACKWARD));
		backAction.setHoverImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_CLCL_NAV_BACKWARD));
		backAction.setDisabledImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_DLCL_NAV_BACKWARD));
		backAction.setEnabled(false);
		toolBarManager.add(backAction);

		forwardAction = new Action("Forward", CommonImages.EXPAND_ALL) {
			@Override
			public void run() {
				searchControl.getSearchText().setText(history.forward());
				setEnabled(history.canForward());
				backAction.setEnabled(history.canBack());
				toolBarManager.update(true);
				new BundleDependencyUpdateJob(true).schedule();
			}
		};
		forwardAction.setImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_ELCL_NAV_FORWARD));
		forwardAction.setHoverImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_CLCL_NAV_FORWARD));
		forwardAction.setDisabledImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_DLCL_NAV_FORWARD));
		forwardAction.setEnabled(false);
		toolBarManager.add(forwardAction);

		refreshAction = new Action("Refresh from server", CommonImages.REFRESH) {

			@Override
			public void run() {
				IRunnableWithProgress runnable = new IRunnableWithProgress() {

					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						monitor.beginTask("Updating bundle status from server", 1);
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								Map<Long, Bundle> allBundles = ManagementConnectorClient.getBundles(getServer()
										.getOriginal());
								contentProvider.setBundles(allBundles);
								viewer.setInput(allBundles.values());
							}
						});
						monitor.worked(1);
					}
				};

				try {
					IRunnableContext context = new ProgressMonitorDialog(Display.getCurrent().getActiveShell());
					context.run(true, true, runnable);
				}
				catch (InvocationTargetException e1) {
				}
				catch (InterruptedException e2) {
				}
			}

		};
		toolBarManager.add(refreshAction);

		sform.updateToolBar();
		initPopupMenu();

		searchControl.getSearchText().addKeyListener(new KeyListener() {

			public void keyPressed(KeyEvent e) {
				if (e.character == SWT.CR || e.character == SWT.LF) {
					if (!"type filter text".equals(searchControl.getSearchText().getText())) {
						history.add(searchControl.getSearchText().getText());
						forwardAction.setEnabled(history.canForward());
						backAction.setEnabled(history.canBack());
						toolBarManager.update(true);
						new BundleDependencyUpdateJob(true).schedule();
					}
				}
			}

			public void keyReleased(KeyEvent e) {
			}
		});

		searchControl.getSearchText().addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				if ("".equals(searchControl.getSearchText().getText())) {
					new BundleDependencyUpdateJob(true).schedule();
				}
			}
		});

	}

	public AbstractZoomableViewer getZoomableViewer() {
		return viewer;
	}

	private void initPopupMenu() {
		zoomContributionItem = new ZoomContributionViewItem(this);

		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
			}
		});

		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	void fillContextMenu(IMenuManager manager) {
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

		if (!viewer.getSelection().isEmpty()) {
			manager.add(new Action("Show Bundle Overview") {
				@Override
				public void run() {
					Object obj = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
					if (obj instanceof Bundle) {
						openDependencyPage((Bundle) obj);
					}
				}
			});
			manager.add(new Separator());
		}
		manager.add(zoomContributionItem);
	}

	public void init(IEditorSite site, IEditorInput input) {
		super.init(site, input);
		commandManager = ((ServerEditorPartInput) input).getServerCommandManager();
	}

	public void showDependenciesForBundle(String bundle, String version) {
		this.searchControl.getSearchText().setText(bundle + " (" + version + ")");
		refreshAction.run();
	}

	public void openDependencyPage(Bundle bundle) {
		IEditorPart[] parts = commandManager.getServerEditor().findEditors(getEditorInput());
		for (IEditorPart part : parts) {
			if (part instanceof BundleInformationEditorPage) {
				commandManager.getServerEditor().setActiveEditor(part);
				((BundleInformationEditorPage) part).showOverviewForBundle(bundle);
				break;
			}
		}
	}

	class BundleDependencyUpdateJob extends Job {

		private final boolean deleteSelection;

		public BundleDependencyUpdateJob(boolean deleteSelection) {
			super("Updating Bundle Dependency Graph");
			this.deleteSelection = deleteSelection;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			Display.getDefault().asyncExec(new Runnable() {

				public void run() {
					contentProvider.clearSelection();
					if (deleteSelection) {
						viewer.setSelection(new StructuredSelection());
					}
					viewer.refresh(true);
					viewer.getGraphControl().applyLayout();
				}
			});
			return Status.OK_STATUS;
		}
	}
}
