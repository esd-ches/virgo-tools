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
package org.eclipse.virgo.ide.runtime.internal.ui.providers;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.virgo.ide.runtime.core.artefacts.Artefact;
import org.eclipse.virgo.ide.runtime.internal.ui.projects.ServerProject;
import org.eclipse.virgo.ide.runtime.internal.ui.projects.ServerProjectManager;
import org.eclipse.wst.server.core.IServer;

/**
 * Common content provider for views on server content.
 * 
 * @author Miles Parker
 */
public class ServerFileContentProvider implements ITreeContentProvider {

	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof IServer) {
			IServer server = (IServer) inputElement;
			ServerProject project = ServerProjectManager.getInstance().getProject(server);
			IFolder folder = project.getWorkspaceProject().getFolder("configuration");
			try {
				return folder.members();
			} catch (CoreException e) {
				throw new RuntimeException(e);
			}
		}
		if (inputElement instanceof IFile) {
			IFile file = (IFile) inputElement;
			if (file.getLocation().getFileExtension().equals("properties")) {
				try {
					String osString = file.getLocation().toOSString();
					BufferedReader bufferedReader = new BufferedReader(new FileReader(osString));
					List<ServerFileSelection> lines = new ArrayList<ServerFileSelection>();
					String readLine = bufferedReader.readLine();
					int offset = 0;
					while (readLine != null) {
						if (readLine.endsWith("\\")) {
							String nextLine = bufferedReader.readLine();
							int lineCount = 1;
							while (nextLine.endsWith("\\")) {
								readLine += nextLine;
								nextLine = bufferedReader.readLine();
								lineCount++;
							}
							readLine += nextLine;
							String cleanLine = readLine.replaceAll("\\\\", "");
							ServerFileSelection serverFileSelection = new ServerFileSelection(file, cleanLine, offset,
									readLine.length() + lineCount, lines.size() - 1);
							lines.add(serverFileSelection);
							offset += lineCount;
						} else if (!readLine.startsWith("#") && !StringUtils.isBlank(readLine)
								&& !readLine.endsWith("\\")) {
							ServerFileSelection serverFileSelection = new ServerFileSelection(file, readLine, offset,
									readLine.length(), lines.size() - 1);
							lines.add(serverFileSelection);
						}
						offset += readLine.length() + 1;
						readLine = bufferedReader.readLine();
					}
					bufferedReader.close();
					return lines.toArray();
				} catch (FileNotFoundException e) {
					//don't really care
				} catch (IOException e) {
					//don't really care
				}
			}
		}
		return new Object[0];
	}

	public Object[] getChildren(Object parentElement) {
		return getElements(parentElement);
	}

	public Object getParent(Object element) {
		if (element instanceof Artefact) {
			Artefact artefact = (Artefact) element;
			return artefact.getSet();
		}
		return null;
	}

	public boolean hasChildren(Object element) {
		return (element instanceof IServer || (element instanceof IFile
				&& (((IFile) element).getLocation().getFileExtension() != null) && ((IFile) element).getLocation()
				.getFileExtension()
				.equals("properties")))
				&& getChildren(element).length > 0;
	}

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
}