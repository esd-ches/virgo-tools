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

package org.eclipse.virgo.ide.ui.editors.text;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.internal.text.html.BrowserInformationControl;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.AbstractReusableInformationControlCreator;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension3;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension5;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.editors.text.EditorsUI;

/**
 * @author Christian Dupuis
 * @author Steffen Pingel
 */
public class BundleTypeCompletionProposal
    implements ICompletionProposal, ICompletionProposalExtension, ICompletionProposalExtension3, ICompletionProposalExtension5 {

    protected String fReplacementString;

    protected Image fImage;

    protected String fDisplayString;

    protected int fBeginInsertPoint;

    protected int fLength;

    protected String fAdditionalInfo;

    private IInformationControlCreator fCreator;

    public BundleTypeCompletionProposal(String replacementString, Image image, String displayString) {
        this(replacementString, image, displayString, 0, 0);
    }

    public BundleTypeCompletionProposal(String replacementString, Image image, String displayString, int startOffset, int length) {
        Assert.isNotNull(replacementString);

        this.fReplacementString = replacementString;
        this.fImage = image;
        this.fDisplayString = displayString;
        this.fBeginInsertPoint = startOffset;
        this.fLength = length;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.text.contentassist.ICompletionProposal#apply(org.eclipse .jface.text.IDocument)
     */
    public void apply(IDocument document) {
        if (this.fLength == -1) {
            String current = document.get();
            this.fLength = current.length();
        }
        try {
            document.replace(this.fBeginInsertPoint, this.fLength, this.fReplacementString);
        } catch (BadLocationException e) {
            // DEBUG
            // e.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @seeorg.eclipse.jface.text.contentassist.ICompletionProposal# getAdditionalProposalInfo()
     */
    public String getAdditionalProposalInfo() {
        // No additional proposal information
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @seeorg.eclipse.jface.text.contentassist.ICompletionProposal# getContextInformation()
     */
    public IContextInformation getContextInformation() {
        // No context information
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getDisplayString ()
     */
    public String getDisplayString() {
        return this.fDisplayString;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getImage()
     */
    public Image getImage() {
        return this.fImage;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getSelection (org.eclipse.jface.text.IDocument)
     */
    public Point getSelection(IDocument document) {
        if (this.fReplacementString.equals("\"\"")) {
            return new Point(this.fBeginInsertPoint + 1, 0);
        }
        return new Point(this.fBeginInsertPoint + this.fReplacementString.length(), 0);
    }

    /**
     * @return
     */
    public String getReplacementString() {
        return this.fReplacementString;
    }

    public Object getAdditionalProposalInfo(IProgressMonitor monitor) {
        return this.fAdditionalInfo;
    }

    public void setAdditionalProposalInfo(String info) {
        this.fAdditionalInfo = info;
    }

    public IInformationControlCreator getInformationControlCreator() {
        Shell shell = JavaPlugin.getActiveWorkbenchShell();
        if (shell == null || !BrowserInformationControl.isAvailable(shell)) {
            return null;
        }

        if (this.fCreator == null) {
            this.fCreator = new AbstractReusableInformationControlCreator() {

                /*
                 * @seeorg.eclipse.jdt.internal.ui.text.java.hover. AbstractReusableInformationControlCreator
                 * #doCreateInformationControl(org.eclipse.swt.widgets.Shell)
                 */
                @Override
                public IInformationControl doCreateInformationControl(Shell parent) {
                    return new BrowserInformationControl(parent, JFaceResources.DIALOG_FONT, EditorsUI.getTooltipAffordanceString());
                }
            };
        }
        return this.fCreator;
    }

    public int getPrefixCompletionStart(IDocument document, int completionOffset) {
        return this.fBeginInsertPoint;
    }

    public CharSequence getPrefixCompletionText(IDocument document, int completionOffset) {
        return this.fReplacementString;
    }

    public void apply(IDocument document, char trigger, int offset) {
        if (this.fLength == -1) {
            String current = document.get();
            this.fLength = current.length();
        }
        try {
            document.replace(this.fBeginInsertPoint, this.fLength, this.fReplacementString);
        } catch (BadLocationException e) {
            // DEBUG
            // e.printStackTrace();
        }
    }

    public int getContextInformationPosition() {
        return this.fBeginInsertPoint;
    }

    public char[] getTriggerCharacters() {
        return new char[0];
    }

    public boolean isValidFor(IDocument document, int offset) {
        if (offset < this.fBeginInsertPoint) {
            return false;
        }
        int newLength = offset - this.fBeginInsertPoint;
        int delta = newLength - this.fLength;
        this.fLength = delta + this.fLength;

        return startsWith(document, offset, this.fDisplayString);
    }

    // code is borrowed from JavaCompletionProposal
    protected boolean startsWith(IDocument document, int offset, String word) {
        int wordLength = word == null ? 0 : word.length();
        if (offset > this.fBeginInsertPoint + wordLength) {
            return false;
        }

        try {
            int length = offset - this.fBeginInsertPoint;
            String start = document.get(this.fBeginInsertPoint, length);
            // Remove " for comparison
            return word != null && word.substring(0, start.length()).equalsIgnoreCase(start);
        } catch (BadLocationException x) {
        }

        return false;
    }
}