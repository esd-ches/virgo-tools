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
package org.eclipse.virgo.ide.ui.editors.text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.pde.core.IBaseModel;
import org.eclipse.pde.internal.core.ibundle.IBundleModel;
import org.eclipse.pde.internal.ui.PDEPluginImages;
import org.eclipse.pde.internal.ui.editor.PDEFormEditor;
import org.eclipse.pde.internal.ui.editor.PDESourcePage;
import org.eclipse.swt.graphics.Image;
import org.eclipse.virgo.ide.bundlerepository.domain.OsgiVersion;
import org.eclipse.virgo.ide.bundlerepository.domain.PackageExport;
import org.eclipse.virgo.ide.manifest.core.IHeaderConstants;
import org.eclipse.virgo.ide.runtime.core.artefacts.Artefact;
import org.eclipse.virgo.ide.runtime.core.artefacts.IArtefact;
import org.eclipse.virgo.ide.runtime.core.provisioning.RepositoryUtils;
import org.osgi.framework.Constants;


/**
 * @author Christian Dupuis
 * @author Leo Dos Santos
 */
public class BundleManifestContentAssistProcessor extends AbstractPdeManifestContentAssistProcessor {

	private IJavaProject fJP;

	public BundleManifestContentAssistProcessor(PDESourcePage sourcePage) {
		super(sourcePage);
	}

	static {
		List<String> headers = new ArrayList<String>(Arrays.asList(fHeader));
		ResourceBundle bundle = ResourceBundle.getBundle("org.eclipse.virgo.ide.ui.editors.text.headers");
		Enumeration<String> values = bundle.getKeys();
		while (values.hasMoreElements()) {
			headers.add(values.nextElement());
		}
		Collections.sort(headers);
		fHeader = headers.toArray(new String[headers.size()]);
	}

	@Override
	protected ICompletionProposal[] computeHeader(String currentValue, int startOffset, int offset) {
		ArrayList<BundleTypeCompletionProposal> completions = new ArrayList<BundleTypeCompletionProposal>();
		IBaseModel model = fSourcePage.getInputContext().getModel();
		int length = fHeader.length;
		if (model instanceof IBundleModel && !((IBundleModel) model).isFragmentModel()) {
			--length;
		}
		for (String element : fHeader) {
			if (element.regionMatches(true, 0, currentValue, 0, currentValue.length()) && fHeaders.get(element) == null) {
				BundleTypeCompletionProposal proposal = new BundleTypeCompletionProposal(
						element + ": ", getImage(F_TYPE_HEADER), //$NON-NLS-1$
						element, startOffset, currentValue.length());
				proposal.setAdditionalProposalInfo(getJavaDoc(element));
				completions.add(proposal);
			}
		}
		return completions.toArray(new ICompletionProposal[completions.size()]);
	}

	@Override
	protected final boolean shouldStoreSet(String header) {
		if (header.equalsIgnoreCase(IHeaderConstants.IMPORT_BUNDLE)
				|| header.equalsIgnoreCase(IHeaderConstants.IMPORT_LIBRARY)
				|| header.equalsIgnoreCase(IHeaderConstants.EXPORT_TEMPLATE)
				|| header.equalsIgnoreCase(IHeaderConstants.IMPORT_TEMPLATE)
				|| header.equalsIgnoreCase(IHeaderConstants.EXCLUDED_IMPORTS)
				|| header.equalsIgnoreCase(IHeaderConstants.EXCLUDED_EXPORTS)
				|| header.equalsIgnoreCase(IHeaderConstants.UNVERSIONED_IMPORTS)) {
			return true;
		}
		return super.shouldStoreSet(header);
	}

