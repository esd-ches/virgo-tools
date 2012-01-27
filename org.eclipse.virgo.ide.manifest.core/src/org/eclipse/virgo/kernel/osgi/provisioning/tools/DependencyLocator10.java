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
package org.eclipse.virgo.kernel.osgi.provisioning.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.virgo.ide.manifest.core.BundleManifestCorePlugin;
import org.eclipse.virgo.ide.manifest.core.dependencies.IDependencyLocator;
import org.eclipse.virgo.kernel.artifact.bundle.BundleBridge;
import org.eclipse.virgo.kernel.artifact.library.LibraryBridge;
import org.eclipse.virgo.kernel.repository.BundleDefinition;
import org.eclipse.virgo.kernel.repository.BundleRepository;
import org.eclipse.virgo.kernel.repository.LibraryDefinition;
import org.eclipse.virgo.kernel.repository.RepositoryBackedBundleRepository;
import org.eclipse.virgo.repository.ArtifactBridge;
import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.repository.HashGenerator;
import org.eclipse.virgo.repository.Repository;
import org.eclipse.virgo.repository.RepositoryCreationException;
import org.eclipse.virgo.repository.RepositoryFactory;
import org.eclipse.virgo.repository.builder.ArtifactDescriptorBuilder;
import org.eclipse.virgo.repository.configuration.ExternalStorageRepositoryConfiguration;
import org.eclipse.virgo.repository.configuration.RepositoryConfiguration;
import org.eclipse.virgo.util.osgi.VersionRange;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.util.osgi.manifest.BundleManifestFactory;
import org.eclipse.virgo.util.osgi.manifest.ExportedPackage;
import org.eclipse.virgo.util.osgi.manifest.ImportedBundle;
import org.eclipse.virgo.util.osgi.manifest.ImportedLibrary;
import org.eclipse.virgo.util.osgi.manifest.ImportedPackage;
import org.eclipse.virgo.util.osgi.manifest.RequiredBundle;
import org.eclipse.virgo.util.osgi.manifest.RequiredBundle.Visibility;
import org.eclipse.virgo.util.osgi.manifest.Resolution;
import org.eclipse.virgo.util.osgi.manifest.parse.DummyParserLogger;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderDeclaration;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderParserFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;

import com.springsource.json.parser.AntlrJSONParser;
import com.springsource.json.parser.JSONParser;
import com.springsource.json.parser.ListNode;
import com.springsource.json.parser.Node;
import com.springsource.json.parser.ScalarNode;
import com.springsource.json.parser.ScalarNodeType;

/**
 * A helper class for locating a bundle's dependencies.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * The class is <strong>thread-safe</strong>
 * 
 * @author Andy Wilkinson
 * @author Christian Dupuis
 * @author Steffen Pingel
 * @since 1.0
 */
public final class DependencyLocator10 implements IDependencyLocator {

	private static final String SYSYEM_PACKAGES_PROPERTY = "org.osgi.framework.system.packages";

	private static final String BUNDLE_VERSION_ATTRIBUTE = "bundle-version";

	private static final String VERSION_ATTRIBUTE = "version";

	private static final String VERSION_ALTERNATE_ATTRIBUTE = "specification-version";

	private final BundleRepository bundleRepository;

	private static final List<String> SEARCH_PATHS_PATH = Arrays.asList(new String[] { "provisioning", "searchPaths" });

	private static final String[] DEFAULT_SEARCH_PATHS = new String[] {
			"repository/bundles/subsystems/{name}/{bundle}.jar", "repository/bundles/ext/{bundle}",
			"repository/libraries/ext/{library}", "repository/bundles/usr/{bundle}",
			"repository/libraries/usr/{library}" };

	private static final String[] LIB_SEARCH_PATH = new String[] { "lib/{bundle}.jar" };

	private static final String PLATFORM_CONFIG_PATH = File.separatorChar + "config" + File.separatorChar
			+ "server.config";

	private static final Pattern PROPERTY_PATTERN = Pattern.compile("(\\$\\{(([^\\}]+))\\})");

