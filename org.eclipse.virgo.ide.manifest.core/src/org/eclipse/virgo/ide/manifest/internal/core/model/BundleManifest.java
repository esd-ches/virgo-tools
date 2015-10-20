/*******************************************************************************
 * Copyright (c) 2009 - 2012 SpringSource, a divison of VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource, a division of VMware, Inc. - initial API and implementation
 *******************************************************************************/

package org.eclipse.virgo.ide.manifest.internal.core.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.virgo.ide.manifest.core.BundleManifestCoreMessages;

/**
 * @author Christian Dupuis
 * @since 1.0.0
 */
/**
 * TODO CD add comments
 */
public class BundleManifest extends AbstractManifestElement {

    private static final int BUNDLE_MANIFEST_TYPE = 1;

    private final IFile file;

    private Map<String, BundleManifestHeader> headerMap;

    // private Set<ValidationProblem> problems;

    private IDocument textDocument;

    public BundleManifest(IFile file) {
        super(null, file.getName());
        this.file = file;
        // this.problems = new LinkedHashSet<ValidationProblem>();
        init();
    }

    public IDocument getDocument() {
        return this.textDocument;
    }

    @Override
    public AbstractManifestElement[] getChildren() {
        List<BundleManifestHeader> headers = new ArrayList<BundleManifestHeader>(this.headerMap.values());
        Collections.sort(headers, new Comparator<BundleManifestHeader>() {

            public int compare(BundleManifestHeader o1, BundleManifestHeader o2) {
                return new Integer(o1.getLineNumber()).compareTo(new Integer(o2.getLineNumber()));
            }
        });

        return headers.toArray(new BundleManifestHeader[this.headerMap.size()]);
    }

    public IResource getElementResource() {
        return this.file;
    }

    public int getElementType() {
        return BUNDLE_MANIFEST_TYPE;
    }

    public BundleManifestHeader getHeader(String key) {
        return this.headerMap.get(key.toLowerCase());
    }

    // public Set<ValidationProblem> getProblems() {
    // return problems;
    // }

    public boolean isElementArchived() {
        return false;
    }

    public boolean isExternal() {
        return false;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.file.getLocation().toString()).append("\n");
        for (AbstractManifestElement element : getChildren()) {
            builder.append(element.toString()).append("\n");
        }
        return builder.toString();
    }

    private String getHeaderName(String line) {
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == ':') {
                return line.substring(0, i);
            }
            if ((c < 'A' || 'Z' < c) && (c < 'a' || 'z' < c) && (c < '0' || '9' < c)) {
                if (i == 0) {
                    return null;
                }
                if (c != '-' && c != '_') {
                    return null;
                }
            }
        }
        return null;
    }

    protected IDocument createDocument(IFile file) {
        if (!file.exists()) {
            return null;
        }
        try {
            // brute force here as we need to get the final file contents as it might have been
            // updated by bundlor
            String contents = convertStreamToString(file.getContents(true));
            return new Document(contents);
        } catch (CoreException e) {
        }
        return null;
    }

    protected void error(int severity, String message, int line) {
        // TODO: re-implement validation with Eclipse APIs
        // problems.add(new ValidationProblem(severity, message, file, line));
    }

    protected void init() {
        this.textDocument = createDocument(this.file);
        parseManifest(this.textDocument);
    }

    protected void parseManifest(IDocument document) {
        try {
            this.headerMap = new HashMap<String, BundleManifestHeader>();
            BundleManifestHeader header = null;
            int l = 0;
            for (; l < document.getNumberOfLines(); l++) {
                IRegion lineInfo = document.getLineInformation(l);
                String line = document.get(lineInfo.getOffset(), lineInfo.getLength());
                // test lines' length
                Charset charset = Charset.forName("UTF-8"); //$NON-NLS-1$
                String lineDelimiter = document.getLineDelimiter(l);
                if (lineDelimiter == null) {
                    lineDelimiter = ""; //$NON-NLS-1$
                }
                ByteBuffer byteBuf = charset.encode(line);
                if (byteBuf.limit() + lineDelimiter.length() > 512) {
                    error(IMarker.SEVERITY_ERROR, BundleManifestCoreMessages.BundleErrorReporter_lineTooLong, l + 1);
                    return;
                }
                // parse
                if (line.length() == 0) {
                    // Empty Line
                    if (l == 0) {
                        error(IMarker.SEVERITY_ERROR, BundleManifestCoreMessages.BundleErrorReporter_noMainSection, 1);
                        return;
                    }
                    /* flush last line */
                    if (header != null) {
                        this.headerMap.put(header.getName().toLowerCase(), header);
                        header = null;
                    }
                    break; /* done processing main attributes */
                }
                if (line.charAt(0) == ' ') {
                    // Continuation Line
                    if (l == 0) { /* if no previous line */
                        error(IMarker.SEVERITY_ERROR, BundleManifestCoreMessages.BundleErrorReporter_noMainSection, 1);
                        return;
                    }
                    if (header != null) {
                        header.append(line.substring(1));
                    }

                    continue;
                }
                // Expecting New Header
                if (header != null) {
                    this.headerMap.put(header.getName().toLowerCase(), header);
                    header = null;
                }

                int colon = line.indexOf(':');
                if (colon == -1) { /* no colon */
                    error(IMarker.SEVERITY_ERROR, BundleManifestCoreMessages.BundleErrorReporter_noColon, l + 1);
                    return;
                }
                String headerName = getHeaderName(line);
                if (headerName == null) {
                    error(IMarker.SEVERITY_ERROR, BundleManifestCoreMessages.BundleErrorReporter_invalidHeaderName, l + 1);
                    return;
                }
                if (line.length() < colon + 2 || line.charAt(colon + 1) != ' ') {
                    error(IMarker.SEVERITY_ERROR, BundleManifestCoreMessages.BundleErrorReporter_noSpaceValue, l + 1);
                    return;
                }
                if ("Name".equals(headerName)) { //$NON-NLS-1$
                    error(IMarker.SEVERITY_ERROR, BundleManifestCoreMessages.BundleErrorReporter_nameHeaderInMain, l + 1);
                    return;
                }
                header = new BundleManifestHeader(this, headerName, line.substring(colon + 2), l);
                if (this.headerMap.containsKey(header.getName().toLowerCase())) {
                    error(IMarker.SEVERITY_WARNING, BundleManifestCoreMessages.BundleErrorReporter_duplicateHeader, l + 1);
                }

            }
            if (header != null) {
                // lingering header, line not terminated
                error(IMarker.SEVERITY_ERROR, BundleManifestCoreMessages.BundleErrorReporter_noLineTermination, l + 1);
                return;
            }
            // If there is any more headers, not starting with a Name header
            // the empty lines are a mistake, report it.
            for (; l < document.getNumberOfLines(); l++) {
                IRegion lineInfo = document.getLineInformation(l);
                String line = document.get(lineInfo.getOffset(), lineInfo.getLength());
                if (line.length() == 0) {
                    continue;
                }
                if (!line.startsWith("Name:")) { //$NON-NLS-1$
                    error(IMarker.SEVERITY_ERROR, BundleManifestCoreMessages.BundleErrorReporter_noNameHeader, l);
                }
                break;
            }

        } catch (BadLocationException ble) {
        }
    }

    public String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
        } finally {
            try {
                is.close();
            } catch (IOException e) {
            }
        }

        return sb.toString();
    }
}
