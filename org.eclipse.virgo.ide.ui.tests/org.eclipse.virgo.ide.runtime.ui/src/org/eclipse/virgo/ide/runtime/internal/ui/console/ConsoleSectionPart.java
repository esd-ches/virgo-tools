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
package org.eclipse.virgo.ide.runtime.internal.ui.console;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.virgo.ide.runtime.internal.ui.overview.BundleInformationMasterDetailsBlock;


/**
 * @author Christian Dupuis
 */
public class ConsoleSectionPart extends SectionPart {

	private final FormToolkit toolkit;

	public ConsoleSectionPart(Composite parent, FormToolkit toolkit, int style,
			BundleInformationMasterDetailsBlock masterDetailsBlock) {
		super(parent, toolkit, style);
		this.toolkit = toolkit;
	}

	protected void createContents() {
		Section section = getSection();
		section.setText("Bundle Status");
		section.setDescription("Information about installed bundles on server.");
		section.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite composite = toolkit.createComposite(section);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		toolkit.paintBordersFor(composite);
		section.setClient(composite);

	}
}
