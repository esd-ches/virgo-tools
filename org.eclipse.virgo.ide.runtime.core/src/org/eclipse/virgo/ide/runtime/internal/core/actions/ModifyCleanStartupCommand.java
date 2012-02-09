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
package org.eclipse.virgo.ide.runtime.internal.core.actions;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.virgo.ide.runtime.core.IServerWorkingCopy;


/**
 * {@link AbstractOperation} to modify the clean startup setting
 * @author Christian Dupuis
 * @since 2.0.0
 */
public class ModifyCleanStartupCommand extends AbstractOperation {

	private IServerWorkingCopy workingCopy;

	private boolean oldValue;

	private boolean newValue;

	public ModifyCleanStartupCommand(IServerWorkingCopy workingCopy, boolean newValue) {
		super("Modify Clean Startup");
		this.workingCopy = workingCopy;
		this.oldValue = workingCopy.shouldCleanStartup();
		this.newValue = newValue;
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		workingCopy.shouldCleanStartup(newValue);
		return Status.OK_STATUS;
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		workingCopy.shouldCleanStartup(newValue);
		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		workingCopy.shouldCleanStartup(oldValue);
		return Status.OK_STATUS;
	}

}
