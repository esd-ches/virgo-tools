
package org.eclipse.virgo.ide.runtime.core.ches;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchKey;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;

public abstract class AbstractFileWatcherRunnable extends AbstractWatchServiceRunnable {

    private final Set<IProject> projects;

    private final String baseFolderName;

    protected final Map<WatchKey, IProject> keyToProject;

    protected final Map<WatchKey, File> keyToFile;

    public AbstractFileWatcherRunnable(Set<IProject> projects, String baseFolderName) {
        this.projects = projects;
        this.baseFolderName = baseFolderName;
        keyToProject = new HashMap<>();
        keyToFile = new HashMap<>();
    }

    protected void setupWatchers() throws IOException {
        for (IProject project : projects) {
            File webContentFolder = new File(project.getLocation().toFile(), baseFolderName);
            if (webContentFolder.exists()) {
                addWatcher(webContentFolder, project);
                setupWatchers(webContentFolder, project);
            }
        }

        VirgoToolingHook.logInfo("Started watching " + baseFolderName + " folder of: " + getProjectsString());
    }

    private String getProjectsString() {
        String string = "";
        Iterator<IProject> iterator = projects.iterator();

        while (iterator.hasNext()) {
            string += iterator.next().getName();

            if (iterator.hasNext()) {
                string += ", ";
            }
        }

        return string;
    }

    protected void addWatcher(File toScan, IProject project) throws IOException {
        Path path = Paths.get(toScan.toURI());
        WatchKey key = path.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        keyToProject.put(key, project);
        keyToFile.put(key, toScan);
    }

    private void setupWatchers(File localRoot, IProject project) throws IOException {
        for (File toScan : localRoot.listFiles()) {
            if (toScan.isFile() || toScan.getName().equals(".svn")) {
                continue;
            }

            addWatcher(toScan, project);
            setupWatchers(toScan, project);
        }
    }

    @Override
    public void terminate() {
        super.terminate();
        VirgoToolingHook.logInfo("Stopped watching " + baseFolderName + " folder of: " + getProjectsString());
    }

}
