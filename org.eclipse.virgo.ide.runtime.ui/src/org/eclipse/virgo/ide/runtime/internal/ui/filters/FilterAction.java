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

package org.eclipse.virgo.ide.runtime.internal.ui.filters;

import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.internal.navigator.NavigatorFilterService;
import org.eclipse.virgo.ide.runtime.ui.views.ArtefactCommonView;

/**
 * 
 * @author Miles Parker
 * 
 */
public class FilterAction extends Action {

	ArtefactCommonView navigator;

	Class<ViewerFilter> implementation;

	String id;

	/*
	 * @see Action#actionPerformed
	 */
	@Override
	public void run() {
		if (navigator.getMemento() != null) {
			navigator.getMemento().putBoolean(id, isChecked());
		}
		NavigatorFilterService activationService = (NavigatorFilterService) navigator.getNavigatorContentService()
				.getFilterService();
		activationService.setActive(id, !isChecked());
		activationService.updateViewer();
	}

	private FilterAction(ArtefactCommonView viewer, Class implementation, String id, ImageDescriptor image) {
		super("", AS_CHECK_BOX); //$NON-NLS-1$
		this.navigator = viewer;
		this.implementation = implementation;
		this.id = id;
//		INavigatorContentDescriptor contentDescriptor = viewer.getNavigatorContentService()
//				.getContentDescriptorById(id);
//		setText("Show " + contentDescriptor.getName());
//		setDescription("Show " + contentDescriptor.getName());
//		setToolTipText("Show " + contentDescriptor.getName() + " in the list of artifacts.");
		setImageDescriptor(image);
		setDisabledImageDescriptor(image);

		//Showing by default
		boolean initialState = true;
		if (viewer.getMemento() != null) {
			Boolean value = viewer.getMemento().getBoolean(id);
			if (value != null) {
				initialState = value;
			}
		}
		setChecked(initialState);
		run();
	}

	/**
	 * @see org.eclipse.ui.navigator.CommonNavigator#saveState(org.eclipse.ui.IMemento)
	 */
	public void saveState(IMemento aMemento) {
		aMemento.putBoolean(id, isChecked());
	}

	public static FilterAction[] createSet(ArtefactCommonView viewer) {
		return new FilterAction[] { createBundleFilter(viewer), createLibraryFilter(viewer) };
	}

	public static FilterAction createBundleFilter(ArtefactCommonView viewer) {
		return new FilterAction(viewer, BundleArtefactFilter.class, "org.eclipse.virgo.ide.runtime.ui.filterBundles",
				JavaPluginImages.DESC_OBJS_EXTJAR);
	}

	public static FilterAction createLibraryFilter(ArtefactCommonView viewer) {
		return new FilterAction(viewer, LibraryArtefactFilter.class,
				"org.eclipse.virgo.ide.runtime.ui.filterLibraries", JavaPluginImages.DESC_OBJS_LIBRARY);
	}
}
