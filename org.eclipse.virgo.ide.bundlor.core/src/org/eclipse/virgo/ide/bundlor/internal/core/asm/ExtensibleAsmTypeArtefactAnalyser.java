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

import java.io.InputStream;

import org.eclipse.virgo.bundlor.support.ArtifactAnalyzer;
import org.eclipse.virgo.bundlor.support.partialmanifest.PartialManifest;
import org.objectweb.asm.ClassReader;

/**
 * {@link ArtifactAnalyzer} implementation that uses ASM to scan <code>.class</code> files for dependencies and exports.
 * <p/>
 *
 * @author Christian Dupuis
 */
public class ExtensibleAsmTypeArtefactAnalyser implements ArtifactAnalyzer {

    /**
     * @inheritDoc
     */
    public void analyse(InputStream artefact, String artefactName, PartialManifest model) throws Exception {
        ClassReader reader = new ClassReader(artefact);
        reader.accept(new ArtefactAnalyserClassVisitor(model), 0);
    }

    /**
     * @inheritDoc
     */
    public boolean canAnalyse(String artefactName) {
        return artefactName.endsWith(".class");
    }

    /**
     * @inheritDoc
     */
    @Override
    public String toString() {
        return "ASM Type Scanner";
    }

}
