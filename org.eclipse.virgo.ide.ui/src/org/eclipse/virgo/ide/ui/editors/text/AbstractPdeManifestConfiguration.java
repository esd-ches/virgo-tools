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

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.quickassist.IQuickAssistAssistant;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.pde.internal.core.ICoreConstants;
import org.eclipse.pde.internal.ui.editor.PDESourcePage;
import org.eclipse.pde.internal.ui.editor.contentassist.ManifestContentAssistProcessor;
import org.eclipse.pde.internal.ui.editor.text.AnnotationHover;
import org.eclipse.pde.internal.ui.editor.text.BasePDEScanner;
import org.eclipse.pde.internal.ui.editor.text.ChangeAwareSourceViewerConfiguration;
import org.eclipse.pde.internal.ui.editor.text.IColorManager;
import org.eclipse.pde.internal.ui.editor.text.IPDEColorConstants;
import org.eclipse.pde.internal.ui.editor.text.ManifestPartitionScanner;
import org.eclipse.pde.internal.ui.editor.text.ManifestTextHover;
import org.eclipse.pde.internal.ui.editor.text.SourceInformationProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.virgo.ide.ui.editors.BundleQuickAssistAssistant;
import org.osgi.framework.Constants;

/**
 * @author Christian Dupuis
 */
public class AbstractPdeManifestConfiguration extends ChangeAwareSourceViewerConfiguration {

    protected IAnnotationHover fAnnotationHover;

    protected BasePDEScanner fPropertyKeyScanner;

    protected BasePDEScanner fPropertyValueScanner;

    protected BundleQuickAssistAssistant fQuickAssistant;

    protected ContentAssistant fContentAssistant;

    private ManifestContentAssistProcessor fContentAssistantProcessor;

    protected ManifestTextHover fTextHover;

    protected String fDocumentPartitioning;

    protected class ManifestHeaderScanner extends BasePDEScanner {

        protected Token fToken;

        public ManifestHeaderScanner() {
            super(AbstractPdeManifestConfiguration.this.fColorManager);
        }

        @Override
        public boolean affectsTextPresentation(String property) {
            return property.startsWith(IPDEColorConstants.P_HEADER_KEY) || property.startsWith(IPDEColorConstants.P_HEADER_OSGI);
        }

        @Override
        protected Token getTokenAffected(PropertyChangeEvent event) {
            if (event.getProperty().startsWith(IPDEColorConstants.P_HEADER_OSGI)) {
                return this.fToken;
            }
            return (Token) this.fDefaultReturnToken;
        }

        @Override
        protected void initialize() {
            this.fToken = new Token(createTextAttribute(IPDEColorConstants.P_HEADER_OSGI));
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
            setRules(new IRule[] { rule });
            setDefaultReturnToken(new Token(createTextAttribute(IPDEColorConstants.P_HEADER_KEY)));
        }
    }

    protected class ManifestValueScanner extends BasePDEScanner {

        protected Token fAssignmentToken;

        protected Token fAttributeToken;

        public ManifestValueScanner() {
            super(AbstractPdeManifestConfiguration.this.fColorManager);
        }

        @Override
        public boolean affectsTextPresentation(String property) {
            return property.startsWith(IPDEColorConstants.P_HEADER_ASSIGNMENT) || property.startsWith(IPDEColorConstants.P_HEADER_VALUE)
                || property.startsWith(IPDEColorConstants.P_HEADER_ATTRIBUTES);
        }

        @Override
        protected Token getTokenAffected(PropertyChangeEvent event) {
            String property = event.getProperty();
            if (property.startsWith(IPDEColorConstants.P_HEADER_ASSIGNMENT)) {
                return this.fAssignmentToken;
            }
            if (property.startsWith(IPDEColorConstants.P_HEADER_ATTRIBUTES)) {
                return this.fAttributeToken;
            }
            return (Token) this.fDefaultReturnToken;
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
            rules[1] = rule;

            setRules(rules);
            setDefaultReturnToken(new Token(createTextAttribute(IPDEColorConstants.P_HEADER_VALUE)));
        }
    }

    class AssignmentDetector implements IWordDetector {

        public boolean isWordStart(char c) {
            return c == ':' || c == '=';
        }

        public boolean isWordPart(char c) {
            return false;
        }
    }

    class KeywordDetector implements IWordDetector {

