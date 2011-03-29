/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.virgo.ide.eclipse.wizards;

/**
 * Replacement for strings found in PDEUIMessages and PDECoreMessages, used by
 * ProjectContentPage
 */
public class PdeMessageStrings {

	// PDEUIMessages
	public static String ContentPage_noid = "ID is not set";

	public static String ContentPage_invalidId = "Invalid ID.  Legal characters are A-Z a-z 0-9 . _";

	public static String ContentPage_noname = "Name is not set";

	public static String ContentPage_illegalCharactersInID = "Project name contained characters which are not legal for the id, they have been converted to underscores.";

	public static String ControlValidationUtility_errorMsgValueMustBeSpecified = "A value must be specified";

	// PDECoreMessages
	public static String BundleErrorReporter_InvalidFormatInBundleVersion = "The specified version does not have the correct format (major.minor.micro.qualifier) or contains invalid characters";

}
