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
package org.eclipse.virgo.ide.ui.editors;

import org.eclipse.pde.internal.ui.editor.PDEFormPage;
import org.eclipse.pde.internal.ui.editor.plugin.ExportPackageSection;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Christian Dupuis
 */
public class BundleExportPackageSection extends ExportPackageSection {

	private static final String DESCRIPTION = "Enumerate all the packages that this bundle exposes to clients. All other packages will be hidden from clients at all times.";

	public BundleExportPackageSection(PDEFormPage page, Composite parent) {
		super(page, parent);
		getSection().setDescription(DESCRIPTION);
	}

}
