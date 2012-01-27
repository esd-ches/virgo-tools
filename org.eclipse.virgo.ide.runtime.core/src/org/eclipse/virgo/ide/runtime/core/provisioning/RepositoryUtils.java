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
package org.eclipse.virgo.ide.runtime.core.provisioning;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.osgi.service.resolver.VersionRange;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.misc.StringMatcher;
import org.eclipse.virgo.ide.bundlerepository.domain.Artefact;
import org.eclipse.virgo.ide.bundlerepository.domain.ArtefactRepository;
import org.eclipse.virgo.ide.bundlerepository.domain.BundleArtefact;
import org.eclipse.virgo.ide.bundlerepository.domain.LibraryArtefact;
import org.eclipse.virgo.ide.bundlerepository.domain.OsgiVersion;
import org.eclipse.virgo.ide.bundlerepository.domain.PackageMember;
import org.eclipse.virgo.ide.facet.core.FacetUtils;
import org.eclipse.virgo.ide.manifest.core.BundleManifestCorePlugin;
import org.eclipse.virgo.ide.runtime.core.ServerCorePlugin;
import org.eclipse.virgo.ide.runtime.core.ServerUtils;
import org.eclipse.virgo.ide.runtime.internal.core.ServerRuntime;
import org.eclipse.virgo.ide.runtime.internal.core.ServerRuntimeUtils;
import org.eclipse.wst.server.core.IRuntime;
import org.osgi.framework.Version;
import org.springframework.ide.eclipse.core.java.JdtUtils;

import org.eclipse.virgo.kernel.repository.BundleRepository;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.util.osgi.manifest.ExportedPackage;

/**
 * Utility class that is able to create {@link Repository} instances from either the remote
 * enterprise bundle repository or from a local installed dm Server instance.
 * @author Christian Dupuis
 * @since 1.0.0
 */
@SuppressWarnings("restriction")
public class RepositoryUtils {

	public static final String DOWNLOAD_TYPE_BINARY = "binary";

	public static final String DOWNLOAD_TYPE_BINARY_HASH = "binary-hash";

	public static final String DOWNLOAD_TYPE_LIBRARY = "library";

	public static final String DOWNLOAD_TYPE_LIBRARY_HASH = "library-hash";

	public static final String DOWNLOAD_TYPE_LICENSE = "license";

	public static final String DOWNLOAD_TYPE_SOURCE = "source";

	public static final String DOWNLOAD_TYPE_SOURCE_HASH = "source-hash";

	private static final String BRITS_BASE = "http://www.springsource.com/repository/app";

	private static final String DOWNLOAD_BASE = "http://repository.springsource.com/ivy";

