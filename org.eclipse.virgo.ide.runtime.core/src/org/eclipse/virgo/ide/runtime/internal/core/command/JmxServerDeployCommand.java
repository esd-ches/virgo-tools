/*******************************************************************************
 * Copyright (c) 2010 SpringSource, a divison of VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource, a division of VMware, Inc. - initial API and implementation
 *******************************************************************************/

package org.eclipse.virgo.ide.runtime.internal.core.command;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import javax.management.openmbean.CompositeData;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.virgo.ide.facet.core.FacetCorePlugin;
import org.eclipse.virgo.ide.facet.core.FacetUtils;
import org.eclipse.virgo.ide.runtime.core.IServerBehaviour;
import org.eclipse.virgo.ide.runtime.core.ServerUtils;
import org.eclipse.virgo.ide.runtime.internal.core.DeploymentIdentity;
import org.eclipse.virgo.ide.runtime.internal.core.Server;
import org.eclipse.virgo.util.io.FileCopyUtils;
import org.eclipse.wst.server.core.IModule;

/**
 * {@link IServerCommand} to deploy a PAR or bundle.
 *
 * @author Christian Dupuis
 * @since 1.0.1
 */
public class JmxServerDeployCommand extends AbstractJmxServerDeployerCommand<CompositeData> implements IServerCommand<DeploymentIdentity> {

    private static final String ITEM_SYMBOLIC_NAME = "symbolicName"; //$NON-NLS-1$

    private static final String ITEM_VERSION = "version"; //$NON-NLS-1$

    private final boolean checkBundleDeployed;

    /**
     * Creates a new {@link JmxServerDeployCommand}.
     */
    public JmxServerDeployCommand(IServerBehaviour serverBehaviour, IModule module) {
        this(serverBehaviour, module, false);
    }

    /**
     * Creates a new {@link JmxServerDeployCommand} that checks whether the bundle to be deployed is actually already
     * deployed.
     *
     * @param checkBundleDeployed <code>true</code> to check before deploying, <code>false</code> otherwise
     */
    public JmxServerDeployCommand(IServerBehaviour serverBehaviour, IModule module, boolean checkBundleDeployed) {
        super(serverBehaviour, module);
        this.checkBundleDeployed = checkBundleDeployed;
    }

    /**
     * {@inheritDoc}
     */
    public DeploymentIdentity execute() throws IOException, TimeoutException {
        Map<String, DeploymentIdentity> identities = this.serverBehaviour.getDeploymentIdentities();

        if (this.checkBundleDeployed) {
            DeploymentIdentity alreadyDeployed = new JmxServerCheckBundleDeployedCommand(this.serverBehaviour, this.module).execute();
            if (alreadyDeployed != null) {
                identities.put(this.module.getId(), alreadyDeployed);
                return alreadyDeployed;
            }
        }

        if (isPlan()) {
            // plan module name is workspace-relative path
            String path = module.getName();
            IFile planFile = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(path));

            List<IFile> files = FacetUtils.getNestedPlanFiles(planFile);
            IPath stageDir = this.serverBehaviour.getServerDeployDirectory();
            File stageFileDir = stageDir.toFile();

            // make sure nested plans, if any, are copied to the stage dir so that
            // they can be found
            for (IFile iFile : files) {
                File oldFile = new File(stageFileDir, iFile.getName());
                if (oldFile.exists()) {
                    oldFile.delete();
                }
                FileCopyUtils.copy(iFile.getLocation().toFile(), oldFile);
            }

        }

        CompositeData returnValue = doExecute();
        if (returnValue != null) {
            String symbolicName = (String) returnValue.get(ITEM_SYMBOLIC_NAME);
            String version = (String) returnValue.get(ITEM_VERSION);
            DeploymentIdentity identity = new DeploymentIdentity(symbolicName, version);
            identities.put(this.module.getId(), identity);
            return identity;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object[] getOperationArguments() {
        URI uri = null;
        if (isPlan()) {
            String fileName = this.module.getId();
            fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
            uri = getUri(this.serverBehaviour.getModuleDeployUri(this.module).append(fileName));
        } else {
            uri = getUri(this.serverBehaviour.getModuleDeployUri(this.module));
        }
        return new Object[] { uri.toString(), false };
    }

    private boolean isPlan() {
        return this.module.getModuleType().getId().equals(FacetCorePlugin.PLAN_FACET_ID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getOperationName() {
        return "deploy";
    }

    @Override
    protected int getTimeout() {
        Server server = ServerUtils.getServer(this.serverBehaviour);
        return server.getDeployTimeout();
    }

}
