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

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.navigator.ICommonFilterDescriptor;
import org.eclipse.ui.navigator.INavigatorFilterService;
import org.eclipse.virgo.ide.runtime.internal.ui.ServerUiImages;
import org.eclipse.virgo.ide.runtime.ui.views.ArtefactCommonView;

/**
 *
 * @author Miles Parker
 *
 */
public class FilterAction extends Action {

    ArtefactCommonView navigator;

    String id;

    /*
     * @see Action#actionPerformed
     */
    @Override
    public void run() {
        INavigatorFilterService activationService = this.navigator.getNavigatorContentService().getFilterService();
        ICommonFilterDescriptor[] visibleFilterDescriptors = activationService.getVisibleFilterDescriptors();
        Collection<String> activeIDs = new HashSet<String>();
        for (ICommonFilterDescriptor filterDescriptor : visibleFilterDescriptors) {
            String filterID = filterDescriptor.getId();
            boolean active = activationService.isActive(filterID);
            if (active) {
                activeIDs.add(filterID);
            }
        }
        if (isChecked()) {
            activeIDs.remove(this.id);
        } else {
            activeIDs.add(this.id);
        }
        String[] ids = activeIDs.toArray(new String[activeIDs.size()]);
        activationService.activateFilterIdsAndUpdateViewer(ids);
    }

    private FilterAction(ArtefactCommonView viewer, String id, ImageDescriptor image) {
        super("", AS_CHECK_BOX); //$NON-NLS-1$
        this.navigator = viewer;
        this.id = id;
        ICommonFilterDescriptor[] descriptors = viewer.getNavigatorContentService().getFilterService().getVisibleFilterDescriptors();
        for (ICommonFilterDescriptor descriptor : descriptors) {
            if (descriptor.getId().equals(id)) {
                setText("Show " + descriptor.getName());
                setDescription("Show " + descriptor.getDescription());
                setToolTipText("Show " + descriptor.getName() + " in the list of artifacts.");
                break;
            }
        }
        setImageDescriptor(image);
        setDisabledImageDescriptor(image);

        INavigatorFilterService activationService = this.navigator.getNavigatorContentService().getFilterService();
        boolean active = activationService.isActive(id);
        setChecked(!active);
    }

    public static FilterAction[] createSet(ArtefactCommonView viewer) {
        return new FilterAction[] { createBundleFilter(viewer), createLibraryFilter(viewer) };
    }

    public static FilterAction createBundleFilter(ArtefactCommonView viewer) {
        return new FilterAction(viewer, "org.eclipse.virgo.ide.runtime.ui.filterBundles", JavaPluginImages.DESC_OBJS_EXTJAR);
    }

    public static FilterAction createLibraryFilter(ArtefactCommonView viewer) {
        return new FilterAction(viewer, "org.eclipse.virgo.ide.runtime.ui.filterLibraries", ServerUiImages.DESC_OBJ_VIRGO_LIB);
    }
}
