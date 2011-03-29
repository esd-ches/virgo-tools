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
package org.eclipse.virgo.ide.runtime.internal.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jst.server.core.RuntimeClasspathProviderDelegate;
import org.eclipse.wst.server.core.IRuntime;

/**
 * {@link RuntimeClasspathProviderDelegate} for the S2AP that provides Tomcat jars to WTP Dynamic
 * Web Projects.
 * @author Christian Dupuis
 * @since 1.0.0
 */
public class ServerRuntimeClasspathProvider extends RuntimeClasspathProviderDelegate {

	/** Empty classpath */
	private static final IClasspathEntry[] EMPTY_CLASSPATH = new IClasspathEntry[0];

	/** List of jars for S2AP 1.0 that need to be added to the classpath container */
	private static final List<String> REQUIRED_LIBS_10 = Arrays.asList(new String[] {
			"com.springsource.javax.annotation-", "com.springsource.javax.el-",
			"com.springsource.javax.servlet-", "com.springsource.javax.servlet.jsp-",
			"com.springsource.javax.servlet.jsp.jstl-" });

	/**
	 * {@inheritDoc}
	 */
	public IClasspathEntry[] resolveClasspathContainer(IProject project, IRuntime runtime) {
		IPath installPath = runtime.getLocation();

		// Verify that install location is there
		if (installPath == null) {
			return EMPTY_CLASSPATH;
		}

		// Check that only Standard WARs use this mechanism
//		if (FacetUtils.isBundleProject(project)
//				|| !FacetUtils.hasProjectFacet(project, FacetCorePlugin.WEB_FACET_ID)) {
//			return EMPTY_CLASSPATH;
//		}

		List<IClasspathEntry> classpathEntries = new ArrayList<IClasspathEntry>();
		String runtimeId = runtime.getRuntimeType().getId();
		if (runtimeId.indexOf("10") > 0) {
			// Install bundles from ext dir as jars on the classpath
			IPath path = installPath.append("repository/bundles/ext");
			addJarFiles(path.toFile(), classpathEntries, REQUIRED_LIBS_10);
		}
		return classpathEntries.toArray(new IClasspathEntry[classpathEntries.size()]);
	}

	/**
	 * Adds jars files form the given <code>dir</code> to the list of {@link IClasspathEntry}s.
	 * <p>
	 * Only those jars are added that match names in the list of <code>requiredLibs</code>.
	 */
	private void addJarFiles(File dir, List<IClasspathEntry> classpathentries,
			List<String> requiredLibs) {
		File[] files = dir.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.getAbsolutePath().endsWith(".jar")
						|| file.getAbsolutePath().endsWith(".zip")) {

					for (String requiredLib : requiredLibs) {
						if (file.getName().startsWith(requiredLib)) {
							IPath path = new Path(file.getAbsolutePath());
							classpathentries.add(JavaCore.newLibraryEntry(path, null, null));
							continue;
						}
					}
				}
			}
		}
	}
}
