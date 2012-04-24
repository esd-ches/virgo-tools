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
package org.eclipse.virgo.ide.runtime.internal.core;

import org.eclipse.virgo.ide.runtime.core.IServer;
import org.eclipse.virgo.ide.runtime.core.IServerWorkingCopy;

/**
 * Default Virgo server implementation.
 * 
 * @author Terry Hon
 * @author Christian Dupuis
 * @since 1.0.0
 */
public class VirgoServer extends Server implements IServer, IServerWorkingCopy {

	public VirgoServerRuntime getRuntime() {
		if (getServer().getRuntime() == null) {
			return null;
		}

		return (VirgoServerRuntime) getServer().getRuntime().loadAdapter(VirgoServerRuntime.class, null);
	}

	@Override
	protected String getServerName() {
		return "Virgo Server";
	}

}
