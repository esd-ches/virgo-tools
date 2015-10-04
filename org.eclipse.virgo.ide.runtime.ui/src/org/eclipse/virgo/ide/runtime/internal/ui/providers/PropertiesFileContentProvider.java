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
import org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider;
import org.eclipse.virgo.ide.runtime.internal.core.runtimes.RuntimeProviders;
import org.eclipse.virgo.ide.runtime.internal.ui.projects.ServerProject;
import org.eclipse.wst.server.core.IServer;

/**
 * Common content provider for views on server content.
 *
 * @author Miles Parker
 */
public class PropertiesFileContentProvider extends ServerFileContentProvider {

    public static final String PROPERTIES_EXT = "properties";

    public static final String[] PROPERTIES_INCLUDE_EXTS = null;

    // Should be null or should we hide xml, etc?
    public static final String[] PROPERTIES_EXCLUDE_EXTS = new String[] { "jar", "plan", "libd" };

    @Override
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof ServerFile && !(inputElement instanceof ServerFileSelection)) {
            ServerFile serverFile = (ServerFile) inputElement;
            IFile file = serverFile.getFile();
            if (file.getLocation().getFileExtension() != null && file.getLocation().getFileExtension().equals(PROPERTIES_EXT)) {
                try {
                    String osString = file.getLocation().toOSString();
                    BufferedReader bufferedReader = new BufferedReader(new FileReader(osString));
                    List<ServerFileSelection> lines = new ArrayList<ServerFileSelection>();
                    String readLine = bufferedReader.readLine();
                    int offset = 0;
                    while (readLine != null) {
                        int posEquals = readLine.indexOf("=") + 1;
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
                            ServerFileSelection serverFileSelection = new ServerFileSelection(serverFile.getServer(), file, cleanLine,
                                offset + posEquals, readLine.length() + lineCount - posEquals, lines.size() + 1);
                            lines.add(serverFileSelection);
                            offset += lineCount;
                        } else if (!readLine.startsWith("#") && !StringUtils.isBlank(readLine) && !readLine.endsWith("\\")) {
                            ServerFileSelection serverFileSelection = new ServerFileSelection(serverFile.getServer(), file, readLine,
                                offset + posEquals, readLine.length() - posEquals, lines.size() + 1);
                            lines.add(serverFileSelection);
                        }
                        offset += readLine.length() + 1;
                        readLine = bufferedReader.readLine();
                    }
                    bufferedReader.close();
                    return lines.toArray();
                } catch (FileNotFoundException e) {
                    // don't really care
                } catch (IOException e) {
                    // don't really care
                }
            }
        }
        return super.getElements(inputElement);
    }

    @Override
    public boolean hasChildren(Object element) {
        if (element instanceof ServerFile && !(element instanceof ServerFileSelection)) {
            IFile file = ((ServerFile) element).file;
            return file.getLocation().getFileExtension() != null && file.getLocation().getFileExtension().equals(PROPERTIES_EXT);
        }
        return super.hasChildren(element);
    }

    /**
     * @see org.eclipse.virgo.ide.runtime.internal.ui.providers.ServerFileContentProvider#getServerDirectories(org.eclipse.wst.server.core.IServer)
     */
    @Override
    public String[] getServerDirectories(IServer server) {
        IServerRuntimeProvider provider = RuntimeProviders.getRuntimeProvider(server.getRuntime());
        return provider.getServerPropertiesDirectories();
    }

    /**
     * @see org.eclipse.virgo.ide.runtime.internal.ui.providers.ServerFileContentProvider#getBaseDirectory()
     */
    @Override
    public String getBaseDirectory() {
        return ServerProject.PROPERTIES_DIR;
    }

    /**
     * @see org.eclipse.virgo.ide.runtime.internal.ui.providers.ServerFileContentProvider#getIncludeExtensions()
     */
    @Override
    public String[] getIncludeExtensions() {
        return PROPERTIES_INCLUDE_EXTS;
    }

    /**
     * @see org.eclipse.virgo.ide.runtime.internal.ui.providers.ServerFileContentProvider#getExcludeExtensions()
     */
    @Override
    public String[] getExcludeExtensions() {
        return PROPERTIES_EXCLUDE_EXTS;
    }

    /**
     * @see org.eclipse.virgo.ide.runtime.internal.ui.providers.ServerFileContentProvider#isIncludeNoExtension()
     */
    @Override
    public boolean isIncludeNoExtension() {
        return false;
    }
}