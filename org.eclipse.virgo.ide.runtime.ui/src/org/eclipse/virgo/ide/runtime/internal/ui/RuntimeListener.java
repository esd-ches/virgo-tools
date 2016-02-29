
package org.eclipse.virgo.ide.runtime.internal.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.progress.WorkbenchJob;
import org.eclipse.virgo.ide.runtime.internal.core.runtimes.VirgoRuntimeProvider;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeLifecycleListener;

public class RuntimeListener implements IRuntimeLifecycleListener {

    private RuntimeListener() {
        super();
    }

    private static RuntimeListener self = new RuntimeListener();

    public static RuntimeListener getDefault() {
        return self;
    }

    public void runtimeAdded(IRuntime runtime) {
    }

    public void runtimeChanged(IRuntime runtime) {
    }

    public void runtimeRemoved(IRuntime runtime) {
        if (runtime.getRuntimeType().getId().startsWith(VirgoRuntimeProvider.SERVER_VIRGO_BASE)) {
            final String name = runtime.getName();
            UIJob job = new WorkbenchJob("RemoveVirgoTargetPlatform") { //$NON-NLS-1$

                @Override
                public IStatus runInUIThread(IProgressMonitor monitor) {
                    if (PDEHelper.existsTargetDefinition(name)) {
                        boolean delete = MessageDialog.openQuestion(getDisplay().getActiveShell(), "Delete target platform?",
                            "A Virgo Runtime has been deleted. Do you also want to delete the associated PDE Target Platform?");
                        if (delete) {
                            PDEHelper.deleteTargetDefinition(name);
                        }
                    }
                    return Status.OK_STATUS;
                }
            };

            job.setUser(true);
            job.schedule();
        }
    }

}
