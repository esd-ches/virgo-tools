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
 * Represents an artifact listed in a plan. Providers proper implementation of {@link #equals(Object)} and
 * {@link #hashCode()}.
 */
public abstract class Artifact {

    protected Artifact(String name, Version version) {
        super();
        this.name = name;
        this.version = version;
    }

    protected final String name;

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Artifact other = (Artifact) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (!version.equals(other.version)) {
            return false;
        }
        return true;
    }

    protected final Version version;

    /**
     * Gets the artifact name.
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the artifact version.
     *
     * @return version or null if version was not specified.
     */
    public Version getVersion() {
        return version;
    }

}