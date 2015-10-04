/*******************************************************************************
 * Copyright (c) 2011 SpringSource, a divison of VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource, a division of VMware, Inc. - initial API and implementation
 *******************************************************************************/

package org.eclipse.virgo.ide.ui.editors;

import org.eclipse.core.resources.IProject;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.pde.internal.ui.editor.FormEntryAdapter;
import org.eclipse.pde.internal.ui.editor.FormLayoutFactory;
import org.eclipse.pde.internal.ui.editor.PDEFormPage;
import org.eclipse.pde.internal.ui.parts.FormEntry;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.virgo.ide.manifest.core.IHeaderConstants;

/**
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @author Martin Lippert
 */
public class ParGeneralInfoSection extends AbstractPdeGeneralInfoSection {

    public ParGeneralInfoSection(PDEFormPage page, Composite parent) {
        super(page, parent);
    }

    @Override
    protected void createSpecificControls(Composite parent, FormToolkit toolkit, IActionBars actionBars) {
    }

    @Override
    protected void createClient(Section section, FormToolkit toolkit) {
        section.setText(PDEUIMessages.ManifestEditor_PluginSpecSection_title);
        section.setLayout(FormLayoutFactory.createClearTableWrapLayout(false, 1));
        TableWrapData data = new TableWrapData(TableWrapData.FILL_GRAB);
        section.setLayoutData(data);

        section.setDescription(getSectionDescription());
        Composite client = toolkit.createComposite(section);
        client.setLayout(FormLayoutFactory.createSectionClientTableWrapLayout(false, 2));
        section.setClient(client);

        IActionBars actionBars = getPage().getPDEEditor().getEditorSite().getActionBars();
        createIDEntry(client, toolkit, actionBars);
        createVersionEntry(client, toolkit, actionBars);
        createNameEntry(client, toolkit, actionBars);
        createProviderEntry(client, toolkit, actionBars);
        createSpecificControls(client, toolkit, actionBars);
        toolkit.paintBordersFor(client);
    }

    @Override
    protected void createIDEntry(Composite client, FormToolkit toolkit, IActionBars actionBars) {
        this.fIdEntry = new FormEntry(client, toolkit, PDEUIMessages.GeneralInfoSection_id, null, false);
        this.fIdEntry.setFormEntryListener(new FormEntryAdapter(this, actionBars) {

            @Override
            public void textValueChanged(FormEntry entry) {
                getBundle().setHeader(IHeaderConstants.PAR_SYMBOLICNAME, ParGeneralInfoSection.this.fIdEntry.getValue());
            }
        });
        this.fIdEntry.setEditable(isEditable());
        // Create validator
        // fIdEntryValidator = new TextValidator(getManagedForm(),
        // fIdEntry.getText(), getProject(), true) {
        // @Override
        // protected boolean validateControl() {
        // return validateIdEntry();
        // }
        // };
    }

    @Override
    protected void createVersionEntry(Composite client, FormToolkit toolkit, IActionBars actionBars) {
        this.fVersionEntry = new FormEntry(client, toolkit, PDEUIMessages.GeneralInfoSection_version, null, false);
        this.fVersionEntry.setFormEntryListener(new FormEntryAdapter(this, actionBars) {

            @Override
            public void textValueChanged(FormEntry entry) {
                getBundle().setHeader(IHeaderConstants.PAR_VERSION, ParGeneralInfoSection.this.fVersionEntry.getValue());
            }
        });
        this.fVersionEntry.setEditable(isEditable());
        // Create validator
        // fVersionEntryValidator = new TextValidator(getManagedForm(),
        // fVersionEntry.getText(), getProject(), true) {
        // @Override
        // protected boolean validateControl() {
        // return validateVersionEntry();
        // }
        // };
    }

    @Override
    protected void createNameEntry(Composite client, FormToolkit toolkit, IActionBars actionBars) {
        this.fNameEntry = new FormEntry(client, toolkit, PDEUIMessages.GeneralInfoSection_name, null, false);
        this.fNameEntry.setFormEntryListener(new FormEntryAdapter(this, actionBars) {

            @Override
            public void textValueChanged(FormEntry entry) {
                getBundle().setHeader(IHeaderConstants.PAR_NAME, ParGeneralInfoSection.this.fNameEntry.getValue());
            }
        });
        this.fNameEntry.setEditable(isEditable());
        // Create validator
        // fNameEntryValidator = new TextValidator(getManagedForm(),
        // fNameEntry.getText(), getProject(), true) {
        // @Override
        // protected boolean validateControl() {
        // return validateNameEntry();
        // }
        // };
    }

    @Override
    protected void createProviderEntry(Composite client, FormToolkit toolkit, IActionBars actionBars) {
        this.fProviderEntry = new FormEntry(client, toolkit, "Description", null, false);
        this.fProviderEntry.setFormEntryListener(new FormEntryAdapter(this, actionBars) {

            @Override
            public void textValueChanged(FormEntry entry) {
                getBundle().setHeader(IHeaderConstants.PAR_DESCRIPTION, ParGeneralInfoSection.this.fProviderEntry.getValue());
            }
        });
        this.fProviderEntry.setEditable(isEditable());
        // Create validator
        // fProviderEntryValidator = new TextValidator(getManagedForm(),
        // fProviderEntry.getText(), getProject(), true) {
        // @Override
        // protected boolean validateControl() {
        // return validateProviderEntry();
        // }
        // };
    }

    @Override
    protected String getSectionDescription() {
        return "This section describes general information about this PAR";
    }

    @Override
    public void refresh() {
        if (this.fIdEntry != null) {
            this.fIdEntry.setValue(getBundle().getHeader(IHeaderConstants.PAR_SYMBOLICNAME), true);
        }
        if (this.fNameEntry != null) {
            this.fNameEntry.setValue(getBundle().getHeader(IHeaderConstants.PAR_NAME), true);
        }
        if (this.fProviderEntry != null) {
            this.fProviderEntry.setValue(getBundle().getHeader(IHeaderConstants.PAR_DESCRIPTION), true);
        }
        if (this.fVersionEntry != null) {
            this.fVersionEntry.setValue(getBundle().getHeader(IHeaderConstants.PAR_VERSION), true);
        }
        getPage().getPDEEditor().updateTitle();
    }

    public IProject getParProject() {
        return getProject();
    }

}
