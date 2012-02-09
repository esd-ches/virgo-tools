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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.virgo.ide.runtime.core.IServerConfiguration;

import com.springsource.json.parser.AntlrJSONParser;
import com.springsource.json.parser.JSONParseException;
import com.springsource.json.parser.JSONParser;
import com.springsource.json.parser.ListNode;
import com.springsource.json.parser.MapNode;
import com.springsource.json.parser.Node;
import com.springsource.json.parser.internal.StandardStringNode;
import com.springsource.json.writer.JSONObject;

/**
 * Default {@link IServerConfiguration} implementation.
 * <p>
 * @author Christian Dupuis
 * @since 1.0.0
 */
public class ServerConfiguration implements IServerConfiguration {

	private static final String ARTEFACT_ORDER_FILE_NAME = "artefact-order.config";

	private IFolder configurationFolder = null;

	public ServerConfiguration(IFolder serverConfiguration) throws IOException {
		configurationFolder = serverConfiguration;
	}

	public void addArtefact(String artefact) {
		List<String> artefacts = getArtefactOrder();
		if (!artefacts.contains(artefact)) {
			artefacts.add(artefact);
			saveArtefactOrderFile(artefacts);
		}
	}

	public List<String> getArtefactOrder() {
		List<String> artefacts = new ArrayList<String>();
		IFile file = configurationFolder.getFile(ARTEFACT_ORDER_FILE_NAME);
		if (file.exists()) {
			JSONParser parser = new AntlrJSONParser();
			try {
				MapNode node = (MapNode) parser.parse(file.getRawLocation().toFile().toURL());
				Node artefactsNode = node.getNode("artefacts");
				if (artefactsNode instanceof MapNode) {
					artefacts.add(((StandardStringNode) ((MapNode) artefactsNode).getNode("id")).getValue());
				}
				else if (artefactsNode instanceof ListNode) {
					for (Node artefactNode : ((ListNode) artefactsNode).getNodes()) {
						if (artefactNode instanceof MapNode) {
							artefacts.add(((StandardStringNode) ((MapNode) artefactNode).getNode("id")).getValue());
						}
					}
				}
			}
			catch (JSONParseException e) {
			}
			catch (MalformedURLException e) {
			}
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
		
		JSONObject artefactNodes = new JSONObject();
		for (String artefact : artefacts) {
			JSONObject artefactNode = new JSONObject();
			artefactNode.put("id", artefact);
			artefactNodes.accumulate("artefacts", artefactNode);
		}

		StringWriter writer = new StringWriter();
		artefactNodes.write(writer, 3);
		String contents = writer.toString();
		InputStream is = new ByteArrayInputStream(contents.getBytes());

		IFile file = configurationFolder.getFile(ARTEFACT_ORDER_FILE_NAME);
		try {
			if (!file.exists()) {
				file.create(is, true, new NullProgressMonitor());
			}
			else {
				file.setContents(is, true, false, new NullProgressMonitor());
			}
		}
		catch (CoreException e) {
		}
	}

}
