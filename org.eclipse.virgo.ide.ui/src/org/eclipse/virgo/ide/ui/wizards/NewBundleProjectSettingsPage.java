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

import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPageOne;

/**
 * @author Christian Dupuis
 * @author Leo Dos Santos
 */
public class NewBundleProjectSettingsPage extends NewJavaProjectWizardPageOne {

    private static String NEW_PROJECT_SETTINGS_TITLE = "Create a Bundle project";

    private static String NEW_PROJECT_SETTINGS_DESCRIPTION = "Create a Bundle project in the workspace or in an external location.";

    public NewBundleProjectSettingsPage() {
        super();
        setTitle(NEW_PROJECT_SETTINGS_TITLE);
        setDescription(NEW_PROJECT_SETTINGS_DESCRIPTION);
    }

}
