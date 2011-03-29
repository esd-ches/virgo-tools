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
package org.eclipse.virgo.ide.runtime.internal.ui.dependencies;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.virgo.ide.management.remote.Bundle;


/**
 * @author Christian Dupuis
 */
public class BundleDependencyContentResult {

	private final Set<Bundle> bundles;

	private final Map<Integer, Set<Bundle>> incomingDependencies = new HashMap<Integer, Set<Bundle>>();

	private final Map<Integer, Set<Bundle>> outgoingDependencies = new HashMap<Integer, Set<Bundle>>();

	public BundleDependencyContentResult(Set<Bundle> bundles) {
		this.bundles = bundles;
	}

	public void addIncomingDependency(Integer level, Bundle bundleDependency) {
		if (!incomingDependencies.containsKey(level)) {
			incomingDependencies.put(level, new TreeSet<Bundle>(new Comparator<Bundle>() {

				public int compare(Bundle o1, Bundle o2) {
					return Long.valueOf(o1.getId()).compareTo(Long.valueOf(o2.getId()));
				}
			}));
		}
		incomingDependencies.get(level).add(bundleDependency);
	}

	public void addOutgoingDependency(Integer level, Bundle bundleDependency) {
		if (!outgoingDependencies.containsKey(level)) {
			outgoingDependencies.put(level, new TreeSet<Bundle>(new Comparator<Bundle>() {

				public int compare(Bundle o1, Bundle o2) {
					return Long.valueOf(o1.getId()).compareTo(Long.valueOf(o2.getId()));
				}
			}));
		}
		outgoingDependencies.get(level).add(bundleDependency);
	}

	public Set<Bundle> getBundles() {
		return bundles;
	}

	public Map<Integer, Set<Bundle>> getIncomingDependencies() {
		return incomingDependencies;
	}

	public Map<Integer, Set<Bundle>> getOutgoingDependencies() {
		return outgoingDependencies;
	}

	public Integer getIncomingDegree() {
		int degree = 0;
		for (Integer integer : incomingDependencies.keySet()) {
			degree = Math.max(degree, integer);
		}
		return degree;
	}

	public Integer getOutgoingDegree() {
		int degree = 0;
		for (Integer integer : outgoingDependencies.keySet()) {
			degree = Math.max(degree, integer);
		}
		return degree;
	}
}