	/**
	 * Creates a new <code>DependencyLocator</code> that will search for dependencies within the Server instance located
	 * at the supplied <code>serverHomePath</code>. To improve search performance, bundle and library locations are
	 * cached. The cache files will be written to the directory identified by the supplied
	 * <code>cacheDirectoryPath</code>.
	 * 
	 * @param serverHomePath The path to the server installation from within which dependencies are to be located
	 * @param cacheDirectoryPath The path of the directory to which cache files should be written
	 * @param javaVersion The version of Java to be used to determine the appropriate OSGi profile to use for defining
	 * system bundle exports
	 * 
	 * @throws IOException if a problem occurs loading and parsing the configuration of the Server instance.
	 */
	public DependencyLocator10(String serverHomePath, String cacheDirectoryPath, JavaVersion javaVersion)
			throws IOException {
		this(serverHomePath, null, cacheDirectoryPath, javaVersion);
	}

	/**
	 * Creates a new <code>DependencyLocator</code> that will search for dependencies within the Server instance located
	 * at the supplied <code>serverHomePath</code>. The supplied <code>additionalSearchPaths</code> will also be
	 * included in the search. Each search path is used to locate bundles and libraries. To improve search performance,
	 * bundle and library locations are cached. The cache files will be written to the directory identified by the
	 * supplied <code>cacheDirectoryPath</code>.
	 * 
	 * @param serverHomePath The path to the server installation from within which dependencies are to be located
	 * @param additionalSearchPaths The additional search paths to use to locate the bundles and libraries that can
	 * satisfy dependencies
	 * @param cacheDirectoryPath The path of the directory to which cache files should be written
	 * @param javaVersion The version of Java to be used to determine the appropriate OSGi profile to use for defining
	 * system bundle exports
	 * 
	 * @throws IOException if a problem occurs loading and parsing the configuration of the Server instance.
	 */
	public DependencyLocator10(String serverHomePath, String[] additionalSearchPaths, String cacheDirectoryPath,
			JavaVersion javaVersion) throws IOException {
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			serverHomePath = serverHomePath.replace('/', '\\');
			cacheDirectoryPath = cacheDirectoryPath.replace('/', '\\');
			for (int i = 0; i < additionalSearchPaths.length; i++) {
				additionalSearchPaths[i] = additionalSearchPaths[i].replace('/', '\\');
			}
		}
		this.bundleRepository = new SystemPackageFilteringCompositeBundleRepository(serverHomePath,
				additionalSearchPaths, cacheDirectoryPath, javaVersion);
	}

	private static String[] readSearchPathsFromConfig(String configPath) {
		if (configPath != null) {
			File configFile = new File(configPath);
			if (configFile.exists()) {
				JSONParser parser = new AntlrJSONParser();
				try {
					Node root = parser.parse(configFile.toURI().toURL());
					return readStringsFromListNode(root, SEARCH_PATHS_PATH, DEFAULT_SEARCH_PATHS);
				}
				catch (Exception e) {
					throw new RuntimeException("Config file '" + configPath + "' could not be parsed.", e);
				}
			}
			else {
				throw new IllegalArgumentException("Config file '" + configPath + "' does not exist.");
			}
		}
		return new String[0];
	}

	private static String[] readStringsFromListNode(Node root, List<String> pathToNode, String[] defaultValues) {
		if (root != null) {
			Node searchPathsNode = root.getNode(pathToNode);
			if (searchPathsNode != null) {
				if (searchPathsNode instanceof ListNode) {
					List<String> searchPaths = new ArrayList<String>();
					ListNode listNode = (ListNode) searchPathsNode;
					List<Node> nodes = listNode.getNodes();
					for (Node node : nodes) {
						if (node instanceof ScalarNode && ScalarNodeType.String.equals(((ScalarNode) node).getType())) {
							String path = ((ScalarNode) node).getValue();
							searchPaths.add(path);
						}
					}
					return searchPaths.toArray(new String[searchPaths.size()]);
				}
				else {
					throw new RuntimeException("The node at " + pathToNode + " must be a list node.");
				}
			}
			else {
				return defaultValues;
			}
		}
		else {
			return defaultValues;
		}
	}

	/**
	 * Locates all of the dependencies defined in the supplied manifest. Dependencies are identified from the manifest's
	 * <code>Import-Package</code>, <code>Import-Bundle</code> and <code>Import-Library</code> headers. The dependencies
	 * are returned in the form of a <code>Map</code>. Each key in the <code>Map</code> is a <code>File</code> which
	 * points to the location of a bundle that satisfies one or more of the manifest's dependencies. Each
	 * <code>List&lt;String&gt;</code> contains all of the packages within a particular bundle which are visible, i.e.
	 * they are imported by the supplied manifest. If a dependency is found to be satisfied by the system bundle a
	 * <code>null</code> key may be included in the returned <code>Map</code> if the location of the satisfying jar
	 * could not be determined, e.g. because the dependency is upon a standard JRE package.
	 * <p>
	 * Processing of the <code>Import-Package</code> header will, for each package, search for a bundle that exports the
	 * package at the specified version and include its location in the returned <code>Map</code> along with an entry in
	 * the associated <code>List</code> for the package. If no such bundle is found and the import does not have a
	 * resolution of optional it will be added to a list of those which cannot be satisfied and included in a
	 * <code>DependencyLocationException</code> thrown once processing is complete. If more than one such bundle is
	 * found they will all be added to the Map.
	 * <p>
	 * Processing of the <code>Import-Library</code> header will, for each library, search for a library with the
	 * required name and version. The location of each bundle that is in the library will be included in the returned
	 * <code>Map</code> along with entries in the associated <code>Lists</code> for every package that is exported from
	 * the library's bundles. If no library with the required name and version can be found, and the import does not
	 * have a resolution of optional, the import of the library will be added to a list of those which cannot be
	 * satisfied and included in a <code>DependencyLocationException</code> thrown once processing is complete.
	 * <p>
	 * If a single bundle satisfies more than one import its location will only be included once in the returned
	 * <code>Map</code>.
	 * <p>
	 * If the manifest has no dependencies an empty <code>Map</code> is returned.
	 * 
	 * @param manifest
	 * @return the locations of all of the given manifest's dependencies
	 * @throws DependencyLocationException if any of the manifest's dependencies cannot be located
	 */
	public Map<File, List<String>> locateDependencies(BundleManifest manifest) throws DependencyLocationException {

		List<ImportDescriptor> unsatisfiablePackageImports = new ArrayList<ImportDescriptor>();
		List<ImportDescriptor> unsatisfiableLibraryImports = new ArrayList<ImportDescriptor>();
		List<ImportDescriptor> unsatisfiableBundleImports = new ArrayList<ImportDescriptor>();
		List<ImportDescriptor> unsatisfiableRequireBundles = new ArrayList<ImportDescriptor>();

		Map<File, List<String>> dependencyLocations = new HashMap<File, List<String>>();

		processImportedPackages(manifest.getImportPackage().getImportedPackages(), dependencyLocations,
				unsatisfiablePackageImports);

		processImportedLibraries(manifest.getImportLibrary().getImportedLibraries(), dependencyLocations,
				unsatisfiableLibraryImports);

		processImportedBundles(manifest.getImportBundle().getImportedBundles(), dependencyLocations,
				unsatisfiableBundleImports);

		List<String> packageNames = createListOfAllPackagesThatHaveAlreadyBeenSatisfied(dependencyLocations);
		processRequiredBundles(manifest.getRequireBundle().getRequiredBundles(), dependencyLocations,
				unsatisfiableRequireBundles, packageNames, true);

		throwDependencyLocationExceptionIfNecessary(unsatisfiablePackageImports, unsatisfiableBundleImports,
				unsatisfiableLibraryImports, unsatisfiableRequireBundles, dependencyLocations);

		return dependencyLocations;
	}

	private static List<String> createListOfAllPackagesThatHaveAlreadyBeenSatisfied(
			Map<File, List<String>> dependencyLocations) {
		List<String> packagesThatHaveBeenSatisfied = new ArrayList<String>();
		for (Entry<File, List<String>> dependencyLocation : dependencyLocations.entrySet()) {
			packagesThatHaveBeenSatisfied.addAll(dependencyLocation.getValue());

		}
		return packagesThatHaveBeenSatisfied;
	}

	private void processImportedBundles(List<ImportedBundle> importedBundles,
			Map<File, List<String>> dependencyLocations, List<ImportDescriptor> unsatisfiableBundleImports) {
		for (ImportedBundle importedBundle : importedBundles) {
			processImportedBundle(importedBundle, dependencyLocations, unsatisfiableBundleImports);
		}
	}

	private void processImportedBundle(ImportedBundle importedBundle, Map<File, List<String>> dependencyLocations,
			List<ImportDescriptor> unsatisfiableBundleImports) {
		String symbolicName = importedBundle.getBundleSymbolicName();
		VersionRange bundleVersionRange = importedBundle.getVersion();
		BundleDefinition bundleDefinition = this.bundleRepository.findBySymbolicName(symbolicName, bundleVersionRange);
		if (bundleDefinition == null) {
			unsatisfiableBundleImports.add(new ImportDescriptor(symbolicName, bundleVersionRange.toString(),
					bundleVersionRange.toParseString()));
		}
		else {
			registerDependencyLocationAndPackageNameForEveryExportedPackage(dependencyLocations, bundleDefinition, null);
		}
	}

	private void processImportedLibraries(List<ImportedLibrary> importedLibraries,
			Map<File, List<String>> dependencyLocations, List<ImportDescriptor> unsatisfiableLibraryImports) {
		for (ImportedLibrary importedLibrary : importedLibraries) {
			String libraryName = importedLibrary.getLibrarySymbolicName();
			VersionRange versionRange = importedLibrary.getVersion();
			LibraryDefinition libraryDefinition = this.bundleRepository.findLibrary(libraryName, versionRange);

			if (libraryDefinition != null) {
				List<ImportedBundle> importedBundles = libraryDefinition.getLibraryBundles();

				for (ImportedBundle libraryBundle : importedBundles) {
					String symbolicName = libraryBundle.getBundleSymbolicName();
					VersionRange bundleVersionRange = libraryBundle.getVersion();

					BundleDefinition bundleDefinition = this.bundleRepository.findBySymbolicName(symbolicName,
							bundleVersionRange);
					if (bundleDefinition == null) {
						unsatisfiableLibraryImports.add(new ImportDescriptor(libraryName, versionRange.toString(),
								versionRange.toParseString()));
					}
					else {
						registerDependencyLocationAndPackageNameForEveryExportedPackage(dependencyLocations,
								bundleDefinition, null);
					}
				}
			}
			else if (Resolution.MANDATORY.equals(importedLibrary.getResolution())) {
				unsatisfiableLibraryImports.add(new ImportDescriptor(libraryName, versionRange.toString(), versionRange
						.toParseString()));
			}
		}
	}

	private void registerDependencyLocationAndPackageNameForEveryExportedPackage(
			Map<File, List<String>> dependencyLocations, BundleDefinition bundleDefinition,
			List<String> packagesThatHaveAlreadyBeenSatisfied) {
		BundleManifest manifest = bundleDefinition.getManifest();
		for (ExportedPackage exportedPackage : manifest.getExportPackage().getExportedPackages()) {
			if (packagesThatHaveAlreadyBeenSatisfied == null
					|| !packagesThatHaveAlreadyBeenSatisfied.contains(exportedPackage.getPackageName())) {
				registerPackageNameAgainstDependencyLocation(bundleDefinition.getLocation(), exportedPackage
						.getPackageName(), dependencyLocations);
			}
		}
	}

	private void processImportedPackages(List<ImportedPackage> importedPackages,
			Map<File, List<String>> dependencyLocations, List<ImportDescriptor> unsatisfiablePackageImports) {
		for (ImportedPackage importedPackage : importedPackages) {

			VersionRange versionRange = importedPackage.getVersion();
			String packageName = importedPackage.getPackageName();
			Set<? extends BundleDefinition> bundleDefinitions = this.bundleRepository.findByExportedPackage(
					packageName, versionRange);
			if (bundleDefinitions.size() > 0) {
				for (BundleDefinition bundleDefinition : bundleDefinitions) {
					registerPackageNameAgainstDependencyLocation(bundleDefinition.getLocation(), packageName,
							dependencyLocations);
				}
			}
			else if (Resolution.MANDATORY.equals(importedPackage.getResolution())) {
				unsatisfiablePackageImports.add(new ImportDescriptor(packageName, versionRange.toString(), versionRange
						.toParseString()));
			}

		}
	}

	/**
	 * Get the version attribute from a given attribute map.
	 * 
	 * @param map the attribute map
	 * @return the value of the version attribute
	 */
	private static String getVersionAttribute(Map<String, String> map) {
		String version = map.get(VERSION_ATTRIBUTE);
		if (version == null) {
			version = map.get(VERSION_ALTERNATE_ATTRIBUTE);
		}
		return version;
	}

	private void processRequiredBundles(List<RequiredBundle> requiredBundles,
			Map<File, List<String>> dependencyLocations, List<ImportDescriptor> unsatisfiableRequireBundles,
			List<String> packagesThatHaveAlreadyBeenSatisfied, boolean root) {

		for (RequiredBundle requiredBundle : requiredBundles) {
			if (root || Visibility.REEXPORT.equals(requiredBundle.getVisibility())) {
				String bundleSymbolicName = requiredBundle.getBundleSymbolicName();
				String bundleVersion = requiredBundle.getAttributes().get(BUNDLE_VERSION_ATTRIBUTE);
				VersionRange versionRange;
				if (bundleVersion != null) {
					versionRange = new VersionRange(bundleVersion);
				}
				else {
					versionRange = VersionRange.NATURAL_NUMBER_RANGE;
				}
				BundleDefinition bundleDefinition = this.bundleRepository.findBySymbolicName(bundleSymbolicName,
						versionRange);
				if (bundleDefinition != null) {
					registerDependencyLocationAndPackageNameForEveryExportedPackage(dependencyLocations,
							bundleDefinition, packagesThatHaveAlreadyBeenSatisfied);
					List<RequiredBundle> dependencysRequiredBundles = bundleDefinition.getManifest().getRequireBundle()
							.getRequiredBundles();
					processRequiredBundles(dependencysRequiredBundles, dependencyLocations,
							unsatisfiableRequireBundles, packagesThatHaveAlreadyBeenSatisfied, false);
				}
				else if (Resolution.MANDATORY.equals(requiredBundle.getResolution())) {
					unsatisfiableRequireBundles.add(new ImportDescriptor(requiredBundle.getBundleSymbolicName(),
							versionRange.toString(), versionRange.toParseString()));
				}
			}
		}
	}

	private void registerPackageNameAgainstDependencyLocation(URI location, String packageName,
			Map<File, List<String>> dependencyLocations) {
		File fileLocation;
		if (location != null) {
			fileLocation = new File(location);
		}
		else {
			fileLocation = null;
		}
		List<String> existingPackageNames = dependencyLocations.get(fileLocation);
		if (existingPackageNames == null) {
			existingPackageNames = new ArrayList<String>();
			dependencyLocations.put(fileLocation, existingPackageNames);
		}
		existingPackageNames.add(packageName);
	}

	private void throwDependencyLocationExceptionIfNecessary(List<ImportDescriptor> unsatisfiablePackageImports,
			List<ImportDescriptor> unsatisfiableBundleImports, List<ImportDescriptor> unsatisfiableLibraryImports,
			List<ImportDescriptor> unsatisfiableRequireBundles, Map<File, List<String>> satisfiedDependencies) {

		if (!unsatisfiableLibraryImports.isEmpty() || !unsatisfiablePackageImports.isEmpty()
				|| !unsatisfiableRequireBundles.isEmpty() || !unsatisfiableBundleImports.isEmpty()) {
			throw new DependencyLocationException(toArray(unsatisfiablePackageImports),
					toArray(unsatisfiableBundleImports), toArray(unsatisfiableLibraryImports),
					toArray(unsatisfiableRequireBundles), satisfiedDependencies);
		}
	}

	private static ImportDescriptor[] toArray(List<ImportDescriptor> importDescriptors) {
		return importDescriptors.toArray(new ImportDescriptor[importDescriptors.size()]);
	}

	private static class SystemPackageFilteringCompositeBundleRepository implements BundleRepository {

		private final Map<String, Version> systemPackages;

		private final BundleRepository mainRepository;

		private final BundleRepository systemPackageRepository;

		private final BundleManifest systemBundleManifest;

		private final Set<BundleDefinition> jreProvidedDependenciesDefinitions;

		private final BundleDefinition systemBundleDefinition;

		private static final String SYSTEM_BUNDLE_SYMBOLIC_NAME = "org.eclipse.osgi";

		SystemPackageFilteringCompositeBundleRepository(String serverHomePath, String[] additionalSearchPaths,
				String cacheDirectoryPath, JavaVersion javaVersion) throws IOException {

			String serverConfigPath = null;
			String serverProfilePath = null;

			if (serverHomePath != null) {
				serverConfigPath = serverHomePath + PLATFORM_CONFIG_PATH;
				if (javaVersion == JavaVersion.Java6) {
					serverProfilePath = serverHomePath + File.separator + "lib" + File.separator
							+ "java6-server.profile";
				}
				else {
					serverProfilePath = serverHomePath + File.separator + "lib" + File.separator
							+ "java5-server.profile";
				}

				File serverProfile = new File(serverProfilePath);
				if (!serverProfile.exists()) {
					serverProfilePath = serverHomePath + File.separator + "lib" + File.separator + "server.profile";
				}
			}

			String[] searchPathsFromConfig = readSearchPathsFromConfig(serverConfigPath);

			String[] searchPaths;

			if (additionalSearchPaths != null) {
				searchPaths = new String[searchPathsFromConfig.length + additionalSearchPaths.length];
				System.arraycopy(searchPathsFromConfig, 0, searchPaths, 0, searchPathsFromConfig.length);
				System.arraycopy(additionalSearchPaths, 0, searchPaths, searchPathsFromConfig.length,
						additionalSearchPaths.length);
			}
			else {
				searchPaths = searchPathsFromConfig;
			}

			File mainCache = new File(cacheDirectoryPath, "main-cache");
			File sysCache = new File(cacheDirectoryPath, "sys-cache");

			try {
				this.mainRepository = createBundleRepository("dep-loc-main", Arrays.asList(searchPaths), mainCache,
						serverHomePath);
			}
			catch (RepositoryCreationException e) {
				StatusManager.getManager().handle(new Status(IStatus.ERROR, BundleManifestCorePlugin.PLUGIN_ID, "Error creating repository index path [" + sysCache + "], server [" + serverHomePath
					                     						+ "]", e));
				//TODO Why not just rethrow the RCE?
				throw new IOException("A failure occurred during repository creation");
			}

			try {
				this.systemPackageRepository = createBundleRepository("dep-loc-sys", Arrays.asList(LIB_SEARCH_PATH),
						sysCache, serverHomePath);
			}
			catch (RepositoryCreationException e) {
				StatusManager.getManager().handle(new Status(IStatus.ERROR, BundleManifestCorePlugin.PLUGIN_ID, "Error creating repository index path [" + sysCache + "], server [" + serverHomePath
				                     						+ "]", e));
				//TODO Why not just rethrow the RCE?
				throw new IOException("A failure occurred during repository creation");
			}

			systemPackages = parseProfile(serverProfilePath);

			systemPackages.putAll(findExportsFromOsgiImplementationBundle(this.systemPackageRepository));

			systemBundleManifest = createManifestExportingPackages(systemPackages);

			jreProvidedDependenciesDefinitions = new HashSet<BundleDefinition>();
			systemBundleDefinition = new SystemBundleDefinition(systemBundleManifest, null);
			jreProvidedDependenciesDefinitions.add(systemBundleDefinition);
		}

		private Map<String, Version> findExportsFromOsgiImplementationBundle(BundleRepository bundleRepository) {
			Map<String, Version> exports = new HashMap<String, Version>();

			BundleDefinition definition = bundleRepository.findBySymbolicName(SYSTEM_BUNDLE_SYMBOLIC_NAME,
					VersionRange.NATURAL_NUMBER_RANGE);
			if (definition != null) {
				BundleManifest manifest = definition.getManifest();
				for (ExportedPackage exportedPackage : manifest.getExportPackage().getExportedPackages()) {
					Version version = exportedPackage.getVersion();
					exports.put(exportedPackage.getPackageName(), version);
				}
			}

			return exports;
		}

		/**
		 * {@inheritDoc}
		 */
		public Set<? extends BundleDefinition> findByExportedPackage(String packageName, VersionRange versionRange) {
			Version version;
			if ((version = systemPackages.get(packageName)) != null) {
				if (versionRange.includes(version)) {
					Set<? extends BundleDefinition> definitionsFromLib = this.systemPackageRepository
							.findByExportedPackage(packageName, versionRange);
					if (definitionsFromLib.isEmpty()) {
						return jreProvidedDependenciesDefinitions;
					}
					else {
						Set<BundleDefinition> systemBundleDefinitions = new HashSet<BundleDefinition>();
						for (BundleDefinition definition : definitionsFromLib) {
							systemBundleDefinitions.add(new SystemBundleDefinition(systemBundleManifest, definition
									.getLocation()));
						}
						return systemBundleDefinitions;
					}
				}
			}
			return this.mainRepository.findByExportedPackage(packageName, versionRange);
		}

		/**
		 * {@inheritDoc}
		 */
		public Set<? extends BundleDefinition> findByFragmentHost(String bundleSymbolicName, Version version) {
			return this.mainRepository.findByFragmentHost(bundleSymbolicName, version);
		}

		/**
		 * {@inheritDoc}
		 */
		public BundleDefinition findBySymbolicName(String symbolicName, VersionRange versionRange) {
			return this.mainRepository.findBySymbolicName(symbolicName, versionRange);
		}

		/**
		 * {@inheritDoc}
		 */
		public LibraryDefinition findLibrary(String libraryName, VersionRange versionRange) {
			return this.mainRepository.findLibrary(libraryName, versionRange);
		}

		/**
		 * {@inheritDoc}
		 */
		public Set<? extends BundleDefinition> getBundles() {
			Set<BundleDefinition> bundles = new HashSet<BundleDefinition>();
			bundles.addAll(this.mainRepository.getBundles());
			// expose jre packages for content assist
			bundles.addAll(this.jreProvidedDependenciesDefinitions);
			return bundles;
		}

		/**
		 * {@inheritDoc}
		 */
		public Set<? extends LibraryDefinition> getLibraries() {
			return this.mainRepository.getLibraries();
		}

		/**
		 * {@inheritDoc}
		 */
		public void refresh() {
			this.mainRepository.refresh();
		}
		
		public void shutdown() {
			if (this.mainRepository instanceof RepositoryBackedBundleRepository) {
				((RepositoryBackedBundleRepository) this.mainRepository).shutdown();
			}
			if (this.systemPackageRepository instanceof RepositoryBackedBundleRepository) {
				((RepositoryBackedBundleRepository) this.systemPackageRepository).shutdown();
			}
		}

		private static Map<String, Version> parseProfile(String serverProfilePath) throws IOException {
			Map<String, Version> systemPackages = new HashMap<String, Version>();
			if (serverProfilePath != null) {
				Properties properties = new Properties();
				FileInputStream propertiesStream = new FileInputStream(new File(serverProfilePath));
				try {
					properties.load(propertiesStream);
				}
				finally {
					if (propertiesStream != null) {
						propertiesStream.close();
					}
				}

				String systemPackagesString = properties.getProperty(SYSYEM_PACKAGES_PROPERTY);

				List<HeaderDeclaration> exportHeaders = HeaderParserFactory.newHeaderParser(new DummyParserLogger())
						.parsePackageHeader(systemPackagesString, "Export-Package");

				for (HeaderDeclaration exportHeader : exportHeaders) {
					String versionString = getVersionAttribute(exportHeader.getAttributes());
					Version version = versionString != null ? new Version(versionString) : Version.emptyVersion;
					for (String packageName : exportHeader.getNames()) {
						systemPackages.put(packageName, version);
					}
				}
			}
			return systemPackages;
		}

		private static BundleManifest createManifestExportingPackages(Map<String, Version> packagesMap) {
			Dictionary<String, String> manifestContents = new Hashtable<String, String>();
			manifestContents.put(Constants.BUNDLE_MANIFESTVERSION, Integer.toString(2));
			manifestContents.put("Manifest-Version", "1.0");

			if (packagesMap.size() > 0) {
				StringWriter writer = new StringWriter();

				Set<Entry<String, Version>> packagesSet = packagesMap.entrySet();
				Iterator<Entry<String, Version>> packages = packagesSet.iterator();

				while (packages.hasNext()) {
					Entry<String, Version> pkg = packages.next();
					Version version = pkg.getValue();
					String packageName = pkg.getKey();
					writer.append(packageName + ";version=\"" + version.toString() + "\"");

					if (packages.hasNext()) {
						writer.append(",");
					}
				}
			}
			return BundleManifestFactory.createBundleManifest(manifestContents);
		}

		public ArtifactDescriptor findSubsystem(String subsystemName) {
			return null;
		}
	}

	static class SystemBundleDefinition implements BundleDefinition {

		private final BundleManifest manifest;

		private final URI location;

		SystemBundleDefinition(BundleManifest manifest, URI location) {
			this.manifest = manifest;
			this.location = location;
		}

		/**
		 * {@inheritDoc}
		 */
		public BundleManifest getManifest() {
			return this.manifest;
		}

		/**
		 * {@inheritDoc}
		 */
		public URI getLocation() {
			return this.location;
		}
	}

	private static BundleRepository createBundleRepository(String name, List<String> searchPaths,
			File baseIndexLocation, String serverHome) throws RepositoryCreationException {

		Set<ArtifactBridge> artefactBridges = new HashSet<ArtifactBridge>();
        artefactBridges.add(new BundleBridge(new HashGenerator() {
            public void generateHash(ArtifactDescriptorBuilder artifactDescriptorBuilder, File artifactFile) {
                // do nothing
            }
        }));
        artefactBridges.add(new LibraryBridge(new HashGenerator() {
            public void generateHash(ArtifactDescriptorBuilder artifactDescriptorBuilder, File artifactFile) {
                // do nothing
            }
        }));

		List<RepositoryConfiguration> chainedConfiguration = new ArrayList<RepositoryConfiguration>();
		int index = 1;
		for (String searchPath : searchPaths) {
			String repoName = name + "-" + index++;
			File indexLocation = new File(baseIndexLocation.getAbsolutePath() + repoName + ".index");
			chainedConfiguration.add(new ExternalStorageRepositoryConfiguration(repoName, indexLocation,
					artefactBridges, makeAbsoluteIfNecessary(convertPath(searchPath), serverHome), null));
		}

		RepositoryFactory repositoryFactory = getRepositoryFactory();
		Repository repository = repositoryFactory.createRepository(chainedConfiguration);

		return new RepositoryBackedBundleRepository(repository);
	}

	private static String convertToAntStylePath(String searchPath) {
		return searchPath.replaceAll("\\{[^\\}]+\\}", "*");
	}

	private static String expandProperties(String value) {
		Pattern regex = PROPERTY_PATTERN;
		StringBuffer buffer = new StringBuffer(value.length());
		Matcher matcher = regex.matcher(value);
		int propertyGroup = matcher.groupCount();
		String key, property = "";
		while (matcher.find()) {
			key = matcher.group(propertyGroup);
			property = "";
			if (key.contains("::")) {
				String[] keyDefault = key.split("::");
				property = System.getProperty(keyDefault[0]);
				if (property == null) {
					property = keyDefault[1];
				}
				else {
					property = property.replace('\\', '/');
				}
			}
			else {
				property = System.getProperty(matcher.group(propertyGroup)).replace('\\', '/');
			}
			matcher.appendReplacement(buffer, property);
		}
		matcher.appendTail(buffer);
		return buffer.toString();
	}

	private static String convertPath(String path) {
		return convertToAntStylePath(expandProperties(path));
	}

	private static String makeAbsoluteIfNecessary(String antPathPattern, String absoluteRoot) {
		String absolutePathPattern;
		if (!antPathPattern.startsWith("/") && !(antPathPattern.indexOf(":") > 0)) {
			absolutePathPattern = absoluteRoot + File.separator + antPathPattern;
		}
		else {
			absolutePathPattern = antPathPattern;
		}
		if (File.separator.equals("/")) {
			return absolutePathPattern.replace('\\', '/');
		}
		else {
			return absolutePathPattern.replace('/', '\\');
		}
	}

	private static RepositoryFactory getRepositoryFactory() {
		BundleContext bundleContext = Platform.getBundle(BundleManifestCorePlugin.PLUGIN_ID).getBundleContext();
		
		RepositoryFactory repositoryFactory = null;

		ServiceReference serviceReference = bundleContext.getServiceReference(RepositoryFactory.class.getName());
		if (serviceReference != null) {
			repositoryFactory = (RepositoryFactory) bundleContext.getService(serviceReference);
		}

		if (repositoryFactory == null) {
			throw new IllegalStateException(
					"RepositoryFactory service was not available. Is the repository bundle installed and started?");
		}

		return repositoryFactory;
	}

	public Set<? extends BundleDefinition> getBundles() {
		return this.bundleRepository.getBundles();
	}

	public Set<? extends LibraryDefinition> getLibraries() {
		return this.bundleRepository.getLibraries();
	}

	public void shutdown() {
		if (this.bundleRepository instanceof SystemPackageFilteringCompositeBundleRepository) {
			((SystemPackageFilteringCompositeBundleRepository) this.bundleRepository).shutdown();
		}
	}

}