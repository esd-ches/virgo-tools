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
package org.eclipse.virgo.ide.bundlor.internal.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import org.eclipse.virgo.bundlor.EntryScannerListener;
import org.eclipse.virgo.bundlor.support.partialmanifest.StandardReadablePartialManifest;

/**
 * Extension to {@link StandardReadablePartialManifest} to allow for
 * re-recording of type dependencies in an incremental manner </p>.
 * 
 * @author Christian Dupuis
 */
public final class IncrementalReadablePartialManifest extends StandardReadablePartialManifest implements
		EntryScannerListener {

	/** Association of analyzed types and their dependencies */
	private final Map<String, TypeDependencies> recordedTypeDependencies = new HashMap<String, TypeDependencies>();

	private final Stack<String> currentlyAnalysedEntries = new Stack<String>();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void recordReferencedType(String fullyQualifiedTypeName) {
		TypeDependencies typeDependencies = createTypeDependencies(null);
		if (typeDependencies != null) {
			typeDependencies.addImportedType(fullyQualifiedTypeName);
		}
		super.recordReferencedType(fullyQualifiedTypeName);
	}

	/**
	 * {@inheritDoc}
	 */
	public void recordReferencedPackage(String fullyQualifiedPackageName) {
		TypeDependencies typeDependencies = createTypeDependencies(null);
		if (typeDependencies != null) {
			typeDependencies.addImportedPackage(fullyQualifiedPackageName);
		}
		super.recordReferencedPackage(fullyQualifiedPackageName);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void recordType(String fullyQualifiedTypeName) {
		createTypeDependencies(fullyQualifiedTypeName);

		super.recordType(fullyQualifiedTypeName);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void recordUsesPackage(String usingPackage, String usedPackage) {
		TypeDependencies typeDependencies = createTypeDependencies(null);
		if (typeDependencies != null) {
			typeDependencies.addUses(usingPackage, usedPackage);
		}
		super.recordUsesPackage(usingPackage, usedPackage);
	}

	/**
	 * Remove a recorded type from the partial manifest; also removes all
	 * induced dependencies that this type created.
	 * 
	 * @param fullyQualifiedTypeName the type to remove
	 */
	private void unrecordType() {
		String currentEntry = currentEntry();
		if (recordedTypeDependencies.containsKey(currentEntry)) {
			TypeDependencies existingTypeDependencies = recordedTypeDependencies.get(currentEntry);
			String fullyQualifiedTypeName = existingTypeDependencies.getFullQualifiedTypeName();

			// Remove in order to iterate over the remaining
			recordedTypeDependencies.remove(currentEntry);

			String oldPackageName = getPackageName(fullyQualifiedTypeName);

			// Remove the actual type
			unrecordType(fullyQualifiedTypeName);

			// Remove the actual package name from the export package set
			boolean otherTypes = false;
			for (TypeDependencies existingTypeDependency : recordedTypeDependencies.values()) {
				String existingPackageName = getPackageName(existingTypeDependency.getFullQualifiedTypeName());
				if (existingPackageName != null && existingPackageName.equals(oldPackageName)) {
					otherTypes = true;
					break;
				}
			}
			if (!otherTypes && oldPackageName != null) {
				unrecordExportPackage(oldPackageName);
			}

			// Remove any imported types
			for (String importedType : existingTypeDependencies.getImportedTypes()) {
				boolean otherImports = false;
				for (TypeDependencies existingTypePartialManifest : recordedTypeDependencies.values()) {
					for (String existingImportedType : existingTypePartialManifest.getImportedTypes()) {
						if (importedType.equals(existingImportedType)) {
							otherImports = true;
							break;
						}
					}
				}
				if (!otherImports) {
					removeImportedType(importedType);
				}
			}

			// Remove any referenced package
			for (String referencedPackage : existingTypeDependencies.getReferencedPackages()) {
				boolean otherReferences = false;
				for (TypeDependencies existingTypePartialManifest : recordedTypeDependencies.values()) {
					for (String existingReferencedPackage : existingTypePartialManifest.getReferencedPackages()) {
						if (referencedPackage.equals(existingReferencedPackage)) {
							otherReferences = true;
							break;
						}
					}
				}
				if (!otherReferences) {
					removeReferencedPackage(referencedPackage);
				}
			}

			// Remove any uses constraints
			for (Map.Entry<String, Set<String>> oldUses : existingTypeDependencies.getUses().entrySet()) {
				for (TypeDependencies existingTypePartialManifest : recordedTypeDependencies.values()) {
					for (Map.Entry<String, Set<String>> existingUses : existingTypePartialManifest.getUses().entrySet()) {
						if (existingUses.getKey().equals(oldUses.getKey())) {
							oldUses.getValue().removeAll(existingUses.getValue());
						}
					}
				}

				removeUses(oldUses.getKey(), oldUses.getValue());
			}
		}
	}

	private TypeDependencies createTypeDependencies(String fullyQualifiedClassName) {
		String currentEntry = currentEntry();
		if (recordedTypeDependencies.containsKey(currentEntry)) {
			return recordedTypeDependencies.get(currentEntry);
		}
		TypeDependencies typeDependency = new TypeDependencies(fullyQualifiedClassName);
		recordedTypeDependencies.put(currentEntry, typeDependency);
		return typeDependency;
	}

	public void onBeginEntry(String name) {
		name = name.replace('\\', '/');
		this.currentlyAnalysedEntries.push(name);
		unrecordType();
	}

	public void onEndEntry() {
		this.currentlyAnalysedEntries.pop();
	}

	public String currentEntry() {
		if (!this.currentlyAnalysedEntries.isEmpty()) {
			return this.currentlyAnalysedEntries.peek();
		}
		return "template.mf";
	}

	/**
	 * Structure that keeps associations between a type and its dependencies
	 */
	class TypeDependencies {

		private final String fullQualifiedTypeName;

		private final Set<String> importedTypes = new HashSet<String>();

		private final Set<String> referencedPackages = new TreeSet<String>();

		private final Map<String, Set<String>> uses = new HashMap<String, Set<String>>();

		public TypeDependencies(String fullQualifiedTypeName) {
			this.fullQualifiedTypeName = fullQualifiedTypeName;
		}

		void addImportedType(String fullyQualifiedTypeName) {
			if (fullyQualifiedTypeName != null) {
				this.importedTypes.add(fullyQualifiedTypeName);
			}
		}

		void addUses(String usingPackage, String usedPackage) {
			if (isRecordablePackage(usingPackage) && isRecordablePackage(usedPackage)
				&& !usingPackage.equals(usedPackage)) {
				Set<String> usesSet = getUsesSet(usingPackage);
				usesSet.add(usedPackage);
			}
		}

		void addImportedPackage(String importedPackage) {
			if (importedPackage != null && isRecordablePackage(importedPackage)) {
				referencedPackages.add(importedPackage);
			}
		}

		private Set<String> getUsesSet(String exportingPackage) {
			Set<String> usesSet = this.uses.get(exportingPackage);
			if (usesSet == null) {
				usesSet = new TreeSet<String>();
				this.uses.put(exportingPackage, usesSet);
			}
			return usesSet;
		}

		String getFullQualifiedTypeName() {
			return fullQualifiedTypeName;
		}

		Set<String> getImportedTypes() {
			return importedTypes;
		}

		Set<String> getReferencedPackages() {
			return referencedPackages;
		}

		Map<String, Set<String>> getUses() {
			return uses;
		}
	}

}
