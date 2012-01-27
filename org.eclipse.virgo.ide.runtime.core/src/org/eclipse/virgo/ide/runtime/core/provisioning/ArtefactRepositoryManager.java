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
package org.eclipse.virgo.ide.runtime.core.provisioning;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.virgo.ide.bundlerepository.domain.ArtefactRepository;
import org.eclipse.virgo.ide.bundlerepository.domain.BundleArtefact;
import org.eclipse.virgo.ide.bundlerepository.domain.BundleImport;
import org.eclipse.virgo.ide.bundlerepository.domain.LibraryArtefact;
import org.eclipse.virgo.ide.bundlerepository.domain.OsgiVersion;
import org.eclipse.virgo.ide.bundlerepository.domain.PackageExport;
import org.eclipse.virgo.ide.bundlerepository.domain.PackageImport;
import org.eclipse.virgo.ide.bundlerepository.domain.SpringSourceApplicationPlatform;
import org.eclipse.virgo.ide.manifest.core.dependencies.IDependencyLocator;
import org.eclipse.virgo.ide.runtime.core.ServerCorePlugin;
import org.eclipse.virgo.ide.runtime.core.ServerUtils;
import org.eclipse.virgo.ide.runtime.internal.core.provisioning.IArtefactRepositoryLoader;
import org.eclipse.virgo.ide.runtime.internal.core.provisioning.JsonArtefactRepositoryLoader;
import org.eclipse.virgo.ide.runtime.internal.core.utils.StatusUtil;
import org.eclipse.virgo.ide.runtime.internal.core.utils.WebDownloadUtils;
import org.eclipse.virgo.kernel.repository.BundleDefinition;
import org.eclipse.virgo.kernel.repository.BundleRepository;
import org.eclipse.virgo.kernel.repository.LibraryDefinition;
import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.util.osgi.VersionRange;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.util.osgi.manifest.ExportedPackage;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.util.PublishUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

/**
 * Manages instances of {@link ArtefactRepository} to represent the current contents of the SpringSource Enterprise
 * Bundle Repository and {@link BundleRepository}s indexed by {@link IRuntime} representing local bundle and library
 * repositories in a dm Server instance.
 * @author Christian Dupuis
 * @since 1.0.0
 */
public class ArtefactRepositoryManager {

	private ArtefactRepository artefactRepository = new ArtefactRepository();
	
	private Map<IRuntime, BundleRepository> bundleRepositories = new ConcurrentHashMap<IRuntime, BundleRepository>();

	private Date repositoryDate = new Date();

	private SpringSourceApplicationPlatform applicationPlatform = new SpringSourceApplicationPlatform();

	private Set<IBundleRepositoryChangeListener> changeListeners = Collections
			.synchronizedSet(new HashSet<IBundleRepositoryChangeListener>());
	
	private volatile boolean initialized = false;

	protected static final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

	protected static final Lock r = rwl.readLock();

	protected static final Lock w = rwl.writeLock();

	private List<OsgiVersion> findBundleVersions(String symbolicName) {
		List<OsgiVersion> versions = new ArrayList<OsgiVersion>();
		try {
			r.lock();
			for (BundleArtefact bundle : artefactRepository.getBundles()) {
				if (bundle.getSymbolicName().equals(symbolicName)) {
					versions.add(bundle.getVersion());
				}
			}
		}
		finally {
			r.unlock();
		}
		return versions;
	}

	public BundleRepository getBundleRepository(IRuntime runtime) {
		try {
			r.lock();
			if (bundleRepositories.containsKey(runtime)) {
				return bundleRepositories.get(runtime);
			}
		}
		finally {
			r.unlock();
		}
		IDependencyLocator locator = null;
		try {
			w.lock();
			if (bundleRepositories.containsKey(runtime)) {
				return bundleRepositories.get(runtime);
			}
			
			locator = ServerUtils.createDependencyLocator(runtime);
			Set<BundleDefinition> bundles = new HashSet<BundleDefinition>();

			for (BundleDefinition bundle : locator.getBundles()) {
				if (bundle.getManifest() != null) {
					bundles.add(new InitializedBundleDefinition(bundle.getManifest(), bundle.getLocation()));
				}
			}

			BundleRepository initializedBundleRepository = new InitializedBundleRepository(bundles,
					new HashSet<LibraryDefinition>(locator.getLibraries()));
			bundleRepositories.put(runtime, initializedBundleRepository);
			return initializedBundleRepository;
		}
		finally {
			// Shutdown DependencyLocator
			if (locator != null) {
				locator.shutdown();
			}

			w.unlock();
		}
	}

