/*******************************************************************************
 * Copyright (c) 2009, 2012 SpringSource, a divison of VMware, Inc.
 * Copyright (c) IBM Corporation (code cribbed from pde and navigator.)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource, a division of VMware, Inc. - initial API and implementation
 *******************************************************************************/

package org.eclipse.virgo.ide.runtime.ui.views;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.virgo.ide.runtime.core.ServerCorePlugin;
import org.eclipse.virgo.ide.runtime.core.provisioning.IBundleRepositoryChangeListener;
import org.eclipse.virgo.ide.runtime.internal.ui.ServerUiPlugin;
import org.eclipse.virgo.ide.runtime.internal.ui.filters.FilterAction;
import org.eclipse.virgo.ide.runtime.internal.ui.projects.ServerProjectManager;
import org.eclipse.virgo.ide.runtime.internal.ui.repository.RefreshBundleJob;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;

/**
 *
 * @see org.eclipse.pde.internal.ui.views.dependencies.DependenciesView
 * @author Miles Parker
 *
 */
@SuppressWarnings("restriction")
public class ArtefactCommonView extends CommonView {

    private static final String FILTER_ACTION_GROUP = "filters";

    private IBundleRepositoryChangeListener repositoryListener;

    /**
     * @see org.eclipse.ui.navigator.CommonNavigator#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite aParent) {

        super.createPartControl(aParent);
        IActionBars actionBars = getViewSite().getActionBars();
        IToolBarManager manager = actionBars.getToolBarManager();

        manager.add(new Separator(FILTER_ACTION_GROUP));
        FilterAction[] filterActions = FilterAction.createSet(this);
        for (FilterAction action : filterActions) {
            manager.appendToGroup(FILTER_ACTION_GROUP, action);
        }

        this.repositoryListener = new IBundleRepositoryChangeListener() {

            public void bundleRepositoryChanged(IRuntime runtime) {
                refreshView();
            }
        };
        ServerCorePlugin.getArtefactRepositoryManager().addBundleRepositoryChangeListener(this.repositoryListener);
    }

    /**
     * @see org.eclipse.virgo.ide.runtime.ui.views.CommonView#refreshAll()
     */
    @Override
    protected void refreshAll() {
        for (IServer server : getServers()) {
            RefreshBundleJob.execute(getSite().getShell(), server.getRuntime());
            ServerProjectManager.getInstance().getProject(server).refresh();
        }
        super.refreshAll();
    }

    /**
     * @see org.eclipse.virgo.ide.runtime.ui.views.CommonView#getListContentId()
     */
    @Override
    protected String getListContentId() {
        return ServerUiPlugin.RUNTIME_FLATTENED_ARTEFACTS_CONTENT_ID;
    }

    /**
     * @see org.eclipse.virgo.ide.runtime.ui.views.CommonView#getTreeContentId()
     */
    @Override
    protected String getTreeContentId() {
        return ServerUiPlugin.RUNTIME_ARTEFACTS_CONTENT_ID;
    }

    /**
     * @see org.eclipse.virgo.ide.runtime.ui.views.CommonView#getViewId()
     */
    @Override
    protected String getViewId() {
        return ServerUiPlugin.ARTEFACTS_DETAIL_VIEW_ID;
    }
}
