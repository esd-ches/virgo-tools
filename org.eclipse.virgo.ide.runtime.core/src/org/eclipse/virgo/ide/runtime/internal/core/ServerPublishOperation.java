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
package org.eclipse.virgo.ide.runtime.internal.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jst.server.core.PublishUtil;
import org.eclipse.virgo.bundlor.util.AntPathMatcher;
import org.eclipse.virgo.ide.facet.core.FacetCorePlugin;
import org.eclipse.virgo.ide.facet.core.FacetUtils;
import org.eclipse.virgo.ide.manifest.core.BundleManifestCorePlugin;
import org.eclipse.virgo.ide.module.core.ServerModuleDelegate;
import org.eclipse.virgo.ide.runtime.core.ServerUtils;
import org.eclipse.virgo.util.common.StringUtils;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.util.osgi.manifest.BundleManifestFactory;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.model.IModuleFile;
import org.eclipse.wst.server.core.model.IModuleFolder;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.eclipse.wst.server.core.model.PublishOperation;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;

/**
 * {@link PublishOperation} extension that deals with deploy, clean and refresh of dm Server modules.
 * @author Christian Dupuis
 * @since 1.0.0
 */
@SuppressWarnings("restriction")
public class ServerPublishOperation extends PublishOperation {

	private int deltaKind;

	private int kind;

	private IModule[] modules;

	private ServerBehaviour server;

	public ServerPublishOperation(ServerBehaviour server, int kind, IModule[] module, int deltaKind) {
		super("Publish to server", "Publish module to SpringSource dm Server");
		this.server = server;
		this.kind = kind;
		this.modules = module;
		this.deltaKind = deltaKind;
	}

	@Override
	public void execute(IProgressMonitor monitor, IAdaptable info) throws CoreException {
		boolean shouldRedeploy = false;
		List<IStatus> status = new ArrayList<IStatus>();
		if (modules.length == 1) {
			shouldRedeploy = publishModule(modules[0], status, monitor);
		}

		boolean shouldReployChild = false;
		IProject project = modules[0].getProject();
		if (!FacetUtils.isBundleProject(project) && !FacetUtils.isParProject(project)
				&& ServerUtils.getServer(server).getChildModules(modules) != null && !FacetUtils.isPlanProject(project)) {
			for (IModule module : ServerUtils.getServer(server).getChildModules(modules)) {
				if (publishJar(module, status, monitor)) {
					shouldReployChild = true;
				}
			}
		}

		if (shouldRedeploy || shouldReployChild) {
			server.getServerDeployer().redeploy(modules[0]);
		}

		server.onModulePublishStateChange(modules, IServer.PUBLISH_STATE_NONE);
	}

	@Override
	public int getKind() {
		return REQUIRED;
	}

	@Override
	public int getOrder() {
		return 0;
	}

	/**
	 * Checks if the given <code>file</code> is a root node that is a known Spring namespace.
	 */
//	private boolean checkIfSpringConfigurationFile(IFile file) {
//		IStructuredModel model = null;
//		try {
//			model = StructuredModelManager.getModelManager().getExistingModelForRead(file);
//			if (model == null) {
//				model = StructuredModelManager.getModelManager().getModelForRead(file);
//			}
//			if (model != null) {
//				IDOMDocument document = ((DOMModelImpl) model).getDocument();
//				if (document != null && document.getDocumentElement() != null) {
//					String namespaceUri = document.getDocumentElement().getNamespaceURI();
//					if (NamespaceUtils.DEFAULT_NAMESPACE_URI.equals(namespaceUri)
//							|| new DelegatingNamespaceHandlerResolver(JdtUtils.getClassLoader(file.getProject(), null),
//									null).resolve(namespaceUri) != null) {
//						return false;
//					}
//				}
//			}
//		}
//		catch (Exception e) {
//		}
//		finally {
//			if (model != null) {
//				model.releaseFromRead();
//			}
//			model = null;
//		}
//		return true;
//	}

	/**
	 * Check if resource delta only contains static resources
	 */
	private boolean onlyStaticResources(IModuleResourceDelta delta, Set<IModuleFile> files) {
		if (delta.getModuleResource() instanceof IModuleFolder) {
			for (IModuleResourceDelta child : delta.getAffectedChildren()) {
				if (!onlyStaticResources(child, files)) {
					return false;
				}
			}
			return true;
		}
		else {
			if (delta.getModuleResource() instanceof IModuleFile) {
				files.add((IModuleFile) delta.getModuleResource());
			}
			String name = delta.getModuleResource().getName();


			boolean isStatic = false;
			// Check the configuration options for static resources
			AntPathMatcher matcher = new AntPathMatcher();
			for (String pattern : StringUtils.delimitedListToStringArray(ServerUtils.getServer(server)
					.getStaticFilenamePatterns(), ",")) {
				if (pattern.startsWith("!") && matcher.match(pattern.substring(1), name)) {
					isStatic = false;
				}
				else if (matcher.match(pattern, name)) {
					isStatic = true;
				}
			}
			return isStatic;
		}
	}