	@Override
	protected ICompletionProposal[] computeValue(IDocument doc, int startOffset, int offset)
			throws BadLocationException {
		String value = doc.get(startOffset, offset - startOffset);
		int lineNum = doc.getLineOfOffset(startOffset) - 1;
		int index;
		while ((index = value.indexOf(':')) == -1
				|| ((value.length() - 1 != index) && (value.charAt(index + 1) == '='))) {
			int startLine = doc.getLineOffset(lineNum);
			value = doc.get(startLine, offset - startLine);
			lineNum--;
		}
		int length = value.length();
		if (value.regionMatches(true, 0, IHeaderConstants.IMPORT_BUNDLE, 0,
				Math.min(length, IHeaderConstants.IMPORT_BUNDLE.length()))) {
			return handleImportBundleCompletion(value.substring(IHeaderConstants.IMPORT_BUNDLE.length() + 1), offset);
		}
		if (value.regionMatches(true, 0, IHeaderConstants.IMPORT_LIBRARY, 0,
				Math.min(length, IHeaderConstants.IMPORT_LIBRARY.length()))) {
			return handleImportLibraryCompletion(value.substring(IHeaderConstants.IMPORT_LIBRARY.length() + 1), offset);
		}
		if (value.regionMatches(true, 0, IHeaderConstants.IMPORT_TEMPLATE, 0,
				Math.min(length, IHeaderConstants.IMPORT_TEMPLATE.length()))) {
			return handleImportPackageCompletion(value.substring(IHeaderConstants.IMPORT_TEMPLATE.length() + 1), offset);
		}
		if (value.regionMatches(true, 0, IHeaderConstants.EXPORT_TEMPLATE, 0,
				Math.min(length, IHeaderConstants.EXPORT_TEMPLATE.length()))) {
			return handleExportPackageCompletion(value.substring(IHeaderConstants.EXPORT_TEMPLATE.length() + 1), offset);
		}
		if (value.regionMatches(true, 0, IHeaderConstants.EXCLUDED_IMPORTS, 0,
				Math.min(length, IHeaderConstants.EXCLUDED_IMPORTS.length()))) {
			return handleImportPackageCompletion(value.substring(IHeaderConstants.EXCLUDED_IMPORTS.length() + 1),
					offset);
		}
		if (value.regionMatches(true, 0, IHeaderConstants.EXCLUDED_EXPORTS, 0,
				Math.min(length, IHeaderConstants.EXCLUDED_EXPORTS.length()))) {
			return handleExportPackageCompletion(value.substring(IHeaderConstants.EXCLUDED_EXPORTS.length() + 1),
					offset);
		}
		if (value.regionMatches(true, 0, IHeaderConstants.UNVERSIONED_IMPORTS, 0,
				Math.min(length, IHeaderConstants.UNVERSIONED_IMPORTS.length()))) {
			return handleImportPackageCompletion(value.substring(IHeaderConstants.UNVERSIONED_IMPORTS.length() + 1),
					offset);
		}
		return super.computeValue(doc, startOffset, offset);
	}

