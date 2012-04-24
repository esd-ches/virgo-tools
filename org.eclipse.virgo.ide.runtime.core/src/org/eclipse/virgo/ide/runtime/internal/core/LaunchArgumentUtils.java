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

import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.launching.IRuntimeClasspathEntry;

/**
 * Utility class that deals with command line arguments.
 * 
 * @author Christian Dupuis
 * @since 1.0.0
 */
public class LaunchArgumentUtils {

	public static String mergeArguments(String originalArg, String[] vmArgs, String[] excludeArgs,
			boolean keepActionLast) {
		if (vmArgs == null) {
			return originalArg;
		}

		if (originalArg == null) {
			originalArg = "";
		}

		// replace and null out all vmargs that already exist
		int size = vmArgs.length;
		for (int i = 0; i < size; i++) {
			int ind = vmArgs[i].indexOf(" ");
			int ind2 = vmArgs[i].indexOf("=");
			if (ind >= 0 && (ind2 == -1 || ind < ind2)) { // -a bc style
				int index = originalArg.indexOf(vmArgs[i].substring(0, ind + 1));
				if (index == 0 || (index > 0 && originalArg.charAt(index - 1) == ' ')) {
					// replace
					String s = originalArg.substring(0, index);
					int index2 = getNextToken(originalArg, index + ind + 1);
					if (index2 >= 0) {
						originalArg = s + vmArgs[i] + originalArg.substring(index2);
					} else {
						originalArg = s + vmArgs[i];
					}
					vmArgs[i] = null;
				}
			} else if (ind2 >= 0) { // a=b style
				int index = originalArg.indexOf(vmArgs[i].substring(0, ind2 + 1));
				if (index == 0 || (index > 0 && originalArg.charAt(index - 1) == ' ')) {
					// replace
					String s = originalArg.substring(0, index);
					int index2 = getNextToken(originalArg, index);
					if (index2 >= 0) {
						originalArg = s + vmArgs[i] + originalArg.substring(index2);
					} else {
						originalArg = s + vmArgs[i];
					}
					vmArgs[i] = null;
				}
			} else { // abc style
				int index = originalArg.indexOf(vmArgs[i]);
				if (index == 0 || (index > 0 && originalArg.charAt(index - 1) == ' ')) {
					// replace
					String s = originalArg.substring(0, index);
					int index2 = getNextToken(originalArg, index);
					if (!keepActionLast || i < (size - 1)) {
						if (index2 >= 0) {
							originalArg = s + vmArgs[i] + originalArg.substring(index2);
						} else {
							originalArg = s + vmArgs[i];
						}
						vmArgs[i] = null;
					} else {
						// The last VM argument needs to remain last,
						// remove original arg and append the vmArg later
						if (index2 >= 0) {
							originalArg = s + originalArg.substring(index2);
						} else {
							originalArg = s;
						}
					}
				}
			}
		}

		// remove excluded arguments
		if (excludeArgs != null && excludeArgs.length > 0) {
			for (String excludeArg : excludeArgs) {
				int ind = excludeArg.indexOf(" ");
				int ind2 = excludeArg.indexOf("=");
				if (ind >= 0 && (ind2 == -1 || ind < ind2)) { // -a bc style
					int index = originalArg.indexOf(excludeArg.substring(0, ind + 1));
					if (index == 0 || (index > 0 && originalArg.charAt(index - 1) == ' ')) {
						// remove
						String s = originalArg.substring(0, index);
						int index2 = getNextToken(originalArg, index + ind + 1);
						if (index2 >= 0) {
							// If remainder will become the first argument,
							// remove leading blanks
							while (index2 < originalArg.length() && originalArg.charAt(index2) == ' ') {
								index2 += 1;
							}
							originalArg = s + originalArg.substring(index2);
						} else {
							originalArg = s;
						}
					}
				} else if (ind2 >= 0) { // a=b style
					int index = originalArg.indexOf(excludeArg.substring(0, ind2 + 1));
					if (index == 0 || (index > 0 && originalArg.charAt(index - 1) == ' ')) {
						// remove
						String s = originalArg.substring(0, index);
						int index2 = getNextToken(originalArg, index);
						if (index2 >= 0) {
							// If remainder will become the first argument,
							// remove leading blanks
							while (index2 < originalArg.length() && originalArg.charAt(index2) == ' ') {
								index2 += 1;
							}
							originalArg = s + originalArg.substring(index2);
						} else {
							originalArg = s;
						}
					}
				} else { // abc style
					int index = originalArg.indexOf(excludeArg);
					if (index == 0 || (index > 0 && originalArg.charAt(index - 1) == ' ')) {
						// remove
						String s = originalArg.substring(0, index);
						int index2 = getNextToken(originalArg, index);
						if (index2 >= 0) {
							// Remove leading blanks
							while (index2 < originalArg.length() && originalArg.charAt(index2) == ' ') {
								index2 += 1;
							}
							originalArg = s + originalArg.substring(index2);
						} else {
							originalArg = s;
						}
					}
				}
			}
		}

		// add remaining vmargs to the end
		for (int i = 0; i < size; i++) {
			if (vmArgs[i] != null) {
				if (originalArg.length() > 0 && !originalArg.endsWith(" ")) {
					originalArg += " ";
				}
				originalArg += vmArgs[i];
			}
		}

		return originalArg;
	}

	public static void mergeClasspath(List<IRuntimeClasspathEntry> cp, IRuntimeClasspathEntry entry) {
		Iterator<IRuntimeClasspathEntry> iterator = cp.iterator();
		while (iterator.hasNext()) {
			IRuntimeClasspathEntry entry2 = iterator.next();
			if (entry2.getPath().equals(entry.getPath())) {
				return;
			}
		}
		cp.add(entry);
	}

	public static void replaceJREContainer(List<IRuntimeClasspathEntry> cp, IRuntimeClasspathEntry entry) {
		int size = cp.size();
		for (int i = 0; i < size; i++) {
			IRuntimeClasspathEntry entry2 = cp.get(i);
			if (entry2.getPath().uptoSegment(2).isPrefixOf(entry.getPath())) {
				cp.set(i, entry);
				return;
			}
		}
		cp.add(0, entry);
	}

	protected static int getNextToken(String s, int start) {
		int i = start;
		int length = s.length();
		char lookFor = ' ';

		while (i < length) {
			char c = s.charAt(i);
			if (lookFor == c) {
				if (lookFor == '"') {
					return i + 1;
				}
				return i;
			}
			if (c == '"') {
				lookFor = '"';
			}
			i++;
		}
		return -1;
	}
}
