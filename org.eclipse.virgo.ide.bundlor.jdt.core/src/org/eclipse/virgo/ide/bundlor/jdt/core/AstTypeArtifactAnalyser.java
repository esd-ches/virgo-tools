/*******************************************************************************
 * Copyright (c) 2009 SpringSource, a divison of VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource, a division of VMware, Inc. - initial API and implementation
 *******************************************************************************/

package org.eclipse.virgo.ide.bundlor.jdt.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;

import org.eclipse.virgo.bundlor.support.ArtifactAnalyzer;
import org.eclipse.virgo.bundlor.support.partialmanifest.PartialManifest;

/**
 * {@link ArtefactAnalyser} that is delegates to JDT AST scanning </p>
 * <strong>Concurrent Semantics</strong><br />
 * Not threadsafe.
 * @author Christian Dupuis
 */
public class AstTypeArtifactAnalyser implements ArtifactAnalyzer {

	private static final String JAVA_EXT = ".java"; //$NON-NLS-1$
	private final IJavaProject javaProject;

	public AstTypeArtifactAnalyser(IJavaProject javaProject) {
		this.javaProject = javaProject;
	}

	/**
	 * {@inheritDoc}
	 */
	public void analyse(InputStream is, String name, PartialManifest model) throws Exception {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setProject(javaProject);
		parser.setSource(convertStreamToString(is).toCharArray());
		parser.setUnitName(name);
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		parser.setStatementsRecovery(true);
		ASTNode node = parser.createAST(new NullProgressMonitor());
		node.accept(new ArtifactAnalyserTypeVisitor(model));
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean canAnalyse(String name) {
		// For now only accept source files
		// TODO does it work with .class files as well?
		return name.endsWith(JAVA_EXT);
	}

	/**
	 * Converts an {@link InputStream} into a {@link String} instance.
	 */
	public String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line).append("\n");
			}
		}
		catch (IOException e) {
		}
		finally {
			try {
				is.close();
			}
			catch (IOException e) {
			}
		}

		return sb.toString();
	}

}
