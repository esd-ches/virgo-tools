/*******************************************************************************
 * Copyright (c) 2012 Eclipse Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial implementation
 *******************************************************************************/
package org.eclipse.virgo.ide.runtime.internal.core;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.virgo.ide.runtime.core.IServerBehaviour;
import org.eclipse.virgo.ide.runtime.core.IServerVersionHandler;
import org.eclipse.virgo.ide.runtime.core.ServerCorePlugin;
import org.eclipse.virgo.ide.runtime.core.ServerUtils;
import org.eclipse.wst.server.core.IRuntime;


/**
 * {@link IServerVersionHandler} for Virgo Server 3.5.0 and above.
 * @author Borislav Kapukaranov
 */
public class ServerVirgo35Handler extends ServerVirgoHandler implements IServerVersionHandler {

    /**
     * {@inheritDoc}
     */
    public String getRuntimeClass() {
        return "org.eclipse.equinox.launcher.Main";
    }
    
    /**
     * {@inheritDoc}
     */
    public String getProfilePath(IRuntime runtime) {
        return runtime.getLocation().append("configuration").append("java6-server.profile").toString();
    }
    
    /**
     * {@inheritDoc}
     */
    public List<IRuntimeClasspathEntry> getRuntimeClasspath(IPath installPath) {
        List<IRuntimeClasspathEntry> cp = new ArrayList<IRuntimeClasspathEntry>();

        IPath binPath = installPath.append("lib");
        if (binPath.toFile().exists()) {
            File libFolder = binPath.toFile();
            for (File library : libFolder.listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    return pathname.isFile() && pathname.toString().endsWith(".jar");
                }
            })) {
                IPath path = binPath.append(library.getName());
                cp.add(JavaRuntime.newArchiveRuntimeClasspathEntry(path));
            }
        }
        
        IPath pluginsPath = installPath.append("plugins");
        if (pluginsPath.toFile().exists()) {
            File pluginsFolder = pluginsPath.toFile();
            for (File library : pluginsFolder.listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    return pathname.isFile() && pathname.toString().endsWith(".jar") && pathname.toString().contains("org.eclipse.osgi_") && pathname.toString().contains("org.eclipse.equinox.console.supportability_");
                }
            })) {
                IPath path = pluginsPath.append(library.getName());
                cp.add(JavaRuntime.newArchiveRuntimeClasspathEntry(path));
            }
        }

        return cp;
    }

    /**
     * {@inheritDoc}
     */
    public String[] getRuntimeProgramArguments(IServerBehaviour behaviour) {
        List<String> list = new ArrayList<String>();
        list.add("-noExit");
        return list.toArray(new String[list.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public String[] getRuntimeVMArguments(IServerBehaviour behaviour, IPath installPath, IPath configPath,
            IPath deployPath) {
        String serverHome = ServerUtils.getServer(behaviour).getRuntimeBaseDirectory().toOSString();
        List<String> list = new ArrayList<String>();
        list.add("-XX:+HeapDumpOnOutOfMemoryError");
        list.add("-XX:ErrorFile=\"" + serverHome + "/serviceability/error.log\"");
        list.add("-XX:HeapDumpPath=\"" + serverHome + "/serviceability/heap_dump.hprof\"");
        list.add("-Djava.rmi.server.hostname=127.0.0.1");
        list.add("-Dorg.eclipse.virgo.kernel.home=\"" + serverHome + "\"");
        list.add("-Djava.io.tmpdir=\"" + serverHome + "/work/tmp/\"");
        list.add("-Dcom.sun.management.jmxremote");
        list.add("-Dcom.sun.management.jmxremote.port=" + ServerUtils.getServer(behaviour).getMBeanServerPort());
        list.add("-Dcom.sun.management.jmxremote.authenticate=false");
        list.add("-Dcom.sun.management.jmxremote.ssl=false");
        list.add("-Dorg.eclipse.virgo.kernel.authentication.file=\"" + serverHome
                + "/config/org.eclipse.virgo.kernel.users.properties\"");
        list.add("-Djava.security.auth.login.config=\"" + serverHome
                + "/config/org.eclipse.virgo.kernel.authentication.config\"");
        
        list.add("-Dorg.eclipse.virgo.kernel.config="+ serverHome + "/configuration");
        list.add("-Dosgi.java.profile=file:" + serverHome + "/configuration/java6-server.profile");
        list.add("-Declipse.ignoreApp=true");
        list.add("-Dosgi.install.area=" + serverHome);
        list.add("-Dosgi.configuration.area=" + serverHome + "/work");
        list.add("-Dssh.server.keystore=" + serverHome + "/configuration/hostkey.ser");
        
        String fwClassPath = createFWClassPath(serverHome);
        
        list.add("-Dosgi.frameworkClassPath=" + fwClassPath);
        
        
        return list.toArray(new String[list.size()]);
    }

    private String createFWClassPath(String serverHome) {
        StringBuilder fwClassPath = new StringBuilder();
        File libDir = new File(serverHome + "/lib");
        if (libDir.exists()) {
            for (File file : libDir.listFiles()) {
                if (file.getName().endsWith(".jar")) {
                    fwClassPath.append("file:" + file.getAbsolutePath() + ",");
                }
            }
        }
        File plugins = new File(serverHome + "/plugins");
        if (plugins.exists()) {
            for (File file : plugins.listFiles()) {
                if (file.getName().contains("org.eclipse.osgi_")) {
                    fwClassPath.append("file:" + file.getAbsolutePath() + ",");
                }
                if (file.getName().contains("org.eclipse.equinox.console.supportability_")) {
                    fwClassPath.append("file:" + file.getAbsolutePath() + ",");
                }
            }
        }
        fwClassPath.deleteCharAt(fwClassPath.length()-1);
        return fwClassPath.toString();
    }
    
    /**
     * {@inheritDoc}
     */
    public IStatus verifyInstallation(IPath installPath) {
        String version = installPath.append("lib").append(".version").toOSString();
        File versionFile = new File(version);
        if (versionFile.exists()) {
            InputStream is = null;
            try {
                is = new FileInputStream(versionFile);
                Properties versionProperties = new Properties();
                versionProperties.load(is);
                String versionString = versionProperties.getProperty("virgo.server.version");

                if (versionString == null) {
                    return new Status(
                            Status.ERROR,
                            ServerCorePlugin.PLUGIN_ID,
                            ".version file in lib directory is missing key 'virgo.server.version'. Make sure to point to a Virgo Server installation.");
                }
            }
            catch (FileNotFoundException e) {
            }
            catch (IOException e) {
            }
            finally {
                if (is != null) {
                    try {
                        is.close();
                    }
                    catch (IOException e) {
                    }
                }
            }
        }
        else {
            return new Status(Status.ERROR, ServerCorePlugin.PLUGIN_ID,
                    ".version file in lib directory is missing. Make sure to point to a Virgo Server installation.");
        }
        return Status.OK_STATUS;
    }

}
