/*******************************************************************************
 * Copyright (c) 2009, 2010 SpringSource, a divison of VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource, a division of VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.virgo.ide.jdt.internal.core.classpath;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.virgo.ide.jdt.core.JdtCorePlugin;
import org.eclipse.virgo.ide.runtime.core.ServerCorePlugin;
import org.eclipse.virgo.util.io.FileCopyUtils;


/**
 * Utility that persists and reads persisted {@link IClasspathEntry}s from a file called
 * <code>.settings/org.eclipse.virgo.ide.jdt.core.xml</code>.
 * @author Christian Dupuis
 * @author Terry Hon
 * @since 1.0.1
 */
@SuppressWarnings("restriction")
class ServerClasspathUtils {

	/** The name of the persisted file within the .settings directory of a Eclipse project */
	private static final String CLASSPATH_FILE = "-" + JdtCorePlugin.PLUGIN_ID + ".xml";

	/**
	 * Saves the contents of the given {@link StringBuilder} into the settings file
	 */
	private static void saveFile(final IJavaProject project, final StringBuilder builder) {

		// Get the file from the project
		File file = new File(ServerCorePlugin.getDefault().getStateLocation().toFile(), project
				.getProject().getName()	+ CLASSPATH_FILE);
		FileOutputStream os = null;
		try {

			// Create a new file; override old file
			file.createNewFile();

			// Write the contents of the StringBuilder
			os = new FileOutputStream(file);
			os.write(builder.toString().getBytes("UTF-8"));
			os.flush();
		}
		catch (UnsupportedEncodingException e) {
			// can't happen as default UTF-8 is used
		}
		catch (IOException e) {
			JdtCorePlugin.log("Cannot save classpath entries to '" + file.getAbsolutePath() + "'",
					e);
		}
		finally {
			if (os != null) {
				try {
					os.close();
				}
				catch (IOException e) {
				}
			}
		}

	}

	/**
	 * Persists the given {@link IClasspathEntry}s to a file
	 */
	protected static void persistClasspathEntries(IJavaProject project, IClasspathEntry[] entries) {

		// Get the line separator from the platform configuration
		String lineSeparator = Util.getLineSeparator((String) null, project);

		// Create the xml string representation of the classpath entries
		StringBuilder builder = new StringBuilder("<classpath>").append(lineSeparator);
		if (project instanceof JavaProject) {
			JavaProject javaProject = (JavaProject) project;
			for (IClasspathEntry entry : entries) {
				builder.append(javaProject.encodeClasspathEntry(entry));
			}
		}
		builder.append("</classpath>").append(lineSeparator);

		// Save the contents to the settings file
		saveFile(project, builder);
	}

	/**
	 * Reads the persisted classpath entries for the given <code>project</code> and returns the
	 * {@link IClasspathEntry}s.
	 * <p>
	 * This method returns <code>null</code> to indicate that the file could not be read.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected static IClasspathEntry[] readPersistedClasspathEntries(IJavaProject project) {
		File file = new File(ServerCorePlugin.getDefault().getStateLocation().toFile(), project
				.getProject().getName()	+ CLASSPATH_FILE);

		String xmlClasspath = null;
		if (file.exists()) {
			try {
				byte[] bytes = FileCopyUtils.copyToByteArray(file);
				xmlClasspath = new String(bytes, org.eclipse.jdt.internal.compiler.util.Util.UTF_8);
			}
			catch (UnsupportedEncodingException e) {
				// can't happen as default UTF-8 is used
			}
			catch (IOException e) {
			}
		}

		if (xmlClasspath == null) {
			return null;
		}

		if (project instanceof JavaProject) {
			JavaProject javaProject = (JavaProject) project;
			try {
				Object decodedClassPath;
				
				try {
					// needs reflection since return type of decodeClasspath has changed in Eclipse 3.6
					Method method = javaProject.getClass().getMethod("decodeClasspath", String.class, Map.class);
					decodedClassPath = method.invoke(javaProject, xmlClasspath, new HashMap());
					if (decodedClassPath instanceof IClasspathEntry[][]) {
						List<IClasspathEntry> decodedEntries = new ArrayList<IClasspathEntry>();
						for (IClasspathEntry[] entry : (IClasspathEntry[][]) decodedClassPath) {
							decodedEntries.addAll(Arrays.asList(entry));
						}
						return decodedEntries.toArray(new IClasspathEntry[decodedEntries.size()]);
					}
					else if (decodedClassPath instanceof IClasspathEntry[]) {
						return (IClasspathEntry[]) decodedClassPath;
					}
				} catch (Exception e) {
					JdtCorePlugin.log(e);
				}
				
			}
			catch (AssertionFailedException e) {
				JdtCorePlugin.log(e);
			}
		}

		return null;
	}
}
