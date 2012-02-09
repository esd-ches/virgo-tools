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
package org.eclipse.virgo.ide.ui.editors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.eclipse.jface.text.quickassist.IQuickAssistProcessor;
import org.eclipse.jface.text.quickassist.QuickAssistAssistant;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.pde.internal.ui.PDEPluginImages;
import org.eclipse.pde.internal.ui.correction.AbstractPDEMarkerResolution;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.MarkerAnnotation;

/**
 * @author Christian Dupuis
 */
public class BundleQuickAssistAssistant extends QuickAssistAssistant {

	private final Image createImage;

	private final Image renameImage;

	private final Image removeImage;

	class BundleCompletionProposal implements ICompletionProposal {

		Position position;

		IMarkerResolution resolution;

		IMarker marker;

		public BundleCompletionProposal(IMarkerResolution resolution, Position pos, IMarker marker) {
			position = pos;
			this.resolution = resolution;
			this.marker = marker;
		}

		public void apply(IDocument document) {
			resolution.run(marker);
		}

		public Point getSelection(IDocument document) {
			return new Point(position.offset, 0);
		}

		public String getAdditionalProposalInfo() {
			if (resolution instanceof AbstractPDEMarkerResolution) {
				return ((AbstractPDEMarkerResolution) resolution).getDescription();
			}
			return null;
		}

		public String getDisplayString() {
			return resolution.getLabel();
		}

		public Image getImage() {
			if (resolution instanceof AbstractPDEMarkerResolution) {
				switch (((AbstractPDEMarkerResolution) resolution).getType()) {
				case AbstractPDEMarkerResolution.CREATE_TYPE:
					return createImage;
				case AbstractPDEMarkerResolution.REMOVE_TYPE:
					return removeImage;
				case AbstractPDEMarkerResolution.RENAME_TYPE:
					return renameImage;
				}
			}
			return null;
		}

		public IContextInformation getContextInformation() {
			return null;
		}

	}

	class BundleQuickAssistProcessor implements IQuickAssistProcessor {

		Map<IMarker, IMarkerResolution[]> resolutionMap = new HashMap<IMarker, IMarkerResolution[]>();

		public String getErrorMessage() {
			return null;
		}

		public boolean canFix(Annotation annotation) {
			if (!(annotation instanceof MarkerAnnotation)) {
				return false;
			}
			IMarker marker = ((MarkerAnnotation) annotation).getMarker();
			IMarkerResolution[] resolutions = IDE.getMarkerHelpRegistry().getResolutions(marker);
			boolean canFix = resolutions.length > 0;
			if (canFix) {
				if (!resolutionMap.containsKey(marker)) {
					resolutionMap.put(marker, resolutions);
				}
			}
			return canFix;
		}

		public boolean canAssist(IQuickAssistInvocationContext invocationContext) {
			return false;
		}

		public ICompletionProposal[] computeQuickAssistProposals(IQuickAssistInvocationContext invocationContext) {
			IAnnotationModel amodel = invocationContext.getSourceViewer().getAnnotationModel();
			IDocument doc = invocationContext.getSourceViewer().getDocument();

			int offset = invocationContext.getOffset();
			Iterator<?> it = amodel.getAnnotationIterator();
			List<BundleCompletionProposal> list = new ArrayList<BundleCompletionProposal>();
			while (it.hasNext()) {
				Object key = it.next();
				if (!(key instanceof MarkerAnnotation)) {
					continue;
				}

				MarkerAnnotation annotation = (MarkerAnnotation) key;
				IMarker marker = annotation.getMarker();

				IMarkerResolution[] mapping = resolutionMap.get(marker);
				if (mapping != null) {
					Position pos = amodel.getPosition(annotation);
					try {
						int line = doc.getLineOfOffset(pos.getOffset());
						int start = pos.getOffset();
						String delim = doc.getLineDelimiter(line);
						int delimLength = delim != null ? delim.length() : 0;
						int end = doc.getLineLength(line) + start - delimLength;
						if (offset >= start && offset <= end) {
							for (IMarkerResolution element : mapping) {
								list.add(new BundleCompletionProposal(element, pos, marker));
							}
						}
					}
					catch (BadLocationException e) {
					}

				}
			}
			return list.toArray(new ICompletionProposal[list.size()]);
		}
	}

	public BundleQuickAssistAssistant() {
		setQuickAssistProcessor(new BundleQuickAssistProcessor());
		createImage = PDEPluginImages.DESC_ADD_ATT.createImage();
		removeImage = PDEPluginImages.DESC_DELETE.createImage();
		renameImage = PDEPluginImages.DESC_REFRESH.createImage();
	}

	public void dispose() {
		createImage.dispose();
		removeImage.dispose();
		renameImage.dispose();
	}

}
