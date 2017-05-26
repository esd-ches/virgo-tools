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
 * Represents a reference to a nested plan. Provides a proper implementation of {@link #equals(Object)} and
 * {@link #hashCode()} and can be used in collections for structural equality.
 */
public class PlanReference extends Artifact {

    public PlanReference(String name, Version version) {
        super(name, version);
    }

}