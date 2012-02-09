/*******************************************************************************
 * Copyright (c) 2007, 2012 SpringSource, a divison of VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     SpringSource, a divison of VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.virgo.ide.management.remote;

import java.util.Map;

/**
 * @author Christian Dupuis
 */
public interface BundleAdmin {
	
	Map<Long, Bundle> retrieveBundles();
	
	String execute(String cmdLine);
	
}
