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
package org.eclipse.virgo.ide.ui.editors;

import java.io.File;
import java.lang.reflect.Constructor;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.virgo.ide.ui.ServerIdeUiPlugin;

/**
 * @author Steffen Pingel
 */
class PdeCompatibilityUtil {

	private static boolean isEclipse34 = true;

	public static boolean isSystemFileEditorInput(IEditorInput input) {
		if (isEclipse34) {
			Class<?> systemFileEditorInputClass;
			try {
				systemFileEditorInputClass = Class.forName("org.eclipse.pde.internal.ui.editor.SystemFileEditorInput");
				return systemFileEditorInputClass.isInstance(input);
			}
			catch (ClassNotFoundException e) {
				isEclipse34 = false;
			}
			catch (Throwable e) {
				StatusManager.getManager().handle(
						new Status(IStatus.ERROR, ServerIdeUiPlugin.PLUGIN_ID,
								"Failed to check for instance of SystemFileEditorInput"));
				isEclipse34 = false;
			}
		}
		return false;
	}

	public static IEditorInput createSystemFileEditorInput(IURIEditorInput input) {
		return createSystemFileEditorInput(new File(input.getURI()));
	}

	public static IEditorInput createSystemFileEditorInput(File file) {
		if (isEclipse34) {
			Class<?> systemFileEditorInputClass;
			try {
				systemFileEditorInputClass = Class.forName("org.eclipse.pde.internal.ui.editor.SystemFileEditorInput");
				Constructor<?> constructor = systemFileEditorInputClass.getConstructor(File.class);
				return (IEditorInput) constructor.newInstance(file);
			}
			catch (ClassNotFoundException e) {
				isEclipse34 = false;
			}
			catch (Throwable e) {
				StatusManager.getManager().handle(
						new Status(IStatus.ERROR, ServerIdeUiPlugin.PLUGIN_ID,
								"Failed to create instance of SystemFileEditorInput"));
				isEclipse34 = false;
			}
		}
		return null;
	}

}
