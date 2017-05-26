/*******************************************************************************
 * Copyright (c) 2015 Eclipse Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     EclipseSource - initial implementation
 *******************************************************************************/

package org.eclipse.virgo.ide.runtime.internal.core.runtimes;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.eclipse.core.runtime.IPath;
import org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider;
import org.eclipse.virgo.util.io.IOUtils;
import org.eclipse.wst.server.core.IRuntime;
import org.osgi.framework.Version;

/**
 * {@link IServerRuntimeProvider} for Virgo Server 3.7.0 and above.
 *
 * @author Florian Waibel
 */
public class Virgo37Provider extends Virgo35Provider {

    // Assumes Stateless
    public static final VirgoRuntimeProvider INSTANCE = new Virgo37Provider();

    private Virgo37Provider() {
    }

    @Override
    public boolean isHandlerFor(IRuntime runtime) {
        IPath libPath = runtime.getLocation().append("lib"); //$NON-NLS-1$
        File libDir = libPath.toFile();
        if (libDir.exists()) {
            IPath versionFilePath = libPath.append(".version"); //$NON-NLS-1$
            File versionFile = versionFilePath.toFile();
            if (versionFile.exists()) {
                String version = (String) readVersionFile(versionFile).get("virgo.server.version"); //$NON-NLS-1$
                if (version != null) {
                    Version ver = Version.parseVersion(version);
                    return (ver.getMajor() == 3 && ver.getMinor() >= 7);
                }
            }
            return false;
        }
        return false;
    }

    private Properties readVersionFile(File versionFile) {
        Properties props = new Properties();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(versionFile);
            props.load(fis);
        } catch (Exception e) {
            // ignore
        } finally {
            IOUtils.closeQuietly(fis);
        }
        return props;
    }

    @Override
    protected String getServerProfileName() {
        return "java-server.profile"; //$NON-NLS-1$
    }

    @Override
    public String getSupportedVersions() {
        return "3.7+"; //$NON-NLS-1$
    }

}