	public BundleRepository refreshBundleRepository(IRuntime runtime) {
		try {
			w.lock();
			bundleRepositories.remove(runtime);
		}
		finally {
			w.unlock();
		}

		BundleRepository bundleRepository = getBundleRepository(runtime);
		fireBundleRepositoryChanged(runtime);
		return bundleRepository;
	}

	public List<BundleArtefact> findLibraryDependencies(LibraryArtefact library, boolean includeOptional) {
		try {
			r.lock();
			List<BundleArtefact> required = new ArrayList<BundleArtefact>();
			List<BundleImport> unsatisfiedBundleImports = new ArrayList<BundleImport>();
			List<BundleImport> bundleImports = library.getBundleImports();
			List<BundleArtefact> bundles = new ArrayList<BundleArtefact>();
			for (BundleImport imp : bundleImports) {
				List<OsgiVersion> candidates = findBundleVersions(imp.getSymbolicName());
				OsgiVersion highestAvailable = new OsgiVersion("0.0.0");
				boolean foundMatch = false;
				for (OsgiVersion foundVersion : candidates) {
					if (imp.getVersionRange().contains(foundVersion)) {
						if (foundVersion.compareTo(highestAvailable) > 0) {
							highestAvailable = foundVersion;
							foundMatch = true;
						}
					}
				}
				if (foundMatch) {
					bundles.add(getBundle(imp.getSymbolicName(), highestAvailable));
				}
				else {
					unsatisfiedBundleImports.add(imp);
				}
			}
			required.addAll(bundles);
			List<PackageImport> unsatisfiedImports = new ArrayList<PackageImport>();
			List<PackageImport> imports = new ArrayList<PackageImport>();
			for (BundleArtefact bundle : bundles) {
				imports.addAll(bundle.getImports());
			}
			collectExporters(required, includeOptional, imports, unsatisfiedImports);
			return required;
		}
		finally {
			r.unlock();
		}
	}

	public ArtefactRepository getArtefactRepository() {
		try {
			r.lock();
			if (!initialized) {
				start();
			}
			return artefactRepository;
		}
		finally {
			r.unlock();
		}
	}

	public Date getArtefactRepositoryDate() {
		try {
			r.lock();
			if (!initialized) {
				start();
			}
			return repositoryDate;
		}
		finally {
			r.unlock();
		}
	}
	
	public boolean isArtefactRepositoryInitialized() {
		return initialized;
	}
	
	private BundleArtefact getBundle(String symbolicName, OsgiVersion version) {
		try {
			r.lock();
			for (BundleArtefact bundle : artefactRepository.getBundles()) {
				if (bundle.getSymbolicName().equals(symbolicName) && bundle.getVersion().equals(version)) {
					return bundle;
				}
			}
			return null;
		}
		finally {
			r.unlock();
		}
	}

	private void start() {
		ArtefactRepositoryStartJob startJob = new ArtefactRepositoryStartJob();
		startJob.setPriority(Job.INTERACTIVE);
		startJob.schedule();
	}

	public void stop() {
		artefactRepository = null;
	}

	public void update() {
		ArtefactRepositoryUpdateJob updateJob = new ArtefactRepositoryUpdateJob();
		updateJob.setPriority(Job.INTERACTIVE);
		updateJob.schedule();
	}

