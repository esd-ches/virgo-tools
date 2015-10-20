/*******************************************************************************
 * Copyright (c) 2003, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.virgo.ide.eclipse.editors;

import java.util.ArrayList;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.pde.internal.ui.IHelpContextIds;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.PDEPluginImages;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.pde.internal.ui.editor.FormLayoutFactory;
import org.eclipse.pde.internal.ui.editor.plugin.DependenciesPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

/**
 * SpringSource Tool Suite Team - Portions of this class were copied from PDE's DependenciesPage in order to provide a
 * dependency editor-like form page outside of the PDEFormEditor.
 */
@SuppressWarnings("restriction")
public abstract class AbstractParXmlEditorPage extends AbstractPdeFormPage {

    public AbstractParXmlEditorPage(FormEditor editor, String id, String title) {
        super(editor, id, title);
    }

    /**
     * @see DependenciesPage
     */
    @Override
    protected void createFormContent(IManagedForm managedForm) {
        ScrolledForm form = managedForm.getForm();
        FormToolkit toolkit = managedForm.getToolkit();
        toolkit.decorateFormHeading(form.getForm());

        // From PDE's DependenciesPage
        form.setImage(PDEPlugin.getDefault().getLabelProvider().get(PDEPluginImages.DESC_REQ_PLUGINS_OBJ));
        form.setText(PDEUIMessages.DependenciesPage_title);
        Composite body = form.getBody();
        body.setLayout(FormLayoutFactory.createFormGridLayout(true, 3));
        Composite left, right;

        left = toolkit.createComposite(body, SWT.NONE);
        left.setLayout(FormLayoutFactory.createFormPaneGridLayout(false, 1));
        GridDataFactory.createFrom(new GridData(GridData.FILL_BOTH)).span(2, 1).applyTo(left);
        right = toolkit.createComposite(body, SWT.NONE);
        right.setLayout(FormLayoutFactory.createFormPaneGridLayout(false, 1));
        right.setLayoutData(new GridData(GridData.FILL_BOTH));

        managedForm.addPart(getFormPart(left, getRequiredSectionLabels()));

        PlatformUI.getWorkbench().getHelpSystem().setHelp(form.getBody(), IHelpContextIds.MANIFEST_PLUGIN_DEPENDENCIES);
    }

    protected abstract IFormPart getFormPart(Composite parent, String[] labels);

    /**
     * @see DependenciesPage
     */
    @SuppressWarnings("unchecked")
    private String[] getRequiredSectionLabels() {
        ArrayList labels = new ArrayList();
        labels.add(PDEUIMessages.RequiresSection_add);
        labels.add(PDEUIMessages.RequiresSection_delete);
        // labels.add(PDEUIMessages.RequiresSection_up);
        // labels.add(PDEUIMessages.RequiresSection_down);
        // if (isBundle()) {
        // labels.add(PDEUIMessages.DependenciesPage_properties);
        // }
        return (String[]) labels.toArray(new String[labels.size()]);
    }

}
