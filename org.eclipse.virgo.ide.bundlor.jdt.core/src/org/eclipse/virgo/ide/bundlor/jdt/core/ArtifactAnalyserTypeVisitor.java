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

package org.eclipse.virgo.ide.bundlor.jdt.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.virgo.bundlor.support.partialmanifest.PartialManifest;

/**
 * {@link MethodVisitor} that is based on JDT's AST </p>
 * <strong>Concurrent Semantics</strong><br />
 * Not thread safe.
 * @author Christian Dupuis
 * @author Glyn Normington
 */
public class ArtifactAnalyserTypeVisitor extends ASTVisitor {

	private final PartialManifest partialManifest;

	private final Set<String> referencedTypes = new HashSet<String>();

	private String typePackage;

	public ArtifactAnalyserTypeVisitor(PartialManifest model) {
		this.partialManifest = model;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean visit(CastExpression node) {
		recordTypeBinding(node.resolveTypeBinding());
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean visit(CatchClause node) {
		recordTypeBinding(node.getException().getType().resolveBinding());
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean visit(ClassInstanceCreation node) {
		recordTypeBinding(node.resolveTypeBinding());
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean visit(FieldDeclaration node) {
		recordTypeBinding(node.getType().resolveBinding());
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean visit(ImportDeclaration node) {
		if (node.resolveBinding() != null && !((IBinding) node.resolveBinding()).isRecovered()) {
			IBinding binding = node.resolveBinding();
			if (binding instanceof IPackageBinding) {
				partialManifest.recordReferencedPackage(((IPackageBinding) binding).getName());
			}
			else if (binding instanceof ITypeBinding) {
				recordTypeBinding((ITypeBinding) binding);
			}
		}
		else {
			if (!node.isOnDemand() && !node.isStatic()) {
				Name importElementName = node.getName();
				recordFullyQualifiedName(importElementName.getFullyQualifiedName());
			}
			else if (node.isOnDemand() && !node.isStatic()) {
				Name importElementName = node.getName();
				partialManifest.recordReferencedPackage(importElementName.getFullyQualifiedName());
			}
			else if (!node.isOnDemand() && node.isStatic()) {
				Name importElementName = node.getName();
				String fqn = importElementName.getFullyQualifiedName().substring(0,
						importElementName.getFullyQualifiedName().lastIndexOf('.'));
				recordFullyQualifiedName(fqn);
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean visit(InstanceofExpression node) {
		recordTypeBinding(node.getRightOperand().resolveBinding());
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean visit(MarkerAnnotation node) {
		ITypeBinding binding = node.resolveTypeBinding();
		recordTypeBinding(binding);
		this.partialManifest.recordUsesPackage(this.typePackage, getPackageBinding(binding).getName());
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean visit(MethodDeclaration node) {
		IMethodBinding binding = node.resolveBinding();
		if (binding == null) {
			return true;
		}
		// return value
		if (binding.getReturnType() != null) {
			String fqn = binding.getReturnType().getQualifiedName();
			if (!"void".equals(fqn)) {
				recordTypeBinding(binding.getReturnType());
				if (!Modifier.isPrivate(node.getModifiers())) {
					IPackageBinding packageBinding = getPackageBinding(binding.getReturnType());
					if (packageBinding != null) {
						this.partialManifest.recordUsesPackage(this.typePackage, packageBinding.getName());
					}
				}
			}
		}
		// throws clause
		if (binding.getExceptionTypes() != null) {
			for (ITypeBinding exceptionBinding : binding.getExceptionTypes()) {
				recordTypeBinding(exceptionBinding);
				if (!Modifier.isPrivate(node.getModifiers())) {
					IPackageBinding packageBinding = getPackageBinding(exceptionBinding);
					if (packageBinding != null) {
						this.partialManifest.recordUsesPackage(this.typePackage, packageBinding.getName());
					}
				}
			}
		}
		// parameter
		if (binding.getParameterTypes() != null) {
			for (ITypeBinding parameterBinding : binding.getParameterTypes()) {
				recordTypeBinding(parameterBinding);
				if (!Modifier.isPrivate(node.getModifiers())) {
					IPackageBinding packageBinding = getPackageBinding(parameterBinding);
					if (packageBinding != null) {
						this.partialManifest.recordUsesPackage(this.typePackage, packageBinding.getName());
					}
				}
			}
		}

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean visit(MethodInvocation node) {
		IMethodBinding binding = node.resolveMethodBinding();
		if (binding != null) {
			recordTypeBinding(binding.getDeclaringClass());

			// return value
			if (binding.getReturnType() != null) {
				recordTypeBinding(binding.getReturnType());
			}
			// parameter
			if (binding.getParameterTypes() != null) {
				for (ITypeBinding parameterBinding : binding.getParameterTypes()) {
					recordTypeBinding(parameterBinding);
				}
			}
		}
		else {

		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean visit(NormalAnnotation node) {
		ITypeBinding binding = node.resolveTypeBinding();
		recordTypeBinding(binding);
		this.partialManifest.recordUsesPackage(this.typePackage, getPackageBinding(binding).getName());
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean visit(QualifiedName node) {
		IBinding binding = node.resolveBinding();

		if (isValid(binding)) {
			// Record the declared type.
			recordTypeBinding(node.resolveTypeBinding());

			// Record the declaring type.
			if (binding.getKind() == IBinding.VARIABLE) {
				recordTypeBinding(((IVariableBinding) binding).getDeclaringClass());
			}
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean visit(SimpleName node) {
		String fqn = node.getFullyQualifiedName();
		recordFullyQualifiedName(fqn);
		return true;
	}

	/**
	 * It is important to record the type binding only if it is not recovered. Recovered bindings occur for invalid
	 * source code and incomplete class paths.
	 * 
	 * @param binding the binding which may be recorded
	 * @return <code>true</code> if and only if the binding should be recorded
	 */
	private boolean isValid(IBinding binding) {
		return binding != null && !binding.isRecovered();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean visit(SimpleType node) {
		if (isValid(node.resolveBinding())) {
			recordTypeBinding(node.resolveBinding());
		}
		else {
			Name simpleName = node.getName();
			String fqn = simpleName.getFullyQualifiedName();
			recordFullyQualifiedName(fqn);
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean visit(SingleMemberAnnotation node) {
		ITypeBinding binding = node.resolveTypeBinding();
		recordTypeBinding(binding);
		this.partialManifest.recordUsesPackage(this.typePackage, getPackageBinding(binding).getName());
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean visit(TypeDeclaration node) {
		ITypeBinding binding = node.resolveBinding();
		if (binding == null) {
			return false;
		}
		String name = binding.getBinaryName();
		this.typePackage = getPackageBinding(binding).getName();

		if (!node.isLocalTypeDeclaration()) {
			this.partialManifest.recordType(name);
		}

		for (String referencedType : this.referencedTypes) {
			recordFullyQualifiedName(referencedType);
		}

		if (node.getSuperclassType() != null) {
			ITypeBinding superClassBinding = node.getSuperclassType().resolveBinding();
			recordTypeBinding(superClassBinding);
			this.partialManifest.recordUsesPackage(this.typePackage, getPackageBinding(superClassBinding).getName());
		}

		for (int i = 0; i < node.superInterfaceTypes().size(); i++) {
			org.eclipse.jdt.core.dom.Type interfaceType = (org.eclipse.jdt.core.dom.Type) node.superInterfaceTypes()
					.get(i);
			ITypeBinding interfaceBinding = interfaceType.resolveBinding();
			recordTypeBinding(interfaceBinding);
			this.partialManifest.recordUsesPackage(this.typePackage, getPackageBinding(interfaceBinding).getName());
		}

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		ITypeBinding binding = node.resolveBinding();
		if (binding == null) {
			return false;
		}
		this.partialManifest.recordType(binding.getBinaryName());

		ITypeBinding superClassBinding = binding.getSuperclass();
		if (superClassBinding != null) {
			recordTypeBinding(superClassBinding);
			this.partialManifest.recordUsesPackage(this.typePackage, getPackageBinding(superClassBinding).getName());
		}

		ITypeBinding[] interfaceBindings = binding.getInterfaces();
		if (interfaceBindings != null) {
			for (ITypeBinding interfaceBinding : interfaceBindings) {
				recordTypeBinding(interfaceBinding);
				this.partialManifest.recordUsesPackage(this.typePackage, getPackageBinding(interfaceBinding).getName());
			}
		}

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean visit(AnnotationTypeDeclaration node) {
		ITypeBinding binding = node.resolveBinding();
		if (binding == null) {
			return false;
		}

		String name = binding.getBinaryName();
		this.typePackage = getPackageBinding(binding).getName();

		this.partialManifest.recordType(name);
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean visit(EnumDeclaration node) {
		ITypeBinding binding = node.resolveBinding();
		if (binding == null) {
			return false;
		}

		String name = binding.getBinaryName();
		this.typePackage = getPackageBinding(binding).getName();

		this.partialManifest.recordType(name);
		return true;
	}

	private IPackageBinding getPackageBinding(ITypeBinding binding) {
		if (binding != null) {
			if (binding.isArray()) {
				return getPackageBinding(binding.getComponentType());
			}
			else {
				return binding.getPackage();
			}
		}
		return null;
	}

	private String recordFullyQualifiedName(String fqn) {
		if (!"void".equals(fqn)) {
			if (this.typePackage == null) {
				this.referencedTypes.add(fqn);
				return fqn;
			}
			else if (fqn != null && !this.typePackage.equals(getPackageName(fqn))) {
				// This is required to get FQCNs of the form
				// org.springframework.util.ReflectionUtils.MethodCallback correctly detected
				StringTokenizer segments = new StringTokenizer(fqn, ".");
				if (segments.countTokens() > 1) {
					List<String> newSegments = new ArrayList<String>();
					while (segments.hasMoreTokens()) {
						String segment = segments.nextToken();
						newSegments.add(segment);
						if (!Character.isLowerCase(segment.charAt(0))) {
							break;
						}
					}
					fqn = StringUtils.join(newSegments, ".");
				}

				this.partialManifest.recordReferencedType(fqn);
				return fqn;
			}
		}
		return "";
	}

	private String getPackageName(String fullyQualifiedTypeName) {
		if (fullyQualifiedTypeName == null) {
			return "";
		}
		int index = fullyQualifiedTypeName.lastIndexOf('.');

		if (index > -1) {
			return fullyQualifiedTypeName.substring(0, index);
		}

		return "";
	}

	private String recordTypeBinding(ITypeBinding binding) {
		if (binding != null) {
			if (binding.isArray()) {
				return recordTypeBinding(binding.getComponentType());
			}
			else {
				String fqn = binding.getBinaryName();
				return recordFullyQualifiedName(fqn);
			}
		}
		return "";
	}

}
