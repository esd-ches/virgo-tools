/*******************************************************************************
 * Copyright (c) 2009, 2010 SpringSource, a divison of VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource, a division of VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.virgo.ide.jdt.internal.core.classpath;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.virgo.ide.facet.core.FacetUtils;
import org.eclipse.virgo.ide.jdt.core.JdtCorePlugin;
import org.eclipse.virgo.ide.jdt.internal.core.util.ClasspathUtils;
import org.eclipse.virgo.ide.jdt.internal.core.util.MarkerUtils;
import org.eclipse.virgo.ide.manifest.core.BundleManifestCorePlugin;
import org.eclipse.virgo.ide.manifest.core.BundleManifestUtils;
import org.eclipse.virgo.ide.manifest.core.IBundleManifestManager;
import org.eclipse.virgo.ide.manifest.core.IBundleManifestMangerWorkingCopy;
import org.eclipse.virgo.ide.manifest.core.dependencies.IDependencyLocator;
import org.eclipse.virgo.ide.par.Bundle;
import org.eclipse.virgo.ide.par.Par;
import org.eclipse.virgo.ide.runtime.core.ServerUtils;
import org.eclipse.virgo.kernel.osgi.provisioning.tools.DependencyLocationException;
import org.eclipse.virgo.kernel.osgi.provisioning.tools.DependencyLocator;
import org.eclipse.wst.server.core.IRuntime;
import org.springframework.ide.eclipse.core.java.JdtUtils;

import org.eclipse.virgo.util.osgi.manifest.BundleManifest;

/**
 * {@link IClasspathContainer} that installs the resolved dependencies taken from a {@link IJavaProject}'s bundle
 * manifest.
 * <p>
 * This implementation creates very rigorous accessibility rules on every {@link IClasspathEntry} that it creates. Those
 * rules match the OSGi runtime environment and therefore mirror the runtime class path in the SpringSource AP.
 * @author Christian Dupuis
 * @since 1.0.0
 */
public class ServerClasspathContainer implements IClasspathContainer {

	/**
	 * Key of the {@link IClasspathAttribute} that indicates that a certain {@link IClasspathEntry} has been created by
	 * this container
	 */
	public static final String CLASSPATH_ATTRIBUTE_VALUE = JdtCorePlugin.PLUGIN_ID + ".CLASSPATH_ENTRY";

	public static final String MANIFEST_TIMESTAMP = "MANIFEST_TIMESTAMP";

	/** Name of this class path container to be stored by JDT */
	private static final String CLASSPATH_CONTAINER = JdtCorePlugin.PLUGIN_ID + ".MANIFEST_CLASSPATH_CONTAINER";

	/** Classpath container */
	public static final String CLASSPATH_CONTAINER_DESCRIPTION = "Bundle Dependencies";

	/** Unique path of this class path container */
	public static final IPath CLASSPATH_CONTAINER_PATH = new Path(CLASSPATH_CONTAINER);

	/**
	 * Key of the {@link IClasspathAttribute} that indicates that a certain {@link IClasspathEntry} has been created by
	 * this container and is a test dependency
	 */
	public static final String TEST_CLASSPATH_ATTRIBUTE_VALUE = JdtCorePlugin.PLUGIN_ID + ".TEST_CLASSPATH_ENTRY";

	/**
	 * {@link IClasspathAttribute} to prevent WTP warning of non exportable container
	 */
	public static final IClasspathAttribute[] CLASSPATH_CONTAINER_ATTRIBUTE = new IClasspathAttribute[] { JavaCore
			.newClasspathAttribute("org.eclipse.jst.component.nondependency", "") };

	/** Internal cache of {@link IAccessRule}s keyed by a {@link IPath} */
	private static Map<IPath, IAccessRule> accessibleRules = new ConcurrentHashMap<IPath, IAccessRule>();

	/**
	 * {@link IClasspathAttribute} that is installed on a {@link IClasspathEntry} to indicate that a certain entry has
	 * been created by this class path container
	 */
	private static final IClasspathAttribute[] CLASSPATH_ATTRIBUTES = new IClasspathAttribute[] { JavaCore
			.newClasspathAttribute(CLASSPATH_ATTRIBUTE_VALUE, "true") };

