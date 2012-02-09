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
package org.eclipse.virgo.ide.ui.editors;

import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.pde.core.plugin.IPluginImport;
import org.eclipse.pde.internal.core.text.bundle.ExportPackageObject;
import org.eclipse.pde.internal.core.text.bundle.ImportPackageObject;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.pde.internal.ui.parts.PluginVersionPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;

/**
 * Based on <code>DependencyPropertiesDialog</code> from PDE for Eclipse 3.4.
 * Used for compatibility with Eclipse 3.4.
 * @author Christian Dupuis
 */
public class BundleDependencyPropertiesDialog extends StatusDialog {

	private Button fReexportButton;

	private Button fOptionalButton;

	private final boolean fEditable;

	private final boolean fShowReexport;

	private boolean fExported;

	private boolean fOptional;

	private PluginVersionPart fVersionPart;

	private final boolean fShowOptional;

	private String fVersion;

	public BundleDependencyPropertiesDialog(boolean editable, IPluginImport plugin) {
		this(editable, true, plugin.isReexported(), plugin.isOptional(), plugin.getVersion(), true, true);
	}

	public BundleDependencyPropertiesDialog(boolean editable, ImportPackageObject object) {
		this(editable, false, false, object.isOptional(), object.getVersion(), true, true);
	}

	public BundleDependencyPropertiesDialog(boolean editable, ExportPackageObject object) {
		this(editable, false, false, false, object.getVersion(), false, false);
	}

	public BundleDependencyPropertiesDialog(boolean editable, boolean showReexport, boolean export, boolean optional,
			String version, boolean showOptional, boolean isImport) {
		super(PDEPlugin.getActiveWorkbenchShell());
		fEditable = editable;
		fShowReexport = showReexport;
		fExported = export;
		fOptional = optional;
		fShowOptional = showOptional;

		if (isImport) {
			fVersionPart = new PluginVersionPart(true);
		}
		else {
			fVersionPart = new PluginVersionPart(false) {
				@Override
				protected String getGroupText() {
					return PDEUIMessages.DependencyPropertiesDialog_exportGroupText;
				}
			};
		}
		fVersionPart.setVersion(version);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite comp = (Composite) super.createDialogArea(parent);

		if (fShowOptional || fShowReexport) {
			Group container = new Group(comp, SWT.NONE);
			container.setText(PDEUIMessages.DependencyPropertiesDialog_properties);
			container.setLayout(new GridLayout());
			container.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			if (fShowOptional) {
				fOptionalButton = new Button(container, SWT.CHECK);
				fOptionalButton.setText(PDEUIMessages.DependencyPropertiesDialog_optional);
				GridData gd = new GridData();
				gd.horizontalSpan = 2;
				fOptionalButton.setLayoutData(gd);
				fOptionalButton.setEnabled(fEditable);
				fOptionalButton.setSelection(fOptional);
			}

			if (fShowReexport) {
				fReexportButton = new Button(container, SWT.CHECK);
				fReexportButton.setText(PDEUIMessages.DependencyPropertiesDialog_reexport);
				GridData gd = new GridData();
				gd.horizontalSpan = 2;
				fReexportButton.setLayoutData(gd);
				fReexportButton.setEnabled(fEditable);
				fReexportButton.setSelection(fExported);
			}
		}

		fVersionPart.createVersionFields(comp, true, fEditable);
		ModifyListener ml = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateStatus(fVersionPart.validateFullVersionRangeText(true));
			}
		};
		fVersionPart.addListeners(ml, ml);

		return comp;
	}

	public boolean isReexported() {
		return fExported;
	}

	public boolean isOptional() {
		return fOptional;
	}

	public String getVersion() {
		return fVersion;
	}

	@Override
	protected void okPressed() {
		fOptional = (fOptionalButton == null) ? false : fOptionalButton.getSelection();
		fExported = (fReexportButton == null) ? false : fReexportButton.getSelection();

		fVersion = fVersionPart.getVersion();

		super.okPressed();
	}
}
