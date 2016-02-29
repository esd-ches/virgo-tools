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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wst.project.facet.SimpleWebFacetInstallDataModelProvider;

/**
 * A wizard page for specifying a Web Bundle context root.
 * <p />
 *
 */
public class NewPDEProjectWABPage extends WizardPage {

    /**
     * A subclass created for the purpose of increasing the visibility of the validation method.
     */
    private static class WSTValidator extends SimpleWebFacetInstallDataModelProvider {

        @Override
        public org.eclipse.core.runtime.IStatus validateContextRoot(String contextRoot) {
            return super.validateContextRoot(contextRoot);
        };
    };

    private final WSTValidator validator = new WSTValidator();

    /**
     * The page name.
     */
    public static final String PAGE_NAME = NewPDEProjectWABPage.class.getSimpleName();

    public NewPDEProjectWABPage() {
        super(PAGE_NAME);
        setTitle(Messages.NewPDEProjectWABPage_title);
        setDescription(Messages.NewPDEProjectWABPage_description);
    }

    private Label contextPathLabel;

    private Text contextPathText;

    private String contextRoot;

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

        this.contextPathLabel = new Label(container, SWT.NONE);
        this.contextPathLabel.setText(Messages.NewPDEProjectWABPage_context_root);

        this.contextPathText = new Text(container, SWT.BORDER | SWT.SINGLE);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).span(2, 1).applyTo(this.contextPathText);

        this.contextPathText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                NewPDEProjectWABPage.this.modifyText(e);
            }
        });
    }

    /**
     * Validates the context root.
     *
     * @param e the modify event fired by the text widget
     */
    protected void modifyText(ModifyEvent e) {
        contextRoot = null;

        String name = contextPathText.getText();
        IStatus status = validator.validateContextRoot(name);

        if (!status.isOK()) {
            setPageComplete(false);
            setErrorMessage(status.getMessage());
            return;
        }

        contextRoot = name;
        setPageComplete(true);
        setErrorMessage(null);
    }

    /**
     * Returns the value of the context root.
     *
     * @return the context root
     */
    public String getContextRoot() {
        return contextRoot;
    }
}
