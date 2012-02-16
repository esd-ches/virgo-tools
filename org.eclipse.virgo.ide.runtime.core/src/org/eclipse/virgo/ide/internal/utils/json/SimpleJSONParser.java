/**
 * <copyright>
 *
 * TODO Copyright
 *
 * </copyright>
 *
 */
package org.eclipse.virgo.ide.internal.utils.json;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * @author Miles Parker
 *
 */
public interface SimpleJSONParser {

	void parse(JSONObject object) throws JSONException;
}