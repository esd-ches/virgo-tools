package org.eclipse.virgo.ide.runtime.internal.core.actions;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.virgo.ide.runtime.core.IServerWorkingCopy;

/**
 * {@link AbstractOperation} to modify the jmx deployer timeout.
 *
 * @author Daan de Wit
 */
public class ModifyDeployerTimeoutCommand extends AbstractOperation {

	private final IServerWorkingCopy workingCopy;

	private final int oldTimeout;

	private final int newTimeout;

	public ModifyDeployerTimeoutCommand(IServerWorkingCopy workingCopy, int timeout) {
		super("Modify JMX deployer timeout");
		this.workingCopy = workingCopy;
		this.oldTimeout = workingCopy.getDeployTimeout();
		this.newTimeout = timeout;
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		workingCopy.setDeployTimeout(newTimeout);
		return Status.OK_STATUS;
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		workingCopy.setDeployTimeout(newTimeout);
		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		workingCopy.setDeployTimeout(oldTimeout);
		return Status.OK_STATUS;
	}

}
