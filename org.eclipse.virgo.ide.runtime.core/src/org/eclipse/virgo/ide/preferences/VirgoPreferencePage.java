/*******************************************************************************
 * Copyright (c) 2012 SpringSource, a divison of VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource, a division of VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.virgo.ide.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.virgo.ide.runtime.core.ServerCorePlugin;

/**
 * This class represents a preference page that is contributed to the
 * Preferences and Project Properties dialogs. By subclassing
 * <samp>FieldEditorPreferencePage</samp>, we can use the field support built
 * into JFace that allows us to create a page that is small and knows how to
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 */

public class VirgoPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage, IWorkbenchPropertyPage {
	
	private IProject fProject; // can be null

	public VirgoPreferencePage() {
		super(GRID);
		setPreferenceStore(ServerCorePlugin.getDefault().getPreferenceStore());
		setDescription("Expand the tree to edit preferences for the Virgo runtime tools.");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

	public IAdaptable getElement() {
		return fProject;
	}

	public void setElement(IAdaptable element) {
		Object obj = element.getAdapter(IResource.class);
		if (obj instanceof IProject) {
			fProject = (IProject) fProject;
		} else {
			fProject = null;
		}
	}
	
}