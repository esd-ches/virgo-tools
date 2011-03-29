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
package org.eclipse.virgo.ide.runtime.internal.ui;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.virgo.ide.bundlerepository.domain.Artefact;
import org.eclipse.virgo.ide.bundlerepository.domain.BundleArtefact;
import org.eclipse.virgo.ide.bundlerepository.domain.LibraryArtefact;
import org.eclipse.virgo.ide.runtime.core.provisioning.LocalBundleArtefact;
import org.eclipse.virgo.ide.runtime.internal.ui.RepositoryViewerUtils.Bundles;
import org.eclipse.virgo.ide.runtime.internal.ui.RepositoryViewerUtils.Libraries;


/**
 * @author Christian Dupuis
 */
public class BasicRepositoryLabelProvider extends LabelProvider {

	public Image getImage(Object parentElement) {
		if (parentElement instanceof Bundles) {
			return ServerUiImages.getImage(ServerUiImages.IMG_OBJ_BUNDLE);
		}
		else if (parentElement instanceof Libraries) {
			return ServerUiImages.getImage(ServerUiImages.IMG_OBJ_LIB);
		}
		else if (parentElement instanceof BundleArtefact) {
			if (parentElement instanceof LocalBundleArtefact
					&& ((LocalBundleArtefact) parentElement).hasDownloadedSource()) {
				return ServerUiImages.getImage(ServerUiImages.IMG_OBJ_BUNDLE_SRC);
			}
			return ServerUiImages.getImage(ServerUiImages.IMG_OBJ_BUNDLE);
		}
		else if (parentElement instanceof LibraryArtefact) {
			return ServerUiImages.getImage(ServerUiImages.IMG_OBJ_LIB);
		}
		return super.getImage(parentElement);
	}
	
	public String getText(Object parentElement) {
		if (parentElement instanceof Bundles) {
			return "Bundles";
		}
		else if (parentElement instanceof Libraries) {
			return "Libraries";
		}
		else if (parentElement instanceof Artefact) {
			Artefact version = (Artefact) parentElement;
			StringBuilder l = new StringBuilder();
			if (version.getName() != null) {
				l.append(version.getName());
				l.append(" - ");
			}
			l.append(version.getSymbolicName());
			l.append(" (");
			l.append(version.getVersion());
			l.append(")");
			return l.toString();
		}
		return super.getText(parentElement);
	}
	
}
