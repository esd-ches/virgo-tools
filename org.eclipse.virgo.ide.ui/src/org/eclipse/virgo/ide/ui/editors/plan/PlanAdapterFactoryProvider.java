/*******************************************************************************
 * Copyright (c) 2010 - 2012 SpringSource, a divison of VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource, a division of VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.virgo.ide.ui.editors.plan;

import org.eclipse.wst.sse.core.internal.ltk.modelhandler.IDocumentTypeHandler;
import org.eclipse.wst.xml.ui.internal.registry.AdapterFactoryProviderForXML;

/**
 * @author Christian Dupuis
 * @since 2.3.1
 */
public class PlanAdapterFactoryProvider extends AdapterFactoryProviderForXML {

	@Override
	public boolean isFor(IDocumentTypeHandler contentTypeDescription) {
		return (contentTypeDescription instanceof PlanModelHandler);
	}

}
