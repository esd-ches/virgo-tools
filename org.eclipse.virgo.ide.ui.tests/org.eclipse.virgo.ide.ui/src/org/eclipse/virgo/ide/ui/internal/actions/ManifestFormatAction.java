/*******************************************************************************
 * Copyright (c) 2009 SpringSource, a divison of VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource, a division of VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.virgo.ide.ui.internal.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.editor.actions.FormatAction;
import org.eclipse.ui.PlatformUI;


/**
 * @author Christian Dupuis
 * @author Leo Dos Santos
 */
public class ManifestFormatAction extends FormatAction {

	@Override
	public void run() {
		if (fTextEditor == null || fTextEditor.getEditorInput() == null) {
			return;
		}

		try {
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(
					new ManifestFormatOperation(new Object[] { fTextEditor.getEditorInput() }));
		}
		catch (InvocationTargetException e) {
			PDEPlugin.log(e);
		}
		catch (InterruptedException e) {
			PDEPlugin.log(e);
		}
	}

}
