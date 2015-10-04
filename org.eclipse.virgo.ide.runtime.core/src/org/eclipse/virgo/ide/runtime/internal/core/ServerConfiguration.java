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

package org.eclipse.virgo.ide.runtime.internal.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.virgo.ide.internal.utils.json.JSONChildParser;
import org.eclipse.virgo.ide.internal.utils.json.JSONFileParser;
import org.eclipse.virgo.ide.runtime.core.IServerConfiguration;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Default {@link IServerConfiguration} implementation.
 * <p>
 *
 * @author Christian Dupuis
 * @since 1.0.0
 */
public class ServerConfiguration implements IServerConfiguration {

    private static final String ARTEFACT_ORDER_FILE_NAME = "artefact-order.config";

    private IFolder configurationFolder = null;

    public ServerConfiguration(IFolder serverConfiguration) throws IOException {
        this.configurationFolder = serverConfiguration;
    }

    public void addArtefact(String artefact) {
        List<String> artefacts = getArtefactOrder();
        if (!artefacts.contains(artefact)) {
            artefacts.add(artefact);
            saveArtefactOrderFile(artefacts);
        }
    }

    public List<String> getArtefactOrder() {
        final List<String> artefacts = new ArrayList<String>();
        IFile file = this.configurationFolder.getFile(ARTEFACT_ORDER_FILE_NAME);
        File configurationFile = file.getLocation().toFile();
        if (configurationFile.exists()) {
            new JSONFileParser(configurationFile) {

                public void parse(JSONObject object) throws JSONException {
                    new JSONChildParser(object, "artefacts") {

                        public void parse(JSONObject object) throws JSONException {
                            artefacts.add(object.getString("id"));
                        }
                    };
                }
            };
        }
        return artefacts;
    }

    public void setArtefactOrder(List<String> artefacts) {
        saveArtefactOrderFile(artefacts);
    }

    public void removeArtefact(String artefact) {
        List<String> artefacts = getArtefactOrder();
        if (artefacts.contains(artefact)) {
            artefacts.remove(artefact);
            saveArtefactOrderFile(artefacts);
        }
    }

    private void saveArtefactOrderFile(List<String> artefacts) {
        IFile file = this.configurationFolder.getFile(ARTEFACT_ORDER_FILE_NAME);
        try {
            JSONObject artefactNodes = new JSONObject();
            for (String artefact : artefacts) {
                JSONObject artefactNode = new JSONObject();
                artefactNode.put("id", artefact);
                artefactNodes.accumulate("artefacts", artefactNode);
            }
            FileWriter fileWriter = new FileWriter(file.getLocation().toFile());
            StringWriter stringWriter = new StringWriter();
            artefactNodes.write(stringWriter);
            String jsonString = stringWriter.toString();
            jsonString = jsonString.replaceAll(",", ",\n");
            fileWriter.write(jsonString);
            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

}
