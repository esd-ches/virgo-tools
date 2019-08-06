
package org.eclipse.virgo.ide.runtime.core.ches;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.virgo.ide.facet.core.FacetUtils;
import org.eclipse.virgo.ide.module.core.ServerModuleDelegate;
import org.eclipse.virgo.ide.runtime.core.ServerCorePlugin;
import org.eclipse.wst.server.core.IModule;
import org.osgi.framework.Version;
import org.w3c.dom.Document;

public class VirgoToolingHook {

    private class RefreshProjectsRunnable implements Runnable {

        private void refreshMember(IProject project, String path) throws CoreException {
            IResource member = project.findMember(path);
            if (member != null) {
                member.refreshLocal(IResource.DEPTH_INFINITE, null);
            }
        }

        private void refreshProject(IProject project) throws CoreException {
            refreshMember(project, "WebContent");
            refreshMember(project, "resources");
        }

        @Override
        public void run() {
            if (scheduledRefreshs.isEmpty()) {
                return;
            }

            for (IProject project : new HashSet<>(scheduledRefreshs)) {
                try {
                    refreshProject(project);
                    scheduledRefreshs.remove(project);
                    logInfo("Refreshed project \"" + project.getName() + "\"");
                } catch (CoreException e) {
                    // ignore and retry
                }
            }
        }

    }

    private class RunGruntRunnable implements Runnable {

        @Override
        public void run() {
            if (scheduledGrunts.isEmpty()) {
                return;
            }

            // perform css compilation only if required, #5656
            boolean cssCompilationRequired = false;
            Map<IProject, Set<String>> projectsToFiles = new HashMap<>(scheduledGrunts);
            for (Set<String> files : projectsToFiles.values()) {
                for (String file : files) {
                    if (file.endsWith(".css") || file.endsWith(".scss")) {
                        cssCompilationRequired = true;
                        break;
                    }
                }

                if (cssCompilationRequired) {
                    break;
                }
            }

            Set<IProject> projects = new HashSet<>();
            projects.addAll(scheduledGrunts.keySet());
            if (cssCompilationRequired) {
                projects.addAll(moduleToBranding.values());
            }
            for (IProject project : projects) {
                scheduledGrunts.remove(project);
            }
            runGrunt(projects, cssCompilationRequired);
        }

    }

    private static final String CONSOLE_NAME = "CHES Virgo Tooling";

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private static final String OS = System.getProperty("os.name").toLowerCase();

    private static MessageConsole findConsole() {
        ConsolePlugin plugin = ConsolePlugin.getDefault();
        IConsoleManager consoleManager = plugin.getConsoleManager();
        IConsole[] consoles = consoleManager.getConsoles();
        for (IConsole console : consoles) {
            if (CONSOLE_NAME.equals(console.getName())) {
                return (MessageConsole) console;
            }
        }

        MessageConsole chesMessageConsole = new MessageConsole(CONSOLE_NAME, null);
        consoleManager.addConsoles(new IConsole[] { chesMessageConsole });
        return chesMessageConsole;
    }

    private static String getTimeStamp() {
        return "[" + DATE_FORMAT.format(new Date()) + "]\t";
    }

    private static boolean isWindows() {
        return (OS.indexOf("win") >= 0);
    }

    public static void logError(String message) {
        ServerCorePlugin.logError(message);
        logInfo(message);
    }

    public static void logError(String message, Exception e) {
        ServerCorePlugin.logError(message, e);
        logInfo(message);
        try {
            try (PrintStream printStream = new PrintStream(findConsole().newMessageStream())) {
                e.printStackTrace(printStream);
            }
        } catch (Exception e1) {
            ServerCorePlugin.logError("An exception occurred while logging an exception to console.", e1);
        }
    }

    public static void logInfo(String message) {
        try {
            try (MessageConsoleStream messageStream = findConsole().newMessageStream()) {
                messageStream.println(getTimeStamp() + message);
            }
        } catch (Exception e) {
            ServerCorePlugin.logError("An exception occurred while logging a message to console.", e);
        }
    }

    public static void logWarning(String message) {
        ServerCorePlugin.logWarning(message);
        logInfo(message);
    }

    private WebContentWatcherRunnable webContentWatcherRunnable;

    private Thread webContentWatcherThread;

    private UpdateRunnable updateRunnable;

