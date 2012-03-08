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
package org.eclipse.virgo.ide.runtime.core;

import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jst.server.core.IJavaRuntime;

/**
 * Extension to {@link IServerRuntime} that allows for set some java related settings.
 * @author Christian Dupuis
 * @since 1.0.0
 */
public interface IServerRuntimeWorkingCopy extends IServerRuntime, IJavaRuntime {
	
	/**
	 * Sets the {@link IVMInstall} for the given dm Server runtime 
	 */
	void setVMInstall(IVMInstall vmInstall);
	
	IServerRuntimeProvider getVirgoVersion();
}
