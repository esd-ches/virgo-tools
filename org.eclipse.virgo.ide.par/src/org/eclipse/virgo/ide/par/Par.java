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
package org.eclipse.virgo.ide.par;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Par</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.virgo.ide.par.Par#getBundle <em>Bundle</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.virgo.ide.par.ParPackage#getPar()
 * @model
 * @generated
 */
public interface Par extends EObject {
	/**
	 * Returns the value of the '<em><b>Bundle</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.virgo.ide.par.Bundle}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Bundle</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Bundle</em>' containment reference list.
	 * @see org.eclipse.virgo.ide.par.ParPackage#getPar_Bundle()
	 * @model containment="true"
	 * @generated
	 */
	EList<Bundle> getBundle();

} // Par
