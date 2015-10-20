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

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.ui.IContextMenuConstants;
import org.eclipse.jdt.ui.actions.IJavaEditorActionDefinitionIds;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.pde.internal.ui.editor.PDEFormEditorContributor;
import org.eclipse.pde.internal.ui.editor.PDEProjectionSourcePage;
import org.eclipse.pde.internal.ui.editor.PDESourcePage;
import org.eclipse.pde.internal.ui.editor.actions.HyperlinkAction;
import org.eclipse.pde.internal.ui.editor.actions.PDEActionConstants;
import org.eclipse.pde.internal.ui.editor.text.BundleHyperlink;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.SubActionBars;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.editors.text.TextEditorActionContributor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.RetargetTextEditorAction;
import org.eclipse.virgo.ide.ui.internal.actions.ManifestFormatAction;

/**
 * @author Christian Dupuis
 */
public class AbstractPdeFormTextEditorContributor extends PDEFormEditorContributor {

    protected RetargetTextEditorAction fCorrectionAssist;

    protected HyperlinkAction fHyperlinkAction;

    protected ManifestFormatAction fFormatAction;

    protected RetargetTextEditorAction fContentAssist;

    protected final TextEditorActionContributor fSourceContributor;

    protected SubActionBars fSourceActionBars;

    protected class PDETextEditorActionContributor extends TextEditorActionContributor {

        @Override
        public void contributeToMenu(IMenuManager mm) {
            super.contributeToMenu(mm);
            IMenuManager editMenu = mm.findMenuUsingPath(IWorkbenchActionConstants.M_EDIT);
            if (editMenu != null) {
                editMenu.add(new Separator(IContextMenuConstants.GROUP_OPEN));
                editMenu.add(new Separator(IContextMenuConstants.GROUP_GENERATE));
                editMenu.add(new Separator(IContextMenuConstants.GROUP_ADDITIONS));
                if (AbstractPdeFormTextEditorContributor.this.fCorrectionAssist != null) {
                    editMenu.appendToGroup(IContextMenuConstants.GROUP_GENERATE, AbstractPdeFormTextEditorContributor.this.fCorrectionAssist);
                }
                if (AbstractPdeFormTextEditorContributor.this.fContentAssist != null) {
                    editMenu.appendToGroup(IContextMenuConstants.GROUP_GENERATE, AbstractPdeFormTextEditorContributor.this.fContentAssist);
                }
            }
        }

        @Override
        public void contributeToToolBar(IToolBarManager toolBarManager) {
            super.contributeToToolBar(toolBarManager);
            if (AbstractPdeFormTextEditorContributor.this.fHyperlinkAction != null) {
                toolBarManager.add(AbstractPdeFormTextEditorContributor.this.fHyperlinkAction);
            }
        }

        @Override
        public void setActiveEditor(IEditorPart part) {
            super.setActiveEditor(part);
            IActionBars actionBars = getActionBars();
            IStatusLineManager manager = actionBars.getStatusLineManager();
            manager.setMessage(null);
            manager.setErrorMessage(null);

            ITextEditor textEditor = part instanceof ITextEditor ? (ITextEditor) part : null;
            if (AbstractPdeFormTextEditorContributor.this.fCorrectionAssist != null) {
                AbstractPdeFormTextEditorContributor.this.fCorrectionAssist.setAction(getAction(textEditor, ITextEditorActionConstants.QUICK_ASSIST));
            }
            if (AbstractPdeFormTextEditorContributor.this.fHyperlinkAction != null) {
                AbstractPdeFormTextEditorContributor.this.fHyperlinkAction.setTextEditor(textEditor);
            }
            if (AbstractPdeFormTextEditorContributor.this.fFormatAction != null) {
                AbstractPdeFormTextEditorContributor.this.fFormatAction.setTextEditor(textEditor);
            }
            if (AbstractPdeFormTextEditorContributor.this.fContentAssist != null) {
                AbstractPdeFormTextEditorContributor.this.fContentAssist.setAction(getAction(textEditor, "ContentAssist")); //$NON-NLS-1$
            }
        }
    }

    public AbstractPdeFormTextEditorContributor(String menuName) {
        super(menuName);
        this.fSourceContributor = createSourceContributor();
        if (supportsCorrectionAssist()) {
            this.fCorrectionAssist = new RetargetTextEditorAction(PDESourcePage.getBundleForConstructedKeys(), "CorrectionAssistProposal."); //$NON-NLS-1$
            this.fCorrectionAssist.setActionDefinitionId(ITextEditorActionDefinitionIds.QUICK_ASSIST);
        }
        if (supportsHyperlinking()) {
            this.fHyperlinkAction = new SpringHyperlinkAction();
            this.fHyperlinkAction.setActionDefinitionId(IJavaEditorActionDefinitionIds.OPEN_EDITOR);
        }
        if (supportsFormatAction()) {
            this.fFormatAction = new ManifestFormatAction();
            this.fFormatAction.setActionDefinitionId(PDEActionConstants.DEFN_FORMAT);
        }
        if (supportsContentAssist()) {
            this.fContentAssist = new RetargetTextEditorAction(PDESourcePage.getBundleForConstructedKeys(), "ContentAssistProposal."); //$NON-NLS-1$
            this.fContentAssist.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
        }
    }

    public boolean supportsCorrectionAssist() {
        return false;
    }

    public boolean supportsContentAssist() {
        return false;
    }

    public boolean supportsFormatAction() {
        return false;
    }

    public boolean supportsHyperlinking() {
        return false;
    }

