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
package org.eclipse.virgo.ide.jdt.internal.ui.classpath;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.virgo.ide.jdt.internal.core.classpath.ServerClasspathContainer;

/**
 * {@link IClasspathContainerPage} for the server classpath container
 * 
 * @author Christian Dupuis
 * @since 1.1.3
 */
public class ServerClasspathContainerPage extends WizardPage implements IClasspathContainerPage {

	private IClasspathEntry classpathEntry = null;

	public ServerClasspathContainerPage() {
		super("Bundle Classpath Container");
	}

	public boolean finish() {
		classpathEntry = JavaCore.newContainerEntry(ServerClasspathContainer.CLASSPATH_CONTAINER_PATH);
		return true;
	}

	public IClasspathEntry getSelection() {
		return classpathEntry;
	}

	public void setSelection(IClasspathEntry containerEntry) {
		this.classpathEntry = containerEntry;
	}

	public void createControl(Composite parent) {
		setTitle("Bundle Classpath Container");
		Label label = new Label(parent, SWT.NONE);
		label.setText("Press Finish to add the SpringSource dm Server Bundle Classpath Container");
		label.setFont(parent.getFont());
		setControl(label);
	}

}