	@Override
	protected ICompletionProposal[] handleImportPackageCompletion(String currentValue, int offset) {
		int comma = currentValue.lastIndexOf(',');
		int semicolon = currentValue.lastIndexOf(';');
		String value = comma != -1 ? currentValue.substring(comma + 1) : currentValue;

		if (comma > semicolon || comma == semicolon) {
			HashSet set = (HashSet) fHeaders.get(Constants.IMPORT_PACKAGE);
			if (set == null) {
				set = parseHeaderForValues(currentValue, offset);
			}

			value = removeLeadingSpaces(value);
			int length = value.length();
			set.remove(value);
			ArrayList<BundleTypeCompletionProposal> completions = new ArrayList<BundleTypeCompletionProposal>();

			Set<PackageExport> packages = RepositoryUtils.getImportPackageProposals(getProject(), value);
			for (PackageExport proposal : packages) {
				if (!set.contains(proposal.getName())) {
					completions.add(new BundleTypeCompletionProposal(proposal.getName(), getImage(F_TYPE_PKG), proposal
							.getName(), offset - length, length));
					set.add(proposal.getName());
				}
			}

			ICompletionProposal[] proposals = completions.toArray(new ICompletionProposal[completions.size()]);
			sortCompletions(proposals);
			return proposals;
		}

		int equals = currentValue.lastIndexOf('=');
		if (equals == -1 || semicolon > equals) {
			String[] validAtts = new String[] { Constants.VERSION_ATTRIBUTE, Constants.RESOLUTION_DIRECTIVE };
			Integer[] validTypes = new Integer[] { new Integer(F_TYPE_ATTRIBUTE), new Integer(F_TYPE_DIRECTIVE) };
			return handleAttrsAndDirectives(value, initializeNewList(validAtts), initializeNewList(validTypes), offset);
		}

		String attributeValue = removeLeadingSpaces(currentValue.substring(semicolon + 1));
		if (Constants.RESOLUTION_DIRECTIVE.regionMatches(true, 0, attributeValue, 0,
				Constants.RESOLUTION_DIRECTIVE.length())) {
			return matchValueCompletion(currentValue.substring(equals + 1), new String[] {
					Constants.RESOLUTION_MANDATORY, Constants.RESOLUTION_OPTIONAL }, new int[] { F_TYPE_VALUE,
					F_TYPE_VALUE }, offset, "RESOLUTION_");
		}
		if (Constants.VERSION_ATTRIBUTE.regionMatches(true, 0, attributeValue, 0, Constants.VERSION_ATTRIBUTE.length())) {
			String pluginId = removeLeadingSpaces(currentValue.substring((comma == -1) ? 0 : comma + 1, semicolon));
			return getPackageVersionCompletions(pluginId, removeLeadingSpaces(currentValue.substring(equals + 1)),
					offset);
		}
		return new ICompletionProposal[0];
	}

	@SuppressWarnings("unchecked")
	@Override
	protected ICompletionProposal[] handleFragmentHostCompletion(String currentValue, int offset) {
		int comma = currentValue.lastIndexOf(',');
		int semicolon = currentValue.lastIndexOf(';');
		String value = comma != -1 ? currentValue.substring(comma + 1) : currentValue;

		if (comma > semicolon || comma == semicolon) {
			Set<String> set = (HashSet<String>) fHeaders.get(Constants.REQUIRE_BUNDLE);
			if (set == null) {
				set = parseHeaderForValues(currentValue, offset);
			}

			value = removeLeadingSpaces(value);
			int length = value.length();
			set.remove(value);
			ArrayList<BundleTypeCompletionProposal> completions = new ArrayList<BundleTypeCompletionProposal>();

			Set<Artefact> bundles = RepositoryUtils.getImportBundleProposals(getProject(), value);
			for (IArtefact proposal : bundles) {
				if (!set.contains(proposal.getSymbolicName())) {
					completions.add(new BundleTypeCompletionProposal(proposal.getSymbolicName(),
							getImage(F_TYPE_BUNDLE), proposal.getSymbolicName(), offset - length, length));
					set.add(proposal.getSymbolicName());
				}
			}

			ICompletionProposal[] proposals = completions.toArray(new ICompletionProposal[completions.size()]);
			sortCompletions(proposals);
			return proposals;
		}
		int equals = currentValue.lastIndexOf('=');
		if (equals == -1 || semicolon > equals) {
			return matchValueCompletion(removeLeadingSpaces(currentValue.substring(semicolon + 1)),
					new String[] { Constants.BUNDLE_VERSION_ATTRIBUTE }, new int[] { F_TYPE_ATTRIBUTE }, offset);
		}
		String attributeValue = removeLeadingSpaces(currentValue.substring(semicolon + 1));
		if (Constants.BUNDLE_VERSION_ATTRIBUTE.regionMatches(true, 0, attributeValue, 0,
				Constants.BUNDLE_VERSION_ATTRIBUTE.length())) {
			String pluginId = removeLeadingSpaces(currentValue.substring((comma == -1) ? 0 : comma + 1, semicolon));
			return getSpringBundleVersionCompletions(pluginId, removeLeadingSpaces(currentValue.substring(equals + 1)),
					offset);
		}
		return new ICompletionProposal[0];
	}

