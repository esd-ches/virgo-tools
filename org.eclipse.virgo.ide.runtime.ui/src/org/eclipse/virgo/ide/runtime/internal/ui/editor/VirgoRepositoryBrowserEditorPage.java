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
package org.eclipse.virgo.ide.runtime.internal.ui.editor;

import org.eclipse.swt.graphics.Image;
import org.eclipse.virgo.ide.runtime.internal.ui.ServerUiImages;
import org.eclipse.wst.server.ui.editor.ServerEditorPart;

/**
 * {@link ServerEditorPart} that allows to browse the local and remote bundle repository.
 * 
 * @author Terry Hon
 * @author Christian Dupuis
 * @since 1.0.0
 */
public class VirgoRepositoryBrowserEditorPage extends RepositoryBrowserEditorPage {

	@Override
	protected Image getFormImage() {
		return ServerUiImages.getImage(ServerUiImages.IMG_OBJ_VIRGO);
	}

	@Override
	protected String getServerName() {
		return Messages.VirgoServerName;
	}

}
