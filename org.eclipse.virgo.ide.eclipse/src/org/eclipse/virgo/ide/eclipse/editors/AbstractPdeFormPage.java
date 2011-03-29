/*******************************************************************************
 * Copyright (c) 2003, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.virgo.ide.eclipse.editors;

import org.eclipse.pde.internal.ui.editor.PDEDetails;
import org.eclipse.pde.internal.ui.editor.PDESection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;

/**
 * SpringSource Tool Suite Team - Portions of this class were copied from PDE's
 * PDEFormPage.
 */
@SuppressWarnings("restriction")
public abstract class AbstractPdeFormPage extends FormPage {

	public AbstractPdeFormPage(FormEditor editor, String id, String title) {
		super(editor, id, title);
	}

	protected boolean canPerformDirectly(String id, Control control) {
		if (control instanceof Text) {
			Text text = (Text) control;
			if (id.equals(ActionFactory.CUT.getId())) {
				text.cut();
				return true;
			}
			if (id.equals(ActionFactory.COPY.getId())) {
				text.copy();
				return true;
			}
			if (id.equals(ActionFactory.PASTE.getId())) {
				text.paste();
				return true;
			}
			if (id.equals(ActionFactory.SELECT_ALL.getId())) {
				text.selectAll();
				return true;
			}
			if (id.equals(ActionFactory.DELETE.getId())) {
				int count = text.getSelectionCount();
				if (count == 0) {
					int caretPos = text.getCaretPosition();
					text.setSelection(caretPos, caretPos + 1);
				}
				text.insert(""); //$NON-NLS-1$
				return true;
			}
		}
		else if (control instanceof Table) {
			Table table = (Table) control;
			if (id.equals(ActionFactory.SELECT_ALL.getId())) {
				table.selectAll();
				return true;
			}
		}
		else if (control instanceof Tree) {
			Tree tree = (Tree) control;
			if (id.equals(ActionFactory.SELECT_ALL.getId())) {
				tree.selectAll();
				return true;
			}
		}
		return false;
	}

	protected Control getFocusControl() {
		IManagedForm form = getManagedForm();
		if (form == null) {
			return null;
		}
		Control control = form.getForm();
		if (control == null || control.isDisposed()) {
			return null;
		}
		Display display = control.getDisplay();
		Control focusControl = display.getFocusControl();
		if (focusControl == null || focusControl.isDisposed()) {
			return null;
		}
		return focusControl;
	}

	private AbstractFormPart getFocusSection() {
		Control focusControl = getFocusControl();
		if (focusControl == null) {
			return null;
		}
		Composite parent = focusControl.getParent();
		AbstractFormPart targetPart = null;
		while (parent != null) {
			Object data = parent.getData("part"); //$NON-NLS-1$
			if (data != null && data instanceof AbstractFormPart) {
				targetPart = (AbstractFormPart) data;
				break;
			}
			parent = parent.getParent();
		}
		return targetPart;
	}

	public boolean performGlobalAction(String actionId) {
		Control focusControl = getFocusControl();
		if (focusControl == null) {
			return false;
		}

		if (canPerformDirectly(actionId, focusControl)) {
			return true;
		}
		AbstractFormPart focusPart = getFocusSection();
		if (focusPart != null) {
			if (focusPart instanceof PDESection) {
				return ((PDESection) focusPart).doGlobalAction(actionId);
			}
			if (focusPart instanceof PDEDetails) {
				return ((PDEDetails) focusPart).doGlobalAction(actionId);
			}
		}
		return false;
	}

}
