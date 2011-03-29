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
package org.eclipse.virgo.ide.runtime.internal.ui;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Dupuis
 */
public class SearchTextHistory {

	private static final int MAX_HISTORY = 10;

	private List<String> history= new ArrayList<String>();

	private int index = 0;

	public void add(String text) {
		int found = -1;
        int size = history.size();
        for (int i = 0; i < size; i++) {
            String s = (String) history.get(i);
            if (s.equals(text)) {
                found = i;
                break;
            }
        }

        if (found == -1) {
            if (size >= MAX_HISTORY)
                history.remove(size - 1);
            history.add(0, text);
        } else if (found != 0) {
            history.remove(found);
            history.add(0, text);
        }
        index = 0;
	}

	public String back() {
		if (canBack()) {
			String text = history.get(index);
			index++;
			return text;
		}
		return null;
	}

	public String forward() {
		if (canForward()) {
			index--;
			String text = history.get(index);
			return text;
		}
		return null;
	}

	public boolean canForward() {
		return index > 0;
	}

	public boolean canBack() {
		return index < history.size();
	}

	public String current() {
		return history.get(0);
	}
}
