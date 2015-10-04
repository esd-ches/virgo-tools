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

package org.eclipse.virgo.ide.ui.wizards;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.pde.internal.ui.wizards.IProjectProvider;
import org.eclipse.pde.internal.ui.wizards.plugin.AbstractFieldData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.virgo.ide.eclipse.wizards.RuntimeConfigurationPage;
import org.eclipse.virgo.ide.facet.core.FacetCorePlugin;
import org.eclipse.virgo.ide.ui.ServerIdeUiPlugin;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;

/**
 * @author Christian Dupuis
 */
public class NewParInformationPage extends RuntimeConfigurationPage {

    protected NewParInformationPage(String pageName, IProjectProvider provider, AbstractFieldData data, IDataModel model) {
        super(pageName, provider, data, model);
    }

    @Override
    protected void createAdditionalPropertiesGroup(Composite container) {
        // TODO Auto-generated method stub

    }

    @Override
    protected String getContentPageDescription() {
        return ProjectContentPageStrings.Par_ContentPage_desc;
    }

    @Override
    protected String getContentPageGroupLabel() {
        return ProjectContentPageStrings.Par_ContentPage_pGroup;
    }

    @Override
    protected String getContentPageIdLabel() {
        return ProjectContentPageStrings.Par_ContentPage_pid;
    }

    @Override
    protected String getContentPageNameLabel() {
        return ProjectContentPageStrings.Par_ContentPage_pname;
    }

    @Override
    protected String getContentPagePluginLabel() {
        return ProjectContentPageStrings.Par_ContentPage_plugin;
    }

    @Override
    protected String getContentPageProviderLabel() {
        return ProjectContentPageStrings.Par_ContentPage_pprovider;
    }

    @Override
    protected String getContentPageTitle() {
        return ProjectContentPageStrings.Par_ContentPage_title;
    }

    @Override
    protected String getContentPageVersionLabel() {
        return ProjectContentPageStrings.Par_ContentPage_pversion;
    }

    @Override
    protected IDialogSettings getDialogSettings() {
        return ServerIdeUiPlugin.getDefault().getDialogSettings();
    }

    @Override
    protected String getModuleTypeID() {
        return FacetCorePlugin.PAR_FACET_ID;
    }
}
