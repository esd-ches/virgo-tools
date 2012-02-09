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
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.virgo.ide.bundlerepository.domain.ArtefactRepository;
import org.eclipse.virgo.ide.bundlerepository.domain.BundleArtefact;
import org.eclipse.virgo.ide.bundlerepository.domain.BundleImport;
import org.eclipse.virgo.ide.bundlerepository.domain.LibraryArtefact;
import org.eclipse.virgo.ide.bundlerepository.domain.OsgiVersion;
import org.eclipse.virgo.ide.bundlerepository.domain.PackageExport;
import org.eclipse.virgo.ide.bundlerepository.domain.PackageImport;
import org.eclipse.virgo.ide.bundlerepository.domain.PackageMemberType;
import org.eclipse.virgo.ide.bundlerepository.domain.VersionRange;
import org.eclipse.virgo.ide.runtime.core.ServerCorePlugin;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.springsource.json.parser.AntlrJSONParser;
import com.springsource.json.parser.JSONParser;
import com.springsource.json.parser.MapNode;
import com.springsource.json.parser.Node;
import com.springsource.json.parser.internal.StandardBooleanNode;
import com.springsource.json.parser.internal.StandardListNode;
import com.springsource.json.parser.internal.StandardStringNode;

/**
 * @author Christian Dupuis
 * @since 1.0.0
 */
public class JsonArtefactRepositoryLoader implements IArtefactRepositoryLoader {

	private Map<String, OsgiVersion> versions = new HashMap<String, OsgiVersion>();

	private Map<String, VersionRange> versionRanges = new HashMap<String, VersionRange>();

	private Map<String, PackageImport> imports = new HashMap<String, PackageImport>();

	private boolean loadClasses = ServerCorePlugin.getDefault().getPreferenceStore().getBoolean(
			ServerCorePlugin.PREF_LOAD_CLASSES_KEY);