	private ICompletionProposal[] handleImportLibraryCompletion(String currentValue, int offset) {
		int comma = currentValue.lastIndexOf(',');
		int semicolon = currentValue.lastIndexOf(';');
		String value = comma != -1 ? currentValue.substring(comma + 1) : currentValue;

		if (comma > semicolon || comma == semicolon) {
			HashSet set = (HashSet) fHeaders.get(IHeaderConstants.IMPORT_LIBRARY);
			if (set == null) {
				set = parseHeaderForValues(currentValue, offset);
			}

			value = removeLeadingSpaces(value);
			int length = value.length();
			set.remove(value);
			ArrayList<BundleTypeCompletionProposal> completions = new ArrayList<BundleTypeCompletionProposal>();

			Set<Artefact> libraries = RepositoryUtils.getImportLibraryProposals(getProject(), value);
			for (IArtefact proposal : libraries) {
				if (!set.contains(proposal.getSymbolicName())) {
					completions.add(new BundleTypeCompletionProposal(proposal.getSymbolicName(), getImage(F_TYPE_LIB),
							proposal.getSymbolicName(), offset - length, length));
					set.add(proposal.getSymbolicName());
				}
			}

			ICompletionProposal[] proposals = completions.toArray(new ICompletionProposal[completions.size()]);
			sortCompletions(proposals);
			return proposals;
		}

		int equals = currentValue.lastIndexOf('=');
		if (equals == -1 || semicolon > equals) {
			String[] validAtts = new String[] { Constants.VERSION_ATTRIBUTE, Constants.RESOLUTION_DIRECTIVE,
					IHeaderConstants.PROMOTES_DIRECTIVE };
			Integer[] validTypes = new Integer[] { new Integer(F_TYPE_ATTRIBUTE), new Integer(F_TYPE_DIRECTIVE),
					new Integer(F_TYPE_DIRECTIVE) };
			return handleAttrsAndDirectives(value, initializeNewList(validAtts), initializeNewList(validTypes), offset);
		}

		String attributeValue = removeLeadingSpaces(currentValue.substring(semicolon + 1));
		if (Constants.RESOLUTION_DIRECTIVE.regionMatches(true, 0, attributeValue, 0,
				Constants.RESOLUTION_DIRECTIVE.length())) {
			return matchValueCompletion(currentValue.substring(equals + 1), new String[] {
					Constants.RESOLUTION_MANDATORY, Constants.RESOLUTION_OPTIONAL }, new int[] { F_TYPE_VALUE,
					F_TYPE_VALUE }, offset, "RESOLUTION_");
		}
		if (IHeaderConstants.PROMOTES_DIRECTIVE.regionMatches(true, 0, attributeValue, 0,
				IHeaderConstants.PROMOTES_DIRECTIVE.length())) {
			return handleTrueFalseValue(currentValue.substring(equals + 1), offset);
		}
		if (Constants.VERSION_ATTRIBUTE.regionMatches(true, 0, attributeValue, 0, Constants.VERSION_ATTRIBUTE.length())) {
			String pluginId = removeLeadingSpaces(currentValue.substring((comma == -1) ? 0 : comma + 1, semicolon));
			return getLibraryVersionCompletions(pluginId, removeLeadingSpaces(currentValue.substring(equals + 1)),
					offset);
		}
		return new ICompletionProposal[0];
	}

