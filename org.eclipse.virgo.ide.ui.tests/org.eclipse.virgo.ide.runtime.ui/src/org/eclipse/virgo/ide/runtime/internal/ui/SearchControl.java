/*******************************************************************************
 * Copyright (c) 2009 SpringSource, a divison of VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource, a division of VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.virgo.ide.runtime.internal.ui;

import org.eclipse.jface.action.ControlContribution;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * @author Christian Dupuis
 */
public class SearchControl extends ControlContribution {

	private final IManagedForm managedForm;

	private Text searchText;

	public SearchControl(String id, IManagedForm managedForm) {
		super(id);
		this.managedForm = managedForm;
	}

	public Text getSearchText() {
		return searchText;
	}

	protected Control createControl(Composite parent) {
		if (parent instanceof ToolBar) {
			parent.setCursor(null);
		}

		FormToolkit toolkit = managedForm.getToolkit();
		Composite composite = toolkit.createComposite(parent);

		GridLayout layout = new GridLayout(3, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;

		composite.setLayout(layout);
		composite.setBackground(null);

		searchText = toolkit.createText(composite, "", SWT.SINGLE | SWT.BORDER | SWT.SEARCH | SWT.CANCEL);
		searchText.setData(FormToolkit.KEY_DRAW_BORDER, Boolean.TRUE);
		searchText.setLayoutData(new GridData(200, -1));
		searchText.setText("type filter text");

		toolkit.paintBordersFor(composite);

		return composite;
	}

}
