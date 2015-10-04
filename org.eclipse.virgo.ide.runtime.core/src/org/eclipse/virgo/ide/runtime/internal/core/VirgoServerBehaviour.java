/*******************************************************************************
 * Copyright (c) 2010 SpringSource, a divison of VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource, a division of VMware, Inc. - initial API and implementation
 *******************************************************************************/

package org.eclipse.virgo.ide.runtime.internal.core;

import org.eclipse.virgo.ide.runtime.core.IServerBehaviour;

/**
 * Default Virgo server behavior.
 *
 * @author Terry Hon
 * @author Christian Dupuis
 * @since 1.0.0
 */
public class VirgoServerBehaviour extends ServerBehaviour implements IServerBehaviour {

    @Override
    public String toString() {
        return "Virgo Server";
    }

}