	private boolean alreadyInBundleList(List<BundleArtefact> required, BundleArtefact exporter) {
		for (BundleArtefact b : required) {
			if (b.getName().equals(exporter.getName())) {
				if (b.getVersion().equals(exporter.getVersion())) {
					return true;
				}
			}
		}
		return false;
	}

	private void collectExporters(List<BundleArtefact> required, boolean includeOptional, List<PackageImport> imports,
			List<PackageImport> unsatisfied) {
		for (PackageImport imp : imports) {
			if (includeOptional || !imp.isOptional()) {
				if (!applicationPlatform.isSatisfiedViaSystemBundle(imp)) {
					// (heuristic: we always prefer to get via system if we can)
					BundleArtefact exporter = findBestExporter(imp);
					if (exporter == null) {
						unsatisfied.add(imp);
					}
					else {
						if (!alreadyInBundleList(required, exporter)) {
							required.add(exporter);
							collectExporters(required, includeOptional, exporter.getImports(), unsatisfied);
						}
					}
				}
			}
		}
	}

	private BundleArtefact findBestExporter(PackageImport imp) {
		List<PackageExport> candidates = findPackagesWithExactName(imp.getName());
		List<PackageExport> versionMatchedCandidates = new ArrayList<PackageExport>();
		for (PackageExport exp : candidates) {
			if (imp.isSatisfiedBy(exp)) {
				versionMatchedCandidates.add(exp);
			}
		}
		if (versionMatchedCandidates.size() > 0) {
			PackageExport withHighestVersion = null;
			for (PackageExport versionMatchedExport : versionMatchedCandidates) {
				if (withHighestVersion == null) {
					withHighestVersion = versionMatchedExport;
				}
				else {
					if (withHighestVersion.getVersion().compareTo(versionMatchedExport.getVersion()) < 0) {
						withHighestVersion = versionMatchedExport;
					}
				}
			}
			return withHighestVersion.getBundle();
		}
		return null;
	}

	private List<PackageExport> findPackagesWithExactName(String name) {
		List<PackageExport> exports = new ArrayList<PackageExport>();
		for (BundleArtefact bundle : artefactRepository.getBundles()) {
			for (PackageExport e : bundle.getExports()) {
				if (e.getName().equals(name)) {
					exports.add(e);
				}
			}
		}
		return exports;
	}

	public void addBundleRepositoryChangeListener(IBundleRepositoryChangeListener changeListener) {
		this.changeListeners.add(changeListener);
	}

	public void removeBundleRepositoryChangeListener(IBundleRepositoryChangeListener changeListener) {
		this.changeListeners.remove(changeListener);
	}

	private void fireBundleRepositoryChanged(IRuntime runtime) {
		for (IBundleRepositoryChangeListener changeListener : changeListeners) {
			changeListener.bundleRepositoryChanged(runtime);
		}
	}

	static class InitializedBundleDefinition implements BundleDefinition {

		private final URI file;

		private final BundleManifest manifest;

		public InitializedBundleDefinition(BundleManifest manifest, URI file) {
			this.file = file;
			this.manifest = manifest;
		}

		public BundleManifest getManifest() {
			return manifest;
		}

		public URI getLocation() {
			return file;
		}

	}

	static class InitializedBundleRepository implements BundleRepository {

		private final Set<BundleDefinition> bundles;

		private final Set<LibraryDefinition> libraries;

		public InitializedBundleRepository(Set<BundleDefinition> bundles, Set<LibraryDefinition> libraries) {
			this.bundles = bundles;
			this.libraries = libraries;
		}

		public Set<? extends BundleDefinition> findByExportedPackage(String packageName, VersionRange versionRange) {
			Set<BundleDefinition> matchingBundles = new HashSet<BundleDefinition>();
			for (BundleDefinition bundle : bundles) {
				if (bundle.getManifest() != null && bundle.getManifest().getExportPackage() != null) {
					for (ExportedPackage header : bundle.getManifest().getExportPackage().getExportedPackages()) {
						if (versionRange.includes(header.getVersion())) {
							if (packageName.equals(header.getPackageName())) {
								matchingBundles.add(bundle);
							}
						}
					}
				}
			}
			return matchingBundles;
		}

