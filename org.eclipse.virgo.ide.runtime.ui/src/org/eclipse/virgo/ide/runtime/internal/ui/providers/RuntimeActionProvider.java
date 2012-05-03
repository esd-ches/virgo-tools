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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;

/**
 * 
 * @author Miles Parker
 * 
 */
public class RuntimeActionProvider extends CommonActionProvider {

	/**
	 * @see org.eclipse.ui.navigator.CommonActionProvider#init(org.eclipse.ui.navigator.ICommonActionExtensionSite)
	 */
	@Override
	public void init(ICommonActionExtensionSite aSite) {
		super.init(aSite);
		if (aSite instanceof ICommonViewerWorkbenchSite) {
			ICommonViewerWorkbenchSite site = (ICommonViewerWorkbenchSite) aSite;
			new OpenProjectFileAction(site.getPage(), site.getSelectionProvider());
		}
	}

	class OpenProjectFileAction extends Action {

		private final ISelectionProvider provider;

		private final IWorkbenchPage site;

		public OpenProjectFileAction(IWorkbenchPage site, ISelectionProvider provider) {
			this.site = site;
			this.provider = provider;
		}

		/**
		 * @see org.eclipse.jdt.ui.actions.SelectionDispatchAction#run()
		 */
		@Override
		public void run() {
			super.run();
		}
	}
}
