
package org.eclipse.virgo.ide.ui.internal.actions;

import java.net.URL;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.virgo.ide.pde.core.internal.Constants;
import org.eclipse.virgo.ide.ui.ServerIdeUiPlugin;

/**
 * ConvertPDE2VirgoProject is a contextual menu action in the Virgo submenu for migrating projects created with the
 * unofficial PDE2Virgo plug-in to the official Virgo tools support for PDE.
 *
 * The migration consists in simply changing the project nature.
 */
public class ConvertPDE2VirgoProject extends AbstractConvertAction implements IObjectActionDelegate {

    private static final String PDE2_VIRGO_NATURE = "org.github.pde2virgo.PDE2VirgoNature"; //$NON-NLS-1$

    @Override
    protected String getNature() {
        return PDE2_VIRGO_NATURE;
    }

    @Override
    protected void migrate(IProgressMonitor monitor, IProject project) {
        monitor.beginTask("", 1); //$NON-NLS-1$
        try {
            IProjectDescription description = project.getDescription();
            String[] natures = description.getNatureIds();
            for (int i = 0; i < natures.length; i++) {
                if (PDE2_VIRGO_NATURE.equals(natures[i])) {
                    natures[i] = Constants.NATURE_ID;
                    break;
                }
            }
            description.setNatureIds(natures);

            project.setDescription(description, monitor);

        } catch (CoreException e) {
            Status s = new Status(IStatus.ERROR, ServerIdeUiPlugin.PLUGIN_ID, e.getMessage());
            StatusManager.getManager().handle(s, StatusManager.SHOW);
        } finally {
            monitor.done();
        }
    }

    @Override
    protected boolean showConfirmationDialog() {
        MessageDialog dlg = new MessageDialog(this.part.getSite().getShell(), Messages.ConvertPDE2VirgoProject_title, null, "", MessageDialog.QUESTION, //$NON-NLS-2$
            new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL }, 0) {

            @Override
            protected Control createMessageArea(Composite composite) {
                composite.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).hint(200, -1).create());
                FormText text = new FormText(composite, SWT.NO_FOCUS);
                text.addHyperlinkListener(new HyperlinkAdapter() {

                    @Override
                    public void linkActivated(HyperlinkEvent e) {
                        try {
                            PlatformUI.getWorkbench().getBrowserSupport().createBrowser("PDE2Virgo").openURL(new URL(e.getHref().toString())); //$NON-NLS-1$
                        } catch (Exception e1) {
                        }
                    }
                });

                String xml = Messages.ConvertPDE2VirgoProject_message;
                text.setText(xml, true, false);

                return composite;
            }

        };
        int rc = dlg.open();
        return rc == Window.OK;
    }

}