	/** Wildcard string used to append to a package */
	private static final String PACKAGE_WILDCARD = "/*";

	/**
	 * {@link IClasspathAttribute} that is installed on a {@link IClasspathEntry} to indicate that a certain entry has
	 * been created by this class path container and is a test dependency
	 */
	private static final IClasspathAttribute[] TEST_CLASSPATH_ATTRIBUTES = new IClasspathAttribute[] { JavaCore
			.newClasspathAttribute(TEST_CLASSPATH_ATTRIBUTE_VALUE, "true") };

	/** Wildcard string indicating the entire packages in a certain {@link IClasspathEntry} */
	private static final String WILDCARD_PATH = "**/*";

	/** {@link IAccessRule} that enables access to any package in a {@link IClasspathEntry}. */
	private static final IAccessRule WILDCARD_ACCESSIBLE_RULE = JavaCore.newAccessRule(new Path(WILDCARD_PATH),
			IAccessRule.K_ACCESSIBLE);

	/**
	 * {@link IAccessRule} that disables access to any package in a {@link IClasspathEntry}. This rule will be ignored
	 * for any package that specifies {@link IAccessRule#K_ACCESSIBLE}, even for the same package (due to
	 * {@link IAccessRule#IGNORE_IF_BETTER}.
	 */
	private static final IAccessRule WILDCARD_NON_ACCESSIBLE_RULE = JavaCore.newAccessRule(new Path(WILDCARD_PATH),
			IAccessRule.K_NON_ACCESSIBLE | IAccessRule.IGNORE_IF_BETTER);

	/**
	 * Looks up and returns a {@link IAccessRule} for the given <code>path</code>.
	 * @param path the path to look up or create the access rule for
	 * @return a {@link IAccessRule} that allows to access the given <code>path</code>
	 */
	private static IAccessRule getAccessibleRule(IPath path) {
		if (!accessibleRules.containsKey(path)) {
			accessibleRules.put(path, JavaCore.newAccessRule(path, IAccessRule.K_ACCESSIBLE));
		}
		return accessibleRules.get(path);
	}

	/** The calculated and stored {@link IClasspathEntry}s */
	private IClasspathEntry[] entries;

	/** The internal flag to indicate the container has been initialized */
	private volatile boolean initialized = false;

	/** The {@link IJavaProject} this class path container instance is responsible for */
	private IJavaProject javaProject;

	/**
	 * Temporal storage for manifest locations to {@link IJavaProject}s. This is used to resolve inter-workspace
	 * dependencies
	 */
	private Map<String, IJavaProject> manifestLocationsByProject;

	/** The set of server runtimes that are used to resolve the dependencies */
	private IRuntime[] serverRuntimes;

	/**
	 * Constructor to create a new class path container
	 * @param javaProject the {@link IJavaProject} that this container is responsible for
	 */
	public ServerClasspathContainer(IJavaProject javaProject) {
		this.javaProject = javaProject;
		this.entries = new IClasspathEntry[0];
	}

	/**
	 * Constructor to create a new class path container
	 * @param javaProject the {@link IJavaProject} that this container is responsible for
	 * @param entries populate the list of {@link IClasspathEntry}s with the given list
	 */
	public ServerClasspathContainer(IJavaProject javaProject, IClasspathEntry[] entries) {
		this.javaProject = javaProject;
		this.entries = entries;
		this.initialized = true;

		// Store targeted runtimes to display in the description
		serverRuntimes = ServerUtils.getTargettedRuntimes(javaProject.getProject());
	}

	/**
	 * Returns the {@link IClasspathEntry}s calculated by this class path container
	 */
	public synchronized IClasspathEntry[] getClasspathEntries() {
		// make sure that the container is initialized on first access
		if (initialized) {
			return this.entries;
		}
		// refresh container before giving out the empty entries list
		refreshClasspathEntries();
		return this.entries;
	}

	/**
	 * Returns the description for this class path container
	 */
	public String getDescription() {
		return CLASSPATH_CONTAINER_DESCRIPTION;
	}

