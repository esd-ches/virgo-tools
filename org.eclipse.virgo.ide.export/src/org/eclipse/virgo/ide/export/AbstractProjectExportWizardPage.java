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
package org.eclipse.virgo.ide.export;

import java.io.File;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.ui.filters.EmptyInnerPackageFilter;
import org.eclipse.jdt.ui.JavaElementComparator;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.ProblemsLabelDecorator;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.WizardExportResourcesPage;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;

/**
 * Abstract wizard page for presenting a list of projects and specifying a location to export to.
 * 
 * @author Christian Dupuis
 * @author Terry Hon
 */
@SuppressWarnings("restriction")
public abstract class AbstractProjectExportWizardPage extends WizardExportResourcesPage {

	private Button browseButton;

	private Text destinationText;

	protected TableViewer tableViewer;

	protected IStructuredSelection initialSelection;

	private boolean overwrite;

	protected AbstractProjectExportWizardPage(String pageName, IStructuredSelection selection) {
		super(pageName, selection);
		this.initialSelection = selection;
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);

		Label selectProject = new Label(composite, SWT.NONE);
		selectProject.setText("Select the project to export:");
		selectProject.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		createInputGroup(composite);

		createDestinationGroup(composite);

		setControl(composite);
	}

	public boolean getOverwrite() {
		return overwrite;
	}

	@Override
	protected void createDestinationGroup(Composite parent) {
		initializeDialogUnits(parent);

		Composite destinationSelectionGroup = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		destinationSelectionGroup.setLayout(layout);
		destinationSelectionGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Label label = new Label(destinationSelectionGroup, SWT.NONE);
		label.setText("Select the export destination:");
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		gridData.horizontalSpan = 3;
		label.setLayoutData(gridData);

		Label jarLabel = new Label(destinationSelectionGroup, SWT.NONE);
		jarLabel.setText(getDestinationLabel());
		jarLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true));

		destinationText = new Text(destinationSelectionGroup, SWT.BORDER);
		destinationText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		destinationText.setEditable(true);
		destinationText.addListener(SWT.Modify, this);

		browseButton = new Button(destinationSelectionGroup, SWT.PUSH);
		browseButton.setText("Browse...");
		browseButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleDestinationBrowseButtonPressed();
			}
		});

		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				updateFileName();
			}
		});

		updateFileName();

		final Button overwriteButton = new Button(destinationSelectionGroup, SWT.CHECK);
		overwriteButton.setText("Overwrite existing file without warning");

		GridData buttonData = new GridData(SWT.FILL, SWT.FILL, true, false);
		buttonData.horizontalSpan = 3;
		buttonData.verticalIndent = 5;
		overwriteButton.setLayoutData(buttonData);
		overwriteButton.setSelection(false);

		overwriteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				overwrite = overwriteButton.getSelection();
			}
		});
	}

	protected void updateFileName() {
		IProject project = getSelectedProject();
		if (project == null) {
			return;
		}

		BundleManifest manifest = getBundleManifest(project);
		if (manifest != null) {
			String name = getSymbolicName(manifest);
			String version = getVersion(manifest);

			IPath path = null;
			if (destinationText.getText() != null) {
				path = new Path("");
			} else {
				path = new Path(destinationText.getText());
			}
			if (name != null && version != null) {
				path = path.removeLastSegments(1).append(name + "-" + version + getExtension());
				destinationText.setText(path.toOSString());
			}
		}
	}

	protected abstract String getSymbolicName(BundleManifest bundleManifest);

	protected abstract String getVersion(BundleManifest bundleManifest);

	protected abstract BundleManifest getBundleManifest(IProject project);

	private void createInputGroup(Composite parent) {
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);

		tableViewer = new TableViewer(parent, SWT.BORDER | SWT.SINGLE);
		tableViewer.getTable().setLayoutData(data);
		tableViewer.setUseHashlookup(true);

		int labelFlags = JavaElementLabelProvider.SHOW_BASICS | JavaElementLabelProvider.SHOW_OVERLAY_ICONS
				| JavaElementLabelProvider.SHOW_SMALL_ICONS;
		ITreeContentProvider treeContentProvider = getTreeContentProvider();

		final DecoratingLabelProvider labelProvider = new DecoratingLabelProvider(new JavaElementLabelProvider(
				labelFlags), new ProblemsLabelDecorator(null));

		tableViewer.setContentProvider(treeContentProvider);
		tableViewer.setLabelProvider(labelProvider);
		tableViewer.addFilter(new EmptyInnerPackageFilter());
		tableViewer.setComparator(new JavaElementComparator());
		tableViewer.addFilter(getTreeViewerFilter());

		tableViewer.setInput(getInput());
		tableViewer.setSelection(new StructuredSelection(initialSelection.toArray()), true);
	}

	abstract protected String getDestinationLabel();

	// copied from AbstractJarDestinationWizardPage
	private String getDestinationValue() {
		String destinationString = destinationText.getText().trim();
		if (destinationString.indexOf('.') < 0) {
			destinationString += getOutputSuffix();
		}
		return destinationString;
	}

	abstract protected String getExtension();

	abstract protected Object getInput();

	public IPath getJarLocation() {
		return Path.fromOSString(destinationText.getText());
	}

	private String getOutputSuffix() {
		return ".jar";
	}

	public IProject getSelectedProject() {
		ISelection selection = tableViewer.getSelection();
		Object[] selectedItems;
		if (selection instanceof IStructuredSelection) {
			selectedItems = ((IStructuredSelection) selection).toArray();
		} else {
			selectedItems = new Object[0];
		}

		for (Object selectedItem : selectedItems) {
			if (selectedItem instanceof IJavaProject) {
				return ((IJavaProject) selectedItem).getProject();
			} else if (selectedItem instanceof IProject) {
				return (IProject) selectedItem;
			}
		}
		return null;
	}

	abstract protected ITreeContentProvider getTreeContentProvider();

	abstract protected ViewerFilter getTreeViewerFilter();

	// copied from AbstractJarDestinationWizardPage
	private void handleDestinationBrowseButtonPressed() {
		FileDialog dialog = new FileDialog(getContainer().getShell(), SWT.SAVE);
		dialog.setFilterExtensions(new String[] { "*" + getExtension() });

		String currentSourceString = getDestinationValue();
		int lastSeparatorIndex = currentSourceString.lastIndexOf(File.separator);
		if (lastSeparatorIndex != -1) {
			dialog.setFilterPath(currentSourceString.substring(0, lastSeparatorIndex));
			dialog.setFileName(currentSourceString.substring(lastSeparatorIndex + 1, currentSourceString.length()));
		} else {
			dialog.setFileName(currentSourceString);
		}
		String selectedFileName = dialog.open();
		if (selectedFileName != null) {
			IContainer[] findContainersForLocation = ResourcesPlugin.getWorkspace()
					.getRoot()
					.findContainersForLocation(new Path(selectedFileName));
			if (findContainersForLocation.length > 0) {
				selectedFileName = findContainersForLocation[0].getFullPath().makeRelative().toString();
			}
			destinationText.setText(selectedFileName);
		}
	}

	public void handleEvent(Event event) {
		setPageComplete(isPageComplete());
	}

	@Override
	public boolean isPageComplete() {
		String text = destinationText.getText();
		if (text.length() == 0) {
			setErrorMessage(null);
			return false;
		}

		if (!text.endsWith(getExtension())) {
			setErrorMessage("Export destination must have " + getExtension() + " extension.");
			return false;
		}

		if (tableViewer.getSelection().isEmpty()) {
			setErrorMessage("Bundle project selection must not be empty.");
			return false;
		}

		setErrorMessage(null);
		return true;
	}

}
