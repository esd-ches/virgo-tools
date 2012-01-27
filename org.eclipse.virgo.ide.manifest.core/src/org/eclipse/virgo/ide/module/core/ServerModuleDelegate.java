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
package org.eclipse.virgo.ide.module.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.virgo.ide.facet.core.FacetCorePlugin;
import org.eclipse.virgo.ide.facet.core.FacetUtils;
import org.eclipse.virgo.ide.manifest.core.BundleManifestCorePlugin;
import org.eclipse.virgo.ide.par.Bundle;
import org.eclipse.virgo.ide.par.Par;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.model.IModuleFile;
import org.eclipse.wst.server.core.model.IModuleFolder;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.ModuleDelegate;
import org.eclipse.wst.server.core.util.ModuleFile;
import org.eclipse.wst.server.core.util.ModuleFolder;
import org.eclipse.wst.server.core.util.ProjectModule;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.util.ObjectUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * {@link ProjectModule} extension that knows how to handle par and bundle projects.
 * @author Christian Dupuis
 * @author Terry Hon
 * @since 1.0.0
 */
@SuppressWarnings( { "deprecation", "restriction" })
public class ServerModuleDelegate extends ProjectModule {

	/** Make */
	public static final String TEST_CLASSPATH_ENTRY_ATTRIBUTE = "org.eclipse.virgo.ide.jdt.core.test.classpathentry";

	public ServerModuleDelegate(IProject project) {
		super(project);
	}

	@Override
	public IModuleResource[] members() throws CoreException {
		IPath moduleRelativePath = Path.EMPTY;

		// Handle simple case of project being a bundle first
		final Set<IModuleResource> resources = new LinkedHashSet<IModuleResource>();
		if (getModule().getModuleType().getId().equals(FacetCorePlugin.BUNDLE_FACET_ID)) {
			if (FacetUtils.hasProjectFacet(getProject(), FacetCorePlugin.WEB_FACET_ID)) {
				IModule[] modules = ServerUtil.getModules(getProject());
				for (IModule webModule : modules) {
					if (webModule.getModuleType().getId().equals(FacetCorePlugin.WEB_FACET_ID)) {
						ModuleDelegate delegate = (ModuleDelegate) webModule.loadAdapter(ModuleDelegate.class, null);
						resources.addAll(Arrays.asList(delegate.members()));
					}
				}
			}
			else {
				resources.addAll(getMembers(getProject(), moduleRelativePath));
			}
		}
		// More complex handling of PAR and nested bundle project
		else if (getModule().getModuleType().getId().equals(FacetCorePlugin.PAR_FACET_ID)) {

			// Get the META-INF folder of the PAR first
			IResource metaInfFolder = getProject().findMember(BundleManifestCorePlugin.MANIFEST_FOLDER_NAME);
			if (metaInfFolder instanceof IContainer) {
				String moduleFolderName = BundleManifestCorePlugin.MANIFEST_FOLDER_NAME;
				moduleRelativePath = new Path(moduleFolderName);
				ModuleFolder folder = new ModuleFolder(null, moduleFolderName, Path.EMPTY);
				folder.setMembers(getModuleResources(moduleRelativePath, (IContainer) metaInfFolder));
				resources.add(folder);
			}
			else {
				StatusManager.getManager().handle(new Status(IStatus.ERROR, BundleManifestCorePlugin.PLUGIN_ID, "Cannot find META-INF/MANIFEST.MF in project [" + getProject().getName() + "]"));
			}

			// Find linked or nested jars and add them to the deployment
			getProject().accept(new IResourceVisitor() {

				public boolean visit(IResource resource) throws CoreException {
					if (resource instanceof IFile && resource.getFileExtension().equals("jar")) {
						resources.add(new ModuleFile((IFile) resource, resource.getName(), Path.EMPTY));
					}
					return true;
				}
			}, IResource.DEPTH_ONE, false);

			// Iterate nested bundle projects
			for (IModule module : getChildModules()) {

				// Special handling of par nested wars with bundle nature
				if (FacetUtils.hasProjectFacet(module.getProject(), FacetCorePlugin.WEB_FACET_ID)) {
					moduleRelativePath = new Path(module.getProject().getName() + ".war");
					ModuleDelegate delegate = (ModuleDelegate) module.loadAdapter(ModuleDelegate.class, null);

					IModuleResource[] members = delegate.members();
					for (IModuleResource member : members) {
						if (member instanceof IModuleFile) {
							resources.add(new ParModuleFile((IModuleFile) member, moduleRelativePath));
						}
						else if (member instanceof IModuleFolder) {
							resources.add(new ParModuleFolder((IModuleFolder) member, moduleRelativePath));
						}
					}
				}
				// All other bundles project nested in a par
				else if (FacetUtils.isBundleProject(module.getProject())) {
					String moduleFolderName = module.getProject().getName() + ".jar";
					moduleRelativePath = new Path(moduleFolderName);
					ModuleFolder folder = new ModuleFolder(null, moduleFolderName, Path.EMPTY);
					folder.setMembers((IModuleResource[]) getMembers(module.getProject(), moduleRelativePath).toArray(
							new IModuleResource[0]));
					resources.add(folder);
				}
			}
		}
		// handling for plan projects
		else if (getModule().getModuleType().getId().equals(FacetCorePlugin.PLAN_FACET_ID)) {

			// Get the plan file
			String fileName = getModule().getId();
			fileName = fileName.substring(fileName.indexOf(':') + 1);
			IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(fileName));
			if (!file.exists()) {
				return resources.toArray(new IModuleResource[resources.size()]);
			}

			ModuleFile planFile = new ModuleFile(file, file.getName(), moduleRelativePath);
			resources.add(planFile);

			// Iterate nested bundle projects
			for (IModule module : getChildModules()) {

				// Special handling of par nested wars with bundle nature
				if (FacetUtils.hasProjectFacet(module.getProject(), FacetCorePlugin.WEB_FACET_ID)) {
					moduleRelativePath = new Path(module.getProject().getName() + ".war");
					ModuleDelegate delegate = (ModuleDelegate) module.loadAdapter(ModuleDelegate.class, null);

					IModuleResource[] members = delegate.members();
					for (IModuleResource member : members) {
						if (member instanceof IModuleFile) {
							resources.add(new ParModuleFile((IModuleFile) member, moduleRelativePath));
						}
						else if (member instanceof IModuleFolder) {
							resources.add(new ParModuleFolder((IModuleFolder) member, moduleRelativePath));
						}
					}
				}
				// All other bundles project nested in a par
				else if (FacetUtils.isBundleProject(module.getProject())) {
					String moduleFolderName = module.getProject().getName() + ".jar";
					moduleRelativePath = new Path(moduleFolderName);
					ModuleFolder folder = new ModuleFolder(null, moduleFolderName, Path.EMPTY);
					folder.setMembers((IModuleResource[]) getMembers(module.getProject(), moduleRelativePath).toArray(
							new IModuleResource[0]));
					resources.add(folder);
				}
				else if (FacetUtils.isParProject(module.getProject())) {
					moduleRelativePath = new Path(module.getProject().getName() + ".par");
					ModuleDelegate delegate = (ModuleDelegate) module.loadAdapter(ModuleDelegate.class, null);

					IModuleResource[] members = delegate.members();
					for (IModuleResource member : members) {
						if (member instanceof IModuleFile) {
							resources.add(new ParModuleFile((IModuleFile) member, moduleRelativePath));
						}
						else if (member instanceof IModuleFolder) {
							resources.add(new ParModuleFolder((IModuleFolder) member, moduleRelativePath));
						}
					}
				}
			}
		}

