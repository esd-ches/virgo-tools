/*******************************************************************************
 * Copyright (c) 2009, 2011 SpringSource, a divison of VMware, Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource, a division of VMware, Inc. - initial API and implementation
 *     SAP AG - moving to Eclipse Libra project and enhancements
 *******************************************************************************/
package org.eclipse.virgo.ide.management.remote;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.libra.framework.editor.core.model.IBundle;
import org.eclipse.libra.framework.editor.core.model.IPackageExport;
import org.eclipse.libra.framework.editor.core.model.IPackageImport;
import org.eclipse.libra.framework.editor.core.model.IServiceReference;
import org.eclipse.virgo.util.common.ObjectUtils;


/**
 * @author Christian Dupuis
 * @author Kaloyan Raev
 */
public class Bundle implements IBundle, Serializable {

	private static final long serialVersionUID = 228698327431610457L;

	private final Set<IPackageExport> exports = new HashSet<IPackageExport>();

	private final String id;

	private final Set<IPackageImport> imports = new HashSet<IPackageImport>();

	private final String state;

	private final String symbolicName;

	private final String version;

	private final String location;

	private final Map<String, String> headers = new HashMap<String, String>();

	private final Set<IServiceReference> registeredServices = new HashSet<IServiceReference>();

	private final Set<IServiceReference> servicesInUse = new HashSet<IServiceReference>();

	public Bundle(String id, String symbolicName, String version, String state, String location) {
		this.symbolicName = symbolicName;
		this.version = version;
		this.id = id;
		this.state = state;
		this.location = location;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public Set<IPackageExport> getPackageExports() {
		return exports;
	}

	public String getId() {
		return id;
	}

	public Set<IPackageImport> getPackageImports() {
		return imports;
	}

	public String getState() {
		return state;
	}

	public String getSymbolicName() {
		return symbolicName;
	}

	public String getVersion() {
		return version;
	}

	public void addPackageExport(PackageExport pe) {
		this.exports.add(pe);
	}

	public void addPackageImport(PackageImport pi) {
		this.imports.add(pi);
	}

	public void addRegisteredService(IServiceReference pi) {
		this.registeredServices.add(pi);
	}

	public void addUsingService(IServiceReference pi) {
		this.servicesInUse.add(pi);
	}

	public void addHeader(String key, String value) {
		this.headers.put(key, value);
	}

	public String getLocation() {
		return location;
	}

	public Set<IServiceReference> getRegisteredServices() {
		return registeredServices;
	}

	public Set<IServiceReference> getServicesInUse() {
		return servicesInUse;
	}

	@Override
	public int hashCode() {
		int hashCode = 17;
		hashCode = 31 * hashCode + id.hashCode();
		hashCode = 31 * hashCode + symbolicName.hashCode();
		return hashCode;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof Bundle)) {
			return false;
		}
		Bundle that = (Bundle) other;
		if (!ObjectUtils.nullSafeEquals(this.id, that.id))
			return false;
		if (!ObjectUtils.nullSafeEquals(this.symbolicName, that.symbolicName))
			return false;
		return true;
	}
}
