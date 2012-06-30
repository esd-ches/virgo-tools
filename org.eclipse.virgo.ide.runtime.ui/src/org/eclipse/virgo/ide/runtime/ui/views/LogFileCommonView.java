/*******************************************************************************
 * Copyright (c) 2009, 2012 SpringSource, a divison of VMware, Inc.
 * Copyright (c) IBM Corporation (code cribbed from pde and navigator.)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource, a division of VMware, Inc. - initial API and implementation
 *******************************************************************************/

package org.eclipse.virgo.ide.runtime.ui.views;

import org.eclipse.virgo.ide.runtime.internal.ui.ServerUiPlugin;
import org.eclipse.virgo.ide.runtime.internal.ui.projects.ServerProject;

/**
 * 
 * @see org.eclipse.pde.internal.ui.views.dependencies.DependenciesView
 * @author Miles Parker
 * 
 */
@SuppressWarnings("restriction")
public class LogFileCommonView extends ServerFileCommonView {

	/**
	 * @see org.eclipse.virgo.ide.runtime.ui.views.CommonView#getContentId()
	 */
	@Override
	protected String getTreeContentId() {
		return ServerUiPlugin.LOG_CONTENT_ID;
	}

	/**
	 * @see org.eclipse.virgo.ide.runtime.ui.views.CommonView#getViewId()
	 */
	@Override
	protected String getViewId() {
		return ServerUiPlugin.LOG_VIEW_ID;
	}

	/**
	 * @see org.eclipse.virgo.ide.runtime.ui.views.ServerFileCommonView#getManagedDirs()
	 */
	@Override
	public String[] getManagedDirs() {
		return new String[] { ServerProject.LOG_WORKSPACE_DIR };
	}
}
