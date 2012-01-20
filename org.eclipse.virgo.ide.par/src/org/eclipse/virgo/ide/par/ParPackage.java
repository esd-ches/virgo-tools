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

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 * <li>each class,</li>
 * <li>each feature of each class,</li>
 * <li>each enum,</li>
 * <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see org.eclipse.virgo.ide.par.ParFactory
 * @model kind="package"
 * @generated
 */
public interface ParPackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "par";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://eclipse.org/virgo/par.ecore";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "org.eclipse.virgo.ide.par";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	ParPackage eINSTANCE = org.eclipse.virgo.ide.par.impl.ParPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.virgo.ide.par.impl.ParImpl <em>Par</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.virgo.ide.par.impl.ParImpl
	 * @see org.eclipse.virgo.ide.par.impl.ParPackageImpl#getPar()
	 * @generated
	 */
	int PAR = 0;

	/**
	 * The feature id for the '<em><b>Bundle</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PAR__BUNDLE = 0;

	/**
	 * The number of structural features of the '<em>Par</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PAR_FEATURE_COUNT = 1;

	/**
	 * The meta object id for the '{@link org.eclipse.virgo.ide.par.impl.BundleImpl <em>Bundle</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.virgo.ide.par.impl.BundleImpl
	 * @see org.eclipse.virgo.ide.par.impl.ParPackageImpl#getBundle()
	 * @generated
	 */
	int BUNDLE = 1;

	/**
	 * The feature id for the '<em><b>Symbolic Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUNDLE__SYMBOLIC_NAME = 0;

	/**
	 * The number of structural features of the '<em>Bundle</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUNDLE_FEATURE_COUNT = 1;

	/**
	 * Returns the meta object for class '{@link org.eclipse.virgo.ide.par.Par <em>Par</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Par</em>'.
	 * @see org.eclipse.virgo.ide.par.Par
	 * @generated
	 */
	EClass getPar();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.virgo.ide.par.Par#getBundle <em>Bundle</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Bundle</em>'.
	 * @see org.eclipse.virgo.ide.par.Par#getBundle()
	 * @see #getPar()
	 * @generated
	 */
	EReference getPar_Bundle();

	/**
	 * Returns the meta object for class '{@link org.eclipse.virgo.ide.par.Bundle <em>Bundle</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Bundle</em>'.
	 * @see org.eclipse.virgo.ide.par.Bundle
	 * @generated
	 */
	EClass getBundle();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.virgo.ide.par.Bundle#getSymbolicName <em>Symbolic Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Symbolic Name</em>'.
	 * @see org.eclipse.virgo.ide.par.Bundle#getSymbolicName()
	 * @see #getBundle()
	 * @generated
	 */
	EAttribute getBundle_SymbolicName();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	ParFactory getParFactory();

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '{@link org.eclipse.virgo.ide.par.impl.ParImpl <em>Par</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.virgo.ide.par.impl.ParImpl
		 * @see org.eclipse.virgo.ide.par.impl.ParPackageImpl#getPar()
		 * @generated
		 */
		EClass PAR = eINSTANCE.getPar();

		/**
		 * The meta object literal for the '<em><b>Bundle</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference PAR__BUNDLE = eINSTANCE.getPar_Bundle();

		/**
		 * The meta object literal for the '{@link org.eclipse.virgo.ide.par.impl.BundleImpl <em>Bundle</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.virgo.ide.par.impl.BundleImpl
		 * @see org.eclipse.virgo.ide.par.impl.ParPackageImpl#getBundle()
		 * @generated
		 */
		EClass BUNDLE = eINSTANCE.getBundle();

		/**
		 * The meta object literal for the '<em><b>Symbolic Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute BUNDLE__SYMBOLIC_NAME = eINSTANCE.getBundle_SymbolicName();

	}

} //ParPackage
