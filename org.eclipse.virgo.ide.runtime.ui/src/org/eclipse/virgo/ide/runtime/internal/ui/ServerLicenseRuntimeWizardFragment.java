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
package org.eclipse.virgo.ide.runtime.internal.ui;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.wst.server.ui.internal.wizard.page.LicenseComposite;
import org.eclipse.wst.server.ui.wizard.IWizardHandle;
import org.eclipse.wst.server.ui.wizard.WizardFragment;


/**
 * {@link WizardFragment} to accept license terms. 
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public class ServerLicenseRuntimeWizardFragment extends WizardFragment {
	
	public static final String LICENSE = "license";

	public static final String LICENSE_NONE = "none";

	public static final String LICENSE_UNKNOWN = "unknown";

	public static final String LICENSE_ACCEPT = "accept";

	public static final String LICENSE_SERVER = "license_server";

	protected LicenseComposite comp;

	public ServerLicenseRuntimeWizardFragment() {
		// do nothing
	}

	public void enter() {
		if (comp != null)
			comp.updateLicense();
	}

	public boolean hasComposite() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.wst.server.ui.internal.task.WizardTask#getWizardPage()
	 */
	public Composite createComposite(Composite parent, IWizardHandle wizard) {
		comp = new LicenseComposite(parent, getTaskModel(), wizard);

		wizard.setTitle("License Terms");
		wizard.setDescription("Review and accept License Terms");
		wizard.setImageDescriptor(ServerUiImages.DESC_WIZB_SERVER);
		return comp;
	}

	public boolean isComplete() {
		try {
			Boolean b = (Boolean) getTaskModel().getObject(
					ServerLicenseRuntimeWizardFragment.LICENSE_ACCEPT);
			if (b != null && b.booleanValue())
				return true;
		}
		catch (Exception e) {
			// ignore
		}
		return false;
	}
}