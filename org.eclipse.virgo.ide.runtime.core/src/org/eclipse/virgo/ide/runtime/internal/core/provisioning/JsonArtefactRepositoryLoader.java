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
package org.eclipse.virgo.ide.runtime.internal.core.provisioning;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.virgo.ide.bundlerepository.domain.BundleImport;
import org.eclipse.virgo.ide.bundlerepository.domain.OsgiVersion;
import org.eclipse.virgo.ide.bundlerepository.domain.PackageExport;
import org.eclipse.virgo.ide.bundlerepository.domain.PackageImport;
import org.eclipse.virgo.ide.bundlerepository.domain.PackageMemberType;
import org.eclipse.virgo.ide.bundlerepository.domain.VersionRange;
import org.eclipse.virgo.ide.internal.utils.json.JSONChildParser;
import org.eclipse.virgo.ide.internal.utils.json.JSONFileParser;
import org.eclipse.virgo.ide.runtime.core.ServerCorePlugin;
import org.eclipse.virgo.ide.runtime.core.artefacts.ArtefactRepository;
import org.eclipse.virgo.ide.runtime.core.artefacts.BundleArtefact;
import org.eclipse.virgo.ide.runtime.core.artefacts.LibraryArtefact;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Christian Dupuis
 * @since 1.0.0
 */
public class JsonArtefactRepositoryLoader implements IArtefactRepositoryLoader {

	private Map<String, OsgiVersion> versions = new HashMap<String, OsgiVersion>();

	private Map<String, VersionRange> versionRanges = new HashMap<String, VersionRange>();

	private Map<String, PackageImport> imports = new HashMap<String, PackageImport>();

	private boolean loadClasses = ServerCorePlugin.getDefault().getPreferenceStore()
			.getBoolean(ServerCorePlugin.PREF_LOAD_CLASSES_KEY);

	// private static final SimpleDateFormat FORMAT = new
	// SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	public ArtefactRepository loadArtefactRepository(File rootFolder) {
		final ArtefactRepository newArtefactRepository = new ArtefactRepository();
		IPath path = new Path(rootFolder.getAbsolutePath());
		IPath bundlePath = path.append("bundles");
		File bundleFolder = bundlePath.toFile();
		for (File bundleFile : bundleFolder.listFiles()) {
			new JSONFileParser(bundleFile) {
				public void parse(JSONObject object) throws JSONException {
					JSONObject bundleNode = (JSONObject) object.get("bundle");
					newArtefactRepository.addBundle(createBundleArtefact(bundleNode));
				}
			};
		}
		IPath libraryPath = path.append("libraries");
		File libraryFolder = libraryPath.toFile();
		for (File libraryFile : libraryFolder.listFiles()) {
			new JSONFileParser(libraryFile) {
				public void parse(JSONObject object) throws JSONException {
					JSONObject bundleNode = (JSONObject) object.get("library");
					newArtefactRepository.addLibrary(createLibraryArtefact(bundleNode));
				}
			};
		}
		return newArtefactRepository;
	}

	private OsgiVersion getVersion(String versionString) {
		if (versions.containsKey(versionString)) {
			return versions.get(versionString);
		}
		OsgiVersion version = new OsgiVersion(versionString);
		versions.put(versionString, version);
		return version;
	}

	private VersionRange getVersionRange(String versionRangeString) {
		if (versionRanges.containsKey(versionRangeString)) {
			return versionRanges.get(versionRangeString);
		}
		VersionRange version = new VersionRange(versionRangeString);
		versionRanges.put(versionRangeString, version);
		return version;
	}

	private PackageImport getPackageImport(String packageName, String versionRangeString, boolean isOptional) {
		String key = packageName + "." + versionRangeString + "." + isOptional;
		if (imports.containsKey(key)) {
			return imports.get(key);
		}
		PackageImport packageImport = new PackageImport(packageName, isOptional, getVersionRange(versionRangeString));
		imports.put(key, packageImport);
		return packageImport;
	}

	private BundleArtefact createBundleArtefact(JSONObject bundleNode) throws JSONException {
		String name = bundleNode.getString("name");
		String version = bundleNode.getString("version");
		String symbolicName = bundleNode.getString("symbolicName");
		String organistationName = bundleNode.getString("organisationName");
		String moduleName = bundleNode.getString("moduleName");
		// String notes = nb.getString("notes");
		boolean sourceAvailable = bundleNode.getBoolean("sourceAvailable");
		// String dateAdded = nb.getString("dateAdded");

		final BundleArtefact artefact = new BundleArtefact(name, symbolicName, getVersion(version), organistationName,
			moduleName);
		// artefact.setNotes(notes);
		artefact.setSourceAvailable(sourceAvailable);
		// artefact.setDateAdded(FORMAT.parse(dateAdded));
		new JSONChildParser(bundleNode, "packageExports") {
			public void parse(JSONObject object) throws JSONException {
				createPackageExport(artefact, object);
			}
		};
		new JSONChildParser(bundleNode, "packageImports") {
			public void parse(JSONObject object) throws JSONException {
				createPackageImport(artefact, object);
			}
		};
		return artefact;
	}

	private void createPackageImport(BundleArtefact artefact, JSONObject e) throws JSONException {
		String name = e.getString("name");
		String version = e.getString("versionRange");
		Boolean optional = e.getBoolean("optional");
		PackageImport npi = getPackageImport(name, version, optional);

		artefact.addImport(npi);
	}

	private void createPackageExport(BundleArtefact artefact, JSONObject e) throws JSONException {
		String name;
		String version;
		name = e.getString("name");
		version = e.getString("version");
		PackageExport npe = new PackageExport(artefact, name, getVersion(version));

		if (loadClasses) {
			JSONArray exportMembers = e.getJSONArray("packageMembers");
			for (int i = 0; i < exportMembers.length(); i++) {
				JSONObject m = (JSONObject) exportMembers.get(i);
				createPackageMember(npe, m);
			}
		}

		artefact.addExport(npe);
	}

	private void createPackageMember(PackageExport npe, JSONObject m) throws JSONException {
		String name = m.getString("name");
		PackageMemberType type = PackageMemberType.valueOf((m.getString("type")));
		if (type == PackageMemberType.CLASS) {
			npe.addClassExport(name);
		} else {
			npe.addResourceExport(name);
		}
	}

	private LibraryArtefact createLibraryArtefact(JSONObject nl) throws JSONException {
		String name = nl.getString("name");
		String version = nl.getString("version");
		String symbolicName = nl.getString("symbolicName");
		String organistationName = nl.getString("organisationName");
		String moduleName = nl.getString("moduleName");
		Boolean sourceAvailable = nl.getBoolean("sourceAvailable");

		final LibraryArtefact artefact = new LibraryArtefact(name, symbolicName, getVersion(version),
			organistationName, moduleName);
		artefact.setSourceAvailable(sourceAvailable);

		new JSONChildParser(nl, "bundleImports") {
			public void parse(JSONObject object) throws JSONException {
				BundleImport npe = new BundleImport(artefact, object.getString("name"),
					getVersionRange(object.getString("versionRange")));
				artefact.addBundleImport(npe);
			}
		};
		return artefact;

	}

}
