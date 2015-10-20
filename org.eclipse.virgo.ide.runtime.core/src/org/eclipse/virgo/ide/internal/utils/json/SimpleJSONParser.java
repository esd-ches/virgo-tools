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

package org.eclipse.virgo.ide.internal.utils.json;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A common interface for very basic JSON parsing. But you shouldn't ever need more than this, JSON just isn't that
 * complicated!
 *
 * @author Miles Parker
 *
 */
public interface SimpleJSONParser {

    /**
     * Implement to process the passed object.
     */
    void parse(JSONObject object) throws JSONException;
}