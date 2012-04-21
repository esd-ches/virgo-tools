/*******************************************************************************
 * Copyright (c) 2010 SpringSource, a divison of VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource, a division of VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.virgo.ide.ui.editors.plan;

import org.eclipse.wst.xml.ui.internal.contentoutline.JFaceNodeLabelProvider;
import org.w3c.dom.Node;

/**
 * @author Christian Dupuis
 * @since 2.3.1
 */
public class PlanOutlineLabelProvider extends JFaceNodeLabelProvider {

	@Override
	public String getText(Object o) {

		Node node = (Node) o;
		String shortNodeName = node.getLocalName();
		String text = shortNodeName;

		return text;
	}
}
