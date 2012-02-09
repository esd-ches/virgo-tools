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

import java.io.Serializable;

/**
 * @author Christian Dupuis
 */
public class PackageImport implements Serializable {

	private static final long serialVersionUID = 8376491037926415151L;

	private final String name;

	private final String version;

	private final String supplierId;

	public PackageImport(String name, String version, String supplierId) {
		this.name = name;
		this.version = version;
		this.supplierId = supplierId;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @return the supplierId
	 */
	public String getSupplierId() {
		return supplierId;
	}

}
