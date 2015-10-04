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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.virgo.ide.runtime.core.ServerCorePlugin;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Safely reads a JSON file.
 *
 * Usage: Just implement {@link #parse(JSONObject)} with the processing you need and pass in a file.
 *
 * @author Miles Parker
 *
 */
public abstract class JSONFileParser implements SimpleJSONParser {

    File file;

    public JSONFileParser(File file) {
        this.file = file;
        // (perhaps we shouldn't do this as side effect of construction, but I'm
        // growing tired of verbosity for its own sake.)
        apply();
    }

    public void apply() {
        try {
            FileReader reader = new FileReader(this.file);
            JSONTokener parser = new JSONTokener(reader);
            JSONObject rootObject = new JSONObject(parser);
            parse(rootObject);
        } catch (FileNotFoundException e) {
            StatusManager.getManager().handle(new Status(IStatus.ERROR, ServerCorePlugin.PLUGIN_ID, "Error while reading file: " + this.file, e));
        } catch (JSONException e) {
            StatusManager.getManager().handle(new Status(IStatus.ERROR, ServerCorePlugin.PLUGIN_ID, "Error while parsing file: " + this.file, e));
        }
    }
}
