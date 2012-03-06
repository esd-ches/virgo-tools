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
import org.eclipse.wst.server.core.IRuntime;

/**
 * Utility that loads {@link IServerVersionHandler}s based on given version identifiers.
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @author Miles Parker
 * @since 1.0.0
 */
public class ServerVersionAdapter {
	
	public static final String SERVER_VIRGO_BASE = "org.eclipse.virgo.server.runtime.virgo";
	
	//Need to preserve id for backward compatibility
	public static final String SERVER_VIRGO_21x_31x = SERVER_VIRGO_BASE;
	
	public static final String SERVER_VIRGO_35 = SERVER_VIRGO_BASE + ".35";

	public static IServerVersionHandler getVersionHandler(String id) {
		if (SERVER_VIRGO_21x_31x.equals(id)) {
			return new ServerVirgo21_30Handler();
		}
		if (SERVER_VIRGO_35.equals(id)) {
			return new ServerVirgo35Handler();
		}
		else {
			return null;
		}
	}

	public static boolean isVirgo(IRuntime runtime) {
		return runtime.getRuntimeType().getId().startsWith(ServerVersionAdapter.SERVER_VIRGO_BASE);
	}
}
