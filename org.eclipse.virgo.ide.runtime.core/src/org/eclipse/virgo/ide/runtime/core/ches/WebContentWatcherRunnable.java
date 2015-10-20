
package org.eclipse.virgo.ide.runtime.core.ches;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.util.Set;

import org.eclipse.core.resources.IProject;

public class WebContentWatcherRunnable extends AbstractFileWatcherRunnable {

    private final VirgoToolingHook hook;

    public WebContentWatcherRunnable(VirgoToolingHook hook, Set<IProject> projects) {
        super(projects, "WebContent");
        this.hook = hook;
    }

    @Override
    public void runInternal() throws Exception {
        setupWatchers();

        WatchKey key;
        do {
            key = watchService.take();
            IProject project = keyToProject.get(key);
            hook.scheduleRefresh(project);
            try {
                // Minimize duplicate detection of same change.
                Thread.sleep(100);
            } catch (Exception e) {
                // ignore
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                Path modified = (Path) event.context();
                File modifiedFile = new File(keyToFile.get(key), modified.getFileName().toString());

                if (modifiedFile.isDirectory() && event.kind().equals(ENTRY_CREATE) && !modifiedFile.getName().equals(".svn")) {
                    addWatcher(modifiedFile, project);
                } else {
                    VirgoToolingHook.logInfo("WebContent change detected - " + event.kind() + ": " + modifiedFile.getAbsolutePath());
                    hook.scheduleGrunt(project);
                }
            }
        } while (key.reset() && !terminated);
    }

    @Override
    public String getName() {
        return "CHES WebContent Watcher";
    }

}