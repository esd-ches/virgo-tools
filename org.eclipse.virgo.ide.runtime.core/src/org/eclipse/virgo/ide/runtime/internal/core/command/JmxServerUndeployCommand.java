/*******************************************************************************
 * Copyright (c) 2010 SpringSource, a divison of VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource, a division of VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.virgo.ide.runtime.internal.core.command;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.eclipse.virgo.ide.facet.core.FacetCorePlugin;
import org.eclipse.virgo.ide.runtime.core.IServerBehaviour;
import org.eclipse.virgo.ide.runtime.internal.core.DeploymentIdentity;
import org.eclipse.wst.server.core.IModule;

/**
 * {@link IServerCommand} to undeploy artifacts on dm Server 2.0.
 * 
 * @author Christian Dupuis
 * @since 2.3.1
 */
public class JmxServerUndeployCommand extends AbstractJmxServerCommand implements IServerCommand<Void> {

	/** {@link DeploymentIdentity} of the deployed PAR or bundle */
	private final DeploymentIdentity identity;

	/** Module that needs refreshing */
	private final IModule module;

	private final String bundleObjectName;

	private final String parObjectName;

	private final String planObjectName;

	/**
	 * Creates a new {@link JmxServerUndeployCommand}.
	 */
	public JmxServerUndeployCommand(IServerBehaviour serverBehaviour, IModule module, String bundleObjectName,
		String parObjectName, String planObjectName) {
		super(serverBehaviour);
		this.module = module;
		this.identity = serverBehaviour.getDeploymentIdentities().remove(module.getId());
		this.bundleObjectName = bundleObjectName;
		this.parObjectName = parObjectName;
		this.planObjectName = planObjectName;
	}

	/**
	 * {@inheritDoc}
	 */
	public Void execute() throws IOException, TimeoutException {

		JmxServerCommandTemplate template = new JmxServerCommandTemplate() {

			public Object invokeOperation(MBeanServerConnection connection) throws Exception {
				ObjectName objectName = null;
				if (FacetCorePlugin.PAR_FACET_ID.equals(module.getModuleType().getId())) {
					objectName = ObjectName.getInstance(parObjectName.replace("$VERSION", identity.getVersion())
							.replace("$NAME", identity.getSymbolicName()));
				} else if (FacetCorePlugin.PLAN_FACET_ID.equals(module.getModuleType().getId())) {
					objectName = ObjectName.getInstance(planObjectName.replace("$VERSION", identity.getVersion())
							.replace("$NAME", identity.getSymbolicName()));
				} else {
					objectName = ObjectName.getInstance(bundleObjectName.replace("$VERSION", identity.getVersion())
							.replace("$NAME", identity.getSymbolicName()));
				}

				return connection.invoke(objectName, "uninstall", null, null);
			}

		};

		execute(template);
		return null;
	}
}