	/**
	 * Returns the {@link IRuntime} that is project is targeted against
	 * @return the serverRuntimes
	 */
	public String getDescriptionSuffix() {
		StringBuilder builder = new StringBuilder();
		if (serverRuntimes != null && serverRuntimes.length > 0) {
			builder.append(" [");
			for (int i = 0; i < serverRuntimes.length; i++) {
				if (serverRuntimes[i] != null) {
					builder.append(serverRuntimes[i].getName());
					if ((i + 1) < serverRuntimes.length) {
						builder.append(", ");
					}
				}
			}
			builder.append("]");
		}
		return builder.toString();
	}

	/**
	 * Returns the kind of this class path container
	 */
	public int getKind() {
		return K_APPLICATION;
	}

	/**
	 * Returns the path of the class path container
	 */
	public IPath getPath() {
		return CLASSPATH_CONTAINER_PATH;
	}

	/**
	 * Refresh the class path entries of the given {@link IJavaProject}.
	 * <p>
	 * This will install the new class path entries on the java project only if the entries have changed since the last
	 * refresh.
	 */
	public void refreshClasspathEntries() {

		this.manifestLocationsByProject = new HashMap<String, IJavaProject>();

		List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();
		IDependencyLocator locator = null;
		try {
			BundleManifest manifest = BundleManifestCorePlugin.getBundleManifestManager()
					.getBundleManifest(javaProject);
			BundleManifest testManifest = BundleManifestCorePlugin.getBundleManifestManager().getTestBundleManifest(
					javaProject);

			if (manifest != null) {
				// Create DependencyLocator
				locator = createDependencyLocator(javaProject);

				// Resolve dependencies for the main manifest
				resolveDependencies(entries, manifest, locator, false);

				// Resolve dependencies for the test manifest
				if (testManifest != null) {
					resolveDependencies(entries, testManifest, locator, true);
				}
			}
		}
		catch (Throwable e) {
			JdtCorePlugin.log(e);
		}
		finally {
			// Shutdown DependencyLocator
			if (locator != null) {
				locator.shutdown();
			}

			// Sort entries alphabetically sort better support the user
			Collections.sort(entries, new Comparator<IClasspathEntry>() {
				public int compare(IClasspathEntry entry1, IClasspathEntry entry2) {
					String path1 = entry1.getPath().lastSegment();
					String path2 = entry2.getPath().lastSegment();
					return path1.compareTo(path2);
				}
			});

			this.entries = entries.toArray(new IClasspathEntry[entries.size()]);
			this.manifestLocationsByProject = null;
			this.initialized = true;

			// Save class path entries to file
			ServerClasspathUtils.persistClasspathEntries(javaProject, this.entries);

			saveTimestamp(BundleManifestUtils.locateManifest(javaProject, false));
			saveTimestamp(BundleManifestUtils.locateManifest(javaProject, true));
		}

	}

	/**
	 * Saves the last modified timestamp to the file resource.
	 */
	private void saveTimestamp(IFile manifestFile) {
		if (manifestFile != null && manifestFile.exists()) {
			try {
				manifestFile.setPersistentProperty(new QualifiedName(JdtCorePlugin.PLUGIN_ID, MANIFEST_TIMESTAMP), Long
						.toString(manifestFile.getLocalTimeStamp()));
			}
			catch (CoreException e) {
				JdtCorePlugin.log(e);
			}
		}
	}

	/**
	 * Create and add the classpath entries for the given {@link BundleManifest}.
	 * @param manifest the given {@link BundleManifest} to add classpath entries for
	 * @param testManifest <code>true</code> if the manifest is the TEST.MF
	 */
	private void addClasspathEntriesFromBundleClassPath(List<IClasspathEntry> entries, BundleManifest manifest,
			boolean testManifest) {
		List<String> bundleClassPathEntries = manifest.getBundleClasspath();
		for (String bundleClassPathEntry : bundleClassPathEntries) {
			IResource resource = javaProject.getProject().findMember(bundleClassPathEntry.trim());
			if (!".".equals(bundleClassPathEntry.trim()) && resource != null) {
				IPath bundleClassPathEntryPath = resource.getRawLocation();
				if (bundleClassPathEntryPath != null) {
					File bundleClassPathEntryFile = bundleClassPathEntryPath.toFile();
					if (bundleClassPathEntryFile != null && bundleClassPathEntryFile.exists()) {
						createClasspathEntryForFile(entries, bundleClassPathEntryFile, testManifest,
								WILDCARD_ACCESSIBLE_RULE);
					}
				}
			}
		}
	}

