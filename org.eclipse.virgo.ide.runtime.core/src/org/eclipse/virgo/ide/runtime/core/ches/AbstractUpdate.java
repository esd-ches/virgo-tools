package org.eclipse.virgo.ide.runtime.core.ches;

import java.io.File;
import java.util.List;

public abstract class AbstractUpdate implements IUpdate {
	protected File toUpdate;
	protected VirgoToolingHook hook;
	private long timestamp;

	public AbstractUpdate(VirgoToolingHook hook, File toUpdate) {
		this.hook = hook;
		this.toUpdate = toUpdate;
		this.timestamp = System.currentTimeMillis();
	}

	@Override
	public long getTimestamp() {
		return timestamp;
	}

	@Override
	public boolean isApplicable() {
		List<File> targets = hook.lookup(toUpdate);
		return !targets.isEmpty();
	}
}
