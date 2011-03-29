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
import java.util.List;

import org.eclipse.zest.layouts.Filter;
import org.eclipse.zest.layouts.InvalidLayoutConfiguration;
import org.eclipse.zest.layouts.LayoutAlgorithm;
import org.eclipse.zest.layouts.LayoutEntity;
import org.eclipse.zest.layouts.LayoutRelationship;
import org.eclipse.zest.layouts.algorithms.CompositeLayoutAlgorithm;
import org.eclipse.zest.layouts.progress.ProgressListener;

/**
 * @author Christian Dupuis
 */
public class FocusedBundleDependencyLayoutAlgorithm implements LayoutAlgorithm {

	private final BundleDependencyLayoutAlgorithm focusedLayoutAlgorithm;

	private final CompositeLayoutAlgorithm layoutAlgorithm;

	private final BundleDependencyContentProvider contentProvider;

	public FocusedBundleDependencyLayoutAlgorithm(int styles, CompositeLayoutAlgorithm layoutAlgorithm,
			BundleDependencyContentProvider contentProvider) {
		this.contentProvider = contentProvider;
		this.layoutAlgorithm = layoutAlgorithm;
		this.focusedLayoutAlgorithm = new BundleDependencyLayoutAlgorithm(contentProvider);
	}

	public void addEntity(LayoutEntity entity) {
		layoutAlgorithm.addEntity(entity);
		focusedLayoutAlgorithm.addEntity(entity);
	}

	public void addProgressListener(ProgressListener listener) {
		layoutAlgorithm.addProgressListener(listener);
		focusedLayoutAlgorithm.addProgressListener(listener);
	}

	public void addRelationship(LayoutRelationship relationship) {
		layoutAlgorithm.addRelationship(relationship);
		focusedLayoutAlgorithm.addRelationship(relationship);
	}

	public void applyLayout(LayoutEntity[] entitiesToLayout, LayoutRelationship[] relationshipsToConsider, double x,
			double y, double width, double height, boolean asynchronous, boolean continuous)
			throws InvalidLayoutConfiguration {
		if (contentProvider.getContentResult() != null) {
			focusedLayoutAlgorithm.applyLayout(entitiesToLayout, relationshipsToConsider, x, y, width, height,
					asynchronous, continuous);
		}
		else {
			layoutAlgorithm.applyLayout(entitiesToLayout, relationshipsToConsider, x, y, width, height, asynchronous,
					continuous);
		}
	}

	public double getEntityAspectRatio() {
		return layoutAlgorithm.getEntityAspectRatio();
	}

	public int getStyle() {
		return layoutAlgorithm.getStyle();
	}

	public boolean isRunning() {
		return layoutAlgorithm.isRunning();
	}

	public void removeEntity(LayoutEntity entity) {
		layoutAlgorithm.removeEntity(entity);
		focusedLayoutAlgorithm.removeEntity(entity);
	}

	public void removeProgressListener(ProgressListener listener) {
		layoutAlgorithm.removeProgressListener(listener);
		focusedLayoutAlgorithm.removeProgressListener(listener);
	}

	public void removeRelationship(LayoutRelationship relationship) {
		layoutAlgorithm.removeRelationship(relationship);
		focusedLayoutAlgorithm.removeRelationship(relationship);
	}

	@SuppressWarnings("unchecked")
	public void removeRelationships(List relationships) {
		layoutAlgorithm.removeRelationships(relationships);
		focusedLayoutAlgorithm.removeRelationships(relationships);
	}

	@SuppressWarnings("unchecked")
	public void setComparator(Comparator comparator) {
		layoutAlgorithm.setComparator(comparator);
		focusedLayoutAlgorithm.setComparator(comparator);
	}

	public void setEntityAspectRatio(double ratio) {
		layoutAlgorithm.setEntityAspectRatio(ratio);
		focusedLayoutAlgorithm.setEntityAspectRatio(ratio);
	}

	public void setFilter(Filter filter) {
		layoutAlgorithm.setFilter(filter);
		focusedLayoutAlgorithm.setFilter(filter);
	}

	public void setStyle(int style) {
		layoutAlgorithm.setStyle(style);
		focusedLayoutAlgorithm.setStyle(style);
	}

	public void stop() {
		layoutAlgorithm.stop();
		focusedLayoutAlgorithm.stop();
	}

}
