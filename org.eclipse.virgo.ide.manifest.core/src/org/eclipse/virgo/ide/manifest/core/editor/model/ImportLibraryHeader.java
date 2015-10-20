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

import org.eclipse.osgi.util.ManifestElement;
import org.eclipse.pde.internal.core.ibundle.IBundle;
import org.eclipse.pde.internal.core.text.bundle.CompositeManifestHeader;
import org.eclipse.pde.internal.core.text.bundle.PDEManifestElement;
import org.osgi.framework.Constants;

/**
 * @author Leo Dos Santos
 */
public class ImportLibraryHeader extends CompositeManifestHeader {

    private static final long serialVersionUID = 1L;

    public ImportLibraryHeader(String name, String value, IBundle bundle, String lineDelimiter) {
        super(name, value, bundle, lineDelimiter);
    }

    public void addLibrary(String id) {
        addLibrary(id, null);
    }

    public void addLibrary(String id, String version) {
        ImportLibraryObject element = new ImportLibraryObject(this, id);

        if (version != null && version.trim().length() > 0) {
            element.setAttribute(Constants.VERSION_ATTRIBUTE, version.trim());
        }

        addManifestElement(element);
    }

    public void removeLibrary(String id) {
        removeManifestElement(id);
    }

    public void removeLibrary(ImportLibraryObject bundle) {
        removeManifestElement(bundle);
    }

    @Override
    protected PDEManifestElement createElement(ManifestElement element) {
        return new ImportLibraryObject(this, element);
    }

    public ImportLibraryObject[] getImportedLibraries() {
        PDEManifestElement[] elements = getElements();
        ImportLibraryObject[] result = new ImportLibraryObject[elements.length];
        System.arraycopy(elements, 0, result, 0, elements.length);
        return result;
    }

}
