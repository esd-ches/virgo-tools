package org.eclipse.virgo.ide.runtime.core.ches;

import java.io.File;
import java.util.List;

public class DirectoryUpdate extends AbstractUpdate {

	public DirectoryUpdate(VirgoToolingHook hook, File toUpdate) {
		super(hook, toUpdate);
	}

	@Override
	public void apply() {
		List<File> targets = hook.lookup(toUpdate);
		for (File file : targets) {
			file.mkdirs();
		}
	}

}