    private VirgoWatcherRunnable virgoWatcherRunnable;

    private Thread virgoWatcherThread;

    private Thread updateThread;

    private final Map<IModule, Set<IProject>> moduleToProjects;

    private final HashMap<IModule, IProject> moduleToBranding;

    private final HashSet<IProject> scheduledRefreshs;

    private final Map<IProject, Set<String>> scheduledGrunts;

    private ResourcesWatcherRunnable resourcesWatcherRunnable;

    private Thread resourcesWatcherThread;

    private ScheduledExecutorService refreshProjectsExecutorService;

    private ScheduledExecutorService runGruntExecutorService;

    private final ListenerList<GruntStatusListener> gruntListeners;

    /**
     * Listeners for the deployment cycle (before deploy, after deploy, before undeploy, before stop)
     */
    private final ListenerList<IDeployListener> deployListeners;

    public VirgoToolingHook() {
        moduleToProjects = new HashMap<>();
        moduleToBranding = new HashMap<>();
        scheduledRefreshs = new HashSet<>();
        scheduledGrunts = new HashMap<>();
        gruntListeners = new ListenerList<>();
        deployListeners = new ListenerList<>();

        gruntListeners.add(new GruntStatusListener());
    }

    /**
     * Adds a new deploy listener that listens to the deploy life-cycle.
     *
     * @param listener the listener to be added
     */
    public void addDeployListener(IDeployListener listener) {
        deployListeners.add(listener);
    }

    /**
     * Remove new deploy listener.
     *
     * @param listener the listener to be removed
     */
    public void removeDeployListener(IDeployListener listener) {
        deployListeners.remove(listener);
    }

    private void addProjects(IModule module, IFile planFile) {
        Set<IProject> projects = moduleToProjects.get(module);
        if (projects == null) {
            projects = new HashSet<>();
            moduleToProjects.put(module, projects);
        }

        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        ServerModuleDelegate serverModuleDelegate = new ServerModuleDelegate(module.getProject());
        Set<IModule> childModules = serverModuleDelegate.getPlanDependencies(planFile);
        for (IModule childModule : childModules) {
            if (childModule.getModuleType().getId().equals("jst.web")) {
                projects.add(workspaceRoot.getProject(childModule.getName()));
            }
            if (childModule.getName().contains("branding")) {
                moduleToBranding.put(module, workspaceRoot.getProject(childModule.getName()));
            }
        }
    }

    synchronized public void beforeDeploy(IModule module) {
        IFile planFile = getPlanFile(module);
        if (planFile == null) {
            // the deployed plan has no corresponding file --> should never happen
            return;
        }

        Set<IProject> projects = moduleToProjects.get(module);
        if (projects != null) {
            // the module has already been deployed, refresh the watched projects, #7016
            projects.clear();
            addProjects(module, planFile);
            refreshWatchers(projects);
        } else {
            // otherwise the plan is not yet deployed --> set up the tooling
            if (!FacetUtils.isPlanProject(module.getProject())) {
                logError("Only plans are supported.");
                return;
            }
            Version planVersion = getPlanVersion(planFile);
            boolean isCtmsPlan = planFile.getName().startsWith("at.ches.ctms");
            if (!isCtmsPlan && (planVersion == null || planVersion.compareTo(Version.parseVersion("5.11.0")) < 0)) {
                logError("Only plans with a version equal or higher than 5.11.0 are supported.");
                return;
            }

            addProjects(module, planFile);
            startWatching();
        }

        for (IDeployListener listener : deployListeners) {
            listener.beforeDeploy(module, projects);
        }
    }

    /**
     * Refreshes the watchers (WebContent and resources) for a given set of projects.
     *
     * @param projects
     */
    private void refreshWatchers(Set<IProject> projects) {
        if (webContentWatcherRunnable != null) {
            webContentWatcherRunnable.terminate();
        }
        if (resourcesWatcherRunnable != null) {
            resourcesWatcherRunnable.terminate();
        }

        setupWebContentAndResourceWatchers(projects);
    }

    synchronized public void beforeStop() {
        stopAll();

        for (IDeployListener listener : deployListeners) {
            listener.beforeStop();
        }
    }

