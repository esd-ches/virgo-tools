/*******************************************************************************
 * Copyright (c) 2009, 2011 SpringSource, a divison of VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource, a division of VMware, Inc. - initial API and implementation
 *******************************************************************************/

package org.eclipse.virgo.ide.ui.wizards;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * @author Christian Dupuis
 * @author Martin Lippert
 */
public class WebModulePropertiesPage extends AbstractPropertiesPage {

    protected static String ID_PAGE = "web.properties"; //$NON-NLS-1$

    private Label contextPathLabel;

    private Text contextPathText;

    public WebModulePropertiesPage() {
        super(ID_PAGE);
    }

    @Override
    protected void createPropertiesGroup(Composite container) {
        Group propertiesGroup = new Group(container, SWT.NONE);
        propertiesGroup.setLayout(new GridLayout(3, false));
        propertiesGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        propertiesGroup.setText(Messages.WebModulePropertiesPage_text);

        this.contextPathLabel = new Label(propertiesGroup, SWT.NONE);
        this.contextPathLabel.setText(Messages.WebModulePropertiesPage_context_path);

        this.contextPathText = new Text(propertiesGroup, SWT.BORDER | SWT.SINGLE);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).span(2, 1).applyTo(this.contextPathText);
    }

    @Override
    public Map<String, String> getProperties() {
        Map<String, String> properties = new HashMap<String, String>();
        if (this.contextPathText != null) {
            properties.put(this.contextPathLabel.getText(), this.contextPathText.getText());
        }
        return properties;
    }

    @Override
    public String getModuleType() {
        return "Web"; //$NON-NLS-1$
    }

}