		public Set<? extends BundleDefinition> findByFragmentHost(String bundleSymbolicName, Version version) {
			return Collections.emptySet();
		}

		public BundleDefinition findBySymbolicName(String symbolicName, VersionRange versionRange) {
			for (BundleDefinition bundle : bundles) {
				if (bundle.getManifest() != null && bundle.getManifest().getBundleSymbolicName() != null
						&& bundle.getManifest().getBundleSymbolicName().getSymbolicName().equals(symbolicName)) {
					Version version = bundle.getManifest().getBundleVersion();
					if (versionRange.includes(version)) {
						return bundle;
					}
				}
			}
			return null;
		}

		public LibraryDefinition findLibrary(String libraryName, VersionRange versionRange) {
			for (LibraryDefinition bundle : libraries) {
				if (bundle.getName() != null && bundle.getName().equals(libraryName)) {
					if (versionRange.includes(bundle.getVersion())) {
						return bundle;
					}
				}
			}
			return null;
		}

		public Set<? extends BundleDefinition> getBundles() {
			return bundles;
		}

		public Set<? extends LibraryDefinition> getLibraries() {
			return libraries;
		}

		public void refresh() {
		}

		public ArtifactDescriptor findSubsystem(String arg0) {
			return null;
		}

	}

	class ArtefactRepositoryStartJob extends Job {

		private Bundle bundle = ServerCorePlugin.getDefault().getBundle();

		public ArtefactRepositoryStartJob() {
			super("Initializing Bundle Repository Index");
		}

		private void createArtefactDescriptorFromCurrentZipEntry(ZipInputStream zipInputStream,
				String newArtefactDescriptorPath) throws IOException, FileNotFoundException {
			File artefactDescriptor = new File(newArtefactDescriptorPath);
			if (!artefactDescriptor.exists()) {
				artefactDescriptor.createNewFile();
				FileOutputStream fileOutputStream = new FileOutputStream(artefactDescriptor);
				byte[] buf = new byte[1024];
				int len;
				while ((len = zipInputStream.read(buf)) > 0) {
					fileOutputStream.write(buf, 0, len);
				}
				fileOutputStream.close();
			}
		}

		private void createLocalFolderStructure() {
			File localDirectory = getLocalDirectory();
			PublishUtil.deleteDirectory(localDirectory, null);
			if (!localDirectory.exists()) {
				localDirectory.mkdir();
				new File(localDirectory, "bundles").mkdirs();
				new File(localDirectory, "libraries").mkdirs();
			}
		}

		protected File getLocalDirectory() {
			IPath path = Platform.getStateLocation(bundle);
			return new File(path.toString() + File.separator + "repository");
		}

		private File getRepositoryIndexFile() {
			return new File(getLocalDirectory(), ".index");
		}

		protected void writeArchiveContentsToLocalRespositoryDirectory(InputStream zipFileInputStream)
				throws CoreException {
			// clean folder
			createLocalFolderStructure();

			ZipInputStream zipInputStream = null;
			try {
				zipInputStream = new ZipInputStream(zipFileInputStream);
				ZipEntry currentZipEntry = zipInputStream.getNextEntry();

				while (currentZipEntry != null) {
					String filePath = getLocalDirectory().getPath() + File.separator + currentZipEntry.getName();
					createArtefactDescriptorFromCurrentZipEntry(zipInputStream, filePath);
					currentZipEntry = zipInputStream.getNextEntry();
				}

				zipInputStream.close();
			}
			catch (IOException e) {
				StatusUtil.error(e);
			}
			finally {
				try {
					if (zipInputStream != null) {
						zipInputStream.close();
					}
				}
				catch (IOException e) {
					StatusUtil.error(e);
				}
			}
		}

		protected IArtefactRepositoryLoader createArtefactRespositoryLoader() {
			return new JsonArtefactRepositoryLoader();
		}