	/**
	 * Creates and adds {@link IClasspathEntry}s to the given set.
	 * @param entries set to add the newly create {@link IClasspathEntry}s to
	 * @param dependencies the resolved dependencies keyed by {@link File}
	 */
	// TODO CD merge if classpath entry already exists when resolving the test manifest
	private void addClasspathEntriesFromResolutionResult(List<IClasspathEntry> entries,
			Map<File, List<String>> dependencies, boolean testManifest) {
		Set<String> resolvedPackageImports = new LinkedHashSet<String>();

		for (Map.Entry<File, List<String>> entry : dependencies.entrySet()) {
			File file = entry.getKey();
			if (file != null) {
				Set<IAccessRule> allowedRules = createAccessRulesFromPackageImports(entry.getValue());
				if (file.isDirectory()) {
					// Adjust file name to eliminate cross platform problems
					String fileName = file.toString().replace("\\", "/");
					if (manifestLocationsByProject.containsKey(fileName)) {
						createClasspathForProject(entries, fileName, testManifest, allowedRules
								.toArray(new IAccessRule[allowedRules.size()]));
					}
				}
				else {
					createClasspathEntryForFile(entries, file, testManifest, allowedRules
							.toArray(new IAccessRule[allowedRules.size()]));
				}
			}
			if (!testManifest) {
				// Store all resolved packages
				resolvedPackageImports.addAll(entry.getValue());
			}
		}

		if (!testManifest) {
			// Store the resolved package imports back into the model
			IBundleManifestManager bundleManifestManager = BundleManifestCorePlugin.getBundleManifestManager();
			if (bundleManifestManager instanceof IBundleManifestMangerWorkingCopy) {
				((IBundleManifestMangerWorkingCopy) bundleManifestManager).updateResolvedPackageImports(javaProject,
						resolvedPackageImports);
			}
		}
	}

	/**
	 * Adds the given project as a workspace project to the list
	 * @param workspaceBundles the already existing workspace bundles
	 * @param project the bundle project to add
	 */
	private void addWorkspaceBundle(Set<String> workspaceBundles, IProject project) {
		if (project.isAccessible() && FacetUtils.isBundleProject(project)) {
			String manifestFolder = BundleManifestUtils.locateManifestFolder(JavaCore.create(project));
			if (manifestFolder != null) {
				workspaceBundles.add(manifestFolder);
				manifestLocationsByProject.put(manifestFolder, JavaCore.create(project));
			}
		}
	}

	/**
	 * Creates {@link IAccessRule}s for the given list of imported packages.
	 * <p>
	 * Only those packages from the {@link IClasspathEntry} that are imported will be visible.
	 * @param packageImports the resolved and explicit package imports
	 * @return the set of {@link IAccessRule}s
	 */
	private Set<IAccessRule> createAccessRulesFromPackageImports(List<String> packageImports) {
		Set<IAccessRule> allowedRules = new LinkedHashSet<IAccessRule>();
		for (String packageImport : packageImports) {
			allowedRules.add(getAccessibleRule(new Path(packageImport.replace('.', '/') + PACKAGE_WILDCARD)));
		}
		allowedRules.add(WILDCARD_NON_ACCESSIBLE_RULE);
		return allowedRules;
	}

	/**
	 * Creates a single {@link IClasspathEntry} for the given <code>file</code> and <code>allowedRules</code>.
	 * @param entries the list of {@link IClasspathEntry}
	 * @param file the {@link File} representing a JAR file
	 * @param allowedRules the set if {@link IAccessRule}s indicating package export restrictions
	 */
	private void createClasspathEntryForFile(List<IClasspathEntry> entries, File file, boolean testManifest,
			IAccessRule... allowedRules) {
		IPath path = new Path(file.getAbsolutePath());
		allowedRules = mergeAccessRules(entries, path, allowedRules);

		if (testManifest) {
			entries.add(JavaCore.newLibraryEntry(new Path(file.getAbsolutePath()), getSourceAttachmentPath(file), null,
					allowedRules, TEST_CLASSPATH_ATTRIBUTES, false));
		}
		else {
			entries.add(JavaCore.newLibraryEntry(new Path(file.getAbsolutePath()), getSourceAttachmentPath(file), null,
					allowedRules, CLASSPATH_ATTRIBUTES, false));
		}

	}

