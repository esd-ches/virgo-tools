/**
 * <copyright>
 *
 * TODO Copyright
 *
 * </copyright>
 *
 */
package org.eclipse.virgo.ide.internal.utils.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Provides a simple parser that visits children objects safely, whether they
 * exist as single JSON objects, JSON arrays, or not at all.
 * 
 * Usage: simply point the parser at a JSON object that may or may not have children with the given element.
 * 
 * (The existing JSON files often store single value entities as objects not as a JSON Array of size 1.)
 * 
 * @author Miles Parker
 * 
 */
public abstract class JSONChildParser implements SimpleJSONParser {
	String key;
	JSONObject parent;

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