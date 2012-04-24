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

import org.eclipse.wst.server.core.IServer;

/**
 * @author Miles Parker
 */
public class ServerViewContentProvider extends RepositoryContentProvider {

	/**
	 * @see org.eclipse.virgo.ide.runtime.internal.ui.providers.RepositoryContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IServer) {
			return new Object[] { new InstalledLibrariesNode((IServer) parentElement) };
		}
		if (parentElement instanceof InstalledLibrariesNode) {
			return super.getElements(((InstalledLibrariesNode) parentElement).server);
		}
		return super.getChildren(parentElement);
	}

	public Object[] getElements(Object parentElement) {
		if (parentElement instanceof IServer) {
			return new Object[] { new InstalledLibrariesNode((IServer) parentElement) };
		}
		if (parentElement instanceof InstalledLibrariesNode) {
			return super.getElements(((InstalledLibrariesNode) parentElement).server);
		}
		return super.getElements(parentElement);
	}
}