	/**
	 * Creates a single {@link IClasspathEntry} for the given <code>file</code> and <code>allowedRules</code>.
	 * <p>
	 * The <code>file</code> actually points to the workspace source folder and therefore this method creates a project
	 * reference in contrast to a JAR reference.
	 * @param entries the list of {@link IClasspathEntry}
	 * @param fileName the file name representing a source folder
	 * @param allowedRules the set if {@link IAccessRule}s indicating package export restrictions
	 */
	private void createClasspathForProject(List<IClasspathEntry> entries, String fileName, boolean testManifest,
			IAccessRule... allowedRules) {
		IJavaProject referencedProject = manifestLocationsByProject.get(fileName);
		if (referencedProject == null || javaProject.equals(referencedProject)) {
			return;
		}

		IPath path = manifestLocationsByProject.get(fileName).getPath();
		allowedRules = mergeAccessRules(entries, path, allowedRules);

		if (testManifest) {
			entries.add(JavaCore.newProjectEntry(path, allowedRules, false, TEST_CLASSPATH_ATTRIBUTES, false));
		}
		else {
			entries.add(JavaCore.newProjectEntry(path, allowedRules, false, CLASSPATH_ATTRIBUTES, false));
		}
	}

	private IAccessRule[] mergeAccessRules(List<IClasspathEntry> entries, IPath path, IAccessRule... allowedRules) {
		IClasspathEntry entry = null;
		// Check if the path is already in and merge if so
		for (IClasspathEntry existingEntry : entries) {
			if (existingEntry.getPath().equals(path)) {
				Set<IAccessRule> existingRules = new TreeSet<IAccessRule>(new Comparator<IAccessRule>() {

					public int compare(IAccessRule o1, IAccessRule o2) {
						if (o1.getKind() == o2.getKind()) {
							return o1.getPattern().toString().compareTo(o2.getPattern().toString());
						}
						else if (o1.getKind() == IAccessRule.K_NON_ACCESSIBLE) {
							return 1;
						}
						else if (o2.getKind() == IAccessRule.K_NON_ACCESSIBLE) {
							return -1;
						}
						else if (o1.getKind() == IAccessRule.K_ACCESSIBLE) {
							return 1;
						}
						else if (o2.getKind() == IAccessRule.K_ACCESSIBLE) {
							return -1;
						}
						return 0;
					}
				});

				existingRules.addAll(Arrays.asList(existingEntry.getAccessRules()));
				existingRules.addAll(Arrays.asList(allowedRules));
				allowedRules = existingRules.toArray(new IAccessRule[existingRules.size()]);
				entry = existingEntry;
				break;
			}
		}

		if (entry != null) {
			entries.remove(entry);
		}
		return allowedRules;
	}

	/**
	 * Creates the {@link DependencyLocator} to be used for resolution.
	 * @param javaProject the {@link IJavaProject} to resolve dependencies for.
	 * @return a configured and ready to use {@link DependencyLocator}
	 * @throws IOException
	 */
	private IDependencyLocator createDependencyLocator(IJavaProject javaProject) throws CoreException, IOException {
		final Set<String> workspaceBundles = new LinkedHashSet<String>();

		// First add projects that belong to the same par project
		for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			Set<String> parBundles = new HashSet<String>();
			if (FacetUtils.isParProject(project)) {
				boolean hasBundle = false;
				Par par = FacetUtils.getParDefinition(project);
				if (par != null && par.getBundle() != null) {
					for (Bundle bundle : par.getBundle()) {
						if (bundle.getSymbolicName().equals(javaProject.getElementName())) {
							hasBundle = true;
						}
						parBundles.add(bundle.getSymbolicName());
					}
				}

				if (hasBundle) {
					for (String bundleName : parBundles) {
						IProject bundleProject = ResourcesPlugin.getWorkspace().getRoot().getProject(bundleName);
						addWorkspaceBundle(workspaceBundles, bundleProject);
					}

					// Add any nested or linked jars from the PAR
					project.accept(new IResourceVisitor() {

						public boolean visit(IResource resource) throws CoreException {
							if (resource instanceof IFile && resource.getFileExtension().equals("jar")) {
								IPath jarLocation = resource.getRawLocation();
								IPath resolvedJarLocation = JavaCore.getResolvedVariablePath(jarLocation);
								if (resolvedJarLocation != null) {
									workspaceBundles.add(resolvedJarLocation.removeLastSegments(1).toString()
											+ File.separator + "{bundle}");
								}
								else {
									workspaceBundles.add(jarLocation.removeLastSegments(1).toString() + File.separator
											+ "{bundle}");
								}
							}
							return true;
						}
					}, IResource.DEPTH_ONE, false);

				}
			}
		}

