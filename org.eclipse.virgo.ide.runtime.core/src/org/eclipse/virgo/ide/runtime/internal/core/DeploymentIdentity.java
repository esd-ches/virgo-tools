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
package org.eclipse.virgo.ide.runtime.internal.core;

/**
 * Simple data holder for a deployed par or bundle.
 * @author Christian Dupuis
 * @since 1.0.0
 */
public class DeploymentIdentity {
	
	private String symbolicName;
	
	private String version;

	public DeploymentIdentity(String symbolicName, String version) {
		this.symbolicName = symbolicName;
		this.version = version;
	}

	public DeploymentIdentity(String output) {
		int ix = output.lastIndexOf('#');
		this.symbolicName = output.substring(0, ix);
		this.version = output.substring(ix+1);
	}

	public String getSymbolicName() {
		return symbolicName;
	}

	public String getVersion() {
		return version;
	}
	
}
