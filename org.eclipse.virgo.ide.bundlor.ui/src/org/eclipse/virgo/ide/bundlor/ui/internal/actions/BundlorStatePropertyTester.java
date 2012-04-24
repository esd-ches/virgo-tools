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
package org.eclipse.virgo.ide.bundlor.ui.internal.actions;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IResource;
import org.eclipse.virgo.ide.bundlor.ui.BundlorUiPlugin;

/**
 * {@link PropertyTester} that can be used to check if the incremental Bundlor project builder is enable on a project.
 * 
 * @author Christian Dupuis
 * @since 1.1.3
 */
public class BundlorStatePropertyTester extends PropertyTester {

	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (receiver instanceof IResource && "isBundlorEnabled".equals(property)) {
			return BundlorUiPlugin.isBundlorBuilding((IResource) receiver);
		}
		return false;
	}
}
