
package org.eclipse.virgo.ide.runtime.core.ches;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.List;

public class FileUpdate extends AbstractUpdate {

    public FileUpdate(VirgoToolingHook hook, File toUpdate) {
        super(hook, toUpdate);
    }

    @Override
    public void apply() {
        List<File> targets = hook.lookup(toUpdate);
        FileInputStream input = null;
        FileOutputStream output = null;

        try {
            for (File target : targets) {
                VirgoToolingHook.logInfo("Copy " + toUpdate + "\n -> " + target);

                input = new FileInputStream(toUpdate);
                output = new FileOutputStream(target);
                FileChannel in = input.getChannel();
                FileChannel out = output.getChannel();
                out.transferFrom(in, 0, in.size());
                close(input);
                close(output);
            }
        } catch (Exception e) {
            close(input);
            close(output);
        }

    }

    private void close(Closeable closable) {
        if (closable == null) {
            return;
        }

        try {
            closable.close();
        } catch (IOException e) {
            // ignore
        }
    }
}