	// private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	public ArtefactRepository loadArtefactRepository(File rootFolder) {
		ArtefactRepository newArtefactRepository = new ArtefactRepository();
		try {
			Resource[] resources = new PathMatchingResourcePatternResolver().getResources("file:"
					+ rootFolder.toString() + "/**/*-*");
			for (Resource resource : resources) {
				JSONParser parser = new AntlrJSONParser();
				try {
					MapNode node = (MapNode) parser.parse(resource.getURL());
					MapNode nb = (MapNode) node.getNode("bundle");
					MapNode nl = (MapNode) node.getNode("library");

					if (nb != null) {
						newArtefactRepository.addBundle(createBundleArtefact(nb));
					}
					if (nl != null) {
						newArtefactRepository.addLibrary(createLibraryArtefact(nl));
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
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

	private BundleArtefact createBundleArtefact(MapNode nb) throws ParseException {
		StandardStringNode name = (StandardStringNode) nb.getNode("name");
		StandardStringNode version = (StandardStringNode) nb.getNode("version");
		StandardStringNode symbolicName = (StandardStringNode) nb.getNode("symbolicName");
		StandardStringNode organistationName = (StandardStringNode) nb.getNode("organisationName");
		StandardStringNode moduleName = (StandardStringNode) nb.getNode("moduleName");
		// StandardStringNode notes = (StandardStringNode) nb.getNode("notes");
		StandardBooleanNode sourceAvailable = (StandardBooleanNode) nb.getNode("sourceAvailable");
		// StandardStringNode dateAdded = (StandardStringNode) nb.getNode("dateAdded");

		BundleArtefact artefact = new BundleArtefact(name.getValue(), symbolicName.getValue(), getVersion(version
				.getValue()), organistationName.getValue(), moduleName.getValue());
		// artefact.setNotes(notes.getValue());
		artefact.setSourceAvailable(sourceAvailable.getValue());
		// artefact.setDateAdded(FORMAT.parse(dateAdded.getValue()));

		Node exports = nb.getNode("packageExports");
		if (exports != null) {
			if (exports instanceof StandardListNode) {
				for (Node en : ((StandardListNode) exports).getNodes()) {
					MapNode e = (MapNode) en;
					createPackageExport(artefact, e);
				}
			}
			else {
				MapNode e = (MapNode) exports;
				createPackageExport(artefact, e);
			}
		}

		Node imports = nb.getNode("packageImports");
		if (imports != null) {
			if (imports instanceof StandardListNode) {
				for (Node en : ((StandardListNode) imports).getNodes()) {
					MapNode e = (MapNode) en;
					createPackageImport(artefact, e);
				}
			}
			else {
				MapNode e = (MapNode) imports;
				createPackageImport(artefact, e);
			}
		}

		return artefact;
	}

	private void createPackageImport(BundleArtefact artefact, MapNode e) {
		StandardStringNode name;
		StandardStringNode version;
		name = (StandardStringNode) e.getNode("name");
		version = (StandardStringNode) e.getNode("versionRange");
		StandardBooleanNode optional = (StandardBooleanNode) e.getNode("optional");
		PackageImport npi = getPackageImport(name.getValue(), version.getValue(), optional.getValue());

		artefact.addImport(npi);
	}

	private void createPackageExport(BundleArtefact artefact, MapNode e) {
		StandardStringNode name;
		StandardStringNode version;
		name = (StandardStringNode) e.getNode("name");
		version = (StandardStringNode) e.getNode("version");
		PackageExport npe = new PackageExport(artefact, name.getValue(), getVersion(version.getValue()));

		if (loadClasses) {
			Node exportMembers = (Node) e.getNode("packageMembers");
			if (exportMembers != null) {
				if (exportMembers instanceof StandardListNode) {
					for (Node em : ((StandardListNode) exportMembers).getNodes()) {
						MapNode m = (MapNode) em;
						createPackageMember(npe, m);
					}
				}
				else {
					MapNode m = (MapNode) exportMembers;
					createPackageMember(npe, m);
				}
			}
		}

		artefact.addExport(npe);
	}

	private void createPackageMember(PackageExport npe, MapNode m) {
		StandardStringNode name = (StandardStringNode) m.getNode("name");
		PackageMemberType type = PackageMemberType.valueOf(((StandardStringNode) m.getNode("type")).getValue());
		if (type == PackageMemberType.CLASS) {
			npe.addClassExport(name.getValue());
		}
		else {
			npe.addResourceExport(name.getValue());
		}
	}

	private LibraryArtefact createLibraryArtefact(MapNode nl) throws ParseException {
		StandardStringNode name = (StandardStringNode) nl.getNode("name");
		StandardStringNode version = (StandardStringNode) nl.getNode("version");
		StandardStringNode symbolicName = (StandardStringNode) nl.getNode("symbolicName");
		StandardStringNode organistationName = (StandardStringNode) nl.getNode("organisationName");
		StandardStringNode moduleName = (StandardStringNode) nl.getNode("moduleName");
		// StandardStringNode notes = (StandardStringNode) nl.getNode("notes");
		StandardBooleanNode sourceAvailable = (StandardBooleanNode) nl.getNode("sourceAvailable");
		// StandardStringNode dateAdded = (StandardStringNode) nl.getNode("dateAdded");

		LibraryArtefact artefact = new LibraryArtefact(name.getValue(), symbolicName.getValue(), getVersion(version
				.getValue()), organistationName.getValue(), moduleName.getValue());
		// artefact.setNotes(notes.getValue());
		artefact.setSourceAvailable(sourceAvailable.getValue());
		// artefact.setDateAdded(FORMAT.parse(dateAdded.getValue()));

		StandardListNode exports = (StandardListNode) nl.getNode("bundleImports");
		if (exports != null) {
			for (Node en : exports.getNodes()) {
				MapNode e = (MapNode) en;
				name = (StandardStringNode) e.getNode("name");
				version = (StandardStringNode) e.getNode("versionRange");
				BundleImport npe = new BundleImport(artefact, name.getValue(), getVersionRange(version.getValue()));

				artefact.addBundleImport(npe);
			}
		}
		return artefact;

	}

}
