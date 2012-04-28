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

import org.eclipse.virgo.ide.runtime.internal.ui.editor.RepositoryBrowserEditorPage;
import org.eclipse.wst.server.core.IServer;

/**
 * Common content provider for repository installation nodes.
 * 
 * @author Miles Parker
 */
public class InstalledBundlesContentProvider extends ServerBundleContainersContentProvider {

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof RepositoryBrowserEditorPage) {
			return getElements(((RepositoryBrowserEditorPage) inputElement).getServer().getOriginal());
		}
		if (inputElement instanceof IServer) {
			return new Object[] { new InstalledLibrariesNode((IServer) inputElement) };
		}
		if (inputElement instanceof InstalledLibrariesNode) {
			return super.getElements(((InstalledLibrariesNode) inputElement).getServer());
		}
		return super.getElements(inputElement);
	}

	@Override
	public Object getParent(Object element) {
//		if (element instanceof InstalledLibrariesNode) {
//			return ((InstalledLibrariesNode) element).getServer();
//		}
//		return super.getParent(element);
		return null;
	}
}