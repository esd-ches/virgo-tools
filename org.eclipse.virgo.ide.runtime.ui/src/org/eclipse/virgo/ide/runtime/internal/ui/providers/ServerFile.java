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

package org.eclipse.virgo.ide.runtime.internal.ui.providers;

import org.eclipse.core.resources.IFile;
import org.eclipse.wst.server.core.IServer;

/**
 * 
 * @author Miles Parker
 * 
 */
public class ServerFile {

	protected final IFile file;

	private final IServer server;

	public ServerFile(IServer server, IFile file) {
		this.server = server;
		this.file = file;
	}

	public IFile getFile() {
		return file;
	}

	public IServer getServer() {
		return server;
	}
}