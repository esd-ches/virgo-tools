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
package org.eclipse.virgo.ide.runtime.core;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.launching.JREContainer;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jdt.launching.environments.IExecutionEnvironmentsManager;
import org.eclipse.virgo.ide.manifest.core.BundleManifestCorePlugin;
import org.eclipse.virgo.ide.manifest.core.dependencies.IDependencyLocator;
import org.eclipse.virgo.ide.manifest.core.dependencies.IDependencyLocator.JavaVersion;
import org.eclipse.virgo.ide.runtime.internal.core.Server;
import org.eclipse.virgo.ide.runtime.internal.core.ServerBehaviour;
import org.eclipse.virgo.ide.runtime.internal.core.ServerRuntime;
import org.eclipse.virgo.ide.runtime.internal.core.ServerRuntimeUtils;
import org.eclipse.virgo.kernel.osgi.provisioning.tools.DependencyLocator;
import org.eclipse.virgo.kernel.osgi.provisioning.tools.DependencyLocator10;
import org.eclipse.virgo.kernel.osgi.provisioning.tools.DependencyLocator20;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.util.PublishUtil;
import org.osgi.framework.Constants;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.java.JdtUtils;

/**
 * @author Christian Dupuis
 * @since 1.0.0
 */
@SuppressWarnings("restriction")
public class ServerUtils {

	/** Path of the JRE container */
	private static final String JRE_CONTAINER_STRING = "org.eclipse.jdt.launching.JRE_CONTAINER";

	/** {@link Path} of the JRE classpath container */
	private static final Path JRE_CONTAINER_PATH = new Path(JRE_CONTAINER_STRING);

	private static final String[] NO_ADDITIONAL_SEARCH_PATHS = new String[0];

	public static final String CACHE_LOCATION_PREFIX = "repository-cache";

	public static final String REPOSITORY_DOWNLOADS_PREFIX = "repository-downloads";

	private static final AtomicLong REPOSITORY_COUNTER = new AtomicLong();

	/**
	 * Location of the bundle resolution cache used by the {@link DependencyLocator}
	 */
	public static final String CACHE_LOCATION = ServerCorePlugin.getDefault().getStateLocation().toFile() + "/"
			+ CACHE_LOCATION_PREFIX + "-" + System.currentTimeMillis();

	/** Source jar file suffix */
	private static final String SOURCES_SUFFIX = "-sources";

	/** Mapping of execution environment ids to {@link JavaVersion} */
	private static final Map<String, JavaVersion> JAVA_VERSION_MAPPING;

	static {
		JAVA_VERSION_MAPPING = new HashMap<String, JavaVersion>();
		JAVA_VERSION_MAPPING.put("J2SE-1.5", JavaVersion.Java5);
		JAVA_VERSION_MAPPING.put("JavaSE-1.6", JavaVersion.Java6);
	}

	/**
	 * Returns the root of the installation directory.
	 * @param targetedServerRuntimes the targeted servers of this project
	 * @return path to server.profile file
	 */
	public static String getServerHome(IRuntime... targetedServerRuntimes) {
		for (org.eclipse.wst.server.core.IRuntime serverRuntime : targetedServerRuntimes) {
			ServerRuntime sRuntime = (ServerRuntime) serverRuntime.loadAdapter(ServerRuntime.class,
					new NullProgressMonitor());
			if (sRuntime != null) {
				return serverRuntime.getLocation().toString();
			}
		}
		return null;
	}

	/**
	 * Returns the targeted runtimes of the given project
	 * @param project the project to return the target runtimes for
	 */
	public static IRuntime[] getTargettedRuntimes(IProject project) {
		final Set<org.eclipse.wst.server.core.IRuntime> targetedServerRuntimes = new LinkedHashSet<org.eclipse.wst.server.core.IRuntime>();

		ServerRuntimeUtils.execute(project, new ServerRuntimeUtils.ServerRuntimeCallback() {

			public boolean doWithRuntime(ServerRuntime runtime) {
				targetedServerRuntimes.add(runtime.getRuntime());
				return true;
			}
		});

		return targetedServerRuntimes.toArray(new IRuntime[targetedServerRuntimes.size()]);
	}

	/**
	 * Creates a {@link DependencyLocator} instance suitable for the given project.
	 * @param project the project
	 * @param additionalSearchPaths any additional search paths.
	 */
	public static IDependencyLocator createDependencyLocator(IProject project, String[] additionalSearchPaths) {
		try {
			IRuntime[] serverRuntimes = getTargettedRuntimes(project);
			if (serverRuntimes == null || serverRuntimes.length == 0) {
				return null;
			}
			return createDependencyLocator(serverRuntimes[0], getServerHome(serverRuntimes), additionalSearchPaths,
					getCacheDirectoryPath(), getJavaVersion(project));
		}
		catch (IOException e) {
			SpringCore.log(e);
		}
		return null;
	}

	/**
	 * Creates a {@link DependencyLocator} instance suitable for the given runtime.
	 * @param project the project
	 * @param additionalSearchPaths any additional search paths.
	 */
	public static IDependencyLocator createDependencyLocator(IRuntime serverRuntime) {
		try {
			// Create DependencyLocator with path to server.config and server.profile
			return createDependencyLocator(serverRuntime, getServerHome(serverRuntime), NO_ADDITIONAL_SEARCH_PATHS,
					getCacheDirectoryPath(), null);
		}
		catch (IOException e) {
			SpringCore.log(e);
		}
		return null;
	}

