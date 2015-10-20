
package org.eclipse.virgo.ide.runtime.core.ches;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.util.List;

public class VirgoWatcherRunnable extends AbstractWatchServiceRunnable {

    private final VirgoToolingHook hook;

    public VirgoWatcherRunnable(VirgoToolingHook hook) throws IOException {
        this.hook = hook;
    }

    @Override
    protected void runInternal() throws Exception {
        File virgo = VirgoToolingHook.getVirgoBase();
        Path path = Paths.get(virgo.toURI());
        path.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        VirgoToolingHook.logInfo("Watching Virgo at: " + virgo);

        WatchKey key;
        do {
            key = watchService.take();

            // check if virgo was restarted; if so, replay old changes
            List<WatchEvent<?>> events = key.pollEvents();
            if (!events.isEmpty()) {
                Path plan = (Path) events.get(0).context();
                if (plan.equals("global")) {
                    hook.virgoPublishFinished();
                }
            }
        } while (key.reset() && !terminated);

    }

    @Override
    public String getName() {
        return "CHES Virgo Watcher";
    }

}
