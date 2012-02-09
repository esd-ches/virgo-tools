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
package org.eclipse.virgo.ide.jdt.ui;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * Bundle {@link AbstractUIPlugin} for the jdt.ui plug-in.
 * @author Christian Dupuis
 * @since 1.0.0
 */
public class JdtUiPlugin extends AbstractUIPlugin {
	
	/** The bundle-symbolic name */
	public static final String PLUGIN_ID = "org.eclipse.virgo.ide.jdt.ui";
	
	/** Id for the decorator */
	public static final String DECORATOR_ID  = PLUGIN_ID + ".notAccessibleJavaElementDecorator";

	// The shared instance
	private static JdtUiPlugin plugin;
	
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static JdtUiPlugin getDefault() {
		return plugin;
	}

}
