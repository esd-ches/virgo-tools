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
package org.eclipse.virgo.ide.runtime.core;

import java.util.List;

/**
 * Marker interface to be implemented by configuration providers for the dm
 * server integration.
 * @author Christian Dupuis
 * @since 1.0.0
 */
public interface IServerConfiguration {
	
	List<String> getArtefactOrder();
	
	void setArtefactOrder(List<String> artefacts);
	
	void addArtefact(String artefact);

	void removeArtefact(String artefact);
	
}