	private ICompletionProposal[] handleImportBundleCompletion(String currentValue, int offset) {
		int comma = currentValue.lastIndexOf(',');
		int semicolon = currentValue.lastIndexOf(';');
		String value = comma != -1 ? currentValue.substring(comma + 1) : currentValue;

		if (comma > semicolon || comma == semicolon) {
			HashSet set = (HashSet) fHeaders.get(IHeaderConstants.IMPORT_BUNDLE);
			if (set == null) {
				set = parseHeaderForValues(currentValue, offset);
			}

			value = removeLeadingSpaces(value);
			int length = value.length();
			set.remove(value);
			ArrayList<BundleTypeCompletionProposal> completions = new ArrayList<BundleTypeCompletionProposal>();

			Set<Artefact> bundles = RepositoryUtils.getImportBundleProposals(getProject(), value);
			for (IArtefact proposal : bundles) {
				if (!set.contains(proposal.getSymbolicName())) {
					completions.add(new BundleTypeCompletionProposal(proposal.getSymbolicName(),
							getImage(F_TYPE_BUNDLE), proposal.getSymbolicName(), offset - length, length));
					set.add(proposal.getSymbolicName());
				}
			}

			ICompletionProposal[] proposals = completions.toArray(new ICompletionProposal[completions.size()]);
			sortCompletions(proposals);
			return proposals;
		}

		int equals = currentValue.lastIndexOf('=');
		if (equals == -1 || semicolon > equals) {
			String[] validAtts = new String[] { Constants.VERSION_ATTRIBUTE, Constants.RESOLUTION_DIRECTIVE };
			Integer[] validTypes = new Integer[] { new Integer(F_TYPE_ATTRIBUTE), new Integer(F_TYPE_DIRECTIVE) };
			return handleAttrsAndDirectives(value, initializeNewList(validAtts), initializeNewList(validTypes), offset);
		}

		String attributeValue = removeLeadingSpaces(currentValue.substring(semicolon + 1));
		if (Constants.RESOLUTION_DIRECTIVE.regionMatches(true, 0, attributeValue, 0,
				Constants.RESOLUTION_DIRECTIVE.length())) {
			return matchValueCompletion(currentValue.substring(equals + 1), new String[] {
					Constants.RESOLUTION_MANDATORY, Constants.RESOLUTION_OPTIONAL }, new int[] { F_TYPE_VALUE,
					F_TYPE_VALUE }, offset, "RESOLUTION_");
		}
		if (Constants.VERSION_ATTRIBUTE.regionMatches(true, 0, attributeValue, 0, Constants.VERSION_ATTRIBUTE.length())) {
			String pluginId = removeLeadingSpaces(currentValue.substring((comma == -1) ? 0 : comma + 1, semicolon));
			return getSpringBundleVersionCompletions(pluginId, removeLeadingSpaces(currentValue.substring(equals + 1)),
					offset);
		}
		return new ICompletionProposal[0];
	}

