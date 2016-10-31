/*******************************************************************************
 *  Copyright (c) 2015 GianMaria Romanato
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     GianMaria Romanato - initial API and implementation
 *******************************************************************************/

package org.eclipse.virgo.ide.ui.wizards;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * A wizard page for specifying a Web Bundle context root.
 * <p />
 *
 */
public class NewPlanProjectFilePage extends WizardPage {

    /**
     * The page name.
     */
    public static final String PAGE_NAME = NewPlanProjectFilePage.class.getSimpleName();

    public NewPlanProjectFilePage() {
        super(PAGE_NAME);
        setTitle(Messages.NewPlanProjectNamePage_title);
        setDescription(Messages.NewPlanProjectNamePage_description);
    }

    private String planName;

    private Label planNameLabel;

    private Text planNameText;

    private Button scoped;

    private Button atomic;

    /**
     * {@inheritDoc}
     */
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout());
        createContextRootSection(container);
        setControl(container);
    }

    /**
     * Creates the context root text field.
     *
     * @param container the parent composite
     */
    protected void createContextRootSection(Composite container) {
        container.setLayout(new GridLayout(3, false));
        container.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        this.planNameLabel = new Label(container, SWT.NONE);
        this.planNameLabel.setText(Messages.NewPlanProjectNamePage_plan_label);

        this.planNameText = new Text(container, SWT.BORDER | SWT.SINGLE);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).span(2, 1).applyTo(this.planNameText);

        this.scoped = new Button(container, SWT.CHECK);
        scoped.setText(Messages.NewPlanProjectFilePage_scoped_label);
        scoped.setSelection(false);

        this.atomic = new Button(container, SWT.CHECK);
        atomic.setText(Messages.NewPlanProjectFilePage_atomic_label);
        atomic.setSelection(true);

        this.planNameText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                NewPlanProjectFilePage.this.modifyText(e);
            }
        });
    }

    /**
     * Validates the context root.
     *
     * @param e the modify event fired by the text widget
     */
    protected void modifyText(ModifyEvent e) {
        planName = null;

        String name = planNameText.getText();

        IStatus nameStatus = ResourcesPlugin.getWorkspace().validateName(name, IResource.FILE);

        if (!nameStatus.isOK()) {
            setErrorMessage(nameStatus.getMessage());
            setPageComplete(false);
            return;
        }

        planName = name;
        setPageComplete(true);
        setErrorMessage(null);
    }

    /**
     * Returns the value of the plan name.
     *
     * @return the plan name
     */
    public String getPlanName() {
        return planName;
    }

    public boolean isScoped() {
        return scoped.getSelection();
    }

    public boolean isAtomic() {
        return atomic.getSelection();
    }

}
