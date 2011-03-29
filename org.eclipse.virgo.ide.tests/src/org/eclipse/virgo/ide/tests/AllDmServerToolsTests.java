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
package org.eclipse.virgo.ide.tests;

import org.eclipse.virgo.ide.export.tests.AllExportTests;
import org.eclipse.virgo.ide.ui.tests.AllServerIdeUiTests;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * @author Christian Dupuis
 */
public class AllDmServerToolsTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Tests for the dm Server Tools");
//		suite.addTest(AllBundlorJdtTests.suite());
		suite.addTest(AllServerIdeUiTests.suite());
//		suite.addTest(AllSwtbotDrivenTests.suite());
		suite.addTest(AllExportTests.suite());
		return suite;
	}

}