    synchronized public void beforeUndeploy(IModule module) {
        if (!moduleToProjects.containsKey(module)) {
            // The module has already been undeployed or has never been deployed.
            return;
        }

        Set<IProject> projects = moduleToProjects.get(module);
        moduleToProjects.remove(module);
        moduleToBranding.remove(module);
        startWatching();

        for (IDeployListener listener : deployListeners) {
            listener.beforeUndeploy(module, projects);
        }
    }

    public void enqueueUpdate(IUpdate update) {
        updateRunnable.enqueueUpdate(update);
    }

    private Set<IProject> getAllProjects() {
        Set<IProject> allProjects = new HashSet<>();
        for (Entry<IModule, Set<IProject>> entry : moduleToProjects.entrySet()) {
            allProjects.addAll(entry.getValue());
        }
        return allProjects;
    }

    private List<String> getGruntCommands(Set<IProject> projects) throws Exception {
        String projectsString = "";
        Iterator<IProject> projectsIterator = projects.iterator();
        while (projectsIterator.hasNext()) {
            projectsString += projectsIterator.next().getName();
            if (projectsIterator.hasNext()) {
                projectsString += ",";
            }
        }

        List<String> commands = new ArrayList<>();
        if (isWindows()) {
            commands.add("cmd.exe");
            commands.add("/c");
        }

        commands.add("grunt");
        commands.add("--no-color");
        commands.add("--projects=" + projectsString);
        return commands;
    }

