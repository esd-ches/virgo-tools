/*******************************************************************************
 * Copyright (c) 2009 - 2012 SpringSource, a divison of VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource, a division of VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.virgo.ide.runtime.internal.core;

import org.eclipse.virgo.ide.runtime.core.IServerVersionHandler;

/**
 * Utility that loads {@link IServerVersionHandler}s based on given version
 * identifiers.
 * 
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @author Miles Parker
 * @since 1.0.0
 */
public class ServerVersionAdapter {

	public static final ServerVirgoHandler[] ALL_HANDLERS = new ServerVirgoHandler[] {
		ServerVirgo21_30Handler.INSTANCE, ServerVirgo35Handler.INSTANCE };

	public static ServerVirgoHandler getVersionHandler(String id) {
		for (ServerVirgoHandler handler : ALL_HANDLERS) {
			if (handler.getID().equals(id)) {
				return handler;
			}
		}
		return null;
	}

	public static String getVersionID(IServerVersionHandler handler) {
		if (handler instanceof ServerVirgoHandler) {
			return ((ServerVirgoHandler) handler).getID();
		}
		return null;
	}
}
