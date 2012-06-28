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

package org.eclipse.virgo.ide.runtime.internal.ui.providers;

import org.apache.commons.lang.ObjectUtils;
import org.eclipse.core.resources.IFile;

/**
 * 
 * @author Miles Parker
 * 
 */
public class ServerFileSelection {
	private final IFile file;

	private final String line;

	private final int offset;

	private final int length;

	private final int item;

	public ServerFileSelection(IFile file, String line, int start, int end, int item) {
		super();
		this.file = file;
		this.line = line;
		this.offset = start;
		this.length = end;
		this.item = item;
	}

	public IFile getFile() {
		return file;
	}

	public String getLine() {
		return line;
	}

	public int getOffset() {
		return offset;
	}

	public int getLength() {
		return length;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return file.hashCode() + offset + 27103 * length;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object arg) {
		if (arg instanceof ServerFileSelection) {
			ServerFileSelection other = (ServerFileSelection) arg;
			return ObjectUtils.equals(file, other.file) && item == other.item;
		}
		return false;
	}
}
