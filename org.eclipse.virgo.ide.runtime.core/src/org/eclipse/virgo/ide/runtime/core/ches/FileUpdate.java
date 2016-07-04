
package org.eclipse.virgo.ide.runtime.core.ches;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.List;

public class FileUpdate extends AbstractUpdate {

    public FileUpdate(VirgoToolingHook hook, File toUpdate) {
        super(hook, toUpdate);
    }

    @Override
    public void apply() {
        List<File> targets = hook.lookup(toUpdate);

        for (File target : targets) {
            try (FileInputStream input = new FileInputStream(toUpdate); FileOutputStream output = new FileOutputStream(target)) {
                VirgoToolingHook.logInfo("Copy " + toUpdate + "\n -> " + target);

                FileChannel in = input.getChannel();
                FileChannel out = output.getChannel();
                out.transferFrom(in, 0, in.size());
            } catch (Exception e) {
                VirgoToolingHook.logError("Error while copying " + toUpdate + "\n -> " + target, e);
            }
        }
    }
}