		@SuppressWarnings("unchecked")
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {

				// firstly make sure that we have the repository in the in the workspace
				Enumeration<URL> urls = bundle.findEntries("/index", "repository-*.zip", false);

				// we will only bundle one repository zip in the plugin so it is safe to take the
				// first element
				if (urls.hasMoreElements()) {
					URL url = urls.nextElement();
					if (!getRepositoryIndexFile().exists()) {
						writeArchiveContentsToLocalRespositoryDirectory(url.openStream());
					}
					else {
						// Check if the timestamp on the bundled repository is newer than the
						// extracted
						String path = url.getFile();
						int ix = path.lastIndexOf("repository-");
						Long timestamp = Long.valueOf(path.substring(ix + 11, path.length() - 4));
						Long localTimestamp = getLocalRepositoryTimestamp();
						if (timestamp.compareTo(localTimestamp) == 1) {
							writeArchiveContentsToLocalRespositoryDirectory(url.openStream());
						}
					}
				}

				initialized = true;

				// secondly load the repository in memory
				ArtefactRepository newArtefactRepository = createArtefactRespositoryLoader().loadArtefactRepository(
						getLocalDirectory());
				try {
					w.lock();
					artefactRepository = newArtefactRepository;
					repositoryDate = new Date(getLocalRepositoryTimestamp());
				}
				finally {
					w.unlock();
				}
			}
			catch (Exception e) {
				StatusUtil.error(e);
				initialized = true;
				artefactRepository = new ArtefactRepository();
			}
			return Status.OK_STATUS;
		}

		protected Long getLocalRepositoryTimestamp() throws IOException, FileNotFoundException {
			FileInputStream is = null;
			try {
				is = new FileInputStream(getRepositoryIndexFile());
				Properties properties = new Properties();
				properties.load(is);
				Long localTimestamp = Long.valueOf(properties.getProperty("creation.timestamp"));
				return localTimestamp;
			}
			finally {
				if (is != null) {
					is.close();
				}
			}
		}

	}

	public class ArtefactRepositoryUpdateJob extends ArtefactRepositoryStartJob {

		/** The url under which the newest version of the repository index will be published */
		private static final String REPOSITORY_INDEX_URL = "http://static.springsource.com/projects/sts-dm-server/index/repository.zip";

		@Override
		protected IStatus run(IProgressMonitor monitor) {

			try {
				// first download file from the online repository
				Date lastModifiedDate = WebDownloadUtils.getLastModifiedDate(REPOSITORY_INDEX_URL, monitor);
				if (lastModifiedDate != null
						&& Long.valueOf(lastModifiedDate.getTime()).compareTo(getLocalRepositoryTimestamp()) == 1) {
					File repositoryArchive = WebDownloadUtils.downloadFile(REPOSITORY_INDEX_URL, getLocalDirectory()
							.getParentFile(), monitor);

					if (repositoryArchive != null) {

						// secondly extract it to the local repository location
						if (repositoryArchive.exists() && repositoryArchive.canRead()) {
							FileInputStream is = null;
							try {
								is = new FileInputStream(repositoryArchive);
								writeArchiveContentsToLocalRespositoryDirectory(is);
							}
							finally {
								if (is != null) {
									try {
										is.close();
									}
									catch (Exception e) {
										// ignore
									}
								}
							}
						}

						// thirdly load the repository in memory
						ArtefactRepository newArtefactRepository = createArtefactRespositoryLoader()
								.loadArtefactRepository(getLocalDirectory());
						try {
							w.lock();
							artefactRepository = newArtefactRepository;
							repositoryDate = new Date(getLocalRepositoryTimestamp());
						}
						finally {
							w.unlock();
						}
					}
				}
			}
			catch (FileNotFoundException e) {
				StatusUtil.error(e);
			}
			catch (IOException e) {
				StatusUtil.error(e);
			}
			catch (CoreException e) {
				StatusUtil.error(e);
			}

			return Status.OK_STATUS;
		}
	}
	
	public static byte[] convert(String string) {
		if (string == null) {
			return null;
		}
		String newString = new String(string);
		try {
			return newString.getBytes("UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
}
