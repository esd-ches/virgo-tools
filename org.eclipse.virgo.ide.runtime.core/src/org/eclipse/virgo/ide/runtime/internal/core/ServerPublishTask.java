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

package org.eclipse.virgo.ide.runtime.internal.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.virgo.ide.facet.core.FacetUtils;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.PublishOperation;
import org.eclipse.wst.server.core.model.PublishTaskDelegate;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;

/**
 * {@link PublishOperation} extension that deals with deploy, clean and refresh of dm Server modules.
 *
 * @author Christian Dupuis
 * @since 1.0.0
 */
public class ServerPublishTask extends PublishTaskDelegate {

    @SuppressWarnings("unchecked")
    @Override
    public PublishOperation[] getTasks(IServer server, int kind, List modules, List kindList) {
        if (modules == null || modules.size() == 0) {
            return null;
        }

        Set<IModule> modulesToPublish = new HashSet<IModule>();
        List<ServerPublishOperation> tasks = new ArrayList<ServerPublishOperation>();

        ServerBehaviour engineServer = (ServerBehaviour) server.loadAdapter(ServerBehaviour.class, null);
        for (int i = 0; i < modules.size(); i++) {
            IModule[] module = (IModule[]) modules.get(i);

            int state = server.getModulePublishState(module);
            if (state != IServer.PUBLISH_STATE_NONE || kind == IServer.PUBLISH_CLEAN) {
                Integer in = (Integer) kindList.get(i);
                if (in != ServerBehaviourDelegate.NO_CHANGE || kind == IServer.PUBLISH_CLEAN) {
                    if (module.length == 1) {
                        addModuleToPublish(tasks, modulesToPublish, engineServer, module[0], in, kind);
                    } else if (FacetUtils.isParProject(module[0].getProject()) && kind != IServer.PUBLISH_CLEAN) {
                        addModuleToPublish(tasks, modulesToPublish, engineServer, module[0], in, kind);
                    } else if (module.length > 1) {
                        IProject project = module[0].getProject();
                        if (!FacetUtils.isBundleProject(project) && !FacetUtils.isParProject(project)) {
                            addModuleToPublish(tasks, modulesToPublish, engineServer, module[0], in, kind);
                        }
                    }
                }
            }
        }

        return tasks.toArray(new PublishOperation[tasks.size()]);
    }

    private void addModuleToPublish(List<ServerPublishOperation> tasks, Set<IModule> modulesToPublish, ServerBehaviour engineServer, IModule module,
        Integer in, int kind) {

        if (!modulesToPublish.contains(module)) {
            tasks.add(new ServerPublishOperation(engineServer, kind, new IModule[] { module }, in.intValue()));
            modulesToPublish.add(module);
        }
    }
}
