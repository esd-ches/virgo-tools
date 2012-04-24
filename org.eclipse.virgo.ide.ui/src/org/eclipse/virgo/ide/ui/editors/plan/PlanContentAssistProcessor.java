/*******************************************************************************
 * Copyright (c) 2010 - 2012 SpringSource, a divison of VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource, a division of VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.virgo.ide.ui.editors.plan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.virgo.ide.bundlerepository.domain.OsgiVersion;
import org.eclipse.virgo.ide.runtime.core.artefacts.Artefact;
import org.eclipse.virgo.ide.runtime.core.artefacts.IArtefact;
import org.eclipse.virgo.ide.runtime.core.provisioning.RepositoryUtils;
import org.eclipse.virgo.ide.ui.ServerIdeUiPlugin;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegionList;
import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.eclipse.wst.xml.ui.internal.contentassist.DefaultXMLCompletionProposalComputer;
import org.w3c.dom.Node;

/**
 * Content assist processor for plan files.
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @since 2.3.1
 */
public class PlanContentAssistProcessor extends DefaultXMLCompletionProposalComputer {

	private static final List<String> TYPES = Arrays.asList(new String[] { "configuration", "bundle", "plan", "par" });

	@Override
	protected void addAttributeValueProposals(ContentAssistRequest request, CompletionProposalInvocationContext context) {
		super.addAttributeValueProposals(request, context);
		String attr = findAttribute(request);
		if (attr != null) {
			if (attr.equals("type")) {
				computeTypeContentProposals(request, context);
			}
			else if (attr.equals("name")) {
				computeNameContentProposals(request, context);
			}
			else if (attr.equals("version")) {
				computeVersionContentProposals(request, context);
			}
		}
	}

	private void computeNameContentProposals(ContentAssistRequest request, CompletionProposalInvocationContext context) {
		String prefix = cleanInput(request.getMatchString());
		String type = getAttributeValue(request.getNode(), "type");
		if (type == null || "bundle".equals(type)) {
			IFile file = getFile(request);
			if (file != null) {
				Set<Artefact> bundles = RepositoryUtils.getImportBundleProposals(file.getProject(), prefix);
				for (IArtefact bundle : bundles) {
					String value = "\"" + bundle.getSymbolicName() + "\"";
					request.addProposal(new CompletionProposal(value, request.getReplacementBeginPosition(), request
							.getReplacementLength(), value.length(), ServerIdeUiPlugin
							.getImage("full/obj16/osgi_obj.gif"), value, null, null));
				}
			}
		}
	}

	private void computeTypeContentProposals(ContentAssistRequest request, CompletionProposalInvocationContext context) {
		String prefix = cleanInput(request.getMatchString());
		for (String type : TYPES) {
			if (type.startsWith(prefix)) {
				String value = "\"" + type + "\"";
				request.addProposal(new CompletionProposal(value, request.getReplacementBeginPosition(), request
						.getReplacementLength(), value.length()));
				;
			}
		}
	}

	private void computeVersionContentProposals(ContentAssistRequest request,
			CompletionProposalInvocationContext context) {
		String prefix = cleanInput(request.getMatchString());
		String bundleId = getAttributeValue(request.getNode(), "name");
		String type = getAttributeValue(request.getNode(), "type");
		if (type == null || "bundle".equals(type)) {
			IFile file = getFile(request);
			if (file != null) {
				Set<Artefact> bundles = RepositoryUtils.getImportBundleProposals(file.getProject(), bundleId);
				for (IArtefact element : bundles) {
					if (element.getSymbolicName().equalsIgnoreCase(bundleId)) {
						List<String> proposalValues = getVersionProposals(element.getVersion());
						for (String proposalValue : proposalValues) {
							if (proposalValue.regionMatches(0, prefix, 0, prefix.length())) {
								String value = "\"" + proposalValue + "\"";
								request.addProposal(new CompletionProposal(value,
										request.getReplacementBeginPosition(), request.getReplacementLength(), value
												.length()));
							}
						}
					}
				}
			}
		}
	}

	private String cleanInput(String input) {
		if (input != null && input.startsWith("\"")) {
			input = input.substring(1);
		}
		if (input != null && input.endsWith("\"")) {
			input = input.substring(0, input.length() - 1);
		}
		return input;
	}

	private String findAttribute(ContentAssistRequest request) {
		IDOMNode node = (IDOMNode) request.getNode();
		IStructuredDocumentRegion nodeRegion = node.getFirstStructuredDocumentRegion();
		ITextRegionList regionList = nodeRegion.getRegions();
		int i = regionList.indexOf(request.getRegion());
		while (i >= 0) {
			ITextRegion region = regionList.get(i);
			if (region.getType() == DOMRegionContext.XML_TAG_ATTRIBUTE_NAME) {
				return nodeRegion.getText(region);
			}
			i--;
		}
		return "";
	}

	private String getAttributeValue(Node node, String attrName) {
		if (node != null && node.hasAttributes() && node.getAttributes().getNamedItem(attrName) != null) {
			return node.getAttributes().getNamedItem(attrName).getNodeValue();
		}
		return null;
	}

	private IFile getFile(ContentAssistRequest request) {
		String location = null;
		IStructuredDocumentRegion docRegion = request.getDocumentRegion();
		if (docRegion != null) {
			IDocument document = docRegion.getParentDocument();
			IStructuredModel model = null;
			try {
				model = StructuredModelManager.getModelManager().getExistingModelForRead(document);
				location = model.getBaseLocation();
			}
			finally {
				if (model != null) {
					model.releaseFromRead();
				}
			}
		}
		if (location != null) {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IPath path = new Path(location);
			return root.getFile(path);
		}
		return null;
	}

	private List<String> getVersionProposals(OsgiVersion version) {
		Set<String> versionStrings = new LinkedHashSet<String>();
		versionStrings.add(version.toString());
		List<String> versions = new ArrayList<String>();
		for (String ver : RepositoryUtils.getVersionProposals(versionStrings)) {
			versions.add(ver);
		}
		return versions;
	}

}
