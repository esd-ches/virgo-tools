/*******************************************************************************
 * Copyright (c) 2013 Eclipse Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial implementation
 *******************************************************************************/
package org.eclipse.virgo.ide.bundlor.ui.internal.properties;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * @author Leo Dos Santos
 */
public class BundlorPropertiesPathDialog extends Dialog {

	private String propertiesPath;

	public BundlorPropertiesPathDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		Control buttonBar = super.createButtonBar(parent);
		getButton(OK).setEnabled(false);
		return buttonBar;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setText("Add properties file");
		Composite control = (Composite) super.createDialogArea(parent);

		Composite composite = new Composite(control, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(composite);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(composite);

		Label description = new Label(composite, SWT.NONE);
		description.setText("Enter the path to a properties file that should be used for variable substitution.");
		GridDataFactory.fillDefaults().span(2, 1).applyTo(description);

		Label pathLabel = new Label(composite, SWT.NONE);
		pathLabel.setText("Path:");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(pathLabel);

		final Text pathText = new Text(composite, SWT.BORDER);
		GridDataFactory.fillDefaults()
				.align(SWT.FILL, SWT.CENTER)
				.grab(true, false)
				.hint(350, SWT.DEFAULT)
				.applyTo(pathText);
		pathText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				propertiesPath = pathText.getText();
				update();
			}
		});

		return control;
	}

	public String getPropertiesPath() {
		return propertiesPath;
	}

	private void update() {
		if (propertiesPath == null || propertiesPath.length() == 0) {
			getButton(OK).setEnabled(false);
		} else {
			getButton(OK).setEnabled(true);
		}
	}

}
