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
package org.eclipse.virgo.ide.runtime.internal.core;

import java.io.IOException;

import org.eclipse.virgo.ide.runtime.internal.core.utils.StatusUtil;

/**
 * Ping thread the tries to connect to the dm server and listens to the recovery
 * notification.
 * <p>
 * This is used to test if a server is running after it has been started by the server view.
 * @author Christian Dupuis
 * @since 1.0.0
 */
public class ServerStatusPingThread {
	
	/** Delay after which the thread should start pinging */
	private static final int PING_DELAY = 3000;

	/** Interval in which the thread to ping the server */
	private static final int PING_INTERVAL = 1000;
	
	/** Indicate that the thread should stop pinging */
	private boolean stop = false;
	
	/** Reference to the server the user has started on the UI */
	private final ServerBehaviour behaviour;
	
	/**
	 * Creates a new {@link ServerStatusPingThread}
	 */
	protected ServerStatusPingThread(ServerBehaviour behaviour) {
		this.behaviour = behaviour;
		Thread t = new Thread("SpringSource dm Server Ping Thread") {
			@Override
			public void run() {
				ping();
			}
		};
		t.setDaemon(true);
		t.start();
	}

	protected void ping() {
		try {
			Thread.sleep(PING_DELAY);
		}
		catch (Exception e) {
			// ignore
		}
		while (!stop) {
			try {
				
				Boolean value = behaviour.getServerDeployer().ping();
				if (!stop && value != null && value.booleanValue()) {
					// ping worked - server is up
					stop = true;
					behaviour.setServerStarted();
				}
				else {
					sleep();
				}
			}
			catch (IOException se) {
				sleep();
			}
			catch (Exception e) {
				StatusUtil.error("Server startup ping failed", e);
				// pinging failed
				if (!stop) {
					sleep();
				}
			}
		}
	}
	
	/**
	 * Sends this thread to sleep for the configured timeout
	 */
	private void sleep() {
		try {
			Thread.sleep(PING_INTERVAL);
		}
		catch (InterruptedException e) {
		}
	}

	/**
	 * Stops this thread from pinging the dm Server instance
	 */
	public void stop() {
		stop = true;
	}
	
}
