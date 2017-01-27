/*******************************************************************************
 *  Copyright (c) 2015 GianMaria Romanato
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     GianMaria Romanato - initial API and implementation
 *******************************************************************************/

package org.eclipse.virgo.ide.runtime.internal.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.pde.core.target.ITargetDefinition;
import org.eclipse.pde.core.target.ITargetHandle;
import org.eclipse.pde.core.target.ITargetLocation;
import org.eclipse.pde.core.target.ITargetPlatformService;
import org.eclipse.pde.core.target.LoadTargetDefinitionJob;
import org.eclipse.ui.PlatformUI;
import org.eclipse.virgo.ide.runtime.core.IServerRuntimeWorkingCopy;
import org.eclipse.virgo.kernel.osgi.provisioning.tools.NoOpEventLogger;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.repository.ArtifactBridge;
import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.repository.ArtifactGenerationException;
import org.eclipse.virgo.repository.builder.ArtifactDescriptorBuilder;
import org.eclipse.virgo.repository.configuration.ExternalStorageRepositoryConfiguration;
import org.eclipse.virgo.repository.configuration.ManagedStorageRepositoryConfiguration;
import org.eclipse.virgo.repository.configuration.PropertiesRepositoryConfigurationReader;
import org.eclipse.virgo.repository.configuration.RepositoryConfiguration;
import org.eclipse.virgo.repository.configuration.WatchedStorageRepositoryConfiguration;
import org.eclipse.virgo.util.math.OrderedPair;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;

/**
 * Utilities used for integrating Virgo Tools and PDE Tools.
 *
 */
public final class PDEHelper {

    private static final String DOT = "."; //$NON-NLS-1$

    private static final String PLUGINS = "plugins"; //$NON-NLS-1$

    private static final String CONFIG_FILENAME = "org.eclipse.virgo.repository.properties"; //$NON-NLS-1$

    private static final String CONFIGURATION = "configuration"; //$NON-NLS-1$

    private static final String ANY_JAR = "*.jar"; //$NON-NLS-1$

    private static final String STAR = "*"; //$NON-NLS-1$

    private static final String WATCH_DIRECTORY = ".watchDirectory"; //$NON-NLS-1$

    private static final String TYPE_WATCHED = "watched"; //$NON-NLS-1$

    private static final String REPO_TYPE = ".type"; //$NON-NLS-1$

    private static final String ADDED_BY_VIRGO_TOOLS = "addedByVirgoTools"; //$NON-NLS-1$

    private static final String COMMA = ","; //$NON-NLS-1$

    private static final String CHAIN_PROPERTY = "chain"; //$NON-NLS-1$

    private PDEHelper() {
    }

    /**
     * Parses the 'org.eclipse.virgo.repository.properties' configuration file and returns the list of folders that must
     * be used as the PDE target platform definition. Note that the returned list also includes the $VIRGO_HOME/plugins
     * folder that, while not listed in the repository configuration file, contains bundles that the container makes
     * available to deployed applications.
     *
     * @param runtime the runtime working copy
     * @return the list of folders for the target definition
     */
    public static List<File> getFoldersForTargetDefinition(IRuntimeWorkingCopy runtime) {
        Assert.isNotNull(runtime, "runtime cannot be null"); //$NON-NLS-1$
        List<File> locations = new ArrayList<File>();
        if (runtime.getLocation() != null) {
            locations.add(runtime.getLocation().append(PLUGINS).toFile());
            locations.addAll(getRepositoryChain(runtime).values());
        }
        return locations;
    }

