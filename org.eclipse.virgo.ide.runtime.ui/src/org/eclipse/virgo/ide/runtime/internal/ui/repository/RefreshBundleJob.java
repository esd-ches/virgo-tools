/*******************************************************************************
 * Copyright (c) 2009 - 2013 SpringSource, a divison of VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource, a division of VMware, Inc. - initial API and implementation
 *******************************************************************************/

package org.eclipse.virgo.ide.runtime.internal.ui.repository;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.virgo.ide.runtime.core.ServerCorePlugin;
import org.eclipse.virgo.ide.runtime.core.provisioning.RepositoryUtils;
import org.eclipse.virgo.ide.runtime.internal.ui.editor.Messages;
import org.eclipse.wst.server.core.IRuntime;

/**
 * @author Miles Parker
 * @author Leo Dos Santos
 */
public class RefreshBundleJob implements IRunnableWithProgress {
	private final IRuntime runtime;

	RefreshBundleJob(IRuntime runtime) {
		this.runtime = runtime;
	}

	public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		monitor.subTask(Messages.RepositoryBrowserEditorPage_RefreshingBundlesMessage);
		if (RepositoryUtils.doesRuntimeSupportRepositories(runtime)) {
			ServerCorePlugin.getArtefactRepositoryManager().refreshBundleRepository(runtime);
		}
		monitor.done();
	}

	public static void execute(Shell shell, IRuntime runtime) {
		RefreshBundleJob job = new RefreshBundleJob(runtime);
		try {
			IRunnableContext context = new ProgressMonitorDialog(shell);
			context.run(true, true, job);
		} catch (InvocationTargetException e1) {
			StatusManager.getManager().handle(
					new Status(IStatus.ERROR, ServerCorePlugin.PLUGIN_ID,
							"Åproblem occurred while updating repository", e1));
		} catch (InterruptedException e) {
		}
	}
}