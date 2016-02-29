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

package org.eclipse.virgo.ide.runtime.internal.ui.providers;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.pde.core.target.ITargetHandle;
import org.eclipse.pde.core.target.ITargetPlatformService;
import org.eclipse.ui.PlatformUI;
import org.eclipse.virgo.ide.runtime.core.ServerCorePlugin;
import org.eclipse.virgo.ide.runtime.internal.ui.ServerUiImages;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;

/**
 * A lightweight decorator that overlays a decoration on the top left of servers whose runtime has an associated PDE
 * target platform.
 */
public class ServerTargetPlatformDecorator extends BaseLabelProvider implements ILightweightLabelDecorator {

    /**
     * {@inheritDoc}
     */
    public void decorate(Object element, IDecoration decoration) {
        if (element instanceof IServer) {
            IServer srv = (IServer) element;
            if (srv.getServerType().getId().equals(ServerCorePlugin.VIRGO_SERVER_ID)) {
                IRuntime runtime = srv.getRuntime();
                String name = runtime.getName();
                ITargetPlatformService service = PlatformUI.getWorkbench().getService(ITargetPlatformService.class);
                ITargetHandle handle = null;
                try {
                    handle = service.getWorkspaceTargetHandle();
                } catch (CoreException e) {
                }
                if (handle != null) {
                    try {
                        String name2 = handle.getTargetDefinition().getName();
                        if (name.equals(name2)) {
                            decoration.addOverlay(ServerUiImages.DESC_OBJ_PDE_OVER, IDecoration.TOP_LEFT);
                        }
                    } catch (CoreException e) {
                    }
                }
            }
        }
    }

}
