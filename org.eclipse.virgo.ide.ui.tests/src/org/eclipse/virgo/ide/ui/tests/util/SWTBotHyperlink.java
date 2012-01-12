/*******************************************************************************
 * Copyright (c) 2007, 2009, 2010 SpringSource
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     SpringSource - initial API and implementation
 *******************************************************************************/
package org.eclipse.virgo.ide.ui.tests.util;

import org.eclipse.swtbot.swt.finder.ReferenceBy;
import org.eclipse.swtbot.swt.finder.SWTBotWidget;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBot;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBotControl;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.hamcrest.SelfDescribing;

/**
 * @author Leo Dos Santos
 */
@SWTBotWidget(clasz = Hyperlink.class, preferredName = "hyperlink", referenceBy = { ReferenceBy.MNEMONIC })
public class SWTBotHyperlink extends AbstractSWTBotControl<Hyperlink> {

	public SWTBotHyperlink(Hyperlink w) throws WidgetNotFoundException {
		super(w);
	}

	public SWTBotHyperlink(Hyperlink w, SelfDescribing description) throws WidgetNotFoundException {
		super(w, description);
	}

	@Override
	public AbstractSWTBot<Hyperlink> click() {
		return click(true);
	}

}
