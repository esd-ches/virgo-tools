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
package org.eclipse.virgo.ide.runtime.internal.core.command;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.eclipse.virgo.ide.runtime.core.IServerBehaviour;

/**
 * {@link IServerCommand} to ping a dm Server.
 * 
 * @author Christian Dupuis
 * @since 1.0.1
 */
public class JmxServerPingCommand extends AbstractJmxServerCommand implements IServerCommand<Boolean> {

	/**
	 * Creates a new {@link JmxServerPingCommand}.
	 */
	public JmxServerPingCommand(IServerBehaviour serverBehaviour) {
		super(serverBehaviour);
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean execute() throws IOException, TimeoutException {

		JmxServerCommandTemplate template = new JmxServerCommandTemplate() {

			public Object invokeOperation(MBeanServerConnection connection) throws Exception {
				ObjectName name = ObjectName.getInstance(serverBehaviour.getVersionHandler()
						.getRecoveryMonitorMBeanName());
				return connection.getAttribute(name, "RecoveryComplete");
			}

		};

		Object returnValue = execute(template);
		if (returnValue instanceof Boolean) {
			return (Boolean) returnValue;
		}
		return null;
	}

}
