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
package org.eclipse.virgo.ide.ui.editors.text;

import java.util.Enumeration;
import java.util.ResourceBundle;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.pde.internal.core.ICoreConstants;
import org.eclipse.pde.internal.ui.editor.PDESourcePage;
import org.eclipse.pde.internal.ui.editor.text.IColorManager;
import org.eclipse.pde.internal.ui.editor.text.IPDEColorConstants;
import org.eclipse.pde.internal.ui.editor.text.ManifestPartitionScanner;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.Constants;

/**
 * @author Christian Dupuis
 */
public class BundleManifestConfiguration extends AbstractPdeManifestConfiguration {

	private BundleManifestContentAssistProcessor fContentAssistantProcessor;

	protected class BundleManifestHeaderScanner extends ManifestHeaderScanner {

		private Token springToken;

		public BundleManifestHeaderScanner() {
			super();
		}

		@Override
		public boolean affectsTextPresentation(String property) {
			if (property.startsWith(ISpringColorConstants.P_HEADER_SPRING)) {
				return true;
			}
			return super.affectsTextPresentation(property);
		}

		@Override
		protected Token getTokenAffected(PropertyChangeEvent event) {
			if (event.getProperty().startsWith(ISpringColorConstants.P_HEADER_SPRING)) {
				return springToken;
			}
			return super.getTokenAffected(event);
		}

		@Override
		protected void initialize() {
			fToken = new Token(createTextAttribute(IPDEColorConstants.P_HEADER_OSGI));
			springToken = new Token(createTextAttribute(ISpringColorConstants.P_HEADER_SPRING));
			WordRule rule = new WordRule(new KeywordDetector(), Token.UNDEFINED, true);
			rule.addWord(Constants.BUNDLE_ACTIVATOR, fToken);
			rule.addWord(Constants.BUNDLE_CATEGORY, fToken);
			rule.addWord(Constants.BUNDLE_CLASSPATH, fToken);
			rule.addWord(Constants.BUNDLE_CONTACTADDRESS, fToken);
			rule.addWord(Constants.BUNDLE_COPYRIGHT, fToken);
			rule.addWord(Constants.BUNDLE_DESCRIPTION, fToken);
			rule.addWord(Constants.BUNDLE_DOCURL, fToken);
			rule.addWord(Constants.BUNDLE_LOCALIZATION, fToken);
			rule.addWord(Constants.BUNDLE_MANIFESTVERSION, fToken);
			rule.addWord(Constants.BUNDLE_NAME, fToken);
			rule.addWord(Constants.BUNDLE_NATIVECODE, fToken);
			rule.addWord(Constants.BUNDLE_REQUIREDEXECUTIONENVIRONMENT, fToken);
			rule.addWord(Constants.BUNDLE_SYMBOLICNAME, fToken);
			rule.addWord(Constants.BUNDLE_UPDATELOCATION, fToken);
			rule.addWord(Constants.BUNDLE_VENDOR, fToken);
			rule.addWord(Constants.BUNDLE_VERSION, fToken);
			rule.addWord(Constants.REQUIRE_BUNDLE, fToken);
			rule.addWord(Constants.DYNAMICIMPORT_PACKAGE, fToken);
			rule.addWord(Constants.EXPORT_PACKAGE, fToken);
			rule.addWord(ICoreConstants.EXPORT_SERVICE, fToken);
			rule.addWord(Constants.FRAGMENT_HOST, fToken);
			rule.addWord(Constants.IMPORT_PACKAGE, fToken);
			rule.addWord(ICoreConstants.IMPORT_SERVICE, fToken);
			rule.addWord(ICoreConstants.PROVIDE_PACKAGE, fToken);

			addRulesForHeaders(rule);

			setRules(new IRule[] { rule });
			setDefaultReturnToken(new Token(createTextAttribute(IPDEColorConstants.P_HEADER_KEY)));
		}

		private void addRulesForHeaders(WordRule rule) {
			ResourceBundle bundle = ResourceBundle.getBundle("org.eclipse.virgo.ide.ui.editors.text.headers");
			Enumeration<String> headers = bundle.getKeys();
			while (headers.hasMoreElements()) {
				rule.addWord(headers.nextElement(), springToken);
			}
		}
	}

	protected class BundleManifestValueScanner extends ManifestValueScanner {

		public BundleManifestValueScanner() {
			super();
		}

