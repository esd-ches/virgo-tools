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
package org.eclipse.virgo.ide.runtime.internal.ui.overview;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.forms.DetailsPart;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.MasterDetailsBlock;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.virgo.ide.management.remote.Bundle;
import org.eclipse.virgo.ide.runtime.internal.ui.dependencies.BundleDependencyEditorPage;
import org.eclipse.wst.server.core.IServer;


/**
 * @author Christian Dupuis
 * @author Steffen Pingel
 */
public class BundleInformationMasterDetailsBlock extends MasterDetailsBlock {

	private BundleInformationDetailsPart detailsPart;

	private BundleInformationMasterPart masterPart;

	private final MultiPageEditorPart serverEditor;

	private final BundleInformationEditorPage editorPage;

	private final IServer server;

	public BundleInformationMasterDetailsBlock(BundleInformationEditorPage bundleInformationEditorPage,
			MultiPageEditorPart serverEditor, IServer server) {
		this.editorPage = bundleInformationEditorPage;
		this.serverEditor = serverEditor;
		this.server = server;
	}

	@Override
	public void createContent(IManagedForm managedForm) {
		final ScrolledForm form = managedForm.getForm();
		FormToolkit toolkit = managedForm.getToolkit();
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		form.getBody().setLayout(layout);
		sashForm = new MDSashForm(form.getBody(), SWT.NULL);
		sashForm.setData("form", managedForm); //$NON-NLS-1$
		toolkit.adapt(sashForm, false, false);
		sashForm.setMenu(form.getBody().getMenu());
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
		createMasterPart(managedForm, sashForm);
		createDetailsPart(managedForm, sashForm);
		hookResizeListener();
		createToolBarActions(managedForm);
		form.updateToolBar();

		layout = new GridLayout(1, true);
		layout.marginTop = 6;
		layout.marginLeft = 6;
		managedForm.getForm().getBody().setLayout(layout);
	}

	private void createDetailsPart(final IManagedForm mform, Composite parent) {
		super.detailsPart = new DetailsPart(mform, mform.getToolkit().createPageBook(parent, SWT.V_SCROLL));
		mform.addPart(super.detailsPart);
		registerPages(super.detailsPart);
	}

	@Override
	protected void createMasterPart(final IManagedForm managedForm, Composite parent) {
		masterPart = new BundleInformationMasterPart(parent, managedForm.getToolkit(), ExpandableComposite.TWISTIE
				| ExpandableComposite.EXPANDED | ExpandableComposite.TITLE_BAR | Section.DESCRIPTION
				| ExpandableComposite.FOCUS_TITLE, this);
		managedForm.addPart(masterPart);
		masterPart.createContents();
	}

	@Override
	protected void createToolBarActions(IManagedForm managedForm) {

	}

	@Override
	protected void registerPages(DetailsPart detailsPart) {
		this.detailsPart = new BundleInformationDetailsPart(this);
		detailsPart.registerPage(Bundle.class, this.detailsPart);
	}

	/**
	 * @param bundles
	 */
	public void refresh(Map<Long, Bundle> bundles) {
		masterPart.refresh(bundles);
		detailsPart.refresh(bundles);
	}

	private void hookResizeListener() {
		Listener listener = ((MDSashForm) sashForm).listener;
		Control[] children = sashForm.getChildren();
		for (Control element : children) {
			if (element instanceof Sash) {
				continue;
			}
			element.addListener(SWT.Resize, listener);
		}
	}

	private void onSashPaint(Event e) {
		Sash sash = (Sash) e.widget;
		IManagedForm form = (IManagedForm) sash.getParent().getData("form"); //$NON-NLS-1$
		FormColors colors = form.getToolkit().getColors();
		boolean vertical = (sash.getStyle() & SWT.VERTICAL) != 0;
		GC gc = e.gc;
		Boolean hover = (Boolean) sash.getData("hover"); //$NON-NLS-1$
		gc.setBackground(colors.getColor(IFormColors.TB_BG));
		gc.setForeground(colors.getColor(IFormColors.TB_BORDER));
		Point size = sash.getSize();
		if (vertical) {
			if (hover != null) {
				gc.fillRectangle(0, 0, size.x, size.y);
				// else
				// gc.drawLine(1, 0, 1, size.y-1);
			}
		}
		else {
			if (hover != null) {
				gc.fillRectangle(0, 0, size.x, size.y);
				// else
				// gc.drawLine(0, 1, size.x-1, 1);
			}
		}
	}

	class MDSashForm extends SashForm {

		List<Sash> sashes = new ArrayList<Sash>();

		Listener listener = new Listener() {
			public void handleEvent(Event e) {
				switch (e.type) {
				case SWT.MouseEnter:
					e.widget.setData("hover", Boolean.TRUE); //$NON-NLS-1$
					((Control) e.widget).redraw();
					break;
				case SWT.MouseExit:
					e.widget.setData("hover", null); //$NON-NLS-1$
					((Control) e.widget).redraw();
					break;
				case SWT.Paint:
					onSashPaint(e);
					break;
				case SWT.Resize:
					hookSashListeners();
					break;
				}
			}
		};

		public MDSashForm(Composite parent, int style) {
			super(parent, style);
		}

		public void layout(boolean changed) {
			super.layout(changed);
			hookSashListeners();
		}

		public void layout(Control[] children) {
			super.layout(children);
			hookSashListeners();
		}

		private void hookSashListeners() {
			purgeSashes();
			Control[] children = getChildren();
			for (Control element : children) {
				if (element instanceof Sash) {
					Sash sash = (Sash) element;
					if (sashes.contains(sash)) {
						continue;
					}
					sash.addListener(SWT.Paint, listener);
					sash.addListener(SWT.MouseEnter, listener);
					sash.addListener(SWT.MouseExit, listener);
					sashes.add(sash);
				}
			}
		}

		private void purgeSashes() {
			for (Iterator<Sash> iter = sashes.iterator(); iter.hasNext();) {
				Sash sash = iter.next();
				if (sash.isDisposed()) {
					iter.remove();
				}
			}
		}
	}

	public void clear() {
		masterPart.clear();
	}

	public void openDependencyPage(String bundle, String version) {
		IEditorPart[] parts = serverEditor.findEditors(editorPage.getEditorInput());
		for (IEditorPart part : parts) {
			if (part instanceof BundleDependencyEditorPage) {
				serverEditor.setActiveEditor(part);
				((BundleDependencyEditorPage) part).showDependenciesForBundle(bundle, version);
				break;
			}
		}
	}

	/**
	 * @return the server
	 */
	public IServer getServer() {
		return server;
	}

	public void setSelectedBundle(Bundle bundle) {
		masterPart.setSelectedBundle(bundle);
	}

	public void refresh() {
		masterPart.updateButtonState();
	}
	
}