	private boolean publishJar(IModule module, List<IStatus> status, IProgressMonitor monitor) throws CoreException {
		IPath path = server.getModuleDeployDirectory(modules[0]).append("WEB-INF").append("lib");
		IPath zipPath = path.append(module.getName() + ".jar");

		if (kind == IServer.PUBLISH_CLEAN || deltaKind == ServerBehaviourDelegate.REMOVED) {
			if (zipPath.toFile().exists()) {
				zipPath.toFile().delete();
			}

			if (deltaKind == ServerBehaviourDelegate.REMOVED) {
				return false;
			}
		}
		if (kind != IServer.PUBLISH_CLEAN && kind != IServer.PUBLISH_FULL) {
			IModuleResourceDelta[] delta = server.getPublishedResourceDelta(new IModule[] { modules[0], module });
			if (delta == null || delta.length == 0) {
				return false;
			}
		}

		if (!path.toFile().exists()) {
			path.toFile().mkdirs();
		}

		PublishUtil.publishZip(server.getResources(new IModule[] { modules[0], module }), zipPath, monitor);
		return true;
	}

	/**
	 * Creates a dummy manifest for WTP Dynamic Web Projects only
	 */
	private void publishManifest(IModule module, IPath path) {

		if (FacetUtils.hasProjectFacet(module.getProject(), FacetCorePlugin.WEB_FACET_ID)) {
			File manifestFile = path.append(BundleManifestCorePlugin.MANIFEST_FOLDER_NAME).append(
					BundleManifestCorePlugin.MANIFEST_FILE_NAME).toFile();
			if (manifestFile.exists()) {
				return;
			}
			BundleManifest manifest = BundleManifestFactory.createBundleManifest();
			Writer writer = null;
			try {
				manifestFile.getParentFile().mkdirs();
				writer = new FileWriter(manifestFile);
				manifest.write(writer);
			}
			catch (IOException e) {
			}
			finally {
				if (writer != null) {
					try {
						writer.flush();
						writer.close();
					}
					catch (IOException e) {
					}
				}
			}
		}
	}

	private boolean publishModule(IModule module, List<IStatus> status, IProgressMonitor monitor) throws CoreException {
		IPath path = server.getModuleDeployDirectory(module);

		if (deltaKind == ServerBehaviourDelegate.REMOVED) {
			File f = path.toFile();
			if (ServerUtils.getServer(server).getServer().getServerState() == IServer.STATE_STARTED) {
				server.getServerDeployer().undeploy(module);
			}
			if (f.exists() && !path.equals(server.getServerDeployDirectory())) {
				PublishUtil.deleteDirectory(f, new NullProgressMonitor());
			}

			if (module.getModuleType().getId().equals(FacetCorePlugin.PLAN_FACET_ID)) {
				// Delete the plan file
				path = path.append(module.getId().substring(module.getId().lastIndexOf('/')));
				File planFile = path.toFile();
				if (planFile.exists()) {
					planFile.delete();
				}

				// Delete all child modules that are not being used anymore
				ServerModuleDelegate planModule = (ServerModuleDelegate) module.loadAdapter(ServerModuleDelegate.class,
						null);
				IServer s = server.getServer();
				for (IModule childModule : planModule.getChildModules()) {
					if (!ServerUtil.containsModule(s, childModule, monitor)) {
						IPath modulePath = server.getModuleDeployDirectory(childModule);
						File moduleFile = modulePath.toFile();
						if (moduleFile.exists()) {
							PublishUtil.deleteDirectory(moduleFile, new NullProgressMonitor());
						}
					}
				}
			}

			return false;
		}

		if (kind == IServer.PUBLISH_CLEAN || kind == IServer.PUBLISH_FULL) {
			IModuleResource[] mr = server.getResources(modules);
			status.addAll(Arrays.asList(PublishUtil.publishFull(mr, path, monitor)));
			// Hack to get a MANIFEST.MF into the deployment area
			publishManifest(module, path);

			if (ServerUtils.getServer(server).getServer().getServerState() == IServer.STATE_STARTED) {
				// redeploy
				return true;
			}
			return false;
		}

		IModuleResourceDelta[] delta = server.getPublishedResourceDelta(modules);
		for (IModuleResourceDelta d : delta) {
			status.addAll(Arrays.asList(PublishUtil.publishDelta(d, path, monitor)));
		}

		publishManifest(module, path);

		if (ServerUtils.getServer(server).getServer().getServerState() == IServer.STATE_STARTED
				&& deltaKind != ServerBehaviourDelegate.NO_CHANGE) {

			Set<IModuleFile> files = new HashSet<IModuleFile>();
			// first check if only static resources are changed
			boolean onlyStaticResources = true;
			for (IModuleResourceDelta d : delta) {
				if (!onlyStaticResources(d, files)) {
					onlyStaticResources = false;
					// don't break to get a full set of all files
				}
			}

			if (!onlyStaticResources) {
				// redeploy
				return true;
			}
			else {
				// refresh static resources
				for (IModuleFile file : files) {
					server.getServerDeployer().refreshStatic(module, file);
				}
			}
		}
		return false;
	}

}
