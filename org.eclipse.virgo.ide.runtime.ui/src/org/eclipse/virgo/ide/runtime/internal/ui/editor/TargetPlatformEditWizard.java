
package org.eclipse.virgo.ide.runtime.internal.ui.editor;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.virgo.ide.runtime.internal.ui.PDETargetPlatformComposite;
import org.eclipse.virgo.ide.runtime.internal.ui.PDEUIMessages;
import org.eclipse.virgo.ide.ui.ServerIdeUiPlugin;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;

public class TargetPlatformEditWizard extends Wizard {

    private final class PDEPage extends WizardPage {

        private PDEPage() {
            super("main"); //$NON-NLS-1$
            setTitle("Configure Eclipse PDE Target Platform"); //$NON-NLS-1$
            setDescription("Define a PDE Target Platform if you intend to develop using PDE Tools"); //$NON-NLS-1$
        }

        public void createControl(Composite parent) {
            composite = new PDETargetPlatformComposite(parent, getContainer(), workingCopy);
            composite.getEnablePDEDevelopmentButton().setEnabled(false);
            setControl(composite);
        }
    }

    private final IRuntimeWorkingCopy workingCopy;

    private PDETargetPlatformComposite composite;

    public TargetPlatformEditWizard(IRuntimeWorkingCopy workingCopy) {
        super();
        this.workingCopy = workingCopy;
        setNeedsProgressMonitor(true);
    }

    @Override
    public void addPages() {
        addPage(new PDEPage());
    }

    @Override
    public boolean performFinish() {
        try {
            getContainer().run(true, false, new IRunnableWithProgress() {

                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    composite.performFinish(monitor);
                }
            });
        } catch (Exception e) {
            StatusManager.getManager().handle(new Status(IStatus.ERROR, ServerIdeUiPlugin.PLUGIN_ID, TargetPlatformSectionMessages.TargetPlatformSection_InternalError, e),
                StatusManager.LOG | StatusManager.SHOW);
        }
        return true;
    }
}
