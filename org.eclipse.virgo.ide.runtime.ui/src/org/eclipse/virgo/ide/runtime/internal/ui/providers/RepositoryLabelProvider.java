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

import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.PDEPluginImages;
import org.eclipse.swt.graphics.Image;
import org.eclipse.virgo.ide.runtime.core.artefacts.ArtefactSet;
import org.eclipse.virgo.ide.runtime.core.artefacts.IArtefact;
import org.eclipse.virgo.ide.runtime.internal.ui.ServerUiImages;
import org.eclipse.virgo.ide.runtime.internal.ui.editor.Messages;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.ui.internal.editor.IServerEditorPartFactory;

/**
 * 
 * @author Miles Parker
 * 
 */
public class RepositoryLabelProvider extends ArtefactLabelProvider {

	public String getText(Object element) {
		if (element instanceof InstalledLibrariesNode) {
			return Messages.RepositoryBrowserEditorPage_InstalledBundlesAndLibraries;
		}
		if (element instanceof IServerEditorPartFactory) {
			return ((IServerEditorPartFactory) element).getName();
		}
		if (element instanceof IServer) {
			return Messages.RepositoryBrowserEditorPage_InstalledBundlesAndLibraries;
		}
		if (element instanceof IArtefact || element instanceof ArtefactSet) {
			return super.getText(element);
		}
		return element.toString();
	}

	public Image getImage(Object element) {
		if (element instanceof IServerEditorPartFactory) {
			return PDEPlugin.getDefault().getLabelProvider().get(PDEPluginImages.DESC_PAGE_OBJ);
		}
		if (element instanceof IArtefact || element instanceof ArtefactSet) {
			return super.getImage(element);
		}
		if (element instanceof InstalledLibrariesNode) {
			return ServerUiImages.getImage(ServerUiImages.IMG_OBJ_LIB);
		}
		if (element == Messages.RepositoryBrowserEditorPage_InstalledBundlesAndLibraries) {
			return ServerUiImages.getImage(ServerUiImages.IMG_OBJ_LIB);
		}
		return null;
	}
}
