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
package org.eclipse.virgo.ide.runtime.internal.ui.properties;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.virgo.ide.runtime.core.ServerCorePlugin;

/**
 * {@link PropertyPage} to configure loading of class and resource names from the repository index
 * 
 * @author Christian Dupuis
 * @since 2.3.0
 */
public class RuntimePreferencePage extends PropertyPage implements IWorkbenchPreferencePage {

	private BooleanFieldEditor booleanEditor;

	protected Control createContents(Composite parent) {

		Composite entryTable = new Composite(parent, SWT.NULL);

		// Create a data that takes up the extra space in the dialog .
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		entryTable.setLayoutData(data);

		GridLayout layout = new GridLayout();
		entryTable.setLayout(layout);

		Label label = new Label(entryTable, SWT.NONE);
		label.setText("Use this preference page to enable loading of class and resource names\n"
				+ "from the Bundle Repository Index so that they will be included in searches.");

		Composite radioComposite = new Composite(entryTable, SWT.NONE);
		radioComposite.setLayout(new GridLayout());

		// Create a data that takes up the extra space in the dialog.
		radioComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Composite radioComposite2 = new Composite(radioComposite, SWT.NONE);
		layout.marginWidth = 3;
		layout.marginHeight = 3;
		radioComposite2.setLayout(layout);
		radioComposite2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		booleanEditor = new BooleanFieldEditor(ServerCorePlugin.PREF_LOAD_CLASSES_KEY, "Load class and resource names",
				radioComposite2);
		booleanEditor.setPage(this);
		booleanEditor.setPreferenceStore(getPreferenceStore());
		booleanEditor.load();

		Label noteLabel = new Label(radioComposite, SWT.NONE);
		noteLabel.setText("Note: enabling this option will increase memory consumption.\nChanging the setting requires a restart to take effect.");

		return entryTable;

	}

	public void init(IWorkbench workbench) {
		// Initialize the preference store we wish to use
		setPreferenceStore(ServerCorePlugin.getDefault().getPreferenceStore());
	}

	protected void performDefaults() {
		booleanEditor.loadDefault();
	}

	public boolean performOk() {
		booleanEditor.store();
		return super.performOk();
	}

}
