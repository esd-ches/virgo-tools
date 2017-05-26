
package org.eclipse.virgo.ide.runtime.internal.ui.editor;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.virgo.ide.runtime.core.IServerWorkingCopy;
import org.eclipse.virgo.ide.runtime.internal.ui.PDEHelper;
import org.eclipse.virgo.ide.runtime.internal.ui.PDEUIMessages;
import org.eclipse.virgo.ide.ui.ServerIdeUiPlugin;
import org.eclipse.wst.server.ui.editor.ServerEditorSection;

public class TargetPlatformSection extends ServerEditorSection {

    private static String FORM_TEXT = TargetPlatformSectionMessages.TargetPlatformSection_form_text;

    private IServerWorkingCopy serverWorkingCopy;

    /**
     * {@inheritDoc}
     */
    @Override
    public void createSection(Composite parent) {
        super.createSection(parent);
        FormToolkit toolkit = getFormToolkit(parent.getDisplay());

        Section section = toolkit.createSection(parent, ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED | ExpandableComposite.TITLE_BAR
            | Section.DESCRIPTION | ExpandableComposite.FOCUS_TITLE);
        section.setText(TargetPlatformSectionMessages.TargetPlatformSection_title);
        section.setDescription(TargetPlatformSectionMessages.TargetPlatformSection_description);
        section.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        Composite composite = toolkit.createComposite(section);
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        toolkit.paintBordersFor(composite);
        section.setClient(composite);

        FormText text = toolkit.createFormText(composite, true);
        text.setText(FORM_TEXT, true, false);
        text.addHyperlinkListener(new HyperlinkAdapter() {

            @Override
            public void linkActivated(HyperlinkEvent e) {
                if (!PDEHelper.existsTargetDefinition(server.getRuntime().getName())) {
                    MessageDialog.openInformation(getShell(), TargetPlatformSectionMessages.TargetPlatformSection_not_configured_title,
                        TargetPlatformSectionMessages.TargetPlatformSection_not_configured_message);
                    return;
                }
                if ("refresh".equals(e.getHref())) { //$NON-NLS-1$
                    refreshPressed();
                    return;
                }
                if ("edit".equals(e.getHref())) { //$NON-NLS-1$
                    editPressed();
                    return;
                }
            }
        });

    }

    protected void editPressed() {
        TargetPlatformEditWizard wiz = new TargetPlatformEditWizard(server.getRuntime().createWorkingCopy());
        wiz.setWindowTitle(PDEUIMessages.PDETargetPlatformWizardFragment_title);

        WizardDialog dlg = new WizardDialog(getShell(), wiz);
        dlg.setTitle(PDEUIMessages.PDETargetPlatformWizardFragment_title);
        dlg.open();
    }

    protected void refreshPressed() {
        ProgressMonitorDialog dlg = new ProgressMonitorDialog(getShell());
        try {
            dlg.run(true, false, new IRunnableWithProgress() {

                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    try {
                        PDEHelper.refreshTargetDefinition(monitor, server.getRuntime().getName());
                    } catch (CoreException e) {
                        StatusManager.getManager().handle(e.getStatus(), StatusManager.SHOW);
                    }
                }
            });
        } catch (Exception e) {
            StatusManager.getManager().handle(new Status(IStatus.ERROR, ServerIdeUiPlugin.PLUGIN_ID, e.getMessage(), e),
                StatusManager.SHOW | StatusManager.LOG);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(IEditorSite site, IEditorInput input) {
        super.init(site, input);
    }

}
