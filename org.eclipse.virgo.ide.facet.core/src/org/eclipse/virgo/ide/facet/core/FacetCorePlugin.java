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
package org.eclipse.virgo.ide.facet.core;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * Bundle activator for the facet.core plugin
 * 
 * @author Christian Dupuis
 * @since 1.0.0
 */
public class FacetCorePlugin extends Plugin {

	/** Class path container path string */
	public static final String CLASSPATH_CONTAINER = "org.eclipse.virgo.ide.jdt.core.MANIFEST_CLASSPATH_CONTAINER";

	/** Class path container path */
	public static final IPath CLASSPATH_CONTAINER_PATH = new Path(CLASSPATH_CONTAINER);

	/** The bundle symbolic name */
	public static final String PLUGIN_ID = "org.eclipse.virgo.ide.facet.core";

	/** The facet id for SpringSource bundle projects */
	public static final String BUNDLE_FACET_ID = "org.eclipse.virgo.server.bundle";

	/** The facet id for SpringSource par project */
	public static final String PAR_FACET_ID = "org.eclipse.virgo.server.par";

	/** The facet id for SpringSource plan project */
	public static final String PLAN_FACET_ID = "org.eclipse.virgo.server.plan";

	/** The facet id of WTP dynamic web projects */
	public static final String WEB_FACET_ID = "jst.web";

	public static final String BUNDLE_NATURE_ID = PLUGIN_ID + ".bundlenature";

	public static final String PAR_NATURE_ID = PLUGIN_ID + ".parnature";

	public static final String PLAN_NATURE_ID = PLUGIN_ID + ".plannature";

	private static FacetCorePlugin plugin;

	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static FacetCorePlugin getDefault() {
		return plugin;
	}

}
