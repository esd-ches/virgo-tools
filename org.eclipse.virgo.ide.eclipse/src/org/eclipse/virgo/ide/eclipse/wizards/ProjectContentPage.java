/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.virgo.ide.eclipse.wizards;

import java.util.StringTokenizer;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.pde.internal.core.util.IdUtil;
import org.eclipse.pde.internal.core.util.VersionUtil;
import org.eclipse.pde.internal.ui.util.PDELabelUtility;
import org.eclipse.pde.internal.ui.wizards.IProjectProvider;
import org.eclipse.pde.internal.ui.wizards.plugin.AbstractFieldData;
import org.eclipse.pde.internal.ui.wizards.plugin.ContentPage;
import org.eclipse.pde.internal.ui.wizards.plugin.PluginContentPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * SpringSource Tool Suite Team - Portions of this class were copied from PDE's ContentPage and PluginContentPage in
 * Eclipse 3.4 in order to provide a bundle content wizard page outside of the new plugin project wizard.
 */
@SuppressWarnings("restriction")
public abstract class ProjectContentPage extends WizardPage {

	private final static int PROPERTIES_GROUP = 1;

	// ContentPage
	private boolean fInitialized = false;

	private Text fIdText;

	private Text fVersionText;

	private Text fNameText;

	private Text fProviderText;

	private final AbstractFieldData fData;

	private final IProjectProvider fProjectProvider;

	private int fChangedGroups = 0;

	/**
	 * @see ContentPage
	 */
	private final ModifyListener propertiesListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			if (fInitialized) {
				fChangedGroups |= PROPERTIES_GROUP;
			}
			validatePage();
		}
	};

	protected ProjectContentPage(String pageName, IProjectProvider provider, AbstractFieldData data) {
		super(pageName);
		fProjectProvider = provider;
		fData = data;
		setTitle(getContentPageTitle());
		setDescription(getContentPageDescription());
	}

	/**
	 * @see ContentPage
	 */
	private String computeId() {
		return IdUtil.getValidId(fProjectProvider.getProjectName());
	}

	/**
	 * @see PluginContentPage
	 */
	protected void createPluginPropertiesGroup(Composite container) {
		Group propertiesGroup = new Group(container, SWT.NONE);
		propertiesGroup.setLayout(new GridLayout(3, false));
		propertiesGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		propertiesGroup.setText(getContentPageGroupLabel());

		Label label = new Label(propertiesGroup, SWT.NONE);
		label.setText(getContentPageIdLabel());
		fIdText = createText(propertiesGroup, propertiesListener, 2);

		label = new Label(propertiesGroup, SWT.NONE);
		label.setText(getContentPageNameLabel());
		fNameText = createText(propertiesGroup, propertiesListener, 2);

		label = new Label(propertiesGroup, SWT.NONE);
		label.setText(getContentPageVersionLabel());
		fVersionText = createText(propertiesGroup, propertiesListener, 2);

		label = new Label(propertiesGroup, SWT.NONE);
		label.setText(getContentPageProviderLabel());
		fProviderText = createText(propertiesGroup, propertiesListener, 2);
	}

	/**
	 * @see ContentPage
	 */
	private Text createText(Composite parent, ModifyListener listener, int horizSpan) {
		Text text = new Text(parent, SWT.BORDER | SWT.SINGLE);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = horizSpan;
		text.setLayoutData(data);
		text.addModifyListener(listener);
		return text;
	}

	protected abstract String getContentPageDescription();

	protected abstract String getContentPageGroupLabel();

	protected abstract String getContentPageIdLabel();

	protected abstract String getContentPageNameLabel();

	protected abstract String getContentPagePluginLabel();

	protected abstract String getContentPageProviderLabel();

	protected abstract String getContentPageTitle();

	protected abstract String getContentPageVersionLabel();

	/**
	 * @see PluginContentPage
	 */
	private String getNameFieldQualifier() {
		return getContentPagePluginLabel();
	}

	/**
	 * @see IdUtil
	 */
	private String getValidName(String id) {
		StringTokenizer tok = new StringTokenizer(id, "."); //$NON-NLS-1$
		while (tok.hasMoreTokens()) {
			String token = tok.nextToken();
			if (!tok.hasMoreTokens()) {
				String name = Character.toUpperCase(token.charAt(0)) + ((token.length() > 1) ? token.substring(1) : ""); //$NON-NLS-1$
				return name;
			}
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * @see ContentPage
	 */
	private boolean isVersionValid(String version) {
		return VersionUtil.validateVersion(version).getSeverity() == IStatus.OK;
	}

	/**
	 * @see ContentPage
	 */
	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			String id = computeId();
			// properties group
			if ((fChangedGroups & PROPERTIES_GROUP) == 0) {
				int oldfChanged = fChangedGroups;
				fIdText.setText(id);
				fVersionText.setText("1.0.0"); //$NON-NLS-1$
				fNameText.setText(getValidName(id));
				fProviderText.setText(IdUtil.getValidProvider(id));
				fChangedGroups = oldfChanged;
			}
			if (fInitialized) {
				validatePage();
			} else {
				fInitialized = true;
			}
		}
		super.setVisible(visible);
	}

	/**
	 * @see ContentPage
	 */
	protected void updateData() {
		fData.setId(fIdText.getText().trim());
		fData.setVersion(fVersionText.getText().trim());
		fData.setName(fNameText.getText().trim());
		fData.setProvider(fProviderText.getText().trim());
	}

	/**
	 * @see ContentPage
	 */
	private String validateId() {
		String id = fIdText.getText().trim();
		if (id.length() == 0) {
			return PdeMessageStrings.ContentPage_noid;
		}

		if (!IdUtil.isValidCompositeID3_0(id)) {
			return PdeMessageStrings.ContentPage_invalidId;
		}
		return null;
	}

	/**
	 * @see ContentPage
	 */
	private String validateName() {
		if (fNameText.getText().trim().length() == 0) {
			return PdeMessageStrings.ContentPage_noname;
		}
		return null;
	}

	/**
	 * @see PluginContentPage
	 */
	protected void validatePage() {
		String errorMessage = validateProperties();
		setErrorMessage(errorMessage);
		setPageComplete(errorMessage == null);
	}

	/**
	 * @see ContentPage
	 */
	private String validateProperties() {

		if (!fInitialized) {
			if (!fIdText.getText().trim().equals(fProjectProvider.getProjectName())) {
				setMessage(PdeMessageStrings.ContentPage_illegalCharactersInID, INFORMATION);
			} else {
				setMessage(null);
			}
			return null;
		}

		setMessage(null);
		String errorMessage = null;

		// Validate ID
		errorMessage = validateId();
		if (errorMessage != null) {
			return errorMessage;
		}

		// Validate Version
		errorMessage = validateVersion(fVersionText);
		if (errorMessage != null) {
			return errorMessage;
		}

		// Validate Name
		errorMessage = validateName();
		if (errorMessage != null) {
			return errorMessage;
		}

		return null;
	}

	/**
	 * @see ContentPage
	 */
	private String validateVersion(Text text) {
		if (text.getText().trim().length() == 0) {
			return PDELabelUtility.qualifyMessage(PDELabelUtility.getFieldLabel(text),
					PdeMessageStrings.ControlValidationUtility_errorMsgValueMustBeSpecified);
		} else if (!isVersionValid(text.getText().trim())) {
			return PDELabelUtility.qualifyMessage(PDELabelUtility.getFieldLabel(text),
					PdeMessageStrings.BundleErrorReporter_InvalidFormatInBundleVersion);
		}
		return null;
	}

}
