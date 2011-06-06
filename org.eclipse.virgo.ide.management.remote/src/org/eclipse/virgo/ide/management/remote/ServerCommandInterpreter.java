/*******************************************************************************
 * Copyright (c) 2007, 2009 SpringSource, a divison of VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     SpringSource, a divison of VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.virgo.ide.management.remote;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.StringTokenizer;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.eclipse.osgi.framework.internal.core.ConsoleMsg;
import org.eclipse.osgi.framework.internal.core.Util;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.Bundle;

/**
 * @author Christian Dupuis
 */
public class ServerCommandInterpreter implements CommandInterpreter {
	private static final String WS_DELIM = " \t\n\r\f"; //$NON-NLS-1$

	/** The command line in StringTokenizer form */
	private StringTokenizer tok;

	/** The active CommandProviders */
	private Object[] commandProviders;

	/** Strings used to format other strings */
	private String tab = "\t"; //$NON-NLS-1$

	private String newline = "\r\n"; //$NON-NLS-1$

	private PrintWriter out;

	public ServerCommandInterpreter(String cmdLine, Object[] commandProviders, PrintWriter writer) {
		this.commandProviders = commandProviders;
		this.tok = new StringTokenizer(cmdLine);
		this.out = writer;
	}

	/**
	 * Get the next argument in the input.
	 * 
	 * E.g. if the commandline is hello world, the _hello method will get "world" as the first argument.
	 * 
	 * @return A string containing the next argument on the command line
	 */
	public String nextArgument() {
		if (tok == null || !tok.hasMoreElements())
			return null;

		String arg = tok.nextToken();
		if (arg.startsWith("\"")) { //$NON-NLS-1$
			if (arg.endsWith("\"")) { //$NON-NLS-1$
				if (arg.length() >= 2)
					// strip the beginning and ending quotes
					return arg.substring(1, arg.length() - 1);
			}
			String remainingArg = tok.nextToken("\""); //$NON-NLS-1$
			arg = arg.substring(1) + remainingArg;
			// skip to next whitespace separated token
			tok.nextToken(WS_DELIM);
		}
		else if (arg.startsWith("'")) { //$NON-NLS-1$ //$NON-NLS-2$
			if (arg.endsWith("'")) { //$NON-NLS-1$
				if (arg.length() >= 2)
					// strip the beginning and ending quotes
					return arg.substring(1, arg.length() - 1);
			}
			String remainingArg = tok.nextToken("'"); //$NON-NLS-1$
			arg = arg.substring(1) + remainingArg;
			// skip to next whitespace separated token
			tok.nextToken(WS_DELIM);
		}
		return arg;
	}

	/**
	 * Execute a command line as if it came from the end user.
	 * 
	 * Searches the list of command providers using introspection until it finds one that contains a matching method. It
	 * searches for a method with the name "_cmd" where cmd is the command to execute. For example, for a command of
	 * "launch" execute searches for a method called "_launch".
	 * 
	 * @param cmd The name of the command to execute.
	 * @return The object returned by the method executed.
	 */
	public Object execute(String cmd) {
		Object retval = null;
		Class[] parameterTypes = new Class[] { CommandInterpreter.class };
		Object[] parameters = new Object[] { this };
		boolean executed = false;
		int size = commandProviders.length;
		for (int i = 0; !executed && (i < size); i++) {
			try {
				Object target = commandProviders[i];
				Method method = target.getClass().getMethod("_" + cmd, parameterTypes); //$NON-NLS-1$
				retval = method.invoke(target, parameters);
				executed = true; // stop after the command has been found
			}
			catch (NoSuchMethodException ite) {
				// keep going - maybe another command provider will be able to
				// execute this command
			}
			catch (InvocationTargetException ite) {
				executed = true; // don't want to keep trying - we found the
				// method but got an error
				printStackTrace(ite.getTargetException());
			}
			catch (Exception ee) {
				executed = true; // don't want to keep trying - we got an error
				// we don't understand
				printStackTrace(ee);
			}
		}
		// if no command was found to execute, display help for all registered
		// command providers
		if (!executed) {
			for (int i = 0; i < size; i++) {
				try {
					CommandProvider commandProvider = (CommandProvider) commandProviders[i];
					out.print(commandProvider.getHelp());
					out.flush();
				}
				catch (Exception ee) {
					printStackTrace(ee);
				}
			}
			// call help for the more command provided by this class
			out.print(getHelp());
			out.flush();
		}
		return retval;
	}

