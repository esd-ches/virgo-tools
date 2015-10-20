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
                return this.springToken;
            }
            return super.getTokenAffected(event);
        }

        @Override
        protected void initialize() {
            this.fToken = new Token(createTextAttribute(IPDEColorConstants.P_HEADER_OSGI));
            this.springToken = new Token(createTextAttribute(ISpringColorConstants.P_HEADER_SPRING));
            WordRule rule = new WordRule(new KeywordDetector(), Token.UNDEFINED, true);
            rule.addWord(Constants.BUNDLE_ACTIVATOR, this.fToken);
            rule.addWord(Constants.BUNDLE_CATEGORY, this.fToken);
            rule.addWord(Constants.BUNDLE_CLASSPATH, this.fToken);
            rule.addWord(Constants.BUNDLE_CONTACTADDRESS, this.fToken);
            rule.addWord(Constants.BUNDLE_COPYRIGHT, this.fToken);
            rule.addWord(Constants.BUNDLE_DESCRIPTION, this.fToken);
            rule.addWord(Constants.BUNDLE_DOCURL, this.fToken);
            rule.addWord(Constants.BUNDLE_LOCALIZATION, this.fToken);
            rule.addWord(Constants.BUNDLE_MANIFESTVERSION, this.fToken);
            rule.addWord(Constants.BUNDLE_NAME, this.fToken);
            rule.addWord(Constants.BUNDLE_NATIVECODE, this.fToken);
            rule.addWord(Constants.BUNDLE_REQUIREDEXECUTIONENVIRONMENT, this.fToken);
            rule.addWord(Constants.BUNDLE_SYMBOLICNAME, this.fToken);
            rule.addWord(Constants.BUNDLE_UPDATELOCATION, this.fToken);
            rule.addWord(Constants.BUNDLE_VENDOR, this.fToken);
            rule.addWord(Constants.BUNDLE_VERSION, this.fToken);
            rule.addWord(Constants.REQUIRE_BUNDLE, this.fToken);
            rule.addWord(Constants.DYNAMICIMPORT_PACKAGE, this.fToken);
            rule.addWord(Constants.EXPORT_PACKAGE, this.fToken);
            rule.addWord(ICoreConstants.EXPORT_SERVICE, this.fToken);
            rule.addWord(Constants.FRAGMENT_HOST, this.fToken);
            rule.addWord(Constants.IMPORT_PACKAGE, this.fToken);
            rule.addWord(ICoreConstants.IMPORT_SERVICE, this.fToken);
            rule.addWord(ICoreConstants.PROVIDE_PACKAGE, this.fToken);

            addRulesForHeaders(rule);

            setRules(new IRule[] { rule });
            setDefaultReturnToken(new Token(createTextAttribute(IPDEColorConstants.P_HEADER_KEY)));
        }

        private void addRulesForHeaders(WordRule rule) {
            ResourceBundle bundle = ResourceBundle.getBundle("org.eclipse.virgo.ide.ui.editors.text.headers");
            Enumeration<String> headers = bundle.getKeys();
            while (headers.hasMoreElements()) {
                rule.addWord(headers.nextElement(), this.springToken);
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
            this.fAssignmentToken = new Token(createTextAttribute(IPDEColorConstants.P_HEADER_ASSIGNMENT));
            rules[0] = new WordRule(new AssignmentDetector(), this.fAssignmentToken);

            this.fAttributeToken = new Token(createTextAttribute(IPDEColorConstants.P_HEADER_ATTRIBUTES));
            WordRule rule = new WordRule(new KeywordDetector());
            rule.addWord(Constants.BUNDLE_NATIVECODE_LANGUAGE, this.fAttributeToken);
            rule.addWord(Constants.BUNDLE_NATIVECODE_OSNAME, this.fAttributeToken);
            rule.addWord(Constants.BUNDLE_NATIVECODE_OSVERSION, this.fAttributeToken);
            rule.addWord(Constants.BUNDLE_NATIVECODE_PROCESSOR, this.fAttributeToken);
            rule.addWord(Constants.BUNDLE_SYMBOLICNAME_ATTRIBUTE, this.fAttributeToken);
            rule.addWord(Constants.BUNDLE_VERSION_ATTRIBUTE, this.fAttributeToken);
            rule.addWord(Constants.EXCLUDE_DIRECTIVE, this.fAttributeToken);
            rule.addWord(Constants.FRAGMENT_ATTACHMENT_DIRECTIVE, this.fAttributeToken);
            rule.addWord(Constants.INCLUDE_DIRECTIVE, this.fAttributeToken);
            rule.addWord(Constants.MANDATORY_DIRECTIVE, this.fAttributeToken);
            rule.addWord(Constants.RESOLUTION_DIRECTIVE, this.fAttributeToken);
            rule.addWord(Constants.SINGLETON_DIRECTIVE, this.fAttributeToken);
            rule.addWord(Constants.USES_DIRECTIVE, this.fAttributeToken);
            rule.addWord(Constants.VERSION_ATTRIBUTE, this.fAttributeToken);
            rule.addWord(Constants.VISIBILITY_DIRECTIVE, this.fAttributeToken);
            rule.addWord(ICoreConstants.FRIENDS_DIRECTIVE, this.fAttributeToken);
            rule.addWord(ICoreConstants.INTERNAL_DIRECTIVE, this.fAttributeToken);
            rule.addWord(ICoreConstants.PACKAGE_SPECIFICATION_VERSION, this.fAttributeToken);

            // EASTER EGG
            for (String element : ICoreConstants.EE_TOKENS) {
                rule.addWord(element, this.fAttributeToken);
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
                rule.addWord(attributes.nextElement(), this.fAttributeToken);
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
        this.fPropertyKeyScanner = new BundleManifestHeaderScanner();
        this.fPropertyValueScanner = new BundleManifestValueScanner();
    }

    @Override
    public void dispose() {
        if (this.fQuickAssistant != null) {
            this.fQuickAssistant.dispose();
        }
        if (this.fContentAssistant != null) {
            this.fContentAssistantProcessor.dispose();
        }
    }

    @Override
    public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
        if (this.fSourcePage != null && this.fSourcePage.isEditable()) {
            if (this.fContentAssistant == null) {
                this.fContentAssistant = new ContentAssistant();
                this.fContentAssistant.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
                this.fContentAssistantProcessor = new BundleManifestContentAssistProcessor(this.fSourcePage);
                this.fContentAssistant.setContentAssistProcessor(this.fContentAssistantProcessor, IDocument.DEFAULT_CONTENT_TYPE);
                this.fContentAssistant.setContentAssistProcessor(this.fContentAssistantProcessor, ManifestPartitionScanner.MANIFEST_HEADER_VALUE);
                this.fContentAssistant.addCompletionListener(this.fContentAssistantProcessor);
                this.fContentAssistant.setInformationControlCreator(new IInformationControlCreator() {

                    public IInformationControl createInformationControl(Shell parent) {
                        return new JFaceDefaultInformationControl(parent, false);
                    }
                });
                this.fContentAssistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
            }
            return this.fContentAssistant;
        }
        return null;
    }
}
