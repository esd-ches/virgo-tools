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
package org.eclipse.virgo.ide.runtime.internal.core.command;

import java.io.IOException;
import java.util.Hashtable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.eclipse.virgo.ide.runtime.core.IServerBehaviour;
import org.eclipse.virgo.ide.runtime.core.ServerCorePlugin;
import org.eclipse.virgo.ide.runtime.core.ServerUtils;
import org.eclipse.virgo.ide.runtime.internal.core.Server;
import org.eclipse.virgo.ide.runtime.internal.core.utils.StatusUtil;


/**
 * @author Christian Dupuis
 * @since 1.0.1
 */
public abstract class AbstractJmxServerCommand {

	protected interface JmxServerCommandTemplate {

		Object invokeOperation(MBeanServerConnection connection) throws Exception;

	}

	private static final String JMX_CONNECTOR_URL = "service:jmx:rmi:///jndi/rmi://%s:%d/jmxrmi"; //$NON-NLS-1$

	protected final IServerBehaviour serverBehaviour;

	public AbstractJmxServerCommand(IServerBehaviour serverBehaviour) {
		this.serverBehaviour = serverBehaviour;
	}

	private JMXConnector getJmxConnector() throws IOException {
		Hashtable<String, Object> h = new Hashtable<String, Object>();
//		String username = behaviour.getDmServer().getDeployerUsername();
//		String password = behaviour.getDmServer().getDeployerPassword();
//		if (StringUtils.hasText(username)) {
//			String[] credentials = new String[] { username, password };
//			h.put(JMX_REMOTE_CREDENTIALS, credentials);
//		}
		Server server = ServerUtils.getServer(serverBehaviour);

		if (serverBehaviour.getMBeanServerIp() == null) {
			throw new IOException(Messages.AbstractJmxServerCommand_MBeanNotOpenMessage);
		}
		String connectorUrl = String.format(JMX_CONNECTOR_URL, serverBehaviour.getMBeanServerIp(),
				server.getMBeanServerPort());
		return JMXConnectorFactory.connect(new JMXServiceURL(connectorUrl), h);
	}

	protected final Object execute(final JmxServerCommandTemplate template) throws TimeoutException {

		Callable<Object> deployOperation = new Callable<Object>() {

			public Object call() throws Exception {
				JMXConnector connector = null;
				try {
					connector = getJmxConnector();
					return template.invokeOperation(connector.getMBeanServerConnection());
				}
				finally {
					if (connector != null) {
						try {
							connector.close();
						}
						catch (IOException e) {
							StatusUtil.error(e);
						}
					}
				}
			}
		};

		FutureTask<Object> task = new FutureTask<Object>(deployOperation);
		ServerCorePlugin.EXECUTOR.submit(task);

		try {
			return task.get(30, TimeUnit.SECONDS);
		}
		catch (InterruptedException e) {
			// swallow exception here
		}
		catch (ExecutionException e) {
			// swallow exception here
		}

		return null;
	}

}