        public boolean isWordStart(char c) {
            return Character.isJavaIdentifierStart(c);
        }

        public boolean isWordPart(char c) {
            return c != ':' && c != '=' && !Character.isSpaceChar(c);
        }
    }

    public AbstractPdeManifestConfiguration(IColorManager manager) {
        this(manager, null, null);
    }

    public AbstractPdeManifestConfiguration(IColorManager manager, PDESourcePage page) {
        this(manager, page, null);
    }

    public AbstractPdeManifestConfiguration(IColorManager manager, PDESourcePage page, String documentPartitioning) {
        super(page, manager);
        this.fPropertyKeyScanner = new ManifestHeaderScanner();
        this.fPropertyValueScanner = new ManifestValueScanner();
        this.fDocumentPartitioning = documentPartitioning;
    }

    @Override
    public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
        String[] partitions = ManifestPartitionScanner.PARTITIONS;
        String[] all = new String[partitions.length + 1];
        all[0] = IDocument.DEFAULT_CONTENT_TYPE;
        System.arraycopy(partitions, 0, all, 1, partitions.length);
        return all;
    }

    @Override
    public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
        if (this.fAnnotationHover == null) {
            this.fAnnotationHover = new AnnotationHover();
        }
        return this.fAnnotationHover;
    }

    @Override
    public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
        PresentationReconciler reconciler = new PresentationReconciler();
        reconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));

        DefaultDamagerRepairer dr = new DefaultDamagerRepairer(this.fPropertyKeyScanner);
        reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
        reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

        dr = new DefaultDamagerRepairer(this.fPropertyValueScanner);
        reconciler.setDamager(dr, ManifestPartitionScanner.MANIFEST_HEADER_VALUE);
        reconciler.setRepairer(dr, ManifestPartitionScanner.MANIFEST_HEADER_VALUE);

        return reconciler;
    }

    @Override
    public boolean affectsTextPresentation(PropertyChangeEvent event) {
        String property = event.getProperty();
        return property.startsWith(IPDEColorConstants.P_HEADER_KEY) || property.startsWith(IPDEColorConstants.P_HEADER_OSGI)
            || property.startsWith(IPDEColorConstants.P_HEADER_VALUE) || property.startsWith(IPDEColorConstants.P_HEADER_ATTRIBUTES)
            || property.startsWith(IPDEColorConstants.P_HEADER_ASSIGNMENT);
    }

    @Override
    public boolean affectsColorPresentation(PropertyChangeEvent event) {
        String property = event.getProperty();
        return property.equals(IPDEColorConstants.P_HEADER_KEY) || property.equals(IPDEColorConstants.P_HEADER_OSGI)
            || property.equals(IPDEColorConstants.P_HEADER_VALUE) || property.equals(IPDEColorConstants.P_HEADER_ATTRIBUTES)
            || property.equals(IPDEColorConstants.P_HEADER_ASSIGNMENT);
    }

    @Override
    public void adaptToPreferenceChange(PropertyChangeEvent event) {
        if (affectsColorPresentation(event)) {
            this.fColorManager.handlePropertyChangeEvent(event);
        }
        this.fPropertyKeyScanner.adaptToPreferenceChange(event);
        this.fPropertyValueScanner.adaptToPreferenceChange(event);
    }

    @Override
    public IQuickAssistAssistant getQuickAssistAssistant(ISourceViewer sourceViewer) {
        if (sourceViewer.isEditable()) {
            if (this.fQuickAssistant == null) {
                this.fQuickAssistant = new BundleQuickAssistAssistant();
            }
            return this.fQuickAssistant;
        }
        return null;
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
                this.fContentAssistantProcessor = new ManifestContentAssistProcessor(this.fSourcePage);
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

    @Override
    public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
        if (this.fTextHover == null && this.fSourcePage != null) {
            this.fTextHover = new ManifestTextHover(this.fSourcePage);
        }
        return this.fTextHover;
    }

    @Override
    protected int getInfoImplementationType() {
        return SourceInformationProvider.F_MANIFEST_IMP;
    }

    @Override
    public String getConfiguredDocumentPartitioning(ISourceViewer sourceViewer) {
        if (this.fDocumentPartitioning != null) {
            return this.fDocumentPartitioning;
        }
        return super.getConfiguredDocumentPartitioning(sourceViewer);
    }
}
