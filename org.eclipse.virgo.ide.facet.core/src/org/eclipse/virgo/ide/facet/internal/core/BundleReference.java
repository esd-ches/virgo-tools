/*******************************************************************************
 *  Copyright (c) 2016 GianMaria Romanato
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     GianMaria Romanato - initial API and implementation
 *******************************************************************************/

package org.eclipse.virgo.ide.facet.internal.core;

import org.osgi.framework.Version;

/**
 * Represents a reference to a bundle.
 */
public class BundleReference extends Artifact {

    /* package */ BundleReference(String name, Version version) {
        super(name, version);
    }
}