		return resources.toArray(new IModuleResource[resources.size()]);
	}

	/**
	 * Get all resources from project's output locations
	 */
	private Set<IModuleResource> getMembers(IProject project, IPath moduleRelativePath) throws JavaModelException,
			CoreException {
		Set<IModuleResource> resources = new LinkedHashSet<IModuleResource>();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IJavaProject javaProject = JavaCore.create(project);

		// Add default output location
		IResource defaultBinFolder = root.findMember(javaProject.getOutputLocation());
		if (defaultBinFolder instanceof IContainer) {
			resources.addAll(Arrays.asList(getModuleResources(moduleRelativePath, (IContainer) defaultBinFolder)));
		}

		// Add output for every source entry
		for (IClasspathEntry entry : getSourceClasspathEntries(project, false)) {
			IResource binFolder = root.findMember(entry.getOutputLocation());
			if (binFolder instanceof IContainer && !(binFolder instanceof IWorkspaceRoot)) {
				resources.addAll(Arrays.asList(getModuleResources(moduleRelativePath, (IContainer) defaultBinFolder)));
			}
		}

		// Add Bundle-ClassPath entries
		BundleManifest manifest = BundleManifestCorePlugin.getBundleManifestManager().getBundleManifest(javaProject);
		if (manifest != null) {
			List<String> bundleClassPathEntries = manifest.getBundleClasspath();
			if (bundleClassPathEntries != null) {
				// remove the . for the class folder from the bundle classpath entries
				bundleClassPathEntries.remove(".");

				// get all resources that match the given Bundle-ClassPath header
				resources.addAll(Arrays.asList(getModuleResources(moduleRelativePath, javaProject.getProject(),
						bundleClassPathEntries)));
			}
		}
		return resources;
	}

	/**
	 * Gets all resources of the given <code>container</code> but filters against names given by <code>filters</code>.
	 */
	protected IModuleResource[] getModuleResources(IPath path, IContainer container, List<String> filters)
			throws CoreException {
		IResource[] resources = container.members();
		if (resources != null) {
			int size = resources.length;
			List<IModuleResource> list = new ArrayList<IModuleResource>(size);
			for (int i = 0; i < size; i++) {
				IResource resource = resources[i];
				if (resource != null && resource.exists()) {
					String name = resource.getName();
					String relativePath = resource.getProjectRelativePath().toString();
					if (resource instanceof IContainer) {
						for (String filter : filters) {
							if (filter.trim().startsWith(relativePath)) {
								IContainer container2 = (IContainer) resource;
								ModuleFolder mf = new org.eclipse.wst.server.core.internal.ModuleFolder(container2,
										name, path);
								mf.setMembers(getModuleResources(path.append(name), container2, filters));
								list.add(mf);
								break;
							}
						}
					}
					else if (resource instanceof IFile) {
						for (String filter : filters) {
							if (relativePath.equals(filter.trim())) {
								list.add(new ModuleFile((IFile) resource, name, path));
								break;
							}
						}
					}
				}
			}
			IModuleResource[] moduleResources = new IModuleResource[list.size()];
			list.toArray(moduleResources);
			return moduleResources;
		}
		return new IModuleResource[0];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IModule[] getChildModules() {
		if (FacetUtils.isParProject(getProject())) {
			Set<IModule> modules = new LinkedHashSet<IModule>();

			Par par = FacetUtils.getParDefinition(getProject());
			if (par != null && par.getBundle() != null) {
				for (Bundle bundle : par.getBundle()) {
					IProject bundleProject = ResourcesPlugin.getWorkspace().getRoot().getProject(
							bundle.getSymbolicName());
					if (FacetUtils.isBundleProject(bundleProject)) {
						for (IModule module : ServerUtil.getModules(bundleProject)) {
							if (module.getId().equals(
									ServerModuleFactoryDelegate.MODULE_FACTORY_ID + ":" + getProject().getName() + "$"
											+ bundleProject.getName())) {
								modules.add(module);
							}
						}
					}
				}
			}
			return (IModule[]) modules.toArray(new IModule[modules.size()]);
		}
		else if (FacetUtils.isPlanProject(getProject())) {
			String fileName = getModule().getId();
			fileName = fileName.substring(fileName.indexOf(':') + 1);
			IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(fileName));
			if (!file.exists()) {
				return new IModule[0];
			}

			return getPlanDependencies(file).toArray(new IModule[0]);

		}
		return new IModule[0];
	}

	/**
	 * {@link IModuleFile} implementation that wraps another {@link IModuleFile} but moves the relative path into a sub
	 * directory of a nested par module.
	 */
	static class ParModuleFile implements IModuleFile {

		private final IModuleFile wrappedFile;

		private final IPath modulePath;

		public ParModuleFile(IModuleFile wrappedFile, IPath modulePath) {
			this.wrappedFile = wrappedFile;
			this.modulePath = modulePath;
		}

		public long getModificationStamp() {
			return wrappedFile.getModificationStamp();
		}

		public IPath getModuleRelativePath() {
			return modulePath.append(wrappedFile.getModuleRelativePath());
		}

		public String getName() {
			return wrappedFile.getName();
		}

		@SuppressWarnings("unchecked")
		public Object getAdapter(Class adapter) {
			return wrappedFile.getAdapter(adapter);
		}

		@Override
		public int hashCode() {
			return modulePath.hashCode() * 37 + wrappedFile.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof ParModuleFile)) {
				return false;
			}
			ParModuleFile other = (ParModuleFile) obj;
			if (!ObjectUtils.nullSafeEquals(modulePath, other.modulePath)) {
				return false;
			}
			return ObjectUtils.nullSafeEquals(wrappedFile, other.wrappedFile);
		}

		@Override
		public String toString() {
			return "ModuleFile [" + modulePath + "/" + wrappedFile.getModuleRelativePath() + ", "
					+ wrappedFile.getName() + ", " + wrappedFile.getModificationStamp() + "]";
		}

	}

	/**
	 * {@link IModuleFolder} implementation that wraps another {@link IModuleFolder} but moves the relative path into a
	 * sub directory of a nested par module.
	 * @see ParModuleFolder#members()
	 */
	static class ParModuleFolder implements IModuleFolder {

		private final IModuleFolder wrappedFolder;

		private final IPath modulePath;

		public ParModuleFolder(IModuleFolder wrappedFolder, IPath modulePath) {
			this.wrappedFolder = wrappedFolder;
			this.modulePath = modulePath;
		}

		public IModuleResource[] members() {
			Set<IModuleResource> members = new LinkedHashSet<IModuleResource>();
			for (IModuleResource resource : wrappedFolder.members()) {
				if (resource instanceof IModuleFile) {
					members.add(new ParModuleFile((IModuleFile) resource, modulePath));
				}
				else if (resource instanceof IModuleFolder) {
					members.add(new ParModuleFolder((IModuleFolder) resource, modulePath));
				}
			}
			return members.toArray(new IModuleResource[members.size()]);
		}

		public IPath getModuleRelativePath() {
			return modulePath.append(wrappedFolder.getModuleRelativePath());
		}

		public String getName() {
			return wrappedFolder.getName();
		}

		@SuppressWarnings("unchecked")
		public Object getAdapter(Class adapter) {
			return wrappedFolder.getAdapter(adapter);
		}

		@Override
		public int hashCode() {
			return modulePath.hashCode() * 37 + wrappedFolder.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof ParModuleFolder)) {
				return false;
			}
			ParModuleFolder other = (ParModuleFolder) obj;
			if (!ObjectUtils.nullSafeEquals(modulePath, other.modulePath)) {
				return false;
			}
			return ObjectUtils.nullSafeEquals(wrappedFolder, other.wrappedFolder);
		}

		@Override
		public String toString() {
			return "ModuleFile [" + modulePath + "/" + wrappedFolder.getModuleRelativePath() + ", "
					+ wrappedFolder.getName() + "]";
		}
	}

	public static Set<IClasspathEntry> getSourceClasspathEntries(IProject project, boolean onlyTestFolders) {
		IJavaProject javaProject = JavaCore.create(project);
		if (javaProject == null) {
			return Collections.emptySet();
		}
		Set<IClasspathEntry> entries = new LinkedHashSet<IClasspathEntry>();
		try {
			for (IClasspathEntry entry : javaProject.getRawClasspath()) {
				if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
					if ((onlyTestFolders && !isSourceFolder(entry.getExtraAttributes()))
							|| (!onlyTestFolders && isSourceFolder(entry.getExtraAttributes()))) {
						entries.add(entry);
					}
				}
			}
		}
		catch (JavaModelException e) {
		}
		return entries;
	}

	private static boolean isSourceFolder(IClasspathAttribute[] extraAttributes) {
		for (IClasspathAttribute attribute : extraAttributes) {
			if (TEST_CLASSPATH_ENTRY_ATTRIBUTE.equals(attribute.getName())) {
				return !Boolean.valueOf(attribute.getValue());
			}
		}
		return true;
	}

	public Set<IModule> getPlanDependencies(IFile file) {
		if (file == null || !file.exists()) {
			return Collections.emptySet();
		}

		Set<IModule> modules = new HashSet<IModule>();

		try {
			DocumentBuilder docBuilder = SpringCoreUtils.getDocumentBuilder();
			Document doc = docBuilder.parse(file.getContents(true));
			NodeList artifactNodes = doc.getDocumentElement().getElementsByTagName("artifact");
			for (int i = 0; i < artifactNodes.getLength(); i++) {
				Element artifact = (Element) artifactNodes.item(i);
				String type = artifact.getAttribute("type");
				String name = artifact.getAttribute("name");

				if ("bundle".equals(type)) {
					IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
					for (IProject candidate : projects) {

						if (FacetUtils.isBundleProject(candidate)
								|| FacetUtils.hasProjectFacet(candidate, FacetCorePlugin.WEB_FACET_ID)) {

							BundleManifest manifest = BundleManifestCorePlugin.getBundleManifestManager()
									.getBundleManifest(JavaCore.create(candidate));

							if ((manifest != null && manifest.getBundleSymbolicName() != null && manifest.
									getBundleSymbolicName().getSymbolicName() != null && manifest.getBundleSymbolicName().
									getSymbolicName().equals(name))
									|| candidate.getName().equals(name)) {
								for (IModule module : ServerUtil.getModules(candidate)) {
									if (!module.getId().contains("$")) {
										modules.add(module);
										break;
									}
								}
							}
						}
					}
				}
				else if ("par".equals(type)) {
					IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
					for (IProject candidate : projects) {

						if (FacetUtils.isParProject(candidate)) {

							if (candidate.getName().equals(name)) {
								for (IModule module : ServerUtil.getModules(candidate)) {
									modules.add(module);
									break;
								}
							}
						}
					}
				}
			}
		}
		catch (SAXException e) {
		}
		catch (IOException e) {
		}
		catch (CoreException e) {
		}

		return modules;
	}
}