		@Override
		protected void initialize() {
			IRule[] rules = new IRule[2];
			fAssignmentToken = new Token(createTextAttribute(IPDEColorConstants.P_HEADER_ASSIGNMENT));
			rules[0] = new WordRule(new AssignmentDetector(), fAssignmentToken);

			fAttributeToken = new Token(createTextAttribute(IPDEColorConstants.P_HEADER_ATTRIBUTES));
			WordRule rule = new WordRule(new KeywordDetector());
			rule.addWord(Constants.BUNDLE_NATIVECODE_LANGUAGE, fAttributeToken);
			rule.addWord(Constants.BUNDLE_NATIVECODE_OSNAME, fAttributeToken);
			rule.addWord(Constants.BUNDLE_NATIVECODE_OSVERSION, fAttributeToken);
			rule.addWord(Constants.BUNDLE_NATIVECODE_PROCESSOR, fAttributeToken);
			rule.addWord(Constants.BUNDLE_SYMBOLICNAME_ATTRIBUTE, fAttributeToken);
			rule.addWord(Constants.BUNDLE_VERSION_ATTRIBUTE, fAttributeToken);
			rule.addWord(Constants.EXCLUDE_DIRECTIVE, fAttributeToken);
			rule.addWord(Constants.FRAGMENT_ATTACHMENT_DIRECTIVE, fAttributeToken);
			rule.addWord(Constants.INCLUDE_DIRECTIVE, fAttributeToken);
			rule.addWord(Constants.MANDATORY_DIRECTIVE, fAttributeToken);
			rule.addWord(Constants.RESOLUTION_DIRECTIVE, fAttributeToken);
			rule.addWord(Constants.SINGLETON_DIRECTIVE, fAttributeToken);
			rule.addWord(Constants.USES_DIRECTIVE, fAttributeToken);
			rule.addWord(Constants.VERSION_ATTRIBUTE, fAttributeToken);
			rule.addWord(Constants.VISIBILITY_DIRECTIVE, fAttributeToken);
			rule.addWord(ICoreConstants.FRIENDS_DIRECTIVE, fAttributeToken);
			rule.addWord(ICoreConstants.INTERNAL_DIRECTIVE, fAttributeToken);
			rule.addWord(ICoreConstants.PACKAGE_SPECIFICATION_VERSION, fAttributeToken);

			// EASTER EGG
			for (String element : ICoreConstants.EE_TOKENS) {
				rule.addWord(element, fAttributeToken);
			}

			addRulesForAttributes(rule);
			rules[1] = rule;

			setRules(rules);
			setDefaultReturnToken(new Token(createTextAttribute(IPDEColorConstants.P_HEADER_VALUE)));
		}

		private void addRulesForAttributes(WordRule rule) {
			ResourceBundle bundle = ResourceBundle.getBundle("org.eclipse.virgo.ide.ui.editors.text.attributes");
			Enumeration<String> attributes = bundle.getKeys();
			while (attributes.hasMoreElements()) {
				rule.addWord(attributes.nextElement(), fAttributeToken);
			}
		}

	}

	public BundleManifestConfiguration(IColorManager manager) {
		this(manager, null, null);
	}

	public BundleManifestConfiguration(IColorManager manager, PDESourcePage page) {
		this(manager, page, null);
	}

	public BundleManifestConfiguration(IColorManager manager, PDESourcePage page, String documentPartitioning) {
		super(manager, page, documentPartitioning);
		fPropertyKeyScanner = new BundleManifestHeaderScanner();
		fPropertyValueScanner = new BundleManifestValueScanner();
	}

	@Override
	public void dispose() {
		if (fQuickAssistant != null) {
			fQuickAssistant.dispose();
		}
		if (fContentAssistant != null) {
			fContentAssistantProcessor.dispose();
		}
	}

	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		if (fSourcePage != null && fSourcePage.isEditable()) {
			if (fContentAssistant == null) {
				fContentAssistant = new ContentAssistant();
				fContentAssistant.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
				fContentAssistantProcessor = new BundleManifestContentAssistProcessor(fSourcePage);
				fContentAssistant.setContentAssistProcessor(fContentAssistantProcessor, IDocument.DEFAULT_CONTENT_TYPE);
				fContentAssistant.setContentAssistProcessor(fContentAssistantProcessor,
						ManifestPartitionScanner.MANIFEST_HEADER_VALUE);
				fContentAssistant.addCompletionListener(fContentAssistantProcessor);
				fContentAssistant.setInformationControlCreator(new IInformationControlCreator() {
					public IInformationControl createInformationControl(Shell parent) {
						return new JFaceDefaultInformationControl(parent, false);
					}
				});
				fContentAssistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
			}
			return fContentAssistant;
		}
		return null;
	}
}
