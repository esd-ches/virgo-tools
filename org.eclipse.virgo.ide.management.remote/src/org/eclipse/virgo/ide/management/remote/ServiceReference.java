/*******************************************************************************
 * Copyright (c) 2007, 2009 SpringSource, a divison of VMware, Inc.
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

	/**
	 * @return the type
	 */
	public Type getType() {
		return type;
	}
}
