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
package org.eclipse.virgo.ide.bundlor.internal.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import org.eclipse.virgo.bundlor.EntryScannerListener;
import org.eclipse.virgo.bundlor.ManifestGenerator;
import org.eclipse.virgo.bundlor.support.ArtifactAnalyzer;
import org.eclipse.virgo.bundlor.support.ManifestGeneratorContributors;
import org.eclipse.virgo.bundlor.support.ManifestModifier;
import org.eclipse.virgo.bundlor.support.ManifestTemplateModifier;
import org.eclipse.virgo.bundlor.support.StandardManifestGenerator;
import org.eclipse.virgo.bundlor.support.contributors.BundleClassPathArtifactAnalyzer;
import org.eclipse.virgo.bundlor.support.contributors.ExcludedImportAndExportPartialManifestModifier;
import org.eclipse.virgo.bundlor.support.contributors.IgnoredExistingHeadersManifestModifier;
import org.eclipse.virgo.bundlor.support.contributors.JspArtifactAnalyzer;
import org.eclipse.virgo.bundlor.support.contributors.ManifestTemplateDirectiveMigrator;
import org.eclipse.virgo.bundlor.support.contributors.OsgiProfileManifestTemplateModifier;
import org.eclipse.virgo.bundlor.support.contributors.StaticResourceArtifactAnalyzer;
import org.eclipse.virgo.bundlor.support.contributors.ToolStampManifestModifier;
import org.eclipse.virgo.bundlor.support.contributors.xml.BlueprintArtifactAnalyzer;
import org.eclipse.virgo.bundlor.support.contributors.xml.HibernateMappingArtifactAnalyzer;
import org.eclipse.virgo.bundlor.support.contributors.xml.JpaPersistenceArtifactAnalyzer;
import org.eclipse.virgo.bundlor.support.contributors.xml.Log4JXmlArtifactAnalyzer;
import org.eclipse.virgo.bundlor.support.contributors.xml.SpringApplicationContextArtifactAnalyzer;
import org.eclipse.virgo.bundlor.support.contributors.xml.WebApplicationArtifactAnalyzer;
import org.eclipse.virgo.bundlor.support.partialmanifest.ReadablePartialManifest;
import org.eclipse.virgo.bundlor.support.partialmanifest.StandardPartialManifestResolver;
import org.eclipse.virgo.bundlor.support.partialmanifest.StandardReadablePartialManifest;
import org.eclipse.virgo.bundlor.support.properties.PropertiesSource;
import org.eclipse.virgo.bundlor.support.propertysubstitution.PlaceholderManifestAndTemplateModifier;
import org.eclipse.virgo.util.parser.manifest.ManifestContents;

/**
 * Factory to create {@link ManifestGenerator}.
 * 
 * @author Christian Dupuis
 * @since 2.3.0
 */
class ManifestGeneratorFactory {

	public static ManifestGenerator create(ReadablePartialManifest partialManifest, ArtifactAnalyzer artifactAnalyzer,
			PropertiesSource... properties) {
		ManifestGeneratorContributors contributors = create(artifactAnalyzer, properties);

		// Remove custom headers
		contributors.addManifestModifier(new HeaderRemovingManifestModifier());
		contributors.addManifestTemplateModifier(new HeaderRemovingTemplateManifestModifier());

		// Add partial manifest model
		contributors.setReadablePartialManifest(partialManifest);

		if (partialManifest instanceof EntryScannerListener) {
			contributors.addEntryScannerListener((EntryScannerListener) partialManifest);
		}

		return new StandardManifestGenerator(contributors);
	}

