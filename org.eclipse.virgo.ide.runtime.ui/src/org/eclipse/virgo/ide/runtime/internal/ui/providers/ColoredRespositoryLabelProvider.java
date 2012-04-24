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

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.virgo.ide.runtime.core.artefacts.IArtefact;

public class ColoredRespositoryLabelProvider extends ArtefactLabelProvider implements IColorProvider {

	private final RepositoryContentProvider repositoryContentProvider;

	public ColoredRespositoryLabelProvider(RepositoryContentProvider repositoryContentProvider) {
		super();
		this.repositoryContentProvider = repositoryContentProvider;
	}

	public Color getBackground(Object element) {
		return null;
	}

	public Color getForeground(Object element) {
		if (repositoryContentProvider.getRepository() != null && element instanceof IArtefact
				&& repositoryContentProvider.getRepository().contains((IArtefact) element)) {
			return Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY);
		}
		return null;
	}

}