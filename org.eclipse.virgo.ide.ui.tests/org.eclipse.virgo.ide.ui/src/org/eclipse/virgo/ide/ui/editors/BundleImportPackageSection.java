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
package org.eclipse.virgo.ide.ui.editors;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.actions.ShowInPackageViewAction;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.service.resolver.ExportPackageDescription;
import org.eclipse.pde.core.IBaseModel;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.core.text.bundle.ExportPackageObject;
import org.eclipse.pde.internal.core.text.bundle.ImportPackageHeader;
import org.eclipse.pde.internal.core.text.bundle.ImportPackageObject;
import org.eclipse.pde.internal.core.text.bundle.PackageObject;
import org.eclipse.pde.internal.core.util.PDEJavaHelper;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.pde.internal.ui.editor.PDEFormPage;
import org.eclipse.pde.internal.ui.elements.DefaultTableProvider;
import org.eclipse.pde.internal.ui.util.SWTUtil;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.virgo.ide.bundlerepository.domain.Artefact;
import org.eclipse.virgo.ide.bundlerepository.domain.ArtefactRepository;
import org.eclipse.virgo.ide.bundlerepository.domain.BundleArtefact;
import org.eclipse.virgo.ide.bundlerepository.domain.OsgiVersion;
import org.eclipse.virgo.ide.bundlerepository.domain.PackageExport;
import org.eclipse.virgo.ide.runtime.core.provisioning.RepositoryUtils;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;


/**
 * @author Christian Dupuis
 */
public class BundleImportPackageSection extends AbstractImportSection {

	private static final String DESCRIPTION = "Specify packages on which this bundle depends without explicitly identifying their originating bundle.";

	private Action fGoToAction;

	private static final int ADD_INDEX = 0;

	private static final int ADD_REMOTE_BUNDLE_INDEX = 1;

	private static final int REMOVE_INDEX = 2;

	private static final int PROPERTIES_INDEX = 3;

	public BundleImportPackageSection(PDEFormPage page, Composite parent) {
		super(page, parent, Section.DESCRIPTION, new String[] { PDEUIMessages.ImportPackageSection_add, "Download...",
				PDEUIMessages.ImportPackageSection_remove, PDEUIMessages.ImportPackageSection_properties });
		getSection().setText("Import Package");
		getSection().setDescription(DESCRIPTION);
		getTablePart().setEditable(false);
	}

	protected void setElementsLocal(ImportListSelectionDialog dialog) {
		IProject project = ((BundleManifestEditor) this.getPage().getEditor()).getCommonProject();
		Set<PackageExport> packages = RepositoryUtils.getImportPackageProposals(project, "");
		ImportPackageHeader header = (ImportPackageHeader) getBundle().getManifestHeader(Constants.IMPORT_PACKAGE);
		Set<PackageExport> filteredElements = new HashSet<PackageExport>();

		if (header != null) {
			ImportPackageObject[] filter = header.getPackages();
			for (PackageExport proposal : packages) {
				for (ImportPackageObject imported : filter) {
					if (proposal.getName().equalsIgnoreCase(imported.getName())) {
						filteredElements.add(proposal);
					}
				}
			}
			packages.removeAll(filteredElements);
		}
		dialog.setElements(packages.toArray());
	}

	protected void setElementsRemote(ImportListSelectionDialog dialog) {
		Collection<BundleArtefact> bundles = null;
		ArtefactRepository bundleRepository = RepositoryUtils.searchForArtifacts("", true, false);
		bundles = bundleRepository.getBundles();

		Set<PackageExport> allPackageExports = new HashSet<PackageExport>();
		for (BundleArtefact currBundleArtefact : bundles) {
			allPackageExports.addAll(currBundleArtefact.getExports());
		}

		dialog.setElements(allPackageExports.toArray());
	}

	@Override
	protected ITableLabelProvider getLabelProvider() {
		return new ImportPackageLabelProvider();
	}

	class ImportPackageLabelProvider extends AbstractSectionViewerLabelProvider {

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_PACKAGE);
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			ImportPackageObject importPackageObject = (ImportPackageObject) element;
			String label = importPackageObject.getName();
			if (null != importPackageObject.getVersion()) {
				label += " " + importPackageObject.getVersion();
			}
			return label;
		}
	}

	@Override
	protected void fillContextMenu(IMenuManager manager) {
		final ISelection selection = fViewer.getSelection();
		boolean singleSelection = selection instanceof IStructuredSelection
				&& ((IStructuredSelection) selection).size() == 1;
		if (singleSelection) {
			manager.add(fGoToAction);
		}
		super.fillContextMenu(manager);
	}

	@Override
	protected void handleAdd() {
		internalHandleAdd(false);
	}

	private void internalHandleAdd(final boolean addRemote) {
		final ImportListSelectionDialog dialog = new ImportListSelectionDialog(PDEPlugin.getActiveWorkbenchShell(),
				new BundleImportPackageDialogLabelProvider());

		Runnable runnable = new Runnable() {
			public void run() {
				if (addRemote) {
					setElementsRemote(dialog);
				}
				else {
					setElementsLocal(dialog);
				}

				dialog.setMultipleSelection(true);
				dialog.setMessage(PDEUIMessages.ImportPackageSection_required);
				dialog.setTitle(PDEUIMessages.ImportPackageSection_selection);
				dialog.create();
				SWTUtil.setDialogSize(dialog, 400, 500);
			}
		};

		BusyIndicator.showWhile(Display.getCurrent(), runnable);
		if (dialog.open() == Window.OK) {
			Object[] selected = dialog.getResult();
			if (addRemote) {
				downloadBundlesForSelectedPackages(selected);
			}
			addSelectedPackagesToManifest(selected);
		}
	}

	private void downloadBundlesForSelectedPackages(Object[] selected) {
		Set<Artefact> bundleArtefacts = new HashSet<Artefact>();
		for (Object element : selected) {
			PackageExport currPackageExport = (PackageExport) element;
			bundleArtefacts.add(currPackageExport.getBundle());
		}

		IProject project = ((BundleManifestEditor) this.getPage().getEditor()).getCommonProject();
		RepositoryUtils.downloadArifacts(bundleArtefacts, project, Display.getDefault().getActiveShell(), true);
	}

	private void addSelectedPackagesToManifest(Object[] selected) {
		ImportPackageHeader importPackageHeader = (ImportPackageHeader) getBundle().getManifestHeader(
				Constants.IMPORT_PACKAGE);
		for (Object element : selected) {
			PackageExport currPackage = (PackageExport) element;
			if (null == importPackageHeader) {
				getBundle().setHeader(Constants.IMPORT_PACKAGE, "");
				importPackageHeader = (ImportPackageHeader) getBundle().getManifestHeader(Constants.IMPORT_PACKAGE);
			}

			String versionString = null;
			OsgiVersion osgiVers = currPackage.getVersion();
			if (osgiVers.getMajor() != 0 || osgiVers.getMinor() != 0 || osgiVers.getService() != 0
					|| (osgiVers.getQualifier() != null && !osgiVers.getQualifier().trim().equals(""))) {
				versionString = "[" + currPackage.getVersion().toString() + "," + currPackage.getVersion().toString()
						+ "]";
			}

			ImportPackageObject newPackageObject = new ImportPackageObject(importPackageHeader, currPackage.getName(),
					versionString, Constants.VERSION_ATTRIBUTE);
			importPackageHeader.addPackage(newPackageObject);
		}
	}

	@Override
	protected void handleRemove() {
		Object[] removed = ((IStructuredSelection) fViewer.getSelection()).toArray();
		ImportPackageHeader importPackageHeader = (ImportPackageHeader) getBundle().getManifestHeader(
				Constants.IMPORT_PACKAGE);
		for (Object element : removed) {
			importPackageHeader.removePackage((PackageObject) element);
		}
	}

	@Override
	protected void handleOpenProperties() {
		Object[] selected = ((IStructuredSelection) fViewer.getSelection()).toArray();
		ImportPackageObject first = (ImportPackageObject) selected[0];
		BundleDependencyPropertiesDialog dialog = new BundleDependencyPropertiesDialog(isEditable(), false, false,
				first.isOptional(), first.getVersion(), true, true);
		dialog.create();
		SWTUtil.setDialogSize(dialog, 400, -1);
		if (selected.length == 1) {
			dialog.setTitle(((ImportPackageObject) selected[0]).getName());
		}
		else {
			dialog.setTitle(PDEUIMessages.ExportPackageSection_props);
		}
		if (dialog.open() == Window.OK && isEditable()) {
			String newVersion = dialog.getVersion();
			boolean newOptional = dialog.isOptional();
			for (Object element : selected) {
				ImportPackageObject object = (ImportPackageObject) element;
				if (!newVersion.equals(object.getVersion())) {
					object.setVersion(newVersion);
				}
				if (!newOptional == object.isOptional()) {
					object.setOptional(newOptional);
				}
			}
		}
	}

	@Override
	protected void handleDoubleClick(IStructuredSelection selection) {
		handleGoToPackage(selection);
	}

	@Override
	protected void makeActions() {
		super.makeActions();
		fGoToAction = new Action(PDEUIMessages.ImportPackageSection_goToPackage) {
			@Override
			public void run() {
				handleGoToPackage(fViewer.getSelection());
			}
		};
	}

	private IPackageFragment getPackageFragment(ISelection sel) {
		if (sel instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) sel;
			if (selection.size() != 1) {
				return null;
			}

			IBaseModel model = getPage().getModel();
			if (!(model instanceof IPluginModelBase)) {
				return null;
			}

			return PDEJavaHelper.getPackageFragment(((PackageObject) selection.getFirstElement()).getName(),
					((IPluginModelBase) model).getPluginBase().getId(), getPage().getPDEEditor().getCommonProject());
		}
		return null;
	}

	private void handleGoToPackage(ISelection selection) {
		IPackageFragment frag = getPackageFragment(selection);
		if (frag != null) {
			try {
				IViewPart part = PDEPlugin.getActivePage().showView(JavaUI.ID_PACKAGES);
				ShowInPackageViewAction action = new ShowInPackageViewAction(part.getSite());
				action.run(frag);
			}
			catch (PartInitException e) {
			}
		}
	}

	@Override
	protected int getAddIndex() {
		return ADD_INDEX;
	}

	@Override
	protected int getRemoveIndex() {
		return REMOVE_INDEX;
	}

	@Override
	protected int getPropertiesIndex() {
		return PROPERTIES_INDEX;
	}

	class BundleImportPackageDialogLabelProvider extends LabelProvider {
		@Override
		public Image getImage(Object element) {
			return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_PACKAGE);
		}

		@Override
		public String getText(Object element) {
			PackageExport packageExport = (PackageExport) element;
			String label = packageExport.getName();
			if (null != packageExport.getVersion()) {
				label += " " + packageExport.getVersion();
			}
			return label;
		}
	}

	@Override
	protected IContentProvider getContentProvider() {
		return new ImportPackageContentProvider();
	}

	@Override
	protected String getHeaderConstant() {
		return Constants.IMPORT_PACKAGE;
	}

	class ImportItemWrapper {
		Object fUnderlying;

		public ImportItemWrapper(Object underlying) {
			fUnderlying = underlying;
		}

		@Override
		public String toString() {
			return getName();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof ImportItemWrapper) {
				ImportItemWrapper item = (ImportItemWrapper) obj;
				return getName().equals(item.getName());
			}
			return false;
		}

		public String getName() {
			if (fUnderlying instanceof ExportPackageDescription) {
				return ((ExportPackageDescription) fUnderlying).getName();
			}
			if (fUnderlying instanceof IPackageFragment) {
				return ((IPackageFragment) fUnderlying).getElementName();
			}
			if (fUnderlying instanceof ExportPackageObject) {
				return ((ExportPackageObject) fUnderlying).getName();
			}
			return null;
		}

		public Version getVersion() {
			if (fUnderlying instanceof ExportPackageDescription) {
				return ((ExportPackageDescription) fUnderlying).getVersion();
			}
			if (fUnderlying instanceof ExportPackageObject) {
				String version = ((ExportPackageObject) fUnderlying).getVersion();
				if (version != null) {
					return new Version(version);
				}
			}
			return null;
		}

		boolean hasVersion() {
			return hasEPD() && ((ExportPackageDescription) fUnderlying).getVersion() != null;
		}

		boolean hasEPD() {
			return fUnderlying instanceof ExportPackageDescription;
		}
	}

	@Override
	protected void buttonSelected(int index) {
		if (index == ADD_REMOTE_BUNDLE_INDEX) {
			internalHandleAdd(true);
		}
		else {
			super.buttonSelected(index);
		}
	}

	class ImportPackageContentProvider extends DefaultTableProvider {
		public Object[] getElements(Object parent) {
			ImportPackageHeader header = (ImportPackageHeader) getBundle().getManifestHeader(Constants.IMPORT_PACKAGE);
			if (header == null) {
				return new Object[0];
			}
			else {
				return header.getPackages();
			}
		}
	}

}
