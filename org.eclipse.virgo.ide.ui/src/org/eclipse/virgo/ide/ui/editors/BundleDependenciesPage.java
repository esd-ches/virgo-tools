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

package org.eclipse.virgo.ide.ui.editors;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.PDEPluginImages;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.pde.internal.ui.editor.FormLayoutFactory;
import org.eclipse.pde.internal.ui.editor.PDEFormPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.virgo.ide.ui.ServerIdeUiPlugin;
import org.eclipse.virgo.ide.ui.StatusHandler;

/**
 * @author Christian Dupuis
 */
public class BundleDependenciesPage extends PDEFormPage implements IBundleManifestSaveListener {

    public static final String PAGE_ID = "bundle_dependencies"; //$NON-NLS-1$

    private BundleImportPackageSection bundleImportPackageSection = null;

    private BundleImportSection bundleImportSection = null;

    private BundleImportLibrarySection bundleImportLibrarySection = null;

    protected ScrolledForm form = null;

    protected IResource resource = null;

    private static final String MANIFEST_ERRORS = "Dependencies: Please correct one or more errors in the manifest";

    public BundleDependenciesPage(FormEditor editor) {
        super(editor, PAGE_ID, "Dependencies");
    }

    @Override
    protected void createFormContent(IManagedForm managedForm) {
        super.createFormContent(managedForm);
        this.form = managedForm.getForm();
        this.form.setImage(PDEPlugin.getDefault().getLabelProvider().get(PDEPluginImages.DESC_REQ_PLUGINS_OBJ));
        this.form.setText(PDEUIMessages.DependenciesPage_title);
        Composite body = this.form.getBody();
        body.setLayout(FormLayoutFactory.createFormGridLayout(true, 2));
        Composite left, right;
        FormToolkit toolkit = managedForm.getToolkit();
        left = toolkit.createComposite(body, SWT.NONE);
        left.setLayout(FormLayoutFactory.createFormPaneGridLayout(false, 1));
        left.setLayoutData(new GridData(GridData.FILL_BOTH));
        right = toolkit.createComposite(body, SWT.NONE);
        right.setLayout(FormLayoutFactory.createFormPaneGridLayout(false, 1));
        right.setLayoutData(new GridData(GridData.FILL_BOTH));

        this.bundleImportPackageSection = new BundleImportPackageSection(this, left);
        this.bundleImportSection = new BundleImportSection(this, right);
        this.bundleImportLibrarySection = new BundleImportLibrarySection(this, right);

        managedForm.addPart(this.bundleImportPackageSection);
        managedForm.addPart(this.bundleImportSection);
        managedForm.addPart(this.bundleImportLibrarySection);

        IPluginModelBase model = (IPluginModelBase) ((BundleManifestEditor) this.getEditor()).getAggregateModel();
        this.resource = model.getUnderlyingResource();
        updateFormText();
    }

    public void manifestSaved() {
        if (this.resource != null) {
            updateFormText();
        }
    }

    protected void updateFormText() {
        try {
            // Wait for build
            Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD, null);
        } catch (OperationCanceledException e) {
            StatusHandler.log(new Status(IStatus.ERROR, ServerIdeUiPlugin.PLUGIN_ID, "Could not update page title text", e));
        } catch (InterruptedException e) {
            // Nothing to do?
        }

        try {
            if (this.resource != null) {
                IMarker[] markers = this.resource.findMarkers(null, true, IResource.DEPTH_ZERO);
                if (ManifestEditorUtils.hasErrorSeverityMarker(markers)) {
                    this.form.setText(MANIFEST_ERRORS);
                    this.form.setImage(ServerIdeUiPlugin.getImage("full/obj16/manifest_error.png"));
                } else {
                    this.form.setText(PDEUIMessages.DependenciesPage_title);
                    this.form.setImage(ServerIdeUiPlugin.getImage("full/obj16/osgi_obj.gif"));
                }
            }
        } catch (CoreException e) {
            StatusHandler.log(new Status(IStatus.ERROR, ServerIdeUiPlugin.PLUGIN_ID, "Could not update page title text", e));
        }

    }

    /** For JUnit testing only * */
    public BundleImportPackageSection getBundleImportPackageSection() {
        return this.bundleImportPackageSection;
    }

    /** For JUnit testing only * */
    public BundleImportSection getBundleImportSection() {
        return this.bundleImportSection;
    }

    /** For JUnit testing only * */
    public BundleImportLibrarySection getBundleImportLibrarySection() {
        return this.bundleImportLibrarySection;
    }

}