	@Override
	protected ICompletionProposal[] handleRequireBundleCompletion(String currentValue, int offset) {
		int comma = currentValue.lastIndexOf(',');
		int semicolon = currentValue.lastIndexOf(';');
		String value = comma != -1 ? currentValue.substring(comma + 1) : currentValue;

		if (comma > semicolon || comma == semicolon) {
			HashSet set = (HashSet) fHeaders.get(Constants.REQUIRE_BUNDLE);
			if (set == null) {
				set = parseHeaderForValues(currentValue, offset);
			}

			value = removeLeadingSpaces(value);
			int length = value.length();
			set.remove(value);
			ArrayList<BundleTypeCompletionProposal> completions = new ArrayList<BundleTypeCompletionProposal>();

			Set<Artefact> bundles = RepositoryUtils.getImportBundleProposals(getProject(), value);
			for (IArtefact proposal : bundles) {
				if (!set.contains(proposal.getSymbolicName())) {
					completions.add(new BundleTypeCompletionProposal(proposal.getSymbolicName(),
							getImage(F_TYPE_BUNDLE), proposal.getSymbolicName(), offset - length, length));
					set.add(proposal.getSymbolicName());
				}
			}

			ICompletionProposal[] proposals = completions.toArray(new ICompletionProposal[completions.size()]);
			sortCompletions(proposals);
			return proposals;
		}
		int equals = currentValue.lastIndexOf('=');
		if (equals == -1 || semicolon > equals) {
			String[] validAttrs = new String[] { Constants.BUNDLE_VERSION_ATTRIBUTE, Constants.RESOLUTION_DIRECTIVE,
					Constants.VISIBILITY_DIRECTIVE };
			Integer[] validTypes = new Integer[] { new Integer(F_TYPE_ATTRIBUTE), new Integer(F_TYPE_DIRECTIVE),
					new Integer(F_TYPE_DIRECTIVE) };
			return handleAttrsAndDirectives(value, initializeNewList(validAttrs), initializeNewList(validTypes), offset);
		}

		String attributeValue = removeLeadingSpaces(currentValue.substring(semicolon + 1));
		if (Constants.VISIBILITY_DIRECTIVE.regionMatches(true, 0, attributeValue, 0,
				Constants.VISIBILITY_DIRECTIVE.length())) {
			return matchValueCompletion(currentValue.substring(equals + 1), new String[] {
					Constants.VISIBILITY_PRIVATE, Constants.VISIBILITY_REEXPORT }, new int[] { F_TYPE_VALUE,
					F_TYPE_VALUE }, offset, "VISIBILITY_");
		}
		if (Constants.RESOLUTION_DIRECTIVE.regionMatches(true, 0, attributeValue, 0,
				Constants.RESOLUTION_DIRECTIVE.length())) {
			return matchValueCompletion(currentValue.substring(equals + 1), new String[] {
					Constants.RESOLUTION_MANDATORY, Constants.RESOLUTION_OPTIONAL }, new int[] { F_TYPE_VALUE,
					F_TYPE_VALUE }, offset, "RESOLUTION_");
		}
		if (Constants.BUNDLE_VERSION_ATTRIBUTE.regionMatches(true, 0, attributeValue, 0,
				Constants.RESOLUTION_DIRECTIVE.length())) {
			String pluginId = removeLeadingSpaces(currentValue.substring((comma == -1) ? 0 : comma + 1, semicolon));
			return getSpringBundleVersionCompletions(pluginId, removeLeadingSpaces(currentValue.substring(equals + 1)),
					offset);
		}
		return new ICompletionProposal[0];
	}

	private IProject getProject() {
		if (fJP == null) {
			IProject project = ((PDEFormEditor) fSourcePage.getEditor()).getCommonProject();
			fJP = JavaCore.create(project);
		}
		return fJP.getProject();
	}

	private ICompletionProposal[] getSpringBundleVersionCompletions(String bundleId, String existingValue, int offset) {
		bundleId = getSymbolicName(bundleId);
		Set<Artefact> bundles = RepositoryUtils.getImportBundleProposals(getProject(), bundleId);
		if (bundles.size() > 0) {
			ArrayList<BundleTypeCompletionProposal> proposals = new ArrayList<BundleTypeCompletionProposal>(
					bundles.size());
			for (IArtefact element : bundles) {
				if (element.getSymbolicName().equalsIgnoreCase(bundleId)) {
					List<String> proposalValues = getVersionProposals(element.getVersion());
					for (String proposalValue : proposalValues) {
						if (proposalValue.regionMatches(0, existingValue, 0, existingValue.length())) {
							proposals.add(new BundleTypeCompletionProposal(proposalValue.substring(existingValue
									.length()), getImage(F_TYPE_VALUE), proposalValue, offset, 0));
						}
					}
				}
			}
			return proposals.toArray(new ICompletionProposal[proposals.size()]);
		}
		else if (existingValue.length() == 0) {
			return new ICompletionProposal[] { new BundleTypeCompletionProposal("\"\"", getImage(F_TYPE_VALUE), "\"\"",
					offset, 0) };
		}
		return new ICompletionProposal[0];
	}