    /**
     * Returns the list of folders contained in the repository configuration file and their name in the repository
     * chain.
     *
     * @param runtime the runtime working copy
     * @return the map from repository name to folder directory
     */
    private static LinkedHashMap<String, File> getRepositoryChain(IRuntimeWorkingCopy runtime) {
        Assert.isNotNull(runtime, "runtime cannot be null"); //$NON-NLS-1$
        LinkedHashMap<String, File> locations = new LinkedHashMap<String, File>();
        File rootDirectory = runtime.getLocation().toFile();
        Set<ArtifactBridge> artifactBridges = createArtifactBridges();
        EventLogger logger = new NoOpEventLogger();
        File indexDirectory = ServerUiPlugin.getDefault().getStateLocation().append(runtime.getName()).toFile();
        indexDirectory.mkdirs();
        PropertiesRepositoryConfigurationReader reader = new PropertiesRepositoryConfigurationReader(indexDirectory, artifactBridges, logger, null,
            rootDirectory);
        File configurationFile = getRepositoryFile(runtime);
        FileInputStream inStream = null;
        try {
            inStream = new FileInputStream(configurationFile);
            Properties p = new Properties();
            p.load(inStream);
            OrderedPair<Map<String, RepositoryConfiguration>, List<String>> pair = reader.readConfiguration(p);
            for (RepositoryConfiguration cfg : pair.getFirst().values()) {
                if (cfg instanceof WatchedStorageRepositoryConfiguration) {
                    WatchedStorageRepositoryConfiguration c1 = (WatchedStorageRepositoryConfiguration) cfg;
                    locations.put(c1.getName(), c1.getDirectoryToWatch());
                } else if (cfg instanceof ManagedStorageRepositoryConfiguration) {
                    ManagedStorageRepositoryConfiguration m1 = (ManagedStorageRepositoryConfiguration) cfg;
                    locations.put(m1.getName(), m1.getStorageLocation());
                } else if (cfg instanceof ExternalStorageRepositoryConfiguration) {
                    ExternalStorageRepositoryConfiguration e1 = (ExternalStorageRepositoryConfiguration) cfg;
                    String patt = e1.getSearchPattern();
                    IPath path = new Path(patt);
                    String lastSegment = path.lastSegment();
                    if (STAR.equals(lastSegment) || ANY_JAR.equals(lastSegment)) {
                        path = path.removeLastSegments(1);
                        if (!path.toOSString().contains(STAR)) {
                            locations.put(e1.getName(), path.toFile());
                        }
                    }
                }
            }
        } catch (Exception e) {
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                }
            }
        }

        return locations;
    }

    private static File getRepositoryFile(IRuntimeWorkingCopy runtime) {
        return runtime.getLocation().append(CONFIGURATION).append(CONFIG_FILENAME).toFile();
    }

    private static Set<ArtifactBridge> createArtifactBridges() {
        Set<ArtifactBridge> artefactBridges = new HashSet<ArtifactBridge>();
        artefactBridges.add(new ArtifactBridge() {

            public ArtifactDescriptor generateArtifactDescriptor(File arg0) throws ArtifactGenerationException {
                return new ArtifactDescriptorBuilder().setUri(arg0.toURI()).setName(arg0.getName()).build();
            }

        });
        return artefactBridges;
    }

    /**
     * Tells whether a PDE Target Definition with the given name exists or not.
     *
     * @param name the target definition name (cannot be null)
     * @return true if a target definition is found, false otherwise
     */
    public static boolean existsTargetDefinition(String name) {
        Assert.isNotNull(name, "name cannot be null"); //$NON-NLS-1$
        ITargetPlatformService srv = PlatformUI.getWorkbench().getService(ITargetPlatformService.class);
        ITargetHandle[] targetplatforms = srv.getTargets(null);
        for (ITargetHandle iTargetHandle : targetplatforms) {
            try {
                if (iTargetHandle.getTargetDefinition().getName().equals(name)) {
                    return true;
                }
            } catch (CoreException e) {
            }
        }
        return false;
    }

    /**
     * Deletes the target definition(s) with the given name.
     *
     * @param name the name (cannot be null)
     */
    public static void deleteTargetDefinition(String name) {
        Assert.isNotNull(name, "name cannot be null"); //$NON-NLS-1$
        ITargetPlatformService srv = PlatformUI.getWorkbench().getService(ITargetPlatformService.class);
        ITargetHandle[] targetplatforms = srv.getTargets(null);
        for (ITargetHandle iTargetHandle : targetplatforms) {
            try {
                if (iTargetHandle.getTargetDefinition().getName().equals(name)) {
                    srv.deleteTarget(iTargetHandle);
                }
            } catch (CoreException e) {
            }
        }
    }

    /**
     * Creates a target platform for the given runtime using the given folders.
     *
     * @param monitor a monitor for progress reporting
     * @param runtime the runtime working copy
     * @param folders the target platform folders
     * @return a status indicating the operation result
     */
    public static Status createTargetDefinition(IProgressMonitor monitor, IRuntimeWorkingCopy runtime, List<File> folders) {
        Assert.isNotNull(runtime, "runtime cannot be null"); //$NON-NLS-1$
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        monitor.beginTask("", 2); //$NON-NLS-1$
        MultiStatus st = new MultiStatus(ServerUiPlugin.PLUGIN_ID, 1, "Error creating target platform", null); //$NON-NLS-1$
        if (folders != null) {
            IServerRuntimeWorkingCopy serverRuntimeWorkingCopy = (IServerRuntimeWorkingCopy) runtime.loadAdapter(IServerRuntimeWorkingCopy.class,
                null);
            ITargetPlatformService srv = PlatformUI.getWorkbench().getService(ITargetPlatformService.class);
            ITargetHandle[] handles = srv.getTargets(null);
            for (ITargetHandle iTargetHandle : handles) {
                try {
                    if (iTargetHandle.getTargetDefinition().getName().equals(runtime.getName())) {
                        srv.deleteTarget(iTargetHandle);
                    }
                } catch (CoreException e) {
                    // ignore, in the worst case another target platform is created with the same name and set active
                }
            }
            ITargetDefinition targetplatform = srv.newTarget();
            targetplatform.setName(runtime.getName());
            IVMInstall vmInstall = serverRuntimeWorkingCopy.getVMInstall();
            if (vmInstall == null) {
                vmInstall = JavaRuntime.getDefaultVMInstall();
            }
            if (vmInstall == null) {
                return new Status(IStatus.ERROR, ServerUiPlugin.PLUGIN_ID, "Cannot determine JDK, please check Java JRE preferences.");
            }
            targetplatform.setJREContainer(JavaRuntime.newJREContainerPath(vmInstall));
            ITargetLocation[] locations = new ITargetLocation[folders.size()];
            int i = 0;

            for (File folder : folders) {
                try {
                    locations[i++] = srv.newDirectoryLocation(folder.getCanonicalPath());
                } catch (IOException e) {
                    st.add(new Status(IStatus.ERROR, ServerUiPlugin.PLUGIN_ID, e.getLocalizedMessage(), e));
                }
            }
            if (st.isOK()) {
                monitor.worked(1);
                targetplatform.setTargetLocations(locations);
                try {
                    srv.saveTargetDefinition(targetplatform);
                    LoadTargetDefinitionJob.load(targetplatform);
                } catch (CoreException e1) {
                    st.add(new Status(IStatus.ERROR, ServerUiPlugin.PLUGIN_ID, "Cannot save target platform definition", e1));
                }
            }
        }
        monitor.done();
        return st;
    }

    /**
     * Updates the Virgo repository configuration to match the specified list of folders.
     *
     * @param runtime the runtime working copy
     * @param newFolders the new folders
     * @throws IOException if the file modification fails for some reason
     */
    public static void updateRepositoryConfiguration(IRuntimeWorkingCopy runtime, List<File> newFolders) throws IOException {
        Assert.isNotNull(runtime, "runtime cannot be null"); //$NON-NLS-1$
        if (runtime.getLocation() != null) {
            LinkedHashMap<String, File> oldFolders = getRepositoryChain(runtime);

            HashSet<File> oldFoldersSet = new HashSet<File>(oldFolders.values());
            HashSet<File> newFoldersSet = newFolders != null ? new HashSet<File>(newFolders) : new HashSet<File>();

            newFoldersSet.remove(runtime.getLocation().append(PLUGINS).toFile());

            if (oldFoldersSet.equals(newFoldersSet)) {
                return;
            }

            LinkedHashMap<String, File> removed = new LinkedHashMap<String, File>(oldFolders);
            for (Iterator<Entry<String, File>> eIt = removed.entrySet().iterator(); eIt.hasNext();) {
                Entry<String, File> e = eIt.next();
                if (newFoldersSet.remove(e.getValue())) {
                    eIt.remove(); // remove those that still exist
                }
            }

            Properties added = new Properties();
            int count = 0;

            IPath virgoHome = runtime.getLocation();

            for (File file : newFoldersSet) {
                IPath path = new Path(file.getCanonicalPath());
                if (virgoHome.isPrefixOf(path)) {
                    path = path.removeFirstSegments(virgoHome.segmentCount());
                }
                added.put(ADDED_BY_VIRGO_TOOLS + count + REPO_TYPE, TYPE_WATCHED);
                added.put(ADDED_BY_VIRGO_TOOLS + count + WATCH_DIRECTORY, path.toString());
                count++;
            }

            File configurationFile = getRepositoryFile(runtime);

            Properties oldProperties = readRepositoryProperties(configurationFile);

            for (Iterator<Entry<Object, Object>> eIt = oldProperties.entrySet().iterator(); eIt.hasNext();) {
                String entryKey = eIt.next().getKey().toString();
                for (String removedRepo : removed.keySet()) {
                    if (entryKey.startsWith(removedRepo + DOT)) {
                        eIt.remove();
                        break;
                    }
                }
            }

            oldProperties.putAll(added);

            String chain = (String) oldProperties.get(CHAIN_PROPERTY);
            StringBuilder sb = new StringBuilder();
            if (chain != null) {
                String[] chainArray = chain.split(COMMA);
                for (String string : chainArray) {
                    if (!removed.containsKey(string)) {
                        sb.append(string).append(COMMA);
                    }
                }
            }

            for (int i = 0; i < count; i++) {
                sb.append(ADDED_BY_VIRGO_TOOLS + i).append(COMMA);
            }

            if (sb.length() > 0) {
                sb.setLength(sb.length() - 1);
            }

            chain = sb.toString();

            oldProperties.put(CHAIN_PROPERTY, chain);

            writeRepositoryProperties(configurationFile, oldProperties);
        }

    }

    private static void writeRepositoryProperties(File configurationFile, Properties properties) throws IOException {
        File newFile = new File(configurationFile.getCanonicalPath());
        File backupFile = new File(configurationFile.getCanonicalPath() + ".backup_" + System.currentTimeMillis()); //$NON-NLS-1$
        if (backupFile.exists()) {
            backupFile.delete();
        }
        configurationFile.renameTo(backupFile);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(newFile);
            properties.store(fos, "Edited by Virgo Tools"); //$NON-NLS-1$
            fos.flush();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private static Properties readRepositoryProperties(File configurationFile) throws IOException {
        FileInputStream fis = null;
        Properties properties = new Properties();
        try {
            fis = new FileInputStream(configurationFile);
            properties.load(fis);
            return properties;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * Refreshes the target definition identified by the given name or does nothing if such a target platform does not
     * exist.
     *
     * @param monitor a monitor for progress reporting
     * @param name the target definition name;
     * @return true if a target platform was found and refreshed.
     * @throws CoreException
     */
    public static boolean refreshTargetDefinition(IProgressMonitor monitor, String name) throws CoreException {
        Assert.isNotNull(name, "name cannot be null"); //$NON-NLS-1$
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        ITargetPlatformService srv = PlatformUI.getWorkbench().getService(ITargetPlatformService.class);
        ITargetDefinition def = srv.getWorkspaceTargetDefinition();
        if (!def.getName().equals(name)) {
            def = null;
            ITargetHandle[] handles = srv.getTargets(null);
            for (ITargetHandle iTargetHandle : handles) {
                ITargetDefinition def2 = iTargetHandle.getTargetDefinition();
                if (def2.getName().equals(name)) {
                    def = def2;
                    break;
                }
            }
        }

        if (def != null) {
            srv.saveTargetDefinition(def);
            def.resolve(monitor);
            LoadTargetDefinitionJob.load(def);
            return true;

        }
        return false;
    }
}
