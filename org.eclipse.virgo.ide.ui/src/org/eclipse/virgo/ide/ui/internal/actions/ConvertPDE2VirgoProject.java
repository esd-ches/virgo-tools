
package org.eclipse.virgo.ide.ui.internal.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
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
import org.eclipse.virgo.ide.pde.core.internal.Constants;
import org.eclipse.virgo.ide.ui.ServerIdeUiPlugin;

/**
 * ConvertPDE2VirgoProject is a contextual menu action in the Virgo submenu for migrating projects created with the
 * unofficial PDE2Virgo plug-in to the official Virgo tools support for PDE.
 *
 * The migration consists in simply changing the project nature.
 */
public class ConvertPDE2VirgoProject implements IObjectActionDelegate {

    private static final String PDE2_VIRGO_NATURE = "org.github.pde2virgo.PDE2VirgoNature"; //$NON-NLS-1$

    private List<?> projects;

    private IWorkbenchPart part;

    public void run(IAction arg0) {
        ProgressMonitorDialog dlg = new ProgressMonitorDialog(part.getSite().getShell());
        try {
            dlg.run(true, true, new IRunnableWithProgress() {

                public void run(IProgressMonitor arg0) throws InvocationTargetException, InterruptedException {
                    ConvertPDE2VirgoProject.this.run(arg0);
                }
            });
        } catch (Exception e) {
            Status s = new Status(IStatus.ERROR, ServerIdeUiPlugin.PLUGIN_ID, e.getMessage());
            StatusManager.getManager().handle(s, StatusManager.SHOW);
        }

    }

    protected void run(IProgressMonitor monitor) {
        monitor.beginTask("", projects.size()); //$NON-NLS-1$
        SubMonitor subMonitor = SubMonitor.convert(monitor);
        for (Object o : projects) {
            IProject project = (IProject) o;
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

                IProgressMonitor projectMonitor = subMonitor.newChild(1);
                projectMonitor.setTaskName(project.getName());
                project.setDescription(description, projectMonitor);
            } catch (CoreException e) {
                Status s = new Status(IStatus.ERROR, ServerIdeUiPlugin.PLUGIN_ID, e.getMessage());
                StatusManager.getManager().handle(s, StatusManager.SHOW);
            }
        }
        monitor.done();
    }

    public void selectionChanged(IAction action, ISelection selection) {
        IStructuredSelection ss = (IStructuredSelection) selection;
        projects = ss.toList();
        boolean validSelection = true;
        for (Iterator<?> iterator = projects.iterator(); iterator.hasNext() && validSelection;) {
            Object object = iterator.next();
            if (object instanceof IProject) {
                IProject prj = (IProject) object;
                if (prj.isOpen()) {
                    try {
                        if (prj.hasNature(PDE2_VIRGO_NATURE)) {
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

    public void setActivePart(IAction action, IWorkbenchPart part) {
        this.part = part;
    }

}
