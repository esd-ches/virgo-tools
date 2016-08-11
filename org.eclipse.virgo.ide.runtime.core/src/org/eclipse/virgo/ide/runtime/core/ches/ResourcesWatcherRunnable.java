
package org.eclipse.virgo.ide.runtime.core.ches;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.resources.IProject;

public class ResourcesWatcherRunnable extends AbstractFileWatcherRunnable {

    private final VirgoToolingHook hook;

    public ResourcesWatcherRunnable(VirgoToolingHook hook, Set<IProject> projects) {
        super(projects, "resources");
        this.hook = hook;
    }

    @Override
    protected void runInternal() throws Exception {
        setupWatchers();

        WatchKey key;
        do {
            key = watchService.poll(1, TimeUnit.SECONDS);
            if (key == null) {
                continue;
            }

            IProject project = keyToProject.get(key);
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
                    DirectoryUpdate update = new DirectoryUpdate(hook, modifiedFile);
                    hook.enqueueUpdate(update);
                } else {
                    if (!modifiedFile.exists() || modifiedFile.getName().endsWith("bak___") || modifiedFile.getName().endsWith("jb_old___")) {
                        continue; // ignore deletion and backup files
                    }

                    VirgoToolingHook.logInfo("Resources change detected - " + event.kind() + ": " + modifiedFile.getAbsolutePath());

                    FileUpdate update = new FileUpdate(hook, modifiedFile);
                    hook.enqueueUpdate(update);
                }
            }
        } while ((key == null || key.reset()) && !terminated);
    }

    @Override
    public String getName() {
        return "CHES Resources Watcher";
    }

}