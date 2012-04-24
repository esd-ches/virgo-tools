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

/**
 * Represents a state-full server command to control remote SpringSource dm
 * Server instances.
 * <p>
 * Each server command should only be executed once.
 * 
 * @author Christian Dupuis
 * @since 1.0.1
 */
public interface IServerCommand<T> {

	/**
	 * Execute this server command.
	 * 
	 * @return returns the generified return value of the command
	 * @throws IOException
	 * @throws TimeoutException
	 */
	T execute() throws IOException, TimeoutException;

}