    private IFile getPlanFile(IModule module) {
        String fileName = module.getId();
        fileName = fileName.substring(fileName.indexOf(':') + 1);
        IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(fileName));
        if (!file.exists()) {
            return null;
        }
        return file;
    }

    private Version getPlanVersion(IFile planFile) {
        try {
            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = docBuilder.parse(planFile.getContents(true));
            String versionString = doc.getDocumentElement().getAttribute("version");
            Version version = Version.parseVersion(versionString);
            return version;
        } catch (Exception e) {
            logError("An error occurred while extracting the version from the plan file.", e);
            return null;
        }
    }

    private void logGruntInfo(IProject gruntProject, List<String> commands) {
        String commandString = "";
        Iterator<String> iterator = commands.iterator();
        while (iterator.hasNext()) {
            commandString += iterator.next();
            if (iterator.hasNext()) {
                commandString += " ";
            }
        }
        logInfo("Running grunt at " + gruntProject.getLocation() + ": " + commandString);
    }

    /**
     * Locates the files within work/deployer/s/{plan}/{x}/{y} for the given file from the workspace.
     *
     * @param file
     * @return
     */
    public List<File> lookup(File file) {
        String relativePath = file.getName();
        File workspaceOrGitParent = null;
        IPath workspacePath = ResourcesPlugin.getWorkspace().getRoot().getRawLocation();
        String workspacePathAsString = workspacePath.toFile().getAbsolutePath();
        if (file.getAbsolutePath().startsWith(workspacePathAsString)) {
            workspaceOrGitParent = workspacePath.toFile();
        } else {
            workspaceOrGitParent = getVirgoBase().toFile().getParentFile();
        }

        File parent = file.getParentFile();
        while (!parent.equals(workspaceOrGitParent) && !parent.getName().equals("resources")) {
            // do not include "resources" in the path --> resources are deployed in the root directory
            relativePath = parent.getName() + File.separator + relativePath;
            parent = parent.getParentFile();
        }

        String project = parent.getParentFile().getName() + ".war";

        IPath virgoBase = getVirgoBase();
        IPath deployerFolder = virgoBase.append("work").append("deployer").append("s");
        if (!deployerFolder.toFile().exists()) {
            return Collections.emptyList(); // in case the virgo is not running, the work folder may be missing
        }

        List<File> matches = new ArrayList<>();
        for (File plan : deployerFolder.toFile().listFiles()) {
            if (!plan.isDirectory() || !plan.getName().startsWith("at.ches")) {
                continue;
            }

            for (File firstDepth : plan.listFiles()) {
                for (File secondDepth : firstDepth.listFiles()) {
                    if (!secondDepth.isDirectory()) {
                        continue;
                    }

                    for (File warDirectory : secondDepth.listFiles()) {
                        if (warDirectory.getName().equals(project)) {
                            matches.add(new File(warDirectory, relativePath));
                        }
                    }
                }
            }
        }

        return matches;
    }

    public static IPath getVirgoBase() {
        IProject virgoProject = ResourcesPlugin.getWorkspace().getRoot().getProject("virgo");
        return virgoProject.getRawLocation();
    }

    /**
     * Tries to find the project containing the grunt files.
     *
     * @return
     */
    private IProject getGruntProject() {
        String[] gruntLocations = new String[] { "at.ches.pro.web.grunt", "at.ches.ctms.grunt" };
        for (String potentialGruntLocation : gruntLocations) {
            IProject gruntProject = ResourcesPlugin.getWorkspace().getRoot().getProject(potentialGruntLocation);
            if (gruntProject.exists()) {
                return gruntProject;
            }

        }

        logWarning("Grunt can't be executed - could not find grunt in one of the following locations: " + StringUtils.join(gruntLocations, ", "));
        return null;
    }

    /**
     * Runs grunt for a set of projects.
     *
     * @param projects the projects for which grunt should be run
     * @param cssCompilationRequired if <code>false</code>, css compilation will be skipped
     */
    private void runGrunt(Set<IProject> projects, boolean cssCompilationRequired) {
        IProject gruntProject = getGruntProject();
        if (gruntProject == null) {
            return;
        }

        for (Object listener : gruntListeners.getListeners()) {
            ((IGruntListener) listener).beforeRun(projects);
        }

        try {
            List<String> commands = getGruntCommands(projects);
            if (!cssCompilationRequired) {
                commands.add("--skip-css-compilation");
            }

            logGruntInfo(gruntProject, commands);
            ProcessBuilder processBuilder = new ProcessBuilder(commands);
            processBuilder.directory(gruntProject.getLocation().toFile());
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            while (line != null) {
                logInfo("Grunt: " + line);
                line = reader.readLine();
            }
            process.waitFor();
        } catch (IOException e) {
            logError("Grunt couldn't be executed. Please check if grunt-cli is installed globally. If not, please run \"npm install -g grunt-cli\".",
                e);
        } catch (InterruptedException e) {
            logError("Grunt was interrupted.", e);
        } catch (Exception e) {
            logError("An exception occurred while executing grunt.", e);
        } finally {
            for (Object listener : gruntListeners.getListeners()) {
                ((IGruntListener) listener).afterRun(projects);
            }
        }
    }

    /**
     * Schedules a grunt run.
     *
     * @param project the project for which grunt should be run
     * @param filename the name of the file that triggered the run
     */
    public void scheduleGrunt(IProject project, String filename) {
        Set<String> filesForProject = scheduledGrunts.get(project);
        if (filesForProject == null) {
            filesForProject = new HashSet<>();
            scheduledGrunts.put(project, filesForProject);
        }

        filesForProject.add(filename);
    }

    public void scheduleRefresh(IProject project) {
        scheduledRefreshs.add(project);
    }

    public void scheduleRefresh(Set<IProject> projects) {
        scheduledRefreshs.addAll(projects);
    }

    private void startWatching() {
        final Set<IProject> projects = getAllProjects();

        if (projects.size() > 0) {
            // Stop watching webcontent and resources
            stopFileWatcher();
            // Run grunt to move files from webcontent to resources
            runGrunt(projects, true);
            // Refresh webcontent and resources folders. Unfortunately, the refresh must be run within an extra thread
            // as the operation can be blocking.
            scheduleRefresh(projects);
            // Start watching webcontent and resources again.
            startWatching(projects);
        } else {
            stopAll();
        }
    }

    synchronized private void startWatching(Set<IProject> projects) {
        try {
            if (refreshProjectsExecutorService == null || refreshProjectsExecutorService.isTerminated()
                || refreshProjectsExecutorService.isShutdown()) {
                refreshProjectsExecutorService = Executors.newScheduledThreadPool(1);
                refreshProjectsExecutorService.scheduleWithFixedDelay(new RefreshProjectsRunnable(), 0, 1, TimeUnit.SECONDS);
            }

            if (runGruntExecutorService == null || runGruntExecutorService.isTerminated() || runGruntExecutorService.isShutdown()) {
                runGruntExecutorService = Executors.newScheduledThreadPool(1);
                runGruntExecutorService.scheduleWithFixedDelay(new RunGruntRunnable(), 0, 1, TimeUnit.SECONDS);
            }

            if (updateThread == null || !updateThread.isAlive()) {
                updateRunnable = new UpdateRunnable();
                updateThread = new Thread(updateRunnable, updateRunnable.getName());
                updateThread.start();
            }

            setupWebContentAndResourceWatchers(projects);

            if (virgoWatcherThread == null || !virgoWatcherThread.isAlive()) {
                virgoWatcherRunnable = new VirgoWatcherRunnable(this);
                virgoWatcherThread = new Thread(virgoWatcherRunnable, virgoWatcherRunnable.getName());
                virgoWatcherThread.start();
            }
        } catch (Exception e) {
            logError("Couldn't start the file watcher.", e);
        }
    }

    /**
     * Sets up watchers that watch for changes in the WebContent and resources folders.
     *
     * @param projects the projects to be watched
     */
    private void setupWebContentAndResourceWatchers(Set<IProject> projects) {
        if (webContentWatcherRunnable == null || webContentWatcherRunnable.isTerminated()) {
            webContentWatcherRunnable = new WebContentWatcherRunnable(this, projects);
            webContentWatcherThread = new Thread(webContentWatcherRunnable, webContentWatcherRunnable.getName());
            webContentWatcherThread.start();
        }

        if (resourcesWatcherRunnable == null || resourcesWatcherRunnable.isTerminated()) {
            resourcesWatcherRunnable = new ResourcesWatcherRunnable(this, projects);
            resourcesWatcherThread = new Thread(resourcesWatcherRunnable, resourcesWatcherRunnable.getName());
            resourcesWatcherThread.start();
        }
    }

    synchronized private void stopAll() {
        try {
            moduleToProjects.clear();
            moduleToBranding.clear();
            stopFileWatcher();
            terminateRunnable(virgoWatcherRunnable, virgoWatcherThread);
            terminateRunnable(updateRunnable, updateThread);
            terminateExecutorService(refreshProjectsExecutorService);
            terminateExecutorService(runGruntExecutorService);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    synchronized private void stopFileWatcher() {
        try {
            terminateRunnable(webContentWatcherRunnable, webContentWatcherThread);
            terminateRunnable(resourcesWatcherRunnable, resourcesWatcherThread);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    private void terminateExecutorService(ExecutorService executorService) throws InterruptedException {
        if (executorService != null) {
            executorService.shutdown();
            executorService.awaitTermination(1, TimeUnit.MINUTES);
        }
    }

    private void terminateRunnable(AbstractRunnable runnable, Thread thread) throws InterruptedException {
        if (runnable != null) {
            runnable.terminate();
            thread.join();
            runnable = null;
            thread = null;
        }
    }

    public void virgoPublishFinished() {
        logInfo("Detected a publishing event of virgo - replaying updates.");
        updateRunnable.replayUpdates();
    }

    /**
     * Copy all content which was probably not deployed as the workspace was not refreshed yet, see #5231
     *
     * @param module
     */
    public void afterDeploy(IModule module) {
        Set<IProject> projects = moduleToProjects.get(module);
        if (projects == null) {
            return;
        }

        for (IProject project : projects) {
            if (!project.getName().endsWith("branding")) {
                continue;
            }

            try {
                IFolder resourcesFolder = project.getFolder("resources");
                if (!resourcesFolder.exists()) {
                    continue;
                }

                copyCssContent(resourcesFolder);
            } catch (Exception e) {
                logError("Unable to copy css content", e);
            }
        }

        for (IDeployListener listener : deployListeners) {
            listener.afterDeploy(module, projects);
        }
    }

    /**
     * Recursively copies all css content.
     *
     * @param project
     * @throws CoreException
     * @throws IOException
     */
    private void copyCssContent(IFolder project) throws CoreException, IOException {
        File resourcesFolder = project.getLocation().toFile();
        copyCssContent(resourcesFolder);
    }

    /**
     * Recursively copies all css content.
     *
     * @param root
     * @throws CoreException
     * @throws IOException
     */
    private void copyCssContent(File root) throws CoreException, IOException {
        for (File child : root.listFiles()) {
            if (child.isDirectory()) {
                copyCssContent(child);
            } else if (child.getName().endsWith(".css") || root.getName().equals("css")) {
                List<File> targets = lookup(child);
                for (File target : targets) {
                    if (!child.exists() || !target.getParentFile().exists()) {
                        continue; // make sure the resource or target have not been deleted meanwhile, #6902
                    }

                    Files.copy(child.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    logInfo("Copying " + child.getName() + " --> " + target.getAbsolutePath());
                }
            }
        }
    }
}