	private static IDependencyLocator createDependencyLocator(IRuntime runtime, String serverHomePath,
			String[] additionalSearchPaths, String indexDirectoryPath, JavaVersion javaVersion) throws IOException {
		// TODO CD check to see if this can be moved into the version handler
		if (runtime.getRuntimeType().getId().startsWith("com.springsource.server") && runtime.getRuntimeType().getId().endsWith("10")) {
			return new DependencyLocator10(serverHomePath, additionalSearchPaths, indexDirectoryPath,
					(javaVersion != null ? javaVersion : JavaVersion.Java5));
		}
		else if (runtime.getRuntimeType().getId().startsWith("com.springsource.server") && runtime.getRuntimeType().getId().endsWith("20")) {
			return new DependencyLocator20(serverHomePath, additionalSearchPaths, indexDirectoryPath, javaVersion);
		}
		else if (runtime.getRuntimeType().getId().startsWith("org.eclipse.virgo.server.runtime.virgo")) {
			return new DependencyLocator20(serverHomePath, additionalSearchPaths, indexDirectoryPath, javaVersion);
		}
		return null;
	}

	/**
	 * Returns the cache directory to be used by the {@link DependencyLocator}
	 */
	private static String getCacheDirectoryPath() {
		// trying to generated a thread unique directory name
		File cacheDirectoryPath = new File(new StringBuilder(CACHE_LOCATION).append("-").append(
				REPOSITORY_COUNTER.getAndIncrement()).toString());
		if (!cacheDirectoryPath.exists()) {
			cacheDirectoryPath.mkdirs();
		}
		return cacheDirectoryPath.toString();
	}

	/**
	 * Returns the name of the source jar following the BRITS conventions
	 */
	public static File getSourceFile(URI uri) {
		File file = new File(uri);
		StringBuilder builder = new StringBuilder(file.getName());
		int ix = builder.lastIndexOf("-");
		if (ix > 0) {
			builder.insert(ix, SOURCES_SUFFIX);
			File sourceFile = new File(file.getParentFile(), builder.toString());
			return sourceFile;
		}
		return null;
	}

	public static void clearCacheDirectory() {
		File stateLocation = ServerCorePlugin.getDefault().getStateLocation().toFile();

		for (File folder : stateLocation.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.startsWith(CACHE_LOCATION_PREFIX) || name.startsWith(REPOSITORY_DOWNLOADS_PREFIX);
			}
		})) {
			PublishUtil.deleteDirectory(folder, new NullProgressMonitor());
		}

	}

	/**
	 * Returns the {@link JavaVersion} of the given {@link IJavaProject}
	 */
	public static JavaVersion getJavaVersion(IProject project) {
		IJavaProject javaProject = JavaCore.create(project);
		if (javaProject != null) {

			// first check the manifest for that
			// Bundle-RequiredExecutionEnvironment
			BundleManifest bundleManifest = BundleManifestCorePlugin.getBundleManifestManager().getBundleManifest(
					javaProject);
			Dictionary<String, String> manifest = bundleManifest.toDictionary();
			if (manifest != null && manifest.get(Constants.BUNDLE_REQUIREDEXECUTIONENVIRONMENT) != null) {
				String javaVersion = manifest.get(Constants.BUNDLE_REQUIREDEXECUTIONENVIRONMENT);
				return JAVA_VERSION_MAPPING.get(javaVersion);
			}

			// second check the project for a matching jvm
			try {
				IClasspathContainer container = JavaCore.getClasspathContainer(JRE_CONTAINER_PATH, javaProject);
				if (container != null && container instanceof JREContainer) {
					// reflection hack to get the internal jvm install
					Field field = JREContainer.class.getDeclaredField("fVMInstall");
					field.setAccessible(true);
					IVMInstall vm = (IVMInstall) field.get((JREContainer) container);

					IExecutionEnvironmentsManager manager = JavaRuntime.getExecutionEnvironmentsManager();
					// check for strict match
					for (IExecutionEnvironment executionEnvironment : manager.getExecutionEnvironments()) {
						if (executionEnvironment.isStrictlyCompatible(vm)) {
							return JAVA_VERSION_MAPPING.get(executionEnvironment.getId());
						}
					}

					// check for default
					for (IExecutionEnvironment executionEnvironment : manager.getExecutionEnvironments()) {
						if (executionEnvironment.getDefaultVM() != null
								&& executionEnvironment.getDefaultVM().equals(vm)) {
							return JAVA_VERSION_MAPPING.get(executionEnvironment.getId());
						}
					}

					// check for compatibility
					for (IExecutionEnvironment executionEnvironment : manager.getExecutionEnvironments()) {
						if (Arrays.asList(executionEnvironment.getCompatibleVMs()).contains(vm)) {
							return JAVA_VERSION_MAPPING.get(executionEnvironment.getId());
						}
					}
				}
			}
			catch (Exception e) {
				SpringCore.log(e);
			}
		}
		return null;
	}

	public static Server getServer(IServerBehaviour server) {
		if (server instanceof ServerBehaviour) {
			return (Server) ((ServerBehaviour) server).getServer().loadAdapter(Server.class, null);
		}
		return null;
	}

	public static ServerRuntime getServerRuntime(IServerBehaviour server) {
		if (server instanceof ServerBehaviour) {
			if (((ServerBehaviour) server).getServer().getRuntime() == null) {
				return null;
			}
			return (ServerRuntime) ((ServerBehaviour) server).getServer().getRuntime().loadAdapter(ServerRuntime.class,
					null);
		}
		return null;
	}

}
