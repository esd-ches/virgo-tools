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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.virgo.ide.management.remote.Bundle;
import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.eclipse.zest.layouts.LayoutEntity;
import org.eclipse.zest.layouts.algorithms.AbstractLayoutAlgorithm;
import org.eclipse.zest.layouts.dataStructures.InternalNode;
import org.eclipse.zest.layouts.dataStructures.InternalRelationship;


/**
 * @author Christian Dupuis
 */
public class BundleDependencyLayoutAlgorithm extends AbstractLayoutAlgorithm {

	private final BundleDependencyContentProvider contentProvider;

	public BundleDependencyLayoutAlgorithm(BundleDependencyContentProvider contentProvider) {
		super(ZestStyles.NODES_NO_LAYOUT_RESIZE);
		this.contentProvider = contentProvider;
	}

	@Override
	protected void applyLayoutInternal(InternalNode[] entitiesToLayout, InternalRelationship[] relationshipsToConsider,
			double boundsX, double boundsY, double boundsWidth, double boundsHeight) {

		BundleDependencyContentResult contentResult = this.contentProvider.getContentResult();
		Set<Bundle> rootBundles = contentResult.getBundles();

		Set<Bundle> bundlesProcessed = new HashSet<Bundle>();

		if (contentResult != null) {
			Set<ColumnHolder> columnNodes = new HashSet<ColumnHolder>();
			double columnWith = 0;
			double currentX = boundsX + 0;
			double currentY = boundsY + 10;
			double maxY = boundsHeight;
			int columns = 0;

			// Start with the incoming dependencies
			int degree = contentResult.getIncomingDegree();
			while (degree > 0) {
				Set<InternalNode> degreeNodes = new HashSet<InternalNode>();
				Set<Bundle> deps = contentResult.getIncomingDependencies().get(degree);
				for (Bundle bundle : deps) {
					if (!bundlesProcessed.contains(bundle)
							&& !rootBundles.contains(bundle)
							&& lowestRanking(bundle, contentResult.getIncomingDegree(), contentResult
									.getIncomingDependencies()) == degree) {
						for (InternalNode node : entitiesToLayout) {
							LayoutEntity obj = node.getLayoutEntity();
							Bundle graphBundle = (Bundle) ((GraphNode) obj.getGraphData()).getData();
							if (graphBundle.equals(bundle)) {
								columnWith = Math.max(columnWith, node.getWidthInLayout());
								node.setLocation(currentX, currentY);
								bundlesProcessed.add(bundle);
								currentY = currentY + node.getHeightInLayout() + 15;
								degreeNodes.add(node);
								maxY = Math.max(currentY, maxY);
								break;
							}
						}
					}
				}

				for (InternalNode node : degreeNodes) {
					if (node.getWidthInLayout() < columnWith) {
						double x = (columnWith - node.getWidthInLayout()) / 2;
						node.setLocation(node.getLayoutEntity().getXInLayout() + x, node.getLayoutEntity()
								.getYInLayout());
					}
				}

				ColumnHolder holder = new ColumnHolder();
				holder.index = columns;
				holder.y = currentY;
				holder.nodes = degreeNodes;
				columnNodes.add(holder);
				columns++;
				currentY = boundsY + 10;
				if (degreeNodes.size() > 0) {
					currentX = currentX + columnWith + 30;
				}
				degree--;
				columnWith = 0;
			}

			Set<InternalNode> rootNodes = new HashSet<InternalNode>();
			for (Bundle bundle : rootBundles) {
				for (InternalNode node : entitiesToLayout) {
					LayoutEntity obj = node.getLayoutEntity();
					Bundle graphBundle = (Bundle) ((GraphNode) obj.getGraphData()).getData();
					if (graphBundle.equals(bundle)) {
						columnWith = Math.max(columnWith, node.getWidthInLayout());
						node.setLocation(currentX, currentY);
						bundlesProcessed.add(bundle);
						currentY = currentY + node.getHeightInLayout() + 15;
						maxY = Math.max(currentY, maxY);
						rootNodes.add(node);
						break;
					}
				}
			}
			for (InternalNode node : rootNodes) {
				if (node.getWidthInLayout() < columnWith) {
					double x = (columnWith - node.getWidthInLayout()) / 2;
					node.setLocation(node.getLayoutEntity().getXInLayout() + x, node.getLayoutEntity().getYInLayout());
				}
			}

			ColumnHolder holder = new ColumnHolder();
			holder.index = columns;
			holder.y = currentY;
			holder.nodes = rootNodes;
			columnNodes.add(holder);

			currentY = boundsY + 10;
			currentX = currentX + columnWith + 30;

			int maxDegree = contentResult.getOutgoingDegree();
			degree = 1;
			while (degree <= maxDegree) {
				Set<InternalNode> degreeNodes = new HashSet<InternalNode>();
				Set<Bundle> deps = contentResult.getOutgoingDependencies().get(degree);
				for (Bundle bundle : deps) {
					if (!bundlesProcessed.contains(bundle)
							&& !rootBundles.contains(bundle)
							&& lowestRanking(bundle, contentResult.getOutgoingDegree(), contentResult
									.getOutgoingDependencies()) == degree) {
						for (InternalNode node : entitiesToLayout) {
							LayoutEntity obj = node.getLayoutEntity();
							Bundle graphBundle = (Bundle) ((GraphNode) obj.getGraphData()).getData();
							if (graphBundle.equals(bundle)) {
								columnWith = Math.max(columnWith, node.getWidthInLayout());
								node.setLocation(currentX, currentY);
								bundlesProcessed.add(bundle);
								currentY = currentY + node.getHeightInLayout() + 15;
								maxY = Math.max(currentY, maxY);
								degreeNodes.add(node);
								break;
							}
						}
					}
				}
				for (InternalNode node : degreeNodes) {
					if (node.getWidthInLayout() < columnWith) {
						double x = (columnWith - node.getWidthInLayout()) / 2;
						node.setLocation(node.getLayoutEntity().getXInLayout() + x, node.getLayoutEntity()
								.getYInLayout());
					}
				}
				holder = new ColumnHolder();
				holder.index = columns;
				holder.y = currentY;
				holder.nodes = degreeNodes;
				columnNodes.add(holder);
				currentY = boundsY + 10;
				currentX = currentX + columnWith + 30;
				degree++;
				columnWith = 0;
			}

			for (ColumnHolder column : columnNodes) {
				if (column.y <= maxY) {
					double y = (maxY - column.y) / 2;
					for (InternalNode node : column.nodes) {
						node.setLocation(node.getLayoutEntity().getXInLayout(), node.getLayoutEntity().getYInLayout()
								+ y);
					}
				}
			}

		}
	}

	private int lowestRanking(Bundle bundle, int maxDegree, Map<Integer, Set<Bundle>> bundles) {
		int ranking = 1;
		while (ranking <= maxDegree) {
			for (Bundle b : bundles.get(ranking)) {
				if (b.equals(bundle)) {
					return ranking;
				}
			}
			ranking++;
		}
		return ranking;
	}

	@Override
	protected int getCurrentLayoutStep() {
		return 1;
	}

	@Override
	protected int getTotalNumberOfLayoutSteps() {
		return 1;
	}

	@Override
	protected boolean isValidConfiguration(boolean asynchronous, boolean continuous) {
		return true;
	}

	@Override
	protected void postLayoutAlgorithm(InternalNode[] entitiesToLayout, InternalRelationship[] relationshipsToConsider) {
	}

	@Override
	protected void preLayoutAlgorithm(InternalNode[] entitiesToLayout, InternalRelationship[] relationshipsToConsider,
			double x, double y, double width, double height) {
	}

	@Override
	public void setLayoutArea(double x, double y, double width, double height) {
	}

	class ColumnHolder {
		protected double y;

		protected int index;

		protected Set<InternalNode> nodes;
	}

}
