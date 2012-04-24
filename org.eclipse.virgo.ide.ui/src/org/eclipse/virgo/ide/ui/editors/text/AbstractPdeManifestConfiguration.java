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
			super(fColorManager);
		}

		@Override
		public boolean affectsTextPresentation(String property) {
			return property.startsWith(IPDEColorConstants.P_HEADER_KEY)
					|| property.startsWith(IPDEColorConstants.P_HEADER_OSGI);
		}

		@Override
		protected Token getTokenAffected(PropertyChangeEvent event) {
			if (event.getProperty().startsWith(IPDEColorConstants.P_HEADER_OSGI)) {
				return fToken;
			}
			return (Token) fDefaultReturnToken;
		}

		@Override
		protected void initialize() {
			fToken = new Token(createTextAttribute(IPDEColorConstants.P_HEADER_OSGI));
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
			setRules(new IRule[] { rule });
			setDefaultReturnToken(new Token(createTextAttribute(IPDEColorConstants.P_HEADER_KEY)));
		}
	}

	protected class ManifestValueScanner extends BasePDEScanner {

		protected Token fAssignmentToken;

		protected Token fAttributeToken;

		public ManifestValueScanner() {
			super(fColorManager);
		}

		@Override
		public boolean affectsTextPresentation(String property) {
			return property.startsWith(IPDEColorConstants.P_HEADER_ASSIGNMENT)
					|| property.startsWith(IPDEColorConstants.P_HEADER_VALUE)
					|| property.startsWith(IPDEColorConstants.P_HEADER_ATTRIBUTES);
		}

		@Override
		protected Token getTokenAffected(PropertyChangeEvent event) {
			String property = event.getProperty();
			if (property.startsWith(IPDEColorConstants.P_HEADER_ASSIGNMENT)) {
				return fAssignmentToken;
			}
			if (property.startsWith(IPDEColorConstants.P_HEADER_ATTRIBUTES)) {
				return fAttributeToken;
			}
			return (Token) fDefaultReturnToken;
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
		fPropertyKeyScanner = new ManifestHeaderScanner();
		fPropertyValueScanner = new ManifestValueScanner();
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
		if (fAnnotationHover == null) {
			fAnnotationHover = new AnnotationHover();
		}
		return fAnnotationHover;
	}

	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		PresentationReconciler reconciler = new PresentationReconciler();
		reconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));

		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(fPropertyKeyScanner);
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		dr = new DefaultDamagerRepairer(fPropertyValueScanner);
		reconciler.setDamager(dr, ManifestPartitionScanner.MANIFEST_HEADER_VALUE);
		reconciler.setRepairer(dr, ManifestPartitionScanner.MANIFEST_HEADER_VALUE);

		return reconciler;
	}

	@Override
	public boolean affectsTextPresentation(PropertyChangeEvent event) {
		String property = event.getProperty();
		return property.startsWith(IPDEColorConstants.P_HEADER_KEY)
				|| property.startsWith(IPDEColorConstants.P_HEADER_OSGI)
				|| property.startsWith(IPDEColorConstants.P_HEADER_VALUE)
				|| property.startsWith(IPDEColorConstants.P_HEADER_ATTRIBUTES)
				|| property.startsWith(IPDEColorConstants.P_HEADER_ASSIGNMENT);
	}

	@Override
	public boolean affectsColorPresentation(PropertyChangeEvent event) {
		String property = event.getProperty();
		return property.equals(IPDEColorConstants.P_HEADER_KEY) || property.equals(IPDEColorConstants.P_HEADER_OSGI)
				|| property.equals(IPDEColorConstants.P_HEADER_VALUE)
				|| property.equals(IPDEColorConstants.P_HEADER_ATTRIBUTES)
				|| property.equals(IPDEColorConstants.P_HEADER_ASSIGNMENT);
	}

	@Override
	public void adaptToPreferenceChange(PropertyChangeEvent event) {
		if (affectsColorPresentation(event)) {
			fColorManager.handlePropertyChangeEvent(event);
		}
		fPropertyKeyScanner.adaptToPreferenceChange(event);
		fPropertyValueScanner.adaptToPreferenceChange(event);
	}

	@Override
	public IQuickAssistAssistant getQuickAssistAssistant(ISourceViewer sourceViewer) {
		if (sourceViewer.isEditable()) {
			if (fQuickAssistant == null) {
				fQuickAssistant = new BundleQuickAssistAssistant();
			}
			return fQuickAssistant;
		}
		return null;
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
				fContentAssistantProcessor = new ManifestContentAssistProcessor(fSourcePage);
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

	@Override
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
		if (fTextHover == null && fSourcePage != null) {
			fTextHover = new ManifestTextHover(fSourcePage);
		}
		return fTextHover;
	}

	@Override
	protected int getInfoImplementationType() {
		return SourceInformationProvider.F_MANIFEST_IMP;
	}

	@Override
	public String getConfiguredDocumentPartitioning(ISourceViewer sourceViewer) {
		if (fDocumentPartitioning != null) {
			return fDocumentPartitioning;
		}
		return super.getConfiguredDocumentPartitioning(sourceViewer);
	}
}
