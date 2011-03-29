/*******************************************************************************
 * Copyright (c) 2010 SpringSource, a divison of VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource, a division of VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.virgo.ide.bundlor.internal.core.asm;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.EmptyVisitor;

import org.eclipse.virgo.bundlor.support.partialmanifest.PartialManifest;

/**
 * ASM {@link ClassVisitor} for scanning class files.
 * @author Christian Dupuis
 * @author Rob Harrop
 */
class ArtefactAnalyserClassVisitor extends EmptyVisitor implements ClassVisitor {

	private static final String CLASS_NAME_PREFIX = "class$";

	/**
	 * That <code>PartialManifest</code> being updated.
	 */
	private final PartialManifest partialManifest;

	/**
	 * The type that is being scanned.
	 */
	private Type type;

	/**
	 * Creates a new <code>ArtefactAnalyserClassVisitor</code> to scan the supplied {@link PartialManifest}.
	 * 
	 * @param partialManifest the <code>PartialManifest</code> to scan.
	 */
	public ArtefactAnalyserClassVisitor(PartialManifest partialManifest) {
		this.partialManifest = partialManifest;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		Type type = Type.getObjectType(name);
		this.type = type;

		this.partialManifest.recordType(VisitorUtils.getFullyQualifiedTypeName(type));

		VisitorUtils.recordReferencedTypes(this.partialManifest, Type.getObjectType(superName));
		VisitorUtils.recordUses(this.partialManifest, type, Type.getObjectType(superName));
		for (String interfaceName : interfaces) {
			VisitorUtils.recordReferencedTypes(this.partialManifest, Type.getObjectType(interfaceName));
			VisitorUtils.recordUses(this.partialManifest, type, Type.getObjectType(interfaceName));
		}
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		Type t = Type.getType(desc);
		VisitorUtils.recordReferencedTypes(this.partialManifest, t);
		VisitorUtils.recordUses(this.partialManifest, this.type, t);
		return null;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		Type t = Type.getType(desc);
		if ((access & Opcodes.ACC_SYNTHETIC) == Opcodes.ACC_SYNTHETIC) {
			if (Class.class.getName().equals(t.getClassName())) {
				if (name != null && name.startsWith(CLASS_NAME_PREFIX)) {
					name = name.substring(CLASS_NAME_PREFIX.length());
					name = name.replace('$', '.');
					int lastDotIndex = name.lastIndexOf('.');

					for (int i = 0; i < lastDotIndex; i++) {
						if (Character.isUpperCase(name.charAt(i))) {
							if (i == 0) {
								return null;
							}
							if (name.charAt(i - 1) == '.') {
								name = name.substring(0, i) + name.substring(i).replace('.', '$');
								break;
							}
						}
					}
					if (Character.isJavaIdentifierStart(name.charAt(0))) {
						this.partialManifest.recordReferencedType(name);
					}
				}
			}
		}
		VisitorUtils.recordReferencedTypes(this.partialManifest, t);
		return new ArtefactAnalyserFieldVisitor(this.partialManifest, this.type);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		VisitorUtils.recordReferencedTypes(this.partialManifest, Type.getArgumentTypes(desc));
		VisitorUtils.recordReferencedTypes(this.partialManifest, Type.getReturnType(desc));
		if (exceptions != null) {
			for (String exception : exceptions) {
				VisitorUtils.recordReferencedTypes(this.partialManifest, Type.getObjectType(exception));
			}
		}
		if (access != Opcodes.ACC_PRIVATE) {
			VisitorUtils.recordUses(this.partialManifest, this.type, Type.getArgumentTypes(desc));
			VisitorUtils.recordUses(this.partialManifest, this.type, Type.getReturnType(desc));
			if (exceptions != null) {
				for (String exception : exceptions) {
					VisitorUtils.recordUses(this.partialManifest, this.type, Type.getObjectType(exception));
				}
			}
		}
		return new ArtefactAnalyserMethodVisitor(this.partialManifest, this.type);
	}

}