		// Secondly add all explicit dependent projects
		for (IProject project : javaProject.getProject().getDescription().getReferencedProjects()) {
			addWorkspaceBundle(workspaceBundles, project);
		}

		// Thirdly add the current plugin to resolve exported packages from the same bundle
		addWorkspaceBundle(workspaceBundles, javaProject.getProject());

		// Store targeted runtimes to display in the description
		serverRuntimes = ServerUtils.getTargettedRuntimes(javaProject.getProject());

		// Adjust the last modified date on the META-INF and root folder
		ClasspathUtils.adjustLastModifiedDate(javaProject, false);
		ClasspathUtils.adjustLastModifiedDate(javaProject, true);

		// Create DependencyLocator with path to server.config and server.profile
		return ServerUtils.createDependencyLocator(javaProject.getProject(), workspaceBundles
				.toArray(new String[workspaceBundles.size()]));
	}

	/**
	 * Returns a path to the source attachment following the BRITS conventions if the sources jar can be find.
	 * <p>
	 * First checks if the user has overridden the convention and attached a custom archive.
	 * @param file the JAR file to
	 * @return the source JAR path
	 */
	private IPath getSourceAttachmentPath(File file) {
		// first check manual configured source attachments
		IPath sourceAttachmentPath = ClasspathUtils.getSourceAttachment(javaProject, file);
		if (sourceAttachmentPath != null) {
			return sourceAttachmentPath;
		}

		// secondly check for source attachment following the conventions
		File sourceFile = ServerUtils.getSourceFile(file.toURI());
		if (sourceFile != null && sourceFile.exists() && sourceFile.canRead()) {
			sourceAttachmentPath = new Path(sourceFile.getAbsolutePath());
		}

		return sourceAttachmentPath;
	}

	/**
	 * Creates error markers for unresolved dependencies stored in the {@link DependencyLocationException}.
	 * @param e a {@link DependencyLocationException} occurred during resolution
	 */
	private void handleDependencyLocationException(DependencyLocationException e, boolean testManifest) {
		MarkerUtils.createErrorMarkers(e, javaProject, testManifest);
	}

	/**
	 * Resolve the dependencies of the given {@link BundleManifest}.
	 * @param entries the already collected {@link IClasspathEntry}
	 * @param manifest the {@link BundleManifest} to add dependencies for
	 * @param locator the {@link DependencyLocator} instance to use
	 * @param testManifest <code>true</code>
	 */
	private void resolveDependencies(List<IClasspathEntry> entries, BundleManifest manifest,
			IDependencyLocator locator, boolean testManifest) {
		DependencyLocationException dependencyLocationException = null;
		if (locator != null) {
			try {
				// Resolve dependencies for the main manifest
				addClasspathEntriesFromResolutionResult(entries, locator.locateDependencies(manifest), testManifest);

				// Add classpath entries that are configured in the bundle manifest
				addClasspathEntriesFromBundleClassPath(entries, manifest, testManifest);
			}
			catch (DependencyLocationException e) {

				// Store for later removal of error markers
				dependencyLocationException = e;

				// Install the resolved dependencies as a safe fallback
				addClasspathEntriesFromResolutionResult(entries, e.getSatisfiedDependencies(), testManifest);

				// Add classpath entries that are configured in the bundle manifest
				addClasspathEntriesFromBundleClassPath(entries, manifest, testManifest);

			}
			finally {
				// Create error markers for un-resolved dependencies
				handleDependencyLocationException(dependencyLocationException, testManifest);
			}
		}
	}

}