	private static ManifestGeneratorContributors create(ArtifactAnalyzer artifactAnalyzer,
			PropertiesSource... propertiesSources) {
		ManifestGeneratorContributors contributors = new ManifestGeneratorContributors();

		Properties properties = combineProperties(propertiesSources);

		BlueprintArtifactAnalyzer blueprintArtifactAnalyzer = new BlueprintArtifactAnalyzer();
		IgnoredExistingHeadersManifestModifier ignoredExistingHeadersManifestModifier = new IgnoredExistingHeadersManifestModifier();
		ExcludedImportAndExportPartialManifestModifier excludedImportAndExportPartialManifestModifier = new ExcludedImportAndExportPartialManifestModifier();
		PlaceholderManifestAndTemplateModifier placeholderManifestAndTemplateModifier = new PlaceholderManifestAndTemplateModifier(
			properties);
		ManifestTemplateDirectiveMigrator manifestTemplateDirectiveMigrator = new ManifestTemplateDirectiveMigrator();
		StandardPartialManifestResolver partialManifestResolver = new StandardPartialManifestResolver();

		List<ArtifactAnalyzer> analyzers = new ArrayList<ArtifactAnalyzer>();
		BundleClassPathArtifactAnalyzer bundleClassPathArtifactAnalyzer = new BundleClassPathArtifactAnalyzer(analyzers);

		contributors //
				.addArtifactAnalyzer(artifactAnalyzer)
				// .addArtifactAnalyzer(new AsmTypeArtefactAnalyser()) //
				.addArtifactAnalyzer(new StaticResourceArtifactAnalyzer()) //
				.addArtifactAnalyzer(new HibernateMappingArtifactAnalyzer()) //
				.addArtifactAnalyzer(new JpaPersistenceArtifactAnalyzer()) //
				.addArtifactAnalyzer(new Log4JXmlArtifactAnalyzer()) //
				.addArtifactAnalyzer(new SpringApplicationContextArtifactAnalyzer()) //
				.addArtifactAnalyzer(blueprintArtifactAnalyzer) //
				.addArtifactAnalyzer(new WebApplicationArtifactAnalyzer()) //
				.addArtifactAnalyzer(bundleClassPathArtifactAnalyzer) //
				.addArtifactAnalyzer(new JspArtifactAnalyzer());

		analyzers.add(artifactAnalyzer);
		analyzers.add(new StaticResourceArtifactAnalyzer());
		analyzers.add(new HibernateMappingArtifactAnalyzer());
		analyzers.add(new JpaPersistenceArtifactAnalyzer());
		analyzers.add(new Log4JXmlArtifactAnalyzer());
		analyzers.add(new SpringApplicationContextArtifactAnalyzer());
		analyzers.add(blueprintArtifactAnalyzer);
		analyzers.add(new WebApplicationArtifactAnalyzer());
		analyzers.add(bundleClassPathArtifactAnalyzer);
		analyzers.add(new JspArtifactAnalyzer());

		contributors //
				.addManifestReader(excludedImportAndExportPartialManifestModifier) //
				.addManifestReader(ignoredExistingHeadersManifestModifier) //
				.addManifestReader(blueprintArtifactAnalyzer);

		contributors //
				.addManifestModifier(placeholderManifestAndTemplateModifier) //
				.addManifestModifier(ignoredExistingHeadersManifestModifier) //
				.addManifestModifier(new ToolStampManifestModifier());

		contributors //
				.addManifestTemplateModifier(manifestTemplateDirectiveMigrator) //
				.addManifestTemplateModifier(placeholderManifestAndTemplateModifier) //
				.addManifestTemplateModifier(new OsgiProfileManifestTemplateModifier(properties));

		contributors //
				.addManifestContributor(bundleClassPathArtifactAnalyzer);

		contributors //
				.addPartialManifestModifier(manifestTemplateDirectiveMigrator) //
				.addPartialManifestModifier(excludedImportAndExportPartialManifestModifier);

		contributors //
				.addTemplateHeaderReader(excludedImportAndExportPartialManifestModifier) //
				.addTemplateHeaderReader(ignoredExistingHeadersManifestModifier) //
				.addTemplateHeaderReader(placeholderManifestAndTemplateModifier) //
				.addTemplateHeaderReader(partialManifestResolver);

		contributors //
				.setReadablePartialManifest(new StandardReadablePartialManifest());

		contributors //
				.setPartialManifestResolver(partialManifestResolver);

		return contributors;
	}

	private static Properties combineProperties(PropertiesSource... propertiesSources) {
		PropertiesSource[] sortedPropertiesSources = new PropertiesSource[propertiesSources.length];
		System.arraycopy(propertiesSources, 0, sortedPropertiesSources, 0, propertiesSources.length);
		// Sort by priority so that sources with lower priority are added first
		// into the final
		// Properties instance to allow for overriding by later instances
		Arrays.sort(sortedPropertiesSources, new Comparator<PropertiesSource>() {

			public int compare(PropertiesSource o1, PropertiesSource o2) {
				if (o1.getPriority() == o2.getPriority()) {
					return 0;
				} else if (o1.getPriority() > o2.getPriority()) {
					return 1;
				}
				return -1;
			}
		});

		Properties properties = new Properties();
		for (PropertiesSource source : propertiesSources) {
			properties.putAll(source.getProperties());
		}
		return properties;
	}

	static class HeaderRemovingManifestModifier implements ManifestModifier {

		public void modify(ManifestContents manifest) {
			manifest.getMainAttributes().remove("Import-Library");
			manifest.getMainAttributes().remove("Import-Bundle");
			manifest.getMainAttributes().remove("Export-Template");
			manifest.getMainAttributes().remove("Import-Template");
			manifest.getMainAttributes().remove("Excluded-Exports");
			manifest.getMainAttributes().remove("Excluded-Imports");
			manifest.getMainAttributes().remove("Ignored-Existing-Headers");
			manifest.getMainAttributes().remove("Test-Import-Package");
			manifest.getMainAttributes().remove("Test-Import-Library");
			manifest.getMainAttributes().remove("Test-Import-Bundle");
			manifest.getMainAttributes().remove("Import-Package");
			manifest.getMainAttributes().remove("Export-Package");
		}

	}

	static class HeaderRemovingTemplateManifestModifier implements ManifestTemplateModifier {

		public void modify(ManifestContents manifest) {
			manifest.getMainAttributes().remove("Test-Import-Package");
			manifest.getMainAttributes().remove("Test-Import-Library");
			manifest.getMainAttributes().remove("Test-Import-Bundle");
		}
	}
}
