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
package org.eclipse.virgo.ide.jdt.internal.ui.properties;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.internal.dialogs.DialogUtil;
import org.eclipse.virgo.ide.module.core.ServerModuleDelegate;
import org.springframework.ide.eclipse.core.java.JdtUtils;


/**
 * {@link PropertyPage} implementation that enables the user to configure java source folders that
 * are pure test folders.
 * @author Christian Dupuis
 * @since 1.0.1
 */
@SuppressWarnings("restriction")
public class TestSourceFolderPreferencePage extends PropertyPage {

	private IProject project;

	private boolean modified = false;

	private CheckboxTableViewer listViewer;

	private static final int PROJECT_LIST_MULTIPLIER = 30;

	protected Control createContents(Composite parent) {

		Font font = parent.getFont();

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		composite.setFont(font);

		initialize();

		Label description = createDescriptionLabel(composite);
		description.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		listViewer = CheckboxTableViewer.newCheckList(composite, SWT.TOP | SWT.BORDER);
		listViewer.getTable().setFont(font);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.grabExcessHorizontalSpace = true;

		if (project != null && !project.isOpen())
			listViewer.getControl().setEnabled(false);

		// Only set a height hint if it will not result in a cut off dialog
		if (DialogUtil.inRegularFontMode(parent)) {
			data.heightHint = getDefaultFontHeight(listViewer.getTable(), PROJECT_LIST_MULTIPLIER);
		}
		listViewer.getTable().setLayoutData(data);
		listViewer.getTable().setFont(font);

		listViewer.setLabelProvider(new ClasspathEntryLabelProvider());
		listViewer.setContentProvider(new ClasspathEntryContentProvider());
		listViewer.setComparator(new ViewerComparator());
		listViewer.setInput(project);
		listViewer.setCheckedElements(ServerModuleDelegate.getSourceClasspathEntries(project, true)
				.toArray());

		// check for initial modification to avoid work if no changes are made
		listViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				modified = true;
			}
		});

		return composite;
	}

	private static int getDefaultFontHeight(Control control, int lines) {
		FontData[] viewerFontData = control.getFont().getFontData();
		int fontHeight = 10;

		// If we have no font data use our guess
		if (viewerFontData.length > 0) {
			fontHeight = viewerFontData[0].getHeight();
		}
		return lines * fontHeight;

	}

	private void initialize() {
		project = (IProject) getElement().getAdapter(IResource.class);
		noDefaultAndApplyButton();
		setDescription("Select Java source folders that contain unit and integration test classes.\nThe contents of those test folders will not get deployed to any server runtime.");
	}

	public boolean performOk() {
		if (!modified) {
			return true;
		}

		try {
			List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();
			for (IClasspathEntry entry : JavaCore.create(project).getRawClasspath()) {
				if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
					Set<IClasspathAttribute> attrs = new HashSet<IClasspathAttribute>();
					for (IClasspathAttribute attr : entry.getExtraAttributes()) {
						if (!attr.getName().equals(
								ServerModuleDelegate.TEST_CLASSPATH_ENTRY_ATTRIBUTE)) {
							attrs.add(attr);
						}
					}
					attrs.add(getClasspathAttribute(entry));

					entries.add(JavaCore.newSourceEntry(entry.getPath(), entry
							.getInclusionPatterns(), entry.getExclusionPatterns(), entry
							.getOutputLocation(), (IClasspathAttribute[]) attrs
							.toArray(new IClasspathAttribute[attrs.size()])));
				}
				else {
					entries.add(entry);
				}
			}

			JavaCore.create(project).setRawClasspath(
					(IClasspathEntry[]) entries.toArray(new IClasspathEntry[entries.size()]),
					new NullProgressMonitor());
		}
		catch (JavaModelException e) {
		}

		return true;
	}

	private IClasspathAttribute getClasspathAttribute(IClasspathEntry entry) {
		IClasspathAttribute testFolderAttribute = JavaCore.newClasspathAttribute(
				ServerModuleDelegate.TEST_CLASSPATH_ENTRY_ATTRIBUTE, "true"); //$NON-NLS-1$
		IClasspathAttribute sourceFolderAttribute = JavaCore.newClasspathAttribute(
				ServerModuleDelegate.TEST_CLASSPATH_ENTRY_ATTRIBUTE, "false"); //$NON-NLS-1$

		Object[] testFolders = listViewer.getCheckedElements();
		for (Object testFolder : testFolders) {
			if (((IClasspathEntry) testFolder).getPath().equals(entry.getPath())) {
				return testFolderAttribute;
			}
		}
		return sourceFolderAttribute;
	}

	class ClasspathEntryLabelProvider implements ILabelProvider {

		public Image getImage(Object element) {
			return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_PACKFRAG_ROOT);
		}

		public String getText(Object element) {
			return ((IClasspathEntry) element).getPath().toString().substring(1);
		}

		public void addListener(ILabelProviderListener listener) {
			// nothing to do
		}

		public void dispose() {
			// nothing to do
		}

		public boolean isLabelProperty(Object element, String property) {
			// nothing to do
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
			// nothing to do
		}

	}

	class ClasspathEntryContentProvider implements IStructuredContentProvider {

		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof IProject) {
				List<IClasspathEntry> sourceFolders = new ArrayList<IClasspathEntry>();
				try {
					for (IClasspathEntry entry : JavaCore.create(project).getRawClasspath()) {
						if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
							sourceFolders.add(entry);
						}
					}
					return sourceFolders.toArray(new IClasspathEntry[sourceFolders.size()]);
				}
				catch (JavaModelException e) {
				}
			}
			return new Object[0];
		}

		public void dispose() {
			// nothing to do
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// nothing to do
		}
	}

}
