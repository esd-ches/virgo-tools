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
package org.eclipse.virgo.ide.export.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Terry Hon
 * @author Christian Dupuis
 * @author Steffen Pingel
 */
public class AllExportTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllExportTests.class.getName());
		suite.addTest(new TestSuite(BundleExportTestCase.class));
		suite.addTest(new TestSuite(ParExportTestCase.class));
		return suite;
	}
	
}
