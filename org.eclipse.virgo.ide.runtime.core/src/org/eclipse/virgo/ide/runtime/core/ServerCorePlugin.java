/*******************************************************************************
 * Copyright (c) 2009, 2010 SpringSource, a divison of VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource, a division of VMware, Inc. - initial API and implementation
 *******************************************************************************/

package org.eclipse.virgo.ide.runtime.core;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.virgo.ide.runtime.core.artefacts.ArtefactRepositoryManager;
import org.osgi.framework.BundleContext;

/**
 * Bundle Activator for the server.core plugin.
 *
 * @author Christian Dupuis
 * @since 1.0.0
 */
public class ServerCorePlugin extends AbstractUIPlugin {

    /** The bundle symbolic name */
    public static final String PLUGIN_ID = "org.eclipse.virgo.ide.runtime.core";

    public static final String PREF_LOAD_CLASSES_KEY = PLUGIN_ID + ".load.classes.from.index";

    /** The shared bundle instance */
    private static ServerCorePlugin plugin;

    /** Internal artefact repository manager */
    private ArtefactRepositoryManager artefactRepositoryManager;

    public static ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    private BundleContext context;

    public static final String VIRGO_SERVER_ID = "org.eclipse.virgo.server.virgo";

    public static final String VIRGO_SERVER_PROJECT_NATURE = "org.eclipse.virgo.ide.runtime.managedProject";

    @Override
    public void start(BundleContext context) throws Exception {
        this.context = context;
        super.start(context);
        plugin = this;
        ServerUtils.clearCacheDirectory();

        plugin.getPreferenceStore().setDefault(PREF_LOAD_CLASSES_KEY, false);

        this.artefactRepositoryManager = new ArtefactRepositoryManager();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        ServerUtils.clearCacheDirectory();
        this.artefactRepositoryManager.stop();
        this.artefactRepositoryManager = null;
        plugin = null;
        super.stop(context);
    }

    public static ServerCorePlugin getDefault() {
        return plugin;
    }

    public static String getPreference(String id) {
        return getDefault().getPluginPreferences().getString(id);
    }

    public static void setPreference(String id, String value) {
        getDefault().getPluginPreferences().setValue(id, value);
        getDefault().savePluginPreferences();
    }

    public static ArtefactRepositoryManager getArtefactRepositoryManager() {
        return getDefault().artefactRepositoryManager;
    }

    public URI getBundleUri(String bundleName) {
        URL url;
        try {
            url = FileLocator.toFileURL(this.context.getBundle().getEntry(bundleName));
            if (url != null) {
                URI uri = new URI("file", url.toString().substring(5), null);
                return uri;
            }
        } catch (IOException e) {
            StatusManager.getManager().handle(new Status(IStatus.ERROR, PLUGIN_ID, "Problem occurred while getting bundle:" + bundleName, e));
        } catch (URISyntaxException e) {
            StatusManager.getManager().handle(new Status(IStatus.ERROR, PLUGIN_ID, "Problem occurred while getting bundle:" + bundleName, e));
        }
        StatusManager.getManager().handle(new Status(IStatus.ERROR, PLUGIN_ID, "Couldn't locate bundle:" + bundleName));
        return null;
    }
}
