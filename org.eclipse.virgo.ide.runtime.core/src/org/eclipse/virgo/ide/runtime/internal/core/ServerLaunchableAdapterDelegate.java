/*******************************************************************************
 * Copyright (c) 2009, 2012 SpringSource, a divison of VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource, a division of VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.virgo.ide.runtime.internal.core;

import java.net.URL;

import org.eclipse.jst.server.core.IWebModule;
import org.eclipse.jst.server.core.Servlet;
import org.eclipse.wst.server.core.IModuleArtifact;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.IURLProvider;
import org.eclipse.wst.server.core.model.LaunchableAdapterDelegate;
import org.eclipse.wst.server.core.util.HttpLaunchable;
import org.eclipse.wst.server.core.util.WebResource;

/**
 * {@link LaunchableAdapterDelegate} to allow "Run as..." actions for web
 * resources.
 * 
 * @author Christian Dupuis
 * @since 1.0.0
 */
public class ServerLaunchableAdapterDelegate extends LaunchableAdapterDelegate {

	public Object getLaunchable(IServer server, IModuleArtifact moduleObject) {
		// Check for correct module type
		if (server.getAdapter(Server.class) == null) {
			return null;
		}
		if (!(moduleObject instanceof Servlet) && !(moduleObject instanceof WebResource)) {
			return null;
		}
		if (moduleObject.getModule().loadAdapter(IWebModule.class, null) == null) {
			return null;
		}

		try {
			URL url = ((IURLProvider) server.loadAdapter(IURLProvider.class, null)).getModuleRootURL(moduleObject
					.getModule());

			if (moduleObject instanceof Servlet) {
				Servlet servlet = (Servlet) moduleObject;
				if (servlet.getAlias() != null) {
					String path = servlet.getAlias();
					if (path.startsWith("/"))
						path = path.substring(1);
					url = new URL(url, path);
				} else
					url = new URL(url, "servlet/" + servlet.getServletClassName());
			} else if (moduleObject instanceof WebResource) {
				WebResource resource = (WebResource) moduleObject;
				String path = resource.getPath().toString();
				if (path != null && path.startsWith("/") && path.length() > 0)
					path = path.substring(1);
				if (path != null && path.length() > 0)
					url = new URL(url, path);
			}
			return new HttpLaunchable(url);
		} catch (Exception e) {
			return null;
		}
	}
}
