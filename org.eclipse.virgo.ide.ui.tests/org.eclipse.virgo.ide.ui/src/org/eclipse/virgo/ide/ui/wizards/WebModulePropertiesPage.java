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
 */
public class WebModulePropertiesPage extends AbstractPropertiesPage {

	protected static String ID_PAGE = "web.properties";

	private Label contextPathLabel;

	private Text contextPathText;

	private Label patternsLabel;

	private Text patternsText;

	private Label mappingsLabel;

	private Text mappingsText;

	public WebModulePropertiesPage() {
		super(ID_PAGE);
	}

	@Override
	protected void createPropertiesGroup(Composite container) {
		Group propertiesGroup = new Group(container, SWT.NONE);
		propertiesGroup.setLayout(new GridLayout(3, false));
		propertiesGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		propertiesGroup.setText("Web Module Properties");

		contextPathLabel = new Label(propertiesGroup, SWT.NONE);
		contextPathLabel.setText("Web-ContextPath");

		contextPathText = new Text(propertiesGroup, SWT.BORDER | SWT.SINGLE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).span(2, 1)
				.applyTo(contextPathText);

		patternsLabel = new Label(propertiesGroup, SWT.NONE);
		patternsLabel.setText("Web-DispatcherServletUrlPatterns");

		patternsText = new Text(propertiesGroup, SWT.BORDER | SWT.SINGLE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).span(2, 1).applyTo(patternsText);

		mappingsLabel = new Label(propertiesGroup, SWT.NONE);
		mappingsLabel.setText("Web-FilterMappings");

		mappingsText = new Text(propertiesGroup, SWT.BORDER | SWT.SINGLE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).span(2, 1).applyTo(mappingsText);
	}

	@Override
	public Map<String, String> getProperties() {
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(contextPathLabel.getText(), contextPathText.getText());
		properties.put(patternsLabel.getText(), patternsText.getText());
		properties.put(mappingsLabel.getText(), mappingsText.getText());
		return properties;
	}

	@Override
	public String getModuleType() {
		return "Web";
	}

}