	/**
	 * Prints a string to the output medium (appended with newline character).
	 * <p>
	 * This method does not increment the line counter for the 'more' prompt.
	 * 
	 * @param o the string to be printed
	 */
	private void printline(Object o) {
		print(o + newline);
	}

	/**
	 * Prints an object to the outputstream
	 * 
	 * @param o the object to be printed
	 */
	public void print(Object o) {
		synchronized (out) {
			out.print(o);
			out.flush();
		}
	}

	/**
	 * Prints a empty line to the outputstream
	 */
	public void println() {
		println(""); //$NON-NLS-1$
	}

	/**
	 * Print a stack trace including nested exceptions.
	 * 
	 * @param t The offending exception
	 */
	public void printStackTrace(Throwable t) {
		t.printStackTrace(out);

		Method[] methods = t.getClass().getMethods();

		int size = methods.length;
		Class throwable = Throwable.class;

		for (int i = 0; i < size; i++) {
			Method method = methods[i];

			if (Modifier.isPublic(method.getModifiers())
					&& method.getName().startsWith("get") && throwable.isAssignableFrom(method.getReturnType()) && (method.getParameterTypes().length == 0)) { //$NON-NLS-1$
				try {
					Throwable nested = (Throwable) method.invoke(t, null);

					if ((nested != null) && (nested != t)) {
						out.println(ConsoleMsg.CONSOLE_NESTED_EXCEPTION);
						printStackTrace(nested);
					}
				}
				catch (IllegalAccessException e) {
				}
				catch (InvocationTargetException e) {
				}
			}
		}
	}

	/**
	 * Prints an object to the output medium (appended with newline character).
	 * <p>
	 * If running on the target environment, the user is prompted with '--more' if more than the configured number of
	 * lines have been printed without user prompt. This enables the user of the program to have control over scrolling.
	 * <p>
	 * For this to work properly you should not embed "\n" etc. into the string.
	 * 
	 * @param o the object to be printed
	 */
	public void println(Object o) {
		if (o == null) {
			return;
		}
		synchronized (out) {
			printline(o);
		}
	}

	/**
	 * Prints the given dictionary sorted by keys.
	 * 
	 * @param dic the dictionary to print
	 * @param title the header to print above the key/value pairs
	 */
	public void printDictionary(Dictionary dic, String title) {
		if (dic == null)
			return;

		int count = dic.size();
		String[] keys = new String[count];
		Enumeration keysEnum = dic.keys();
		int i = 0;
		while (keysEnum.hasMoreElements()) {
			keys[i++] = (String) keysEnum.nextElement();
		}
		Util.sortByString(keys);

		if (title != null) {
			println(title);
		}
		for (i = 0; i < count; i++) {
			println(" " + keys[i] + " = " + dic.get(keys[i])); //$NON-NLS-1$//$NON-NLS-2$
		}
		println();
	}

	/**
	 * Prints the given bundle resource if it exists
	 * 
	 * @param bundle the bundle containing the resource
	 * @param resource the resource to print
	 */
	public void printBundleResource(Bundle bundle, String resource) {
		URL entry = null;
		entry = bundle.getEntry(resource);
		if (entry != null) {
			try {
				println(resource);
				InputStream in = entry.openStream();
				byte[] buffer = new byte[1024];
				int read = 0;
				try {
					while ((read = in.read(buffer)) != -1)
						print(new String(buffer, 0, read));
				}
				finally {
					if (in != null) {
						try {
							in.close();
						}
						catch (IOException e) {
						}
					}
				}
			}
			catch (Exception e) {
				System.err.println(NLS.bind(ConsoleMsg.CONSOLE_ERROR_READING_RESOURCE, resource));
			}
		}
		else {
			println(NLS.bind(ConsoleMsg.CONSOLE_RESOURCE_NOT_IN_BUNDLE, resource, bundle.toString()));
		}
	}

	/**
	 * Answer a string (may be as many lines as you like) with help texts that explain the command.
	 */
	public String getHelp() {
		StringBuffer help = new StringBuffer(256);
		help.append(ConsoleMsg.CONSOLE_HELP_CONTROLLING_CONSOLE_HEADING);
		help.append(newline);
		help.append(tab);
		help.append("more - "); //$NON-NLS-1$
		help.append(ConsoleMsg.CONSOLE_HELP_MORE);
		help.append(newline);
		return help.toString();
	}

}
