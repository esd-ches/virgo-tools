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
package org.eclipse.virgo.ide.management.remote;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.util.ObjectUtils;

/**
 * @author Christian Dupuis
 */
public class ServiceReference implements Serializable {
	
	public enum Type {
		IN_USE, REGISTERED
	}

	private static final long serialVersionUID = -4896924600246187914L;
	
	private final Long bundleId;

	private final String[] clazzes;

	private final Map<String, String> properties = new HashMap<String, String>();

	private final Set<Long> usingBundles = new HashSet<Long>();
	
	private final Type type;

	public ServiceReference(Type type, Long bundleId, String[] clazzes) {
		this.bundleId = bundleId;
		this.clazzes = clazzes;
		this.type = type;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public String[] getClazzes() {
		return clazzes;
	}
	
	public Set<Long> getUsingBundleIds() {
		return usingBundles;
	}

	public void addProperty(String key, String value) {
		this.properties.put(key, value);
	}

	public void addUsingBundle(Long id) {
		this.usingBundles.add(id);
	}

	public Long getBundleId() {
		return bundleId;
	}

	public Type getType() {
		return type;
	}
	
	@Override
	public int hashCode() {
		int hashCode = 17;
		hashCode = 31 * hashCode + clazzes.hashCode();
		hashCode = 31 * hashCode + type.hashCode();
		return hashCode;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof ServiceReference)) {
			return false;
		}
		ServiceReference that = (ServiceReference) other;
		if (!ObjectUtils.nullSafeEquals(this.clazzes, that.clazzes))
			return false;
		if (!ObjectUtils.nullSafeEquals(this.type, that.type))
			return false;
		return true;
	}
}
