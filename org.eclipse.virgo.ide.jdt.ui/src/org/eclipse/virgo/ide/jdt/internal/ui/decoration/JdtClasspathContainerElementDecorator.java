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
package org.eclipse.virgo.ide.jdt.internal.ui.decoration;

import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.packageview.ClassPathContainer;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.virgo.ide.facet.core.FacetUtils;
import org.eclipse.virgo.ide.jdt.internal.core.classpath.ServerClasspathContainer;
import org.eclipse.virgo.ide.manifest.core.BundleManifestCorePlugin;

/**
 * {@link ILightweightLabelDecorator} that decorates non-accessible packages in the class path container.
 * 
 * @author Christian Dupuis
 * @since 1.0.0
 */
@SuppressWarnings("restriction")
public class JdtClasspathContainerElementDecorator extends LabelProvider implements ILightweightLabelDecorator {

	/** A JDT model listener that gets notified if a class path changed */
	private IElementChangedListener changeListener = null;

	public JdtClasspathContainerElementDecorator() {

		// {@link IElementChangedListener} JDT listener that detects changes to the project's class
		// path and triggers a decorator refresh.
		changeListener = new IElementChangedListener() {

			/**
			 * Flag indicating a change in the resolved class path. This entry is copied from Eclipse 3.4 in order to
			 * make this compatible with 3.3.
			 */
			private static final int F_RESOLVED_CLASSPATH_CHANGED = 0x200000;

			public void elementChanged(ElementChangedEvent event) {
				boolean refresh = false;
				for (IJavaElementDelta delta : event.getDelta().getAffectedChildren()) {
					if ((delta.getFlags() & F_RESOLVED_CLASSPATH_CHANGED) != 0
							|| (delta.getFlags() & IJavaElementDelta.F_CLASSPATH_CHANGED) != 0) {
						refresh = true;
					}
				}
				if (refresh) {
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							fireLabelProviderChanged(new LabelProviderChangedEvent(
									JdtClasspathContainerElementDecorator.this));
						}
					});
				}
			}
		};
		JavaCore.addElementChangedListener(changeListener, ElementChangedEvent.POST_CHANGE);
	}

	/**
	 * Decorates the given <code>element</code>.
	 */
	public void decorate(Object element, IDecoration decoration) {
		// package fragments are the first elements below the JAR file
		if (element instanceof IPackageFragment) {
			IPackageFragment packageFragment = (IPackageFragment) element;
			if (shouldDecorateImportedPackageFragment(packageFragment)) {
				// make light gray and lock icon decoration
				decoration.setForegroundColor(ColorMap.GRAY_LIGHT);
				decoration.addOverlay(JdtUiImages.DESC_OVR_LOCKED, IDecoration.TOP_LEFT);
			} else if (shouldDecorateExportedPackageFragment(packageFragment)) {
				decoration.addOverlay(JdtUiImages.DESC_OVR_EXPORTED, IDecoration.TOP_RIGHT);
			}
		}
		// jar package fragments roots; decorate those that come from a test dependency
		else if (element instanceof IPackageFragmentRoot) {
			IPackageFragmentRoot root = (IPackageFragmentRoot) element;
			try {
				if (ServerClasspathContainer.CLASSPATH_CONTAINER_PATH.equals(root.getRawClasspathEntry().getPath())
						&& root.getJavaProject().getProject().isAccessible() && root.getJavaProject().isOpen()) {
					ServerClasspathContainer cpContainer = (ServerClasspathContainer) JavaCore.getClasspathContainer(
							ServerClasspathContainer.CLASSPATH_CONTAINER_PATH, root.getJavaProject());
					if (cpContainer != null) {
						for (IClasspathEntry entry : cpContainer.getClasspathEntries()) {
							if (entry.getPath().equals(root.getPath()) && entry.getExtraAttributes() != null) {
								for (IClasspathAttribute attribute : entry.getExtraAttributes()) {
									if (attribute.getName().equals(
											ServerClasspathContainer.TEST_CLASSPATH_ATTRIBUTE_VALUE)) {
										decoration.setForegroundColor(ColorMap.GRAY_LIGHT);
										decoration.addOverlay(JdtUiImages.DESC_OVR_LOCKED, IDecoration.TOP_LEFT);
									}
								}
								break;
							}
						}
					}

				}
			} catch (JavaModelException e) {
			}
		}
		// class files represent a single type file in a JAR
		else if (element instanceof IClassFile) {
			IClassFile classFile = (IClassFile) element;
			if (classFile.getParent() instanceof IPackageFragment) {
				if (shouldDecorateImportedPackageFragment((IPackageFragment) classFile.getParent())) {
					// make light gray
					decoration.setForegroundColor(ColorMap.GRAY_LIGHT);
				}
			}
		}
		// decorate the class path container and add the originating target runtime
		else if (element instanceof ClassPathContainer) {
			ClassPathContainer container = (ClassPathContainer) element;
			if (container.getClasspathEntry().getPath().equals(ServerClasspathContainer.CLASSPATH_CONTAINER_PATH)) {
				try {
					if (container.getJavaProject().getProject().isAccessible() && container.getJavaProject().isOpen()) {
						ServerClasspathContainer cpContainer = (ServerClasspathContainer) JavaCore.getClasspathContainer(
								ServerClasspathContainer.CLASSPATH_CONTAINER_PATH, container.getJavaProject());
						decoration.addSuffix(cpContainer.getDescriptionSuffix());
					}
				} catch (JavaModelException e) {
				}
			}
		}
	}

	/**
	 * Checks if a given {@link IPackageFragment} is in the list of exported packages for the current
	 * {@link IJavaProject}.
	 * 
	 * @return true if the {@link IPackageFragment} is in the list of exported packages
	 */
	private boolean shouldDecorateExportedPackageFragment(IPackageFragment packageFragment) {
		IJavaProject lavaProject = packageFragment.getJavaProject();
		return FacetUtils.isBundleProject(lavaProject.getProject())
				&& BundleManifestCorePlugin.getBundleManifestManager()
						.getPackageExports(lavaProject)
						.contains(packageFragment.getElementName());
	}

	/**
	 * Checks if a given {@link IPackageFragment} is in the list of resolved imports for the current
	 * {@link IJavaProject}.
	 * 
	 * @return true if the {@link IPackageFragment} is not accessible from the java project
	 */
	private boolean shouldDecorateImportedPackageFragment(IPackageFragment packageFragment) {
		IPackageFragmentRoot root = (IPackageFragmentRoot) packageFragment.getParent();
		IJavaProject javaProject = packageFragment.getJavaProject();

		if (!(javaProject.getProject().isAccessible() && javaProject.isOpen())) {
			return false;
		}

		// Only decorate in bundle projects
		if (!FacetUtils.isBundleProject(javaProject.getProject())) {
			return false;
		}

		try {
			IClasspathEntry entry = root.getRawClasspathEntry();
			if (entry.getPath().equals(ServerClasspathContainer.CLASSPATH_CONTAINER_PATH)) {
				if (!BundleManifestCorePlugin.getBundleManifestManager()
						.getResolvedPackageImports(root.getJavaProject())
						.contains(packageFragment.getElementName())) {
					return true;
				}
			}
		} catch (JavaModelException e) {
		}
		return false;
	}

	public void addListener(ILabelProviderListener listener) {
		// Don't care
	}

	public void dispose() {
		ColorMap.dispose();
		JavaCore.removeElementChangedListener(changeListener);
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
		// Don't care
	}

	/**
	 * Internal class used for creating {@link Color} instances.
	 */
	private static class ColorMap {

		public static final Color GRAY_LIGHT = new Color(Display.getDefault(), 145, 145, 145);

		public static void dispose() {
			GRAY_LIGHT.dispose();
		}

	}

}
