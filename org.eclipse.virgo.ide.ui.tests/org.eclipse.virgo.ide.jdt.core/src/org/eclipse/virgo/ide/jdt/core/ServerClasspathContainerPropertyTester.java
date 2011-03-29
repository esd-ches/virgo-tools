/*******************************************************************************
 * Copyright (c) 2009 SpringSource, a divison of VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource, a division of VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.virgo.ide.jdt.core;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IResource;
import org.eclipse.virgo.ide.jdt.internal.core.util.ClasspathUtils;
import org.springframework.ide.eclipse.core.java.JdtUtils;


/**
 * @author Christian Dupuis
 * @since 1.1.3
 */
public class ServerClasspathContainerPropertyTester extends PropertyTester {

	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (receiver instanceof IResource && "isEnabled".equals(property)) {
			if (JdtUtils.isJavaProject((IResource) receiver))
				return ClasspathUtils.hasClasspathContainer(JdtUtils
						.getJavaProject((IResource) receiver));
		}
		return false;
	}
}
