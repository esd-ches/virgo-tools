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

import org.eclipse.virgo.ide.runtime.core.IServerBehaviour;
import org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider;
import org.eclipse.virgo.ide.runtime.internal.core.DeploymentIdentity;
import org.eclipse.virgo.ide.runtime.internal.core.command.IServerCommand;
import org.eclipse.virgo.ide.runtime.internal.core.command.JmxServerDeployCommand;
import org.eclipse.wst.server.core.IModule;

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
    protected String getServerProfileName() {
        return "java7-server.profile";
    }

    @Override
    public String getSupportedVersions() {
        return "3.7+";
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.virgo.ide.runtime.internal.core.runtimes.VirgoRuntimeProvider#getServerDeployCommand(org.eclipse.
     * virgo.ide.runtime.core.IServerBehaviour, org.eclipse.wst.server.core.IModule)
     */
    @Override
    public IServerCommand<DeploymentIdentity> getServerDeployCommand(IServerBehaviour IServerBehaviour, IModule module) {
        return new JmxServerDeployCommand(IServerBehaviour, module, true);
    }
}
