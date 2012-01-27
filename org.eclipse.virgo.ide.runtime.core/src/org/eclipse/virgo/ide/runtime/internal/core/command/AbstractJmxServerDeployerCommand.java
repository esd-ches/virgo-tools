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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.eclipse.core.runtime.IPath;
import org.eclipse.virgo.ide.runtime.core.IServerBehaviour;
import org.eclipse.virgo.ide.runtime.internal.core.utils.StatusUtil;
import org.eclipse.wst.server.core.IModule;
import org.springframework.util.ClassUtils;


/**
 * Base implementation that encapsulates the communication with a JMX MBean.
 * @author Christian Dupuis
 * @since 1.0.1
 */
public abstract class AbstractJmxServerDeployerCommand<T> extends AbstractJmxServerCommand {

	private static final String FILE_SCHEME = "file";
	
	protected final IModule module;

	/**
	 * Creates a new {@link AbstractJmxServerDeployerCommand}.
	 */
	public AbstractJmxServerDeployerCommand(IServerBehaviour serverBehaviour, IModule module) {
		super(serverBehaviour);
		this.module = module;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("[");
		builder.append(ClassUtils.getShortName(getClass())).append(" -> ").append(getMBeanName())
				.append(".").append(getOperationName()).append("(").append(
						Arrays.deepToString(getOperationArguments())).append(")").append("]");
		return builder.toString();
	}

	/**
	 * Method to be called by sub-classes to execute given command against the MBean
	 */
	@SuppressWarnings("unchecked")
	protected final T doExecute() throws IOException, TimeoutException {

		JmxServerCommandTemplate template = new JmxServerCommandTemplate() {

			public Object invokeOperation(MBeanServerConnection connection) throws Exception {

				ObjectName name = ObjectName.getInstance(getMBeanName());

				Object[] operationArguments = getOperationArguments();

				String[] classNames = new String[operationArguments.length];
				for (int i = 0; i < operationArguments.length; i++) {
					if (operationArguments[i] instanceof Boolean) {
						classNames[i] = boolean.class.getName();
					}
					else {
						classNames[i] = operationArguments[i].getClass().getName();
					}
				}

				return connection.invoke(name, getOperationName(), operationArguments, classNames);
			}
		};

		return (T) execute(template);
	}
	
	/**
	 * Returns the name of the MBean to connect to.
	 */
	protected String getMBeanName() {
		return serverBehaviour.getVersionHandler().getDeployerMBeanName();
	}
	
	/**
	 * Returns the arguments of the MBean operation. 
	 */
	protected abstract Object[] getOperationArguments();
	
	/**
	 * Returns the name of the MBean operation. 
	 */
	protected abstract String getOperationName();
	
	/**
	 * Create a {@link URI} from the given <code>path</code>. 
	 */
	static final URI getUri(IPath path) {
		// we can't use path.toFile().toURI() as this will use OS specific paths and will fail
		// miserable if you run for VMware
		try {
			if (path.toString().startsWith("/")) {
				return new URI(FILE_SCHEME, path.toString(), null);
			}
			else {
				return new URI(FILE_SCHEME, "/" + path.toString(), null);
			}
		}
		catch (URISyntaxException e) {
			StatusUtil.error(e);
		}
		return null;
	}
}
