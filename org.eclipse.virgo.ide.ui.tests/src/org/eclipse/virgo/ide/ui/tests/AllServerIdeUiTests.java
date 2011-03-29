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
package org.eclipse.virgo.ide.ui.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.virgo.ide.ui.editors.BundleManifestEditorTest;
import org.eclipse.virgo.ide.ui.editors.ParManifestEditorTest;

/**
 * @author Steffen Pingel
 */
public class AllServerIdeUiTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.eclipse.virgo.ide.ui.tests");
		suite.addTestSuite(ParManifestEditorTest.class);
		suite.addTestSuite(BundleManifestEditorTest.class);
		return suite;
	}

}
