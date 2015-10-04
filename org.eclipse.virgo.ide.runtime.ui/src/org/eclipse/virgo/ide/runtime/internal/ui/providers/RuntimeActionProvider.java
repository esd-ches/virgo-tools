/*******************************************************************************
 * Copyright (c) 2009, 2012 SpringSource, a divison of VMware, Inc.
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource, a division of VMware, Inc. - initial API and implementation
 *******************************************************************************/

package org.eclipse.virgo.ide.runtime.internal.ui.providers;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.OpenWithMenu;
import org.eclipse.ui.internal.navigator.AdaptabilityUtility;
import org.eclipse.ui.internal.navigator.resources.plugin.WorkbenchNavigatorMessages;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonMenuConstants;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;
import org.eclipse.virgo.ide.runtime.internal.ui.actions.OpenServerProjectFileAction;

/**
 * @see org.eclipse.ui.internal.navigator.resources.actions#OpenActionProvider
 * @author Miles Parker
 *
 */
public class RuntimeActionProvider extends CommonActionProvider {

    private OpenServerProjectFileAction openFileAction;

    private ICommonViewerWorkbenchSite viewSite = null;

    private boolean contribute = false;

    @Override
    public void init(ICommonActionExtensionSite aConfig) {
        if (aConfig.getViewSite() instanceof ICommonViewerWorkbenchSite) {
            this.viewSite = (ICommonViewerWorkbenchSite) aConfig.getViewSite();
            this.openFileAction = new OpenServerProjectFileAction(this.viewSite.getPage());
            this.contribute = true;
        }
    }

    @Override
    public void fillContextMenu(IMenuManager aMenu) {
        if (!this.contribute || getContext().getSelection().isEmpty()) {
            return;
        }

        IStructuredSelection selection = (IStructuredSelection) getContext().getSelection();

        this.openFileAction.selectionChanged(selection);
        if (this.openFileAction.isEnabled()) {
            aMenu.insertAfter(ICommonMenuConstants.GROUP_OPEN, this.openFileAction);
        }
        addOpenWithMenu(aMenu);
    }

    @Override
    public void fillActionBars(IActionBars theActionBars) {
        if (!this.contribute) {
            return;
        }
        IStructuredSelection selection = (IStructuredSelection) getContext().getSelection();
        if (selection.size() == 1 && selection.getFirstElement() instanceof IFile) {
            this.openFileAction.selectionChanged(selection);
            theActionBars.setGlobalActionHandler(ICommonActionConstants.OPEN, this.openFileAction);
        }

    }

    private void addOpenWithMenu(IMenuManager aMenu) {
        IStructuredSelection ss = (IStructuredSelection) getContext().getSelection();

        if (ss == null || ss.size() != 1) {
            return;
        }

        Object o = ss.getFirstElement();

        // first try IResource
        IAdaptable openable = (IAdaptable) AdaptabilityUtility.getAdapter(o, IResource.class);
        // otherwise try ResourceMapping
        if (openable == null) {
            openable = (IAdaptable) AdaptabilityUtility.getAdapter(o, ResourceMapping.class);
        } else if (((IResource) openable).getType() != IResource.FILE) {
            openable = null;
        }

        if (openable != null) {
            // Create a menu flyout.
            IMenuManager submenu = new MenuManager(WorkbenchNavigatorMessages.OpenActionProvider_OpenWithMenu_label,
                ICommonMenuConstants.GROUP_OPEN_WITH);
            submenu.add(new GroupMarker(ICommonMenuConstants.GROUP_TOP));
            submenu.add(new OpenWithMenu(this.viewSite.getPage(), openable));
            submenu.add(new GroupMarker(ICommonMenuConstants.GROUP_ADDITIONS));

            // Add the submenu.
            if (submenu.getItems().length > 2 && submenu.isEnabled()) {
                aMenu.appendToGroup(ICommonMenuConstants.GROUP_OPEN_WITH, submenu);
            }
        }
    }
}