    @Override
    public IEditorActionBarContributor getSourceContributor() {
        return this.fSourceContributor;
    }

    @Override
    public void init(IActionBars bars) {
        super.init(bars);
        this.fSourceActionBars = new SubActionBars(bars);
        this.fSourceContributor.init(this.fSourceActionBars);
    }

    @Override
    public void dispose() {
        this.fSourceActionBars.dispose();
        this.fSourceContributor.dispose();
        super.dispose();
    }

    protected void setSourceActionBarsActive(boolean active) {
        IActionBars rootBars = getActionBars();
        rootBars.clearGlobalActionHandlers();
        rootBars.updateActionBars();
        if (active) {
            this.fSourceActionBars.activate();
            Map handlers = this.fSourceActionBars.getGlobalActionHandlers();
            if (handlers != null) {
                Set keys = handlers.keySet();
                for (Iterator iter = keys.iterator(); iter.hasNext();) {
                    String id = (String) iter.next();
                    rootBars.setGlobalActionHandler(id, (IAction) handlers.get(id));
                }
            }
        } else {
            this.fSourceActionBars.deactivate();
            registerGlobalActionHandlers();
        }
        rootBars.setGlobalActionHandler(PDEActionConstants.OPEN, active ? this.fHyperlinkAction : null);
        rootBars.setGlobalActionHandler(PDEActionConstants.FORMAT, active ? this.fFormatAction : null);
        // Register the revert action
        rootBars.setGlobalActionHandler(ActionFactory.REVERT.getId(), getRevertAction());

        rootBars.updateActionBars();
    }

    protected void registerGlobalActionHandlers() {
        registerGlobalAction(ActionFactory.DELETE.getId());
        registerGlobalAction(ActionFactory.UNDO.getId());
        registerGlobalAction(ActionFactory.REDO.getId());
        registerGlobalAction(ActionFactory.CUT.getId());
        registerGlobalAction(ActionFactory.COPY.getId());
        registerGlobalAction(ActionFactory.PASTE.getId());
        registerGlobalAction(ActionFactory.SELECT_ALL.getId());
        registerGlobalAction(ActionFactory.FIND.getId());
    }

    protected void registerGlobalAction(String id) {
        IAction action = getGlobalAction(id);
        getActionBars().setGlobalActionHandler(id, action);
    }

    @Override
    public void setActivePage(IEditorPart newEditor) {
        if (this.fEditor == null) {
            return;
        }

        IFormPage oldPage = this.fPage;
        this.fPage = this.fEditor.getActivePageInstance();
        if (this.fPage == null) {
            return;
        }
        // Update the quick outline action to the navigate menu
        updateQuickOutlineMenuEntry();

        updateActions();
        if (oldPage != null && !oldPage.isEditor() && !this.fPage.isEditor()) {
            getActionBars().updateActionBars();
            return;
        }

        boolean isSourcePage = this.fPage instanceof PDESourcePage;
        if (isSourcePage && this.fPage.equals(oldPage)) {
            return;
        }
        this.fSourceContributor.setActiveEditor(this.fPage);
        setSourceActionBarsActive(isSourcePage);
    }

    /**
     *
     */
    protected void updateQuickOutlineMenuEntry() {
        // Get the main action bar
        IActionBars actionBars = getActionBars();
        IMenuManager menuManager = actionBars.getMenuManager();
        // Get the navigate menu
        IMenuManager navigateMenu = menuManager.findMenuUsingPath(IWorkbenchActionConstants.M_NAVIGATE);
        // Ensure there is a navigate menu
        if (navigateMenu == null) {
            return;
        }
        // Remove the previous version of the quick outline menu entry - if
        // one exists
        // Prevent duplicate menu entries
        // Prevent wrong quick outline menu from being brought up for the wrong
        // page
        navigateMenu.remove(PDEActionConstants.COMMAND_ID_QUICK_OUTLINE);
        // Ensure the active page is a source page
        // Only add the quick outline menu to the source pages
        if (this.fPage instanceof PDEProjectionSourcePage == false) {
            return;
        }
        PDEProjectionSourcePage page = (PDEProjectionSourcePage) this.fPage;
        // Only add the action if the source page supports it
        if (page.isQuickOutlineEnabled() == false) {
            return;
        }
        // Get the appropriate quick outline action associated with the active
        // source page
        IAction quickOutlineAction = page.getAction(PDEActionConstants.COMMAND_ID_QUICK_OUTLINE);
        // Ensure it is defined
        if (quickOutlineAction == null) {
            return;
        }
        // Add the quick outline action after the "Show In" menu contributed
        // by JDT
        // This could break if JDT changes the "Show In" menu ID
        try {
            navigateMenu.insertAfter("showIn", quickOutlineAction); //$NON-NLS-1$
        } catch (IllegalArgumentException e) {
            // Ignore
        }
    }

    protected TextEditorActionContributor createSourceContributor() {
        return new PDETextEditorActionContributor();
    }

    protected HyperlinkAction getHyperlinkAction() {
        return this.fHyperlinkAction;
    }

    protected ManifestFormatAction getFormatAction() {
        return this.fFormatAction;
    }

    class SpringHyperlinkAction extends HyperlinkAction {

        @Override
        public void generateActionText() {
            String text = PDEUIMessages.HyperlinkActionNoLinksAvailable;
            if (this.fLink instanceof BundleHyperlink) {
                text = PDEUIMessages.HyperlinkActionOpenPackage;
                setText(text);
                setToolTipText(text);
            } else {
                super.generateActionText();
            }
        }
    }

}
