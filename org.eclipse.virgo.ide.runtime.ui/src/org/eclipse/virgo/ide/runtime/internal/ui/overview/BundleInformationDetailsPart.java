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
package org.eclipse.virgo.ide.runtime.internal.ui.overview;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonImages;
import org.eclipse.pde.internal.ui.PDEPluginImages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.virgo.ide.management.remote.Bundle;
import org.eclipse.virgo.ide.management.remote.PackageExport;
import org.eclipse.virgo.ide.management.remote.PackageImport;
import org.eclipse.virgo.ide.management.remote.ServiceReference;
import org.eclipse.virgo.ide.runtime.internal.ui.ServerUiImages;
import org.eclipse.virgo.ide.runtime.internal.ui.overview.BundleInformationDetailsPart.ServicesContentProvider.ServicesHolder;
import org.eclipse.virgo.ide.ui.editors.BundleManifestEditor;
import org.osgi.framework.Constants;
import org.springframework.util.StringUtils;


/**
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public class BundleInformationDetailsPart extends AbstractFormPart implements IDetailsPage {

	class PackageExportContentProvider implements ITreeContentProvider {

		private Bundle bundle;

		public void dispose() {

		}

		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof PackageExport) {
				Set<Bundle> bundles = new HashSet<Bundle>();
				String name = ((PackageExport) parentElement).getName();
				String version = ((PackageExport) parentElement).getVersion();
				String id = bundle.getId();

				for (Bundle bundle : BundleInformationDetailsPart.this.bundles.values()) {
					for (PackageImport pi : bundle.getPackageImports()) {
						if (pi.getSupplierId().equals(id) && pi.getName().equals(name)
								&& pi.getVersion().equals(version)) {
							bundles.add(bundle);
						}
					}
				}
				return bundles.toArray(new Bundle[bundles.size()]);
			}
			return new Object[0];
		}

		public Object[] getElements(Object inputElement) {
			if (bundle.getPackageExports().size() > 0) {
				return bundle.getPackageExports().toArray();
			}
			return new Object[] { "<no exported packages>" };
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			bundle = (Bundle) newInput;
		}
	}

	class PackageImportContentProvider implements ITreeContentProvider {

		private Bundle bundle;

		public void dispose() {

		}

		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof PackageImport) {
				String supplierId = ((PackageImport) parentElement).getSupplierId();
				return new Object[] { bundles.get(Long.valueOf(supplierId)) };
			}
			return new Object[0];
		}

		public Object[] getElements(Object inputElement) {
			if (bundle.getPackageImports().size() > 0) {
				return bundle.getPackageImports().toArray();
			}
			return new Object[] { "<no imported packages>" };
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			bundle = (Bundle) newInput;
		}
	}

	class PackageLabelProvider extends LabelProvider {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
		 */
		@Override
		public Image getImage(Object element) {
			if (element instanceof PackageImport) {
				return PDEPluginImages.DESC_PACKAGE_OBJ.createImage();
			}
			else if (element instanceof PackageExport) {
				return PDEPluginImages.DESC_PACKAGE_OBJ.createImage();
			}
			else if (element instanceof Bundle) {
				return ServerUiImages.getImage(ServerUiImages.IMG_OBJ_BUNDLE);
			}
			return super.getImage(element);
		}

		@Override
		public String getText(Object element) {
			if (element instanceof PackageImport) {
				return ((PackageImport) element).getName() + " (" + ((PackageImport) element).getVersion() + ")";
			}
			else if (element instanceof PackageExport) {
				return ((PackageExport) element).getName() + " (" + ((PackageExport) element).getVersion() + ")";
			}
			else if (element instanceof Bundle) {
				return ((Bundle) element).getSymbolicName() + " (" + ((Bundle) element).getVersion() + ")";
			}
			return super.getText(element);
		}
	}

	class ServicePropertyComparator extends ViewerComparator {

		@SuppressWarnings("unchecked")
		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			Map.Entry<String, String> p1 = (Entry<String, String>) e1;
			Map.Entry<String, String> p2 = (Entry<String, String>) e2;
			return p1.getKey().compareTo(p2.getKey());
		}
	}

	class ServicePropertyContentProvider implements IStructuredContentProvider {

		private ServiceReference ref;

		public void dispose() {
		}

		public Object[] getElements(Object inputElement) {
			if (ref != null) {
				return ref.getProperties().entrySet().toArray();
			}
			return new Object[0];
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if (newInput instanceof ServiceReference) {
				ref = (ServiceReference) newInput;
			}
			else {
				ref = null;
			}
		}

	}

	class ServicePropertyLabelProvider extends LabelProvider implements ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@SuppressWarnings("unchecked")
		public String getColumnText(Object element, int columnIndex) {
			Map.Entry<String, String> entry = (Entry<String, String>) element;
			if (columnIndex == 0) {
				return entry.getKey();
			}
			else if (columnIndex == 1) {
				return entry.getValue();
			}
			return null;
		}
	}

	class ServicesContentProvider implements ITreeContentProvider {

		class ServicesHolder {

			private final String label;

			private final Set<ServiceReference> refs;

			public ServicesHolder(Set<ServiceReference> refs, String label) {
				this.refs = refs;
				this.label = label;
			}

			/**
			 * @return the label
			 */
			public String getLabel() {
				return label;
			}

			public Set<ServiceReference> getRefs() {
				return refs;
			}
		}

		private Bundle bundle;

		public void dispose() {

		}

		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof ServicesHolder) {
				return ((ServicesHolder) parentElement).getRefs().toArray();
			}
			else if (parentElement instanceof ServiceReference) {
				Set<Bundle> bs = new HashSet<Bundle>();
				ServiceReference ref = (ServiceReference) parentElement;
				if (ref.getType() == ServiceReference.Type.IN_USE) {
					bs.add(bundles.get(ref.getBundleId()));
				}
				else if (ref.getType() == ServiceReference.Type.REGISTERED) {
					for (Long id : ref.getUsingBundleIds()) {
						bs.add(bundles.get(id));
					}
				}
				return bs.toArray();
			}
			return new Object[0];
		}

		public Object[] getElements(Object inputElement) {
			Set<ServicesHolder> serviceHolder = new HashSet<ServicesHolder>();
			if (bundle.getRegisteredServices().size() > 0) {
				serviceHolder.add(new ServicesHolder(bundle.getRegisteredServices(), "Registered Services"));
			}
			if (bundle.getServicesInUse().size() > 0) {
				serviceHolder.add(new ServicesHolder(bundle.getServicesInUse(), "Services in Use"));
			}
			if (serviceHolder.size() > 0) {
				return serviceHolder.toArray();
			}
			return new Object[] { "<no registered or used services>" };
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			bundle = (Bundle) newInput;
		}
	}

	class ServicesLabelProvider extends LabelProvider {

		@Override
		public Image getImage(Object element) {
			if (element instanceof ServiceReference) {
				return PDEPluginImages.DESC_EXTENSION_OBJ.createImage();
			}
			else if (element instanceof ServicesHolder) {
				return PDEPluginImages.DESC_EXTENSIONS_OBJ.createImage();
			}
			else if (element instanceof Bundle) {
				return ServerUiImages.getImage(ServerUiImages.IMG_OBJ_BUNDLE);
			}
			return super.getImage(element);
		}

		@Override
		public String getText(Object element) {
			if (element instanceof ServiceReference) {
				String id = ((ServiceReference) element).getProperties().get(Constants.SERVICE_ID);
				if (StringUtils.hasText(id)) {
					return ((ServiceReference) element).getClazzes()[0] + " (" + id + ")";
				}
				return ((ServiceReference) element).getClazzes()[0];
			}
			else if (element instanceof Bundle) {
				return ((Bundle) element).getSymbolicName() + " (" + ((Bundle) element).getVersion() + ")";
			}
			else if (element instanceof ServicesHolder) {
				return ((ServicesHolder) element).getLabel();
			}
			return super.getText(element);
		}
	}

	private Bundle bundle;

	private Map<Long, Bundle> bundles;

	private Text bundleSymbolicNameText;

	private FilteredTree exportsTable;

	private TreeViewer exportsTableViewer;

	private Text idText;

	private FilteredTree importsTable;

	private TreeViewer importsTableViewer;

	private Text locationText;

	private Text manifestText;

	private final BundleInformationMasterDetailsBlock masterDetailsBlock;

	private Text providerText;

	private Table servicePropertiesTable;

	private TableViewer servicePropertiesTableViewer;

	private FilteredTree servicesTable;

	private TreeViewer servicesTableViewer;

	private Text stateText;

	private Text versionText;

	public BundleInformationDetailsPart(BundleInformationMasterDetailsBlock bundleInformationMasterDetailsBlock) {
		this.masterDetailsBlock = bundleInformationMasterDetailsBlock;
	}

	public void createContents(Composite parent) {
		FormToolkit toolkit = getManagedForm().getToolkit();
		GridLayout layout = new GridLayout();
		layout.marginTop = -5;
		layout.marginLeft = 6;
		parent.setLayout(layout);

		GridData data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
		Section detailsSection = toolkit.createSection(parent, ExpandableComposite.TWISTIE
				| ExpandableComposite.EXPANDED | ExpandableComposite.TITLE_BAR | Section.DESCRIPTION
				| ExpandableComposite.FOCUS_TITLE);
		detailsSection.setText("Bundle Details");
		detailsSection.setDescription("Details about the selected bundle.");
		detailsSection.setLayoutData(data);
		createSectionToolbar(detailsSection, toolkit, new Action("Show dependency graph for bundle",
				CommonImages.GROUPING) {
			@Override
			public void run() {
				masterDetailsBlock.openDependencyPage(bundle.getSymbolicName(), bundle.getVersion());
			}
		});

		Composite detailsComposite = toolkit.createComposite(detailsSection);
		layout = new GridLayout();
		layout.numColumns = 4;
		detailsComposite.setLayout(layout);

		data = new GridData(GridData.FILL_BOTH);
		data.minimumHeight = 100;
		data.heightHint = 100;
		detailsComposite.setLayoutData(data);

		toolkit.paintBordersFor(detailsComposite);
		detailsSection.setClient(detailsComposite);

		Label idLabel = toolkit.createLabel(detailsComposite, "Id:");
		idLabel.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		idText = toolkit.createText(detailsComposite, "");
		idText.setEditable(false);
		idText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING));

		Label stateLabel = toolkit.createLabel(detailsComposite, "State:");
		stateLabel.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		stateText = toolkit.createText(detailsComposite, "", SWT.NONE);
		stateText.setEditable(false);
		stateText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING));

		Label bundleSymbolicNameLabel = toolkit.createLabel(detailsComposite, "Symbolic-Name:");
		bundleSymbolicNameLabel.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		bundleSymbolicNameText = toolkit.createText(detailsComposite, "", SWT.NONE);
		bundleSymbolicNameText.setEditable(false);
		bundleSymbolicNameText
				.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING));
		GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(bundleSymbolicNameText);

		Label versionLabel = toolkit.createLabel(detailsComposite, "Version:");
		versionLabel.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		versionText = toolkit.createText(detailsComposite, "");
		versionText.setEditable(false);
		versionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING));

		Label providerLabel = toolkit.createLabel(detailsComposite, "Provider:");
		providerLabel.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		providerText = toolkit.createText(detailsComposite, "", SWT.NONE);
		providerText.setEditable(false);
		providerText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING));

		Label locationLabel = toolkit.createLabel(detailsComposite, "Location:");
		locationLabel.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		locationText = toolkit.createText(detailsComposite, "", SWT.NONE);
		locationText.setEditable(false);
		locationText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING));
		GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(locationText);

		Link openDependenciesText = new Link(detailsComposite, SWT.NONE);
		openDependenciesText.setText("<a href=\"open\">Show dependency graph for bundle</a>");
		openDependenciesText.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				masterDetailsBlock.openDependencyPage(bundle.getSymbolicName(), bundle.getVersion());
			}
		});
		GridDataFactory.fillDefaults().grab(true, false).span(4, 1).applyTo(openDependenciesText);

		Section manifestSection = toolkit.createSection(parent, ExpandableComposite.TWISTIE
				| ExpandableComposite.TITLE_BAR | Section.DESCRIPTION | ExpandableComposite.FOCUS_TITLE);
		manifestSection.setText("Manifest");
		manifestSection.setDescription("Displays the bundle manifest.");
		manifestSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING));

		Composite manifestComposite = toolkit.createComposite(manifestSection);
		layout = new GridLayout();
		layout.numColumns = 1;
		manifestComposite.setLayout(layout);
		manifestComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		toolkit.paintBordersFor(manifestComposite);
		manifestSection.setClient(manifestComposite);
		createSectionToolbar(manifestSection, toolkit, new Action("Open MANIFEST.MF in editor",
				PDEPluginImages.DESC_TOC_LEAFTOPIC_OBJ) {
			@Override
			public void run() {
				try {
					File file = new File(new URI(bundle.getLocation()));
					BundleManifestEditor.openExternalPlugin(file, "META-INF/MANIFEST.MF");
				}
				catch (URISyntaxException e) {
				}
			}
		});

		manifestText = toolkit.createText(manifestComposite, "", SWT.MULTI | SWT.WRAP);
		manifestText.setEditable(false);
		manifestText.setFont(JFaceResources.getTextFont());
		GC gc = new GC(manifestText);
		FontMetrics fm = gc.getFontMetrics();
		int height = 10 * fm.getHeight();
		gc.dispose();

		data = new GridData(GridData.FILL_HORIZONTAL);
		data.heightHint = manifestText.computeSize(SWT.DEFAULT, height).y;
		manifestText.setLayoutData(data);

		Link openManifestText = new Link(manifestComposite, SWT.NONE);
		openManifestText.setText("<a href=\"open\">Open MANIFEST.MF in editor</a>");
		openManifestText.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					File file = new File(new URI(bundle.getLocation()));
					BundleManifestEditor.openExternalPlugin(file, "META-INF/MANIFEST.MF");
				}
				catch (URISyntaxException e1) {
				}
			}
		});

		Section importsSection = toolkit.createSection(parent, ExpandableComposite.TWISTIE
				| ExpandableComposite.EXPANDED | ExpandableComposite.TITLE_BAR | Section.DESCRIPTION
				| ExpandableComposite.FOCUS_TITLE);
		importsSection.setText("Package Imports");
		importsSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING));
		importsSection.setDescription("Information about visible packages and consumers of those packages.");

		Composite importsComposite = toolkit.createComposite(importsSection);
		layout = new GridLayout();
		layout.numColumns = 1;
		importsComposite.setLayout(layout);
		importsComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		toolkit.paintBordersFor(importsComposite);
		importsSection.setClient(importsComposite);
		createSectionToolbar(importsSection, toolkit, new Action("Collapse All", CommonImages.COLLAPSE_ALL) {
			@Override
			public void run() {
				importsTableViewer.collapseAll();
			}
		});

		importsTable = new FilteredTree(importsComposite, SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER,
				new PatternFilter());
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.heightHint = 200;
		importsTable.getViewer().getControl().setLayoutData(data);

		importsTableViewer = importsTable.getViewer();
		importsTableViewer.setContentProvider(new PackageImportContentProvider());
		importsTableViewer.setLabelProvider(new PackageLabelProvider());
		importsTableViewer.setAutoExpandLevel(2);

		Section exportsSection = toolkit.createSection(parent, ExpandableComposite.TWISTIE
				| ExpandableComposite.TITLE_BAR | Section.DESCRIPTION | ExpandableComposite.FOCUS_TITLE);
		exportsSection.setText("Package Exports");
		exportsSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING));
		exportsSection.setDescription("Information about exported packages and bundles that use these packages.");

		Composite exportsComposite = toolkit.createComposite(exportsSection);
		layout = new GridLayout();
		layout.numColumns = 1;
		exportsComposite.setLayout(layout);
		exportsComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		toolkit.paintBordersFor(exportsComposite);
		exportsSection.setClient(exportsComposite);
		createSectionToolbar(exportsSection, toolkit, new Action("Collapse All", CommonImages.COLLAPSE_ALL) {
			@Override
			public void run() {
				exportsTableViewer.collapseAll();
			}
		});

		exportsTable = new FilteredTree(exportsComposite, SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER,
				new PatternFilter());
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.heightHint = 200;
		exportsTable.getViewer().getControl().setLayoutData(data);

		exportsTableViewer = exportsTable.getViewer();
		exportsTableViewer.setContentProvider(new PackageExportContentProvider());
		exportsTableViewer.setLabelProvider(new PackageLabelProvider());
		exportsTableViewer.setAutoExpandLevel(2);

		Section servicesSection = toolkit.createSection(parent, ExpandableComposite.TWISTIE
				| ExpandableComposite.TITLE_BAR | Section.DESCRIPTION | ExpandableComposite.FOCUS_TITLE);
		servicesSection.setText("Services");
		servicesSection.setDescription("Details about registered and in-use services.");
		servicesSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING));

		Composite servicesComposite = toolkit.createComposite(servicesSection);
		layout = new GridLayout();
		layout.numColumns = 1;
		servicesComposite.setLayout(layout);
		servicesComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		toolkit.paintBordersFor(servicesComposite);
		servicesSection.setClient(servicesComposite);
		createSectionToolbar(servicesSection, toolkit, new Action("Collapse All", CommonImages.COLLAPSE_ALL) {
			@Override
			public void run() {
				servicesTableViewer.collapseAll();
			}
		});

		servicesTable = new FilteredTree(servicesComposite, SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER,
				new PatternFilter());
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.heightHint = 200;
		servicesTable.getViewer().getControl().setLayoutData(data);

		servicesTableViewer = servicesTable.getViewer();
		servicesTableViewer.setContentProvider(new ServicesContentProvider());
		servicesTableViewer.setLabelProvider(new ServicesLabelProvider());
		servicesTableViewer.setAutoExpandLevel(2);

		servicesTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				servicePropertiesTableViewer.setInput(((IStructuredSelection) event.getSelection()).getFirstElement());
			}
		});

		Label servicePropertiesLabel = toolkit.createLabel(servicesComposite, "Service Properties:");
		servicePropertiesLabel.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));

		servicePropertiesTable = toolkit.createTable(servicesComposite, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.heightHint = 100;
		servicePropertiesTable.setLayoutData(data);
		servicePropertiesTable.setLinesVisible(true);
		TableColumn keyColumn = new TableColumn(servicePropertiesTable, SWT.LEFT);
		keyColumn.setText("Key");
		keyColumn.setWidth(150);
		TableColumn valueColumn = new TableColumn(servicePropertiesTable, SWT.LEFT);
		valueColumn.setText("Value");
		valueColumn.setWidth(450);
		servicePropertiesTable.setHeaderVisible(true);

		servicePropertiesTableViewer = new TableViewer(servicePropertiesTable);
		servicePropertiesTableViewer.setContentProvider(new ServicePropertyContentProvider());
		servicePropertiesTableViewer.setLabelProvider(new ServicePropertyLabelProvider());
		servicePropertiesTableViewer.setComparator(new ServicePropertyComparator());

	}

	public void refresh(Map<Long, Bundle> bundles) {
		this.bundles = bundles;
	}

	public void selectionChanged(IFormPart part, ISelection selection) {
		bundle = (Bundle) ((IStructuredSelection) selection).getFirstElement();

		idText.setText(bundle.getId());
		stateText.setText(bundle.getState());
		bundleSymbolicNameText.setText(bundle.getSymbolicName());
		versionText.setText(bundle.getVersion());
		String vendor = bundle.getHeaders().get("Bundle-Vendor");
		if (vendor != null) {
			providerText.setText(vendor);
		}
		locationText.setText(bundle.getLocation());

		importsTableViewer.setInput(bundle);
		importsTableViewer.setAutoExpandLevel(2);

		exportsTableViewer.setInput(bundle);
		exportsTableViewer.setAutoExpandLevel(2);

		StringBuilder builder = new StringBuilder();
		for (Map.Entry<String, String> header : bundle.getHeaders().entrySet()) {
			builder.append(header.getKey()).append(": ").append(header.getValue()).append(Text.DELIMITER);
		}
		manifestText.setText(builder.toString());

		servicesTableViewer.setInput(bundle);
		servicesTableViewer.setAutoExpandLevel(2);

	}

	private void createSectionToolbar(Section section, FormToolkit toolkit, Action... actions) {
		ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
		ToolBar toolbar = toolBarManager.createControl(section);
		final Cursor handCursor = new Cursor(Display.getCurrent(), SWT.CURSOR_HAND);
		toolbar.setCursor(handCursor);
		// Cursor needs to be explicitly disposed
		toolbar.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if ((handCursor != null) && (handCursor.isDisposed() == false)) {
					handCursor.dispose();
				}
			}
		});

		for (Action action : actions) {
			toolBarManager.add(action);
		}

		toolBarManager.update(true);

		section.setTextClient(toolbar);
	}
}