	private ICompletionProposal[] getLibraryVersionCompletions(String libraryId, String existingValue, int offset) {
		libraryId = getSymbolicName(libraryId);
		Set<Artefact> libraries = RepositoryUtils.getImportLibraryProposals(getProject(), libraryId);
		if (libraries.size() > 0) {
			ArrayList<BundleTypeCompletionProposal> proposals = new ArrayList<BundleTypeCompletionProposal>(
					libraries.size());
			for (IArtefact element : libraries) {
				if (element.getSymbolicName().equalsIgnoreCase(libraryId)) {
					List<String> proposalValues = getVersionProposals(element.getVersion());
					for (String proposalValue : proposalValues) {
						if (proposalValue.regionMatches(0, existingValue, 0, existingValue.length())) {
							proposals.add(new BundleTypeCompletionProposal(proposalValue.substring(existingValue
									.length()), getImage(F_TYPE_VALUE), proposalValue, offset, 0));
						}
					}
				}
			}
			return proposals.toArray(new ICompletionProposal[proposals.size()]);
		}
		else if (existingValue.length() == 0) {
			return new ICompletionProposal[] { new BundleTypeCompletionProposal("\"\"", getImage(F_TYPE_VALUE), "\"\"",
					offset, 0) };
		}
		return new ICompletionProposal[0];
	}

	private ICompletionProposal[] getPackageVersionCompletions(String packageId, String existingValue, int offset) {
		Set<String> proposedVersions = new HashSet<String>();
		boolean wildcard = packageId.endsWith(".*");
		packageId = getSymbolicName(packageId);
		Set<PackageExport> packages = RepositoryUtils.getImportPackageProposals(getProject(), packageId);
		if (packages.size() > 0) {
			ArrayList<BundleTypeCompletionProposal> proposals = new ArrayList<BundleTypeCompletionProposal>(
					packages.size());
			for (PackageExport element : packages) {
				if (wildcard || element.getName().equalsIgnoreCase(packageId)) {
					List<String> proposalValues = getVersionProposals(element.getVersion());
					for (String proposalValue : proposalValues) {
						if (!proposedVersions.contains(proposalValue)
								&& proposalValue.regionMatches(0, existingValue, 0, existingValue.length())) {
							proposedVersions.add(proposalValue);
							proposals.add(new BundleTypeCompletionProposal(proposalValue.substring(existingValue
									.length()), getImage(F_TYPE_VALUE), proposalValue, offset, 0));
						}
					}
				}
			}
			return proposals.toArray(new ICompletionProposal[proposals.size()]);
		}
		else if (existingValue.length() == 0) {
			return new ICompletionProposal[] { new BundleTypeCompletionProposal("\"\"", getImage(F_TYPE_VALUE), "\"\"",
					offset, 0) };
		}
		return new ICompletionProposal[0];
	}

	private List<String> getVersionProposals(OsgiVersion version) {
		Set<String> versionStrings = new LinkedHashSet<String>();
		versionStrings.add(version.toString());
		List<String> versions = new ArrayList<String>();
		for (String ver : RepositoryUtils.getVersionProposals(versionStrings)) {
			versions.add(new StringBuilder().append("\"").append(ver).append("\"").toString());
		}
		return versions;
	}

	private String getSymbolicName(String string) {
		if (string.endsWith(".*")) {
			string = string.substring(0, string.length() - 2);
		}
		int ix = string.indexOf(';');
		if (ix > 0) {
			return string.substring(0, ix);
		}
		return string;
	}

	@Override
	public Image getImage(int type) {
		if (type >= 0 && type < F_TOTAL_TYPES) {
			if (fImages[type] == null) {
				switch (type) {
				case F_TYPE_LIB:
					return fImages[type] = PDEPluginImages.DESC_JAR_LIB_OBJ.createImage();
				case F_TYPE_BUNDLE:
					return fImages[type] = PDEPluginImages.DESC_BUNDLE_OBJ.createImage();
				}
			}
			else {
				return fImages[type];
			}
		}
		return super.getImage(type);
	}
}
