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
package org.eclipse.virgo.ide.tests.util;

import java.io.IOException;

import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.junit.After;

/**
 * Derived from AbstractBeansCoreTestCase
 * 
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @author Terry Hon
 */
public abstract class VirgoIdeTestCase extends TestCase {

	protected IProject createPredefinedProject(final String projectName) throws CoreException, IOException {
		return VirgoIdeTestUtil.createPredefinedProject(projectName, getBundleName());
	}

	protected IResource createPredefinedProjectAndGetResource(String projectName, String resourcePath)
			throws CoreException, IOException {
		IProject project = createPredefinedProject(projectName);
		// XXX do a second full build to ensure markers are up-to-date
		project.build(IncrementalProjectBuilder.FULL_BUILD, null);

		IResource resource = project.findMember(resourcePath);
		VirgoIdeTestUtil.waitForResource(resource);
		return resource;
	}

	protected abstract String getBundleName();

	protected String getSourceWorkspacePath() {
		return VirgoIdeTestUtil.getSourceWorkspacePath(getBundleName());
	}

	@After
	@Override
	public void tearDown() throws Exception {
		VirgoIdeTestUtil.cleanUpProjects();
		super.tearDown();
	}

}
