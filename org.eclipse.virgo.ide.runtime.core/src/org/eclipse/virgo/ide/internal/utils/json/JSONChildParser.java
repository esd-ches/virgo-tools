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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Provides a simple parser that visits children objects safely, whether they
 * exist as single JSON objects, JSON arrays, or not at all.
 * 
 * Usage: Simply give the parser the parent object and child key, and implement
 * {@link #parse(JSONObject)} with the processing you need.
 * 
 * (The existing JSON files often store single value entities as objects not as
 * a JSON Array of size 1.)
 * 
 * @author Miles Parker
 * 
 */
public abstract class JSONChildParser implements SimpleJSONParser {
	String key;
	JSONObject parent;

	/**
	 * Constructs and executes the parser.
	 * 
	 * @param parent Any parent JSON object.
	 * @param key A key within the parent that identifies an object, an array, or nothing at all.
	 */
	public JSONChildParser(JSONObject parent, String key) throws JSONException {
		super();
		this.key = key;
		this.parent = parent;
		apply();
	}

	private void apply() throws JSONException {
		if (parent.has(key)) {
			Object packageNode = parent.get(key);
			if (packageNode instanceof JSONObject) {
				JSONObject packageObject = (JSONObject) packageNode;
				parse(packageObject);
			} else {
				JSONArray packageArray = (JSONArray) packageNode;
				for (int i = 0; i < packageArray.length(); i++) {
					JSONObject packageObject = (JSONObject) packageArray.get(i);
					parse(packageObject);
				}
			}
		}
	}
}