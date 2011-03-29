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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.internal.ide.StringMatcher;
import org.eclipse.virgo.ide.management.remote.Bundle;
import org.eclipse.virgo.ide.management.remote.PackageImport;
import org.eclipse.virgo.ide.management.remote.ServiceReference;
import org.eclipse.virgo.ide.runtime.internal.ui.SearchControl;
import org.eclipse.virgo.ide.runtime.internal.ui.model.BundleDependency;
import org.eclipse.virgo.ide.runtime.internal.ui.model.PackageBundleDependency;
import org.eclipse.virgo.ide.runtime.internal.ui.model.ServiceReferenceBundleDependency;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.viewers.IGraphContentProvider;
import org.springframework.util.StringUtils;


/**
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public class BundleDependencyContentProvider implements IGraphContentProvider, ISelectionChangedListener {

	private static final Object[] NO_ELEMENTS = new Object[0];

	private Map<Long, Bundle> bundles;

	private BundleDependencyContentResult contentResult;

	private Map<Bundle, Set<BundleDependency>> dependenciesByBundle;

	private int incomingDependencyDegree = 1;

	private int outgoingDependencyDegree = 1;

	private final SearchControl searchControl;

	private Set<BundleDependency> selectedDependencies = new HashSet<BundleDependency>();

	private boolean showPackage = true;

	private boolean showServices = false;

	private final GraphViewer viewer;

	public BundleDependencyContentProvider(GraphViewer viewer, SearchControl control) {
		this.viewer = viewer;
		this.searchControl = control;
	}

	public void clearSelection() {
		for (BundleDependency dep : selectedDependencies) {
			viewer.unReveal(dep);
		}
		selectedDependencies.clear();
		for (Object node : viewer.getNodeElements()) {
			viewer.update(node, null);
		}

		for (Object connection : viewer.getConnectionElements()) {
			viewer.update(connection, null);
		}
	}

	public void dispose() {
	}

	public BundleDependencyContentResult getContentResult() {
		return this.contentResult;
	}

	public Object getDestination(Object rel) {
		if (rel instanceof BundleDependency) {
			return ((BundleDependency) rel).getExportingBundle();
		}
		return null;
	}

	@SuppressWarnings( { "unchecked" })
	public Object[] getElements(Object input) {
		if (input instanceof Collection) {
			dependenciesByBundle = new HashMap<Bundle, Set<BundleDependency>>();
			Set<Bundle> bundles = new HashSet<Bundle>((Collection<Bundle>) input);
			if (!"type filter text".equals(searchControl.getSearchText().getText())
					&& StringUtils.hasText(searchControl.getSearchText().getText())) {
				String searchText = searchControl.getSearchText().getText().trim() + "*";
				StringMatcher matcher = new StringMatcher(searchText, true, false);
				for (Bundle dep : new HashSet<Bundle>(bundles)) {
					boolean filter = true;
					if (matcher.match(dep.getSymbolicName())) {
						filter = false;
					}
					if (matcher.match(dep.getSymbolicName() + " (" + dep.getVersion() + ")")) {
						filter = false;
					}
					if (filter) {
						bundles.remove(dep);
					}
				}
			}
			this.contentResult = new BundleDependencyContentResult(bundles);

			Set<BundleDependency> dependencies = new HashSet<BundleDependency>();
			if (showPackage) {
				Set<Bundle> bundlesToProcess = new HashSet<Bundle>(bundles);
				Set<Bundle> alreadyProcessedBundles = new HashSet<Bundle>();
				int degree = 0;

				do {
					degree++;
					Set<Bundle> copy = new HashSet<Bundle>(bundlesToProcess);
					bundlesToProcess = new HashSet<Bundle>();
					for (Bundle b : copy) {
						bundlesToProcess.addAll(addOutgoingPackageDependencies(dependencies, b, degree,
								alreadyProcessedBundles));
					}
				} while (bundlesToProcess.size() > 0);

				bundlesToProcess = new HashSet<Bundle>(bundles);
				alreadyProcessedBundles = new HashSet<Bundle>();
				degree = 0;

				do {
					degree++;
					Set<Bundle> copy = new HashSet<Bundle>(bundlesToProcess);
					bundlesToProcess = new HashSet<Bundle>();
					for (Bundle b : copy) {
						bundlesToProcess.addAll(addIncomingPackageDependencies(dependencies, b, degree,
								alreadyProcessedBundles));
					}
				} while (bundlesToProcess.size() > 0);
			}
			else if (showServices) {
				Set<Bundle> bundlesToProcess = new HashSet<Bundle>(bundles);
				Set<Bundle> alreadyProcessedBundles = new HashSet<Bundle>();
				int degree = 0;

				do {
					degree++;
					Set<Bundle> copy = new HashSet<Bundle>(bundlesToProcess);
					bundlesToProcess = new HashSet<Bundle>();
					for (Bundle b : copy) {
						bundlesToProcess.addAll(addOutgoingServiceDependencies(dependencies, b, degree,
								alreadyProcessedBundles));
					}
				} while (bundlesToProcess.size() > 0);

				bundlesToProcess = new HashSet<Bundle>(bundles);
				alreadyProcessedBundles = new HashSet<Bundle>();
				degree = 0;

				do {
					degree++;
					Set<Bundle> copy = new HashSet<Bundle>(bundlesToProcess);
					bundlesToProcess = new HashSet<Bundle>();
					for (Bundle b : copy) {
						bundlesToProcess.addAll(addIncomingServiceDependencies(dependencies, b, degree,
								alreadyProcessedBundles));
					}
				} while (bundlesToProcess.size() > 0);
			}

			if ("type filter text".equals(searchControl.getSearchText().getText())
					|| !StringUtils.hasText(searchControl.getSearchText().getText())) {
				this.contentResult = null;
			}

			return dependencies.toArray(new BundleDependency[dependencies.size()]);
		}
		return NO_ELEMENTS;
	}

	public Object getSource(Object rel) {
		if (rel instanceof BundleDependency) {
			return ((BundleDependency) rel).getImportingBundle();
		}
		return null;
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	public boolean isSelected(Bundle bundle) {
		for (BundleDependency dep : selectedDependencies) {
			if (dep.getExportingBundle().equals(bundle)) {
				return true;
			}
		}
		return false;
	}

	public boolean isSelected(BundleDependency deb) {
		return (selectedDependencies != null && selectedDependencies.contains(deb));
	}

	@SuppressWarnings("unchecked")
	public void selectionChanged(SelectionChangedEvent event) {
		for (BundleDependency dep : selectedDependencies) {
			viewer.unReveal(dep);
		}
		selectedDependencies = new HashSet<BundleDependency>();
		Set<Object> newSelection = new HashSet<Object>();
		Iterator<Object> iterator = ((IStructuredSelection) event.getSelection()).iterator();
		while (iterator.hasNext()) {
			Object selectedObject = iterator.next();
			if (selectedObject instanceof Bundle) {
				newSelection.add(selectedObject);
				if (dependenciesByBundle.containsKey(selectedObject)) {
					for (BundleDependency dep : dependenciesByBundle.get(selectedObject)) {
						selectedDependencies.add(dep);
						newSelection.add(dep.getExportingBundle());
					}
				}
			}
			else if (selectedObject instanceof BundleDependency) {
				BundleDependency dep = (BundleDependency) selectedObject;
				selectedDependencies.add(dep);
				newSelection.add(dep.getExportingBundle());
				newSelection.add(dep.getImportingBundle());
			}
		}

		// viewer.setSelection(new StructuredSelection((Object[]) newSelection
		// .toArray(new Object[newSelection.size()])), true);

		for (Object node : viewer.getNodeElements()) {
			viewer.update(node, null);
		}

		for (Object connection : viewer.getConnectionElements()) {
			viewer.update(connection, null);
		}

		for (BundleDependency dep : selectedDependencies) {
			viewer.reveal(dep);
		}

		// viewer.getGraphControl().redraw();
	}

	public void setBundles(Map<Long, Bundle> bundles) {
		this.bundles = new HashMap<Long, Bundle>(bundles);
	}

	public void setIncomingDependencyDegree(int degree) {
		this.incomingDependencyDegree = degree;
	}

	public void setOutgoingDependencyDegree(int degree) {
		this.outgoingDependencyDegree = degree;
	}

	public void setShowPackage(boolean showPackage) {
		this.showPackage = showPackage;
	}

	public void setShowServices(boolean showServices) {
		this.showServices = showServices;
	}

	private Set<Bundle> addIncomingPackageDependencies(Set<BundleDependency> dependencies, Bundle bundle, int degree,
			Set<Bundle> processedBundles) {

		if (processedBundles.contains(bundle)) {
			return Collections.emptySet();
		}
		processedBundles.add(bundle);

		Set<Bundle> dependentBundles = new HashSet<Bundle>();
		if (incomingDependencyDegree >= degree) {
			String id = bundle.getId();

			for (Bundle dependantBundle : this.bundles.values()) {
				for (PackageImport packageImport : dependantBundle.getPackageImports()) {
					if (packageImport.getSupplierId().equals(id)) {

						Set<BundleDependency> bundleDependencies = null;
						if (dependenciesByBundle.containsKey(dependantBundle)) {
							bundleDependencies = dependenciesByBundle.get(dependantBundle);
						}
						else {
							bundleDependencies = new HashSet<BundleDependency>();
							dependenciesByBundle.put(dependantBundle, bundleDependencies);
						}

						PackageBundleDependency bundleDependency = null;

						for (BundleDependency dep : bundleDependencies) {
							if (dep.getExportingBundle().equals(bundle) && dep instanceof PackageBundleDependency) {
								bundleDependency = (PackageBundleDependency) dep;
								break;
							}
						}

						if (bundleDependency == null) {
							bundleDependency = new PackageBundleDependency(bundle, dependantBundle);
							bundleDependencies.add(bundleDependency);
							dependencies.add(bundleDependency);
						}
						contentResult.addIncomingDependency(degree, dependantBundle);

						bundleDependency.addPackageImport(packageImport);
						dependentBundles.add(dependantBundle);
					}
				}
			}
		}
		if (incomingDependencyDegree > degree) {
			return dependentBundles;
		}
		return Collections.emptySet();
	}

	private Set<Bundle> addIncomingServiceDependencies(Set<BundleDependency> dependencies, Bundle bundle, int degree,
			Set<Bundle> processedBundles) {

		if (processedBundles.contains(bundle)) {
			return Collections.emptySet();
		}
		processedBundles.add(bundle);

		Set<Bundle> dependentBundles = new HashSet<Bundle>();
		if (incomingDependencyDegree >= degree) {
			for (ServiceReference pe : bundle.getRegisteredServices()) {
				for (Long id : pe.getUsingBundleIds()) {
					Bundle dependantBundle = this.bundles.get(id);

					Set<BundleDependency> bundleDependencies = null;
					if (dependenciesByBundle.containsKey(bundle)) {
						bundleDependencies = dependenciesByBundle.get(bundle);
					}
					else {
						bundleDependencies = new HashSet<BundleDependency>();
						dependenciesByBundle.put(bundle, bundleDependencies);
					}

					ServiceReferenceBundleDependency bundleDependency = null;

					for (BundleDependency dep : bundleDependencies) {
						if (dep.getExportingBundle().equals(dependantBundle)
								&& dep instanceof ServiceReferenceBundleDependency) {
							bundleDependency = (ServiceReferenceBundleDependency) dep;
							break;
						}
					}

					if (bundleDependency == null) {
						bundleDependency = new ServiceReferenceBundleDependency(bundle, dependantBundle);
						bundleDependencies.add(bundleDependency);
						dependencies.add(bundleDependency);
					}
					contentResult.addIncomingDependency(degree, dependantBundle);

					bundleDependency.addServiceReferece(pe);
					dependentBundles.add(dependantBundle);
				}
			}
		}
		if (incomingDependencyDegree > degree) {
			return dependentBundles;
		}
		return Collections.emptySet();
	}

	private Set<Bundle> addOutgoingPackageDependencies(Set<BundleDependency> dependencies, Bundle bundle, int degree,
			Set<Bundle> processedBundles) {

		if (processedBundles.contains(bundle)) {
			return Collections.emptySet();
		}
		processedBundles.add(bundle);

		Set<Bundle> dependentBundles = new HashSet<Bundle>();
		if (outgoingDependencyDegree >= degree) {
			for (PackageImport pe : bundle.getPackageImports()) {
				Bundle dependantBundle = this.bundles.get(Long.valueOf(pe.getSupplierId()));

				Set<BundleDependency> bundleDependencies = null;
				if (dependenciesByBundle.containsKey(bundle)) {
					bundleDependencies = dependenciesByBundle.get(bundle);
				}
				else {
					bundleDependencies = new HashSet<BundleDependency>();
					dependenciesByBundle.put(bundle, bundleDependencies);
				}

				PackageBundleDependency bundleDependency = null;

				for (BundleDependency dep : bundleDependencies) {
					if (dep.getExportingBundle().equals(dependantBundle) && dep instanceof PackageBundleDependency) {
						bundleDependency = (PackageBundleDependency) dep;
						break;
					}
				}

				if (bundleDependency == null) {
					bundleDependency = new PackageBundleDependency(dependantBundle, bundle);
					bundleDependencies.add(bundleDependency);
					dependencies.add(bundleDependency);
				}
				contentResult.addOutgoingDependency(degree, dependantBundle);

				bundleDependency.addPackageImport(pe);
				dependentBundles.add(dependantBundle);
			}
		}
		if (outgoingDependencyDegree > degree) {
			return dependentBundles;
		}
		return Collections.emptySet();
	}

	private Set<Bundle> addOutgoingServiceDependencies(Set<BundleDependency> dependencies, Bundle bundle, int degree,
			Set<Bundle> processedBundles) {

		if (processedBundles.contains(bundle)) {
			return Collections.emptySet();
		}
		processedBundles.add(bundle);

		Set<Bundle> dependentBundles = new HashSet<Bundle>();
		if (outgoingDependencyDegree >= degree) {
			for (ServiceReference pe : bundle.getServicesInUse()) {
				Bundle dependantBundle = this.bundles.get(pe.getBundleId());

				Set<BundleDependency> bundleDependencies = null;
				if (dependenciesByBundle.containsKey(bundle)) {
					bundleDependencies = dependenciesByBundle.get(bundle);
				}
				else {
					bundleDependencies = new HashSet<BundleDependency>();
					dependenciesByBundle.put(bundle, bundleDependencies);
				}

				ServiceReferenceBundleDependency bundleDependency = null;

				for (BundleDependency dep : bundleDependencies) {
					if (dep.getExportingBundle().equals(dependantBundle)
							&& dep instanceof ServiceReferenceBundleDependency) {
						bundleDependency = (ServiceReferenceBundleDependency) dep;
						break;
					}
				}

				if (bundleDependency == null) {
					bundleDependency = new ServiceReferenceBundleDependency(dependantBundle, bundle);
					bundleDependencies.add(bundleDependency);
					dependencies.add(bundleDependency);
				}
				contentResult.addOutgoingDependency(degree, dependantBundle);

				bundleDependency.addServiceReferece(pe);
				dependentBundles.add(dependantBundle);
			}
		}
		if (outgoingDependencyDegree > degree) {
			return dependentBundles;
		}
		return Collections.emptySet();
	}

}
