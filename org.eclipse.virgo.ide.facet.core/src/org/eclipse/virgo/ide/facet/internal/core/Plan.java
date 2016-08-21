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

import java.util.List;

import org.osgi.framework.Version;

/**
 * Represents a plan in the workspace and includes the list of referred plans. Note that this class does not intend to
 * represent the full content of a plan file, just the minimum needed by the Tooling for deploying plans to the Virgo
 * Runtime.
 */
public final class Plan extends PlanReference {

    public PlanReference asRefence() {
        return new PlanReference(getName(), getVersion());
    }

    private final List<PlanReference> nestedPlans;

    /* default */ Plan(String name, Version version, List<PlanReference> nestedPlans) {
        super(name, version);
        this.nestedPlans = nestedPlans;
    }

    public List<PlanReference> getNestedPlans() {
        return nestedPlans;
    }

}