	/**
	 * Checks if a given version of an artifact (either a bundle or a library) is installed in the
	 * repository.
	 */
	public static boolean containsArtifact(Artefact artifact, ArtefactRepository repository) {
		if (artifact instanceof BundleArtefact) {
			for (BundleArtefact repositoryArtifact : repository.getBundles()) {
				if (repositoryArtifact.getSymbolicName().equals(
						((BundleArtefact) artifact).getSymbolicName())) {
					if (repositoryArtifact.getVersion().equals(
							((BundleArtefact) artifact).getVersion())) {
						return true;
					}
				}
			}
		}
		else if (artifact instanceof LibraryArtefact) {
			for (LibraryArtefact repositoryArtifact : repository.getLibraries()) {
				if (repositoryArtifact.getSymbolicName().equals(
						((LibraryArtefact) artifact).getSymbolicName())) {
					if (repositoryArtifact.getVersion().equals(
							((LibraryArtefact) artifact).getVersion())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Downloads the given <code>artifacts</code>.
	 */
	public static void downloadArifacts(final Set<Artefact> artifacts, final IProject project,
			final Shell shell, boolean resolveDependencies) {

		if (resolveDependencies) {
			artifacts.addAll(resolveDependencies(artifacts, false));
		}

		final Set<IRuntime> runtimes = new HashSet<IRuntime>();

		ServerRuntimeUtils.execute(project, new ServerRuntimeUtils.ServerRuntimeCallback() {

			public boolean doWithRuntime(ServerRuntime runtime) {
				runtimes.add(runtime.getRuntime());
				return true;
			}
		});

		if (runtimes.size() > 0) {

			IRunnableWithProgress runnable = new IRunnableWithProgress() {

				public void run(IProgressMonitor monitor) throws InvocationTargetException,
						InterruptedException {
					RepositoryProvisioningJob job = new RepositoryProvisioningJob(runtimes,
							artifacts, true);
					job.run(monitor);
				}
			};

			try {
				IRunnableContext context = new ProgressMonitorDialog(shell);
				context.run(true, true, runnable);
			}
			catch (InvocationTargetException e1) {
			}
			catch (InterruptedException e2) {
			}
		}
	}

	/**
	 * Returns a BundleDefinition for the given bundle symbolic name and version
	 */
	public static org.eclipse.virgo.kernel.repository.BundleDefinition getBundleDefinition(
			final String bundle, final String version, IProject project) {
		final List<org.eclipse.virgo.kernel.repository.BundleDefinition> bundles = new ArrayList<org.eclipse.virgo.kernel.repository.BundleDefinition>();

		ServerRuntimeUtils.execute(project, new ServerRuntimeUtils.ServerRuntimeCallback() {

			public boolean doWithRuntime(ServerRuntime runtime) {
				try {
					BundleRepository bundleRepository = ServerCorePlugin
							.getArtefactRepositoryManager().getBundleRepository(
									runtime.getRuntime());
					org.eclipse.virgo.kernel.repository.BundleDefinition bundleDefinition = bundleRepository
							.findBySymbolicName(bundle,
									new org.eclipse.virgo.util.osgi.VersionRange(version));
					if (bundleDefinition != null) {
						bundles.add(bundleDefinition);
						return false;
					}
				}
				catch (Exception e) {

				}
				return true;
			}
		});

		if (bundles.size() > 0) {
			return bundles.get(0);
		}
		return null;
	}

	/**
	 * Returns {@link BundleArtefact}s which bundle symbolic name matches the given
	 * <code>value</code>.
	 */
	public static Set<BundleArtefact> getImportBundleProposals(final IProject project,
			final String value) {
		final Set<BundleArtefact> packages = new TreeSet<BundleArtefact>(
				new Comparator<BundleArtefact>() {

					public int compare(BundleArtefact o1, BundleArtefact o2) {
						if (o1.getName() != null && o1.getName().equals(o2.getName())) {
							return o1.getVersion().compareTo(o2.getVersion());
						}
						else if (o1.getName() != null && o2.getName() != null) {
							return o1.getName().compareTo(o2.getName());
						}
						else if (o1.getSymbolicName() != null && o1.getSymbolicName().equals(o2.getSymbolicName())) {
							return o1.getVersion().compareTo(o2.getVersion());
						}
						else if (o1.getSymbolicName() != null) {
							return o1.getSymbolicName().compareTo(o2.getSymbolicName());
						}
						return 0;
					}
				});

		ServerRuntimeUtils.execute(project, new ServerRuntimeUtils.ServerRuntimeCallback() {

			public boolean doWithRuntime(ServerRuntime runtime) {
				packages.addAll(getImportBundleProposalsForRuntime(runtime, project, value));
				return true;
			}
		});

		return packages;

	}

	/**
	 * Returns {@link LibraryArtefact}s which library symbolic name matches the given
	 * <code>value</code>.
	 */
	public static Set<LibraryArtefact> getImportLibraryProposals(final IProject project,
			final String value) {
		final Set<LibraryArtefact> packages = new TreeSet<LibraryArtefact>(
				new Comparator<LibraryArtefact>() {

					public int compare(LibraryArtefact o1, LibraryArtefact o2) {
						if (o1.getName() != null && o1.getName().equals(o2.getName())) {
							return o1.getVersion().compareTo(o2.getVersion());
						}
						else if (o1.getName() != null) {
							return o1.getName().compareTo(o2.getName());
						}
						return 0;
					}
				});

		ServerRuntimeUtils.execute(project, new ServerRuntimeUtils.ServerRuntimeCallback() {

			public boolean doWithRuntime(ServerRuntime runtime) {
				packages.addAll(getImportLibraryProposalsForRuntime(runtime, project, value));
				return true;
			}
		});

		return packages;

	}

	/**
	 * Returns {@link org.eclipse.virgo.ide.bundlerepository.domain.PackageExport}s which name matches the given
	 * <code>value</code>.
	 */
	public static Set<org.eclipse.virgo.ide.bundlerepository.domain.PackageExport> getImportPackageProposals(
			final IProject project, final String value) {
		final Set<org.eclipse.virgo.ide.bundlerepository.domain.PackageExport> packages = new TreeSet<org.eclipse.virgo.ide.bundlerepository.domain.PackageExport>(
				new Comparator<org.eclipse.virgo.ide.bundlerepository.domain.PackageExport>() {

					public int compare(org.eclipse.virgo.ide.bundlerepository.domain.PackageExport o1,
							org.eclipse.virgo.ide.bundlerepository.domain.PackageExport o2) {
						if (o1.getName().equals(o2.getName())) {
							return o1.getVersion().compareTo(o2.getVersion());
						}
						return o1.getName().compareTo(o2.getName());
					}
				});

		ServerRuntimeUtils.execute(project, new ServerRuntimeUtils.ServerRuntimeCallback() {

			public boolean doWithRuntime(ServerRuntime runtime) {
				packages.addAll(getImportPackageProposalsForRuntime(runtime, project, value));
				return true;
			}
		});

		return packages;

	}

	/**
	 * Creates a {@link Repository} inventory from the given runtime.
	 */
	public static ArtefactRepository getRepositoryContents(IRuntime runtime) {

		ArtefactRepository artifacts = new ArtefactRepository();

		BundleRepository bundleRepository = ServerCorePlugin.getArtefactRepositoryManager()
				.getBundleRepository(runtime);

		for (org.eclipse.virgo.kernel.repository.BundleDefinition bundleDefinition : bundleRepository
				.getBundles()) {
			if (bundleDefinition.getManifest() != null
					&& bundleDefinition.getManifest().getBundleSymbolicName() != null
					&& bundleDefinition.getManifest().getBundleSymbolicName().getSymbolicName() != null) {
				BundleManifest manifest = bundleDefinition.getManifest();
				boolean sourcefileExists = (ServerUtils.getSourceFile(bundleDefinition
						.getLocation()) != null && ServerUtils.getSourceFile(
						bundleDefinition.getLocation()).exists());
				OsgiVersion version = null;
				if (manifest.getBundleVersion() != null) {
					version = new OsgiVersion(manifest.getBundleVersion());
				}
				artifacts.addBundle(new LocalBundleArtefact(manifest.getBundleName(), manifest
						.getBundleSymbolicName().getSymbolicName(), version, sourcefileExists,
						bundleDefinition.getLocation()));
			}

		}
		for (org.eclipse.virgo.kernel.repository.LibraryDefinition libraryDefinition : bundleRepository
				.getLibraries()) {
			if (libraryDefinition.getSymbolicName() != null) {
				artifacts.addLibrary(new LocalLibraryArtefact(libraryDefinition.getName(),
						libraryDefinition.getSymbolicName(), new OsgiVersion(libraryDefinition
								.getVersion()), libraryDefinition.getLocation()));
			}
		}

		return artifacts;
	}

	public static String getRepositoryUrl(Artefact artefact) {
		StringBuilder url = new StringBuilder(BRITS_BASE);
		if (artefact instanceof BundleArtefact) {
			url.append("/bundle/version/detail?name=");
		}
		else if (artefact instanceof LibraryArtefact) {
			url.append("/library/version/detail?name=");
		}
		url.append(artefact.getSymbolicName());
		url.append("&version=");
		url.append(artefact.getVersion().toString());
		return url.toString();
	}

	public static String getResourceUrl(BundleArtefact bundle, String downloadType) {
		StringBuffer url = new StringBuffer(DOWNLOAD_BASE);
		url.append("/bundles");
		url.append(getCategory(bundle));
		if (DOWNLOAD_TYPE_SOURCE.equals(downloadType)) {
			url.append(bundle.getRelativeSourceUrlPath());
		}
		else if (DOWNLOAD_TYPE_SOURCE_HASH.equals(downloadType)) {
			url.append(bundle.getRelativeSourceUrlPath()).append(".sha1");
		}
		else if (DOWNLOAD_TYPE_LICENSE.equals(downloadType)) {
			url.append(bundle.getRelativeLicenseUrlPath());
		}
		else if (DOWNLOAD_TYPE_BINARY_HASH.equals(downloadType)) {
			url.append(bundle.getRelativeUrlPath()).append(".sha1");
		}
		else {
			url.append(bundle.getRelativeUrlPath());
		}
		return url.toString();
	}

	public static String getResourceUrl(LibraryArtefact library, String downloadType) {
		StringBuffer url = new StringBuffer(DOWNLOAD_BASE);
		url.append("/libraries");
		url.append(getCategory(library));
		if (DOWNLOAD_TYPE_LICENSE.equals(downloadType)) {
			url.append(library.getRelativeLicenseUrlPath());
		}
		else if (DOWNLOAD_TYPE_LIBRARY_HASH.equals(downloadType)) {
			url.append(library.getRelativeUrlPath()).append(".sha1");
		}
		else {
			url.append(library.getRelativeUrlPath());
		}
		return url.toString();
	}

	/**
	 * Returns proposals for version content assist requests. 2.5.4 -> 2.5.4 [2.5.4,2.6.0)
	 * [2.5.4,2.5.4] [2.5.4,3.0.0)
	 */
	@SuppressWarnings("unchecked")
	public static List<String> getVersionProposals(Set<String> versionStrings) {
		// First order the version so that the highest appears first
		List<Version> versions = new ArrayList<Version>();
		for (String versionString : versionStrings) {
			versions.add(Version.parseVersion(versionString));
		}
		Collections.sort(versions);
		Collections.reverse(versions);

		// Now add version ranges to the version
		List<String> versionRanges = new ArrayList<String>();
		for (Version version : versions) {
			// 2.5.4 - removed as we don't want to make people use the completion
			// versionRanges.add(version.toString());

			// [2.5.4,2.6.0)
			versionRanges.add(new VersionRange(version, true, new Version(version.getMajor(),
					version.getMinor() + 1, 0, null), false).toString());

			// [2.5.4,2.5.4]
			versionRanges.add(new VersionRange(version, true, version, true).toString());

			// [2.5.4,3.0.0)
			versionRanges.add(new VersionRange(version, true, new Version(version.getMajor() + 1,
					0, 0, null), false).toString());

		}

		return versionRanges;
	}

	/**
	 * Resolves dependencies of given <code>artefacts</code>. Currently this implementation only
	 * resolves direct bundle dependencies of {@link LibraryArtefact}s.
	 */
	public static Set<Artefact> resolveDependencies(Set<Artefact> artifacts, boolean includeOptional) {
		Set<Artefact> resolvedArtefacts = new HashSet<Artefact>(artifacts);
		// resolve library dependencies
		for (Artefact artefact : artifacts) {
			if (artefact instanceof LibraryArtefact) {
				resolvedArtefacts.addAll(ServerCorePlugin.getArtefactRepositoryManager()
						.findLibraryDependencies((LibraryArtefact) artefact, includeOptional));
			}
		}
		return resolvedArtefacts;
	}

	/**
	 * Searches the remote bundle repository for matches of a given search string.
	 */
	public static ArtefactRepository searchForArtifacts(final String search) {
		return searchForArtifacts(search, true, true);
	}

	/**
	 * Searches the remote bundle repository for matches of a given search string.
	 */
	public static ArtefactRepository searchForArtifacts(final String search,
			boolean includeBundles, boolean includesLibraries) {
		StringMatcher matcher = new StringMatcher("*" + search + "*", true, false);

		ArtefactRepository repository = new ArtefactRepository();
		if (includeBundles) {
			for (BundleArtefact bundle : ServerCorePlugin.getArtefactRepositoryManager()
					.getArtefactRepository().getBundles()) {
				// check symbolic name and name
				if (matcher.match(bundle.getSymbolicName()) || matcher.match(bundle.getName())) {
					repository.addBundle(bundle);
				}
				// check export packages
				else {
					for (org.eclipse.virgo.ide.bundlerepository.domain.PackageExport pe : bundle.getExports()) {
						if (matcher.match(pe.getName())) {
							repository.addBundle(bundle);
							break;
						}
						for (PackageMember pm : pe.getExports()) {
							if (matcher.match(pm.getName())) {
								repository.addBundle(bundle);
								break;
							}
						}
					}
				}
			}
		}
		if (includesLibraries) {
			for (LibraryArtefact library : ServerCorePlugin.getArtefactRepositoryManager()
					.getArtefactRepository().getLibraries()) {
				// check symbolic name and name
				if (matcher.match(library.getSymbolicName()) || matcher.match(library.getName())) {
					repository.addLibrary(library);
				}
			}
		}
		return repository;
	}

	private static void addImportBundleProposalsFromManifest(Set<BundleArtefact> bundles,
			BundleManifest manifest, String value) {
		if (manifest != null && manifest.getBundleSymbolicName() != null
				&& manifest.getBundleSymbolicName().getSymbolicName() != null
				&& manifest.getBundleSymbolicName().getSymbolicName().startsWith(value)) {
			OsgiVersion version = null;
			if (manifest.getBundleVersion() != null) {
				version = new OsgiVersion(manifest.getBundleVersion());
			}
			bundles.add(new LocalBundleArtefact(manifest.getBundleName(), manifest
					.getBundleSymbolicName().getSymbolicName(), version, false, null));
		}
	}

	private static void addImportPackageProposalsFromManifest(String value,
			Set<org.eclipse.virgo.ide.bundlerepository.domain.PackageExport> packages, BundleManifest manifest) {
		if (manifest != null && manifest.getExportPackage() != null) {
			for (ExportedPackage export : manifest.getExportPackage().getExportedPackages()) {
				Object version = export.getAttributes().get("version");
				String packageName = export.getPackageName();
				if (packageName.startsWith(value)) {
					OsgiVersion v = null;
					if (version != null) {
						v = new OsgiVersion(version.toString());
					}
					else {
						v = new OsgiVersion(Version.emptyVersion);
					}
					packages.add(new org.eclipse.virgo.ide.bundlerepository.domain.PackageExport(null, packageName,
							v));
				}
			}
		}
	}

	private static String getCategory(Artefact artefact) {
		if (artefact.getOrganisationName() != null
				&& (artefact.getOrganisationName().startsWith("com.springsource") || artefact
						.getOrganisationName().startsWith("org.springframework"))) {
			return "/release";
		}
		else {
			return "/external";
		}
	}

	private static Set<BundleArtefact> getImportBundleProposalsForRuntime(ServerRuntime runtime,
			IProject project, String value) {
		if (value == null) {
			value = "";
		}
		Set<BundleArtefact> bundles = new HashSet<BundleArtefact>();

		BundleRepository bundleRepository = ServerCorePlugin.getArtefactRepositoryManager()
				.getBundleRepository(runtime.getRuntime());

		for (org.eclipse.virgo.kernel.repository.BundleDefinition definition : bundleRepository
				.getBundles()) {
			if (definition.getManifest() != null) {
				BundleManifest manifest = definition.getManifest();
				addImportBundleProposalsFromManifest(bundles, manifest, value);
			}
		}

		// Add workspace bundles
		for (IProject p : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			if (FacetUtils.isBundleProject(p) && !p.equals(project)) {
				BundleManifest manifest = BundleManifestCorePlugin.getBundleManifestManager()
						.getBundleManifest(JavaCore.create(project));
				addImportBundleProposalsFromManifest(bundles, manifest, value);
			}
		}

		return bundles;
	}

	private static Set<LibraryArtefact> getImportLibraryProposalsForRuntime(ServerRuntime runtime,
			IProject project, String value) {
		if (value == null) {
			value = "";
		}
		Set<LibraryArtefact> libraries = new HashSet<LibraryArtefact>();

		BundleRepository bundleRepository = ServerCorePlugin.getArtefactRepositoryManager()
				.getBundleRepository(runtime.getRuntime());

		for (org.eclipse.virgo.kernel.repository.LibraryDefinition definition : bundleRepository
				.getLibraries()) {
			if (definition.getSymbolicName() != null
					&& definition.getSymbolicName().startsWith(value)) {
				libraries.add(new LibraryArtefact(definition.getName(), definition
						.getSymbolicName(), new OsgiVersion(definition.getVersion()), definition
						.getSymbolicName(), definition.getSymbolicName()));
			}
		}
		return libraries;
	}

	private static Set<org.eclipse.virgo.ide.bundlerepository.domain.PackageExport> getImportPackageProposalsForRuntime(
			ServerRuntime runtime, IProject project, String value) {
		if (value == null) {
			value = "";
		}
		Set<org.eclipse.virgo.ide.bundlerepository.domain.PackageExport> packages = new HashSet<org.eclipse.virgo.ide.bundlerepository.domain.PackageExport>();

		BundleRepository bundleRepository = ServerCorePlugin.getArtefactRepositoryManager()
				.getBundleRepository(runtime.getRuntime());

		for (org.eclipse.virgo.kernel.repository.BundleDefinition definition : bundleRepository
				.getBundles()) {
			if (definition.getManifest() != null) {
				BundleManifest manifest = definition.getManifest();
				addImportPackageProposalsFromManifest(value, packages, manifest);
			}
		}

		// Add workspace bundles
		for (IProject p : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			if (FacetUtils.isBundleProject(p) && !p.equals(project)) {
				BundleManifest manifest = BundleManifestCorePlugin.getBundleManifestManager()
						.getBundleManifest(JavaCore.create(project));
				addImportPackageProposalsFromManifest(value, packages, manifest);
			}
		}

		return packages;
	}

}
