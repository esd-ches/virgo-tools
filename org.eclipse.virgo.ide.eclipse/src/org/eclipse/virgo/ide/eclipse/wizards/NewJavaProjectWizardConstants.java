/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.virgo.ide.eclipse.wizards;

/**
 * Strings and constants from 3.4's NewWizardMessages class which are necessary for NewJavaProjectWizardPageOne and
 * NewJavaProjectWizardPageTwo
 */
public class NewJavaProjectWizardConstants {

	// Originally from NewWizardMessages in 3.4
	public static String NewJavaProjectWizardPage_title = "Java Settings";

	public static String NewJavaProjectWizardPage_description = "Define the Java build settings.";

	public static String NewJavaProjectWizardPageTwo_error_message = "An error occurred while creating project. Check log for details.";

	public static String NewJavaProjectWizardPageOne_directory_message = "Choose a directory for the project contents:";

	public static String NewJavaProjectWizardPageOne_JREGroup_link_description = "<a>C&onfigure JREs...</a>";

	public static String NewJavaProjectWizardPageOne_JREGroup_default_compliance = "Use def&ault JRE (Currently ''{0}'')";

	public static String NewJavaProjectWizardPageOne_JREGroup_specific_compliance = "U&se a project specific JRE:";

	public static String NewJavaProjectWizardPageOne_JREGroup_specific_EE = "Use an execution en&vironment JRE:";

	public static String NewJavaProjectWizardPageOne_JREGroup_title = "JRE";

	public static String NewJavaProjectWizardPageOne_page_description = "Create a Java project in the workspace or in an external location.";

	public static String NewJavaProjectWizardPageOne_page_title = "Create a Java project";

	public static String NewJavaProjectWizardPageOne_NoJREFound_link = "The default JRE could be detected. To add a JRE manually go to the <a href=\"JRE\">JREs preference page</a>.";

	public static String NewJavaProjectWizardPageOne_LayoutGroup_link_description = "<a>Configure d&efault...</a>";

	public static String NewJavaProjectWizardPageOne_LayoutGroup_option_oneFolder = "&Use project folder as root for sources and class files";

	public static String NewJavaProjectWizardPageOne_DetectGroup_differendWorkspaceCC_message = "The default compiler compliance level for the current workspace is {0}. The new project will use a project specific compiler compliance level of {1}.<a></a>";

	public static String NewJavaProjectWizardPageOne_Message_invalidProjectNameForWorkspaceRoot = "The name of the new project must be ''{0}''";

	public static String NewJavaProjectWizardPageOne_Message_cannotCreateAtExternalLocation = "Cannot create project content at the given external location";

	public static String NewJavaProjectWizardPageOne_Message_notExisingProjectOnWorkspaceRoot = "The selected existing source location in the workspace root does not exist";

	public static String NewJavaProjectWizardPageOne_LayoutGroup_option_separateFolders = "&Create separate folders for sources and class files";

	public static String NewJavaProjectWizardPageOne_LayoutGroup_title = "Project layout";

	public static String NewJavaProjectWizardPageOne_WorkingSets_group = "Working sets";

	public static String NewJavaProjectWizardPageOne_LocationGroup_title = "Contents";

	public static String NewJavaProjectWizardPageOne_LocationGroup_external_desc = "Create project from e&xisting source";

	public static String NewJavaProjectWizardPageOne_LocationGroup_browseButton_desc = "B&rowse...";

	public static String NewJavaProjectWizardPageOne_LocationGroup_locationLabel_desc = "&Directory:";

	public static String NewJavaProjectWizardPageOne_LocationGroup_workspace_desc = "Create new project in &workspace";

	public static String NewJavaProjectWizardPageOne_NameGroup_label_text = "&Project name:";

	public static String NewJavaProjectWizardPageOne_DetectGroup_jre_message = "The current workspace uses a {1} JRE with compiler compliance level {0}. This is not recommended and either the JRE or the compiler compliance level should be changed. <a>Configure...</a>";

	public static String NewJavaProjectWizardPageOne_DetectGroup_message = "The wizard will automatically configure the JRE and the project layout based on the existing source.<a></a>";

	public static String NewJavaProjectWizardPageOne_Message_enterLocation = "Enter a location for the project.";

	public static String NewJavaProjectWizardPageOne_Message_enterProjectName = "Enter a project name.";

	public static String NewJavaProjectWizardPageOne_Message_invalidDirectory = "Invalid project contents directory";

	public static String NewJavaProjectWizardPageOne_Message_notOnWorkspaceRoot = "Projects located in the workspace folder must be direct sub folders of the workspace folder";

	public static String NewJavaProjectWizardPageOne_Message_projectAlreadyExists = "A project with this name already exists.";

	public static String NewJavaProjectWizardPageOne_UnknownDefaultJRE_name = "Unknown";

	public static String NewJavaProjectWizardPageTwo_error_remove_message = "An error occurred while removing a temporary project.";

	public static String NewJavaProjectWizardPageTwo_error_remove_title = "Error Creating Java Project";

	public static String NewJavaProjectWizardPageTwo_problem_backup = "Problem while creating backup for ''{0}''";

	public static String NewJavaProjectWizardPageTwo_DeleteCorruptProjectFile_message = "A problem occurred while creating the project from existing source:\n\n''{0}''\n\nThe corrupt project file will be replaced by a valid one.";

	public static String NewJavaProjectWizardPageTwo_monitor_init_build_path = "Initializing build path";

	public static String NewJavaProjectWizardPageTwo_problem_restore_classpath = "Problem while restoring backup for .classpath";

	public static String NewJavaProjectWizardPageTwo_problem_restore_project = "Problem while restoring backup for .project";

	public static String NewJavaProjectWizardPageTwo_operation_create = "Creating project...";

	public static String NewJavaProjectWizardPageTwo_operation_remove = "Removing project...";

	public static String NewJavaProjectWizardPageTwo_operation_initialize = "Initializing project...";

	public static String NewJavaProjectWizardPageTwo_error_title = "New Java Project";

	public static String NewJavaProjectWizardPage_op_desc = "Creating Java project...";

	// Originally from PackageExplorerPart in 3.4
	public static final int PROJECTS_AS_ROOTS = 1;

}
