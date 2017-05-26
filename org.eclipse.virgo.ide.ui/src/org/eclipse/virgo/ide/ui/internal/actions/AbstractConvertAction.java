
package org.eclipse.virgo.ide.ui.internal.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.virgo.ide.ui.ServerIdeUiPlugin;

/**
 * Base class for migrating projects to Virgo Tools PDE Bundle project
 *
 * @author giamma
 *
 */
abstract class AbstractConvertAction implements IObjectActionDelegate {

    protected List<?> projects;

    protected IWorkbenchPart part;

    public AbstractConvertAction() {
        super();
    }

    /**
     * Return the nature used for selecting projects target of this convertion action.
     *
     * @return
     */
    protected abstract String getNature();

    /**
     * {@inheritDoc}
     */
    public void run(IAction arg0) {

        boolean result = showConfirmationDialog();

        if (!result) {
            return;
        }

        ProgressMonitorDialog dlg = new ProgressMonitorDialog(this.part.getSite().getShell());
        try {
            dlg.run(true, true, new IRunnableWithProgress() {

                public void run(IProgressMonitor arg0) throws InvocationTargetException, InterruptedException {
                    AbstractConvertAction.this.migrate(arg0);
                }
            });
        } catch (Exception e) {
            Status s = new Status(IStatus.ERROR, ServerIdeUiPlugin.PLUGIN_ID, e.getMessage());
            StatusManager.getManager().handle(s, StatusManager.SHOW);
        }
    }

    /**
     * Perform migration.
     *
     * @param monitor
     */
    protected void migrate(IProgressMonitor monitor) {
        monitor.beginTask("", this.projects.size()); //$NON-NLS-1$
        SubMonitor subMonitor = SubMonitor.convert(monitor);
        for (Object o : this.projects) {
            IProject project = (IProject) o;
            migrate(subMonitor.newChild(1), project);

        }
        monitor.done();
    }

    /**
     * Migrate an individual project.
     *
     * @param monitor the progress monitor.
     * @param project the project.
     */
    protected abstract void migrate(IProgressMonitor monitor, IProject project);

    /**
     * Show a confirmation dialog to the user.
     *
     * @return
     */
    protected abstract boolean showConfirmationDialog();

    /**
     * {@inheritDoc}
     */
    public void selectionChanged(IAction action, ISelection selection) {
        IStructuredSelection ss = (IStructuredSelection) selection;
        this.projects = ss.toList();
        boolean validSelection = true;
        for (Iterator<?> iterator = this.projects.iterator(); iterator.hasNext() && validSelection;) {
            Object object = iterator.next();
            if (object instanceof IProject) {
                IProject prj = (IProject) object;
                if (prj.isOpen()) {
                    try {
                        if (prj.hasNature(getNature())) {
                            continue;
                        }
                    } catch (CoreException e) {
                        validSelection = false;
                    }
                } else {
                    validSelection = false;
                }
            }
        }

        action.setEnabled(validSelection);
        if (!validSelection) {
            this.projects = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setActivePart(IAction action, IWorkbenchPart part) {
        this.part = part;
    }

}