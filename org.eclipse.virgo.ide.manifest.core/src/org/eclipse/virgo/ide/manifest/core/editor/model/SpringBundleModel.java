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
package org.eclipse.virgo.ide.manifest.core.editor.model;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.pde.internal.core.ibundle.IBundleModelFactory;
import org.eclipse.pde.internal.core.text.bundle.BundleModel;

/**
 * @author Leo Dos Santos
 */
public class SpringBundleModel extends BundleModel {

	private IBundleModelFactory fFactory;

	public SpringBundleModel(String string, boolean isReconciling) {
		this(new Document(string), isReconciling);
	}

	public SpringBundleModel(IDocument document, boolean isReconciling) {
		super(document, isReconciling);
	}

	@Override
	public IBundleModelFactory getFactory() {
		if (fFactory == null) {
			fFactory = new SpringBundleModelFactory(this);
		}
		return fFactory;
	}

}
