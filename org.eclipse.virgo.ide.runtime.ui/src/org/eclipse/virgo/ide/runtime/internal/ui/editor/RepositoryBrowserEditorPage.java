/*******************************************************************************
 * Copyright (c) 2009, 2010 SpringSource, a divison of VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource, a division of VMware, Inc. - initial API and implementation
 *******************************************************************************/

package org.eclipse.virgo.ide.runtime.internal.ui.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.progress.IProgressConstants;
import org.eclipse.virgo.ide.runtime.core.IServerWorkingCopy;
import org.eclipse.virgo.ide.runtime.core.ServerCorePlugin;
import org.eclipse.virgo.ide.runtime.core.artefacts.Artefact;
import org.eclipse.virgo.ide.runtime.core.artefacts.ArtefactRepository;
import org.eclipse.virgo.ide.runtime.core.artefacts.ArtefactRepositoryManager;
import org.eclipse.virgo.ide.runtime.core.artefacts.BundleArtefact;
import org.eclipse.virgo.ide.runtime.core.artefacts.IArtefact;
import org.eclipse.virgo.ide.runtime.core.artefacts.IArtefactTyped;
import org.eclipse.virgo.ide.runtime.core.artefacts.LibraryArtefact;
import org.eclipse.virgo.ide.runtime.core.artefacts.LocalBundleArtefact;
import org.eclipse.virgo.ide.runtime.core.provisioning.IBundleRepositoryChangeListener;
import org.eclipse.virgo.ide.runtime.core.provisioning.RepositoryProvisioningJob;
import org.eclipse.virgo.ide.runtime.core.provisioning.RepositorySourceProvisiongJob;
import org.eclipse.virgo.ide.runtime.core.provisioning.RepositoryUtils;
import org.eclipse.virgo.ide.runtime.internal.ui.ServerUiImages;
import org.eclipse.virgo.ide.runtime.internal.ui.ServerUiPlugin;
import org.eclipse.virgo.ide.runtime.internal.ui.providers.RepositorySearchResultContentProvider;
import org.eclipse.virgo.ide.runtime.internal.ui.providers.RuntimeLabelProvider;
import org.eclipse.virgo.ide.runtime.internal.ui.repository.RefreshBundleJob;
import org.eclipse.virgo.ide.runtime.internal.ui.sorters.RepositoryViewerSorter;
import org.eclipse.virgo.ide.ui.editors.BundleManifestEditor;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.ui.editor.ServerEditorPart;

/**
 * {@link ServerEditorPart} that allows to browse the local and remote bundle repository.
 *
 * @author Christian Dupuis
 * @author Miles Parker
 * @since 1.0.0
 */
public class RepositoryBrowserEditorPage extends ServerEditorPart implements ISelectionChangedListener {

    private static final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());

    private Button anaylseButton;

    private Button downloadButton;

    private Button downloadSourcesCheckbox;

    private final IJobChangeListener jobListener = new ArtefactOperationJobListener();

    private IServerWorkingCopy serverWC;

    private Button refreshButton;

    private PropertyChangeListener propertyListener;

    private final RepositorySearchResultContentProvider searchResultContentProvider = new RepositorySearchResultContentProvider();

    private final ColoredRespositoryLabelProvider coloredRespositoryLabelProvider = new ColoredRespositoryLabelProvider();

    // private Tree repositoryTable;

    private CommonViewer repositoryTableViewer;

    private Button searchButton;

    private Button downloadSourcesButton;

    private Tree searchResultTable;

    private CheckboxTreeViewer searchResultTableViewer;

    private Text searchText;

    private Shell shell;

    private Button licenseButton;

    private Button openManifestButton;

    private Link update;

    private IBundleRepositoryChangeListener repositoryListener;

    private static String PROXY_PREF_PAGE_ID = Messages.RepositoryBrowserEditorPage_0;

    @Override
    public void createPartControl(Composite parent) {

        this.shell = parent.getShell();

        FormToolkit toolkit = getFormToolkit(parent.getDisplay());

        ScrolledForm form = toolkit.createScrolledForm(parent);
        toolkit.decorateFormHeading(form.getForm());
        form.setText(Messages.RepositoryBrowserEditorPage_BundleBrowserLabel);
        form.setImage(getFormImage());
        GridLayout layout = new GridLayout(2, true);
        layout.marginTop = 6;
        layout.marginLeft = 6;
        form.getBody().setLayout(layout);

        Section leftSection = createLeftSection(toolkit, form);
        createRightSection(toolkit, form);

        form.setContent(leftSection);
        // This reflow breaks the TableWrapLayout around the repository
        // hyperlink
        // form.reflow(true);

        initialize();
    }

    protected Image getFormImage() {
        return ServerUiImages.getImage(ServerUiImages.IMG_OBJ_VIRGO);
    }

    @Override
    public void dispose() {
        super.dispose();

        if (this.serverWC != null) {
            this.serverWC.removeConfigurationChangeListener(this.propertyListener);
        }
        Job.getJobManager().removeJobChangeListener(this.jobListener);
        ServerCorePlugin.getArtefactRepositoryManager().removeBundleRepositoryChangeListener(this.repositoryListener);
    }

    @Override
    public void init(IEditorSite site, IEditorInput input) {
        super.init(site, input);

        IServerWorkingCopy ts = (IServerWorkingCopy) this.server.loadAdapter(IServerWorkingCopy.class, null);
        this.serverWC = ts;
        addListeners();
        initialize();
    }

    @Override
    public void setFocus() {
        if (this.searchResultTable != null) {
            this.searchResultTable.setFocus();
        }
    }

    private Section createLeftSection(FormToolkit toolkit, ScrolledForm form) {
        GridLayout layout;
        Section leftSection = toolkit.createSection(form.getBody(), ExpandableComposite.TITLE_BAR | Section.DESCRIPTION);
        leftSection.setText(Messages.RepositoryBrowserEditorPage_SearchBundlesAndLibraries);
        leftSection.setDescription(Messages.RepositoryBrowserEditorPage_SearchBundlesAndLibrariesByName);
        leftSection.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite leftComposite = toolkit.createComposite(leftSection);
        layout = new GridLayout();
        layout.numColumns = 2;
        leftComposite.setLayout(layout);
        leftComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        toolkit.paintBordersFor(leftComposite);
        leftSection.setClient(leftComposite);

        this.searchText = toolkit.createText(leftComposite, Messages.RepositoryBrowserEditorPage_4);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        this.searchText.setLayoutData(data);
        this.searchText.addKeyListener(new KeyListener() {

            public void keyPressed(KeyEvent e) {
                if (e.character == SWT.CR || e.character == SWT.LF) {
                    handleSearch();
                }
            }

            public void keyReleased(KeyEvent e) {
            }
        });

        Composite searchButtonComposite = new Composite(leftComposite, SWT.NONE);
        searchButtonComposite.setLayout(new GridLayout(1, true));
        data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
        searchButtonComposite.setLayoutData(data);

        this.searchButton = toolkit.createButton(searchButtonComposite, Messages.RepositoryBrowserEditorPage_Search, SWT.PUSH);
        this.searchButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                handleSearch();
            }
        });
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = 100;
        this.searchButton.setLayoutData(data);

        this.searchResultTable = toolkit.createTree(leftComposite, SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION | SWT.CHECK);

        this.searchResultTableViewer = new CheckboxTreeViewer(this.searchResultTable);
        this.searchResultTableViewer.setContentProvider(this.searchResultContentProvider);
        this.searchResultTableViewer.setLabelProvider(this.coloredRespositoryLabelProvider);
        this.searchResultTableViewer.setInput(this); // activate content provider
        this.searchResultTableViewer.setSorter(new RepositoryViewerSorter());
        this.searchResultTableViewer.addCheckStateListener(new ICheckStateListener() {

            public void checkStateChanged(CheckStateChangedEvent event) {
                handleCheckStateChange(event);
                RepositoryBrowserEditorPage.this.downloadButton.setEnabled(
                    RepositoryBrowserEditorPage.this.searchResultTableViewer.getCheckedElements().length > 0);
            }
        });
        this.searchResultTableViewer.addTreeListener(new ITreeViewerListener() {

            public void treeCollapsed(TreeExpansionEvent event) {
            }

            public void treeExpanded(TreeExpansionEvent event) {
                final Object element = event.getElement();
                if (RepositoryBrowserEditorPage.this.searchResultTableViewer.getGrayed(element) == false) {
                    BusyIndicator.showWhile(RepositoryBrowserEditorPage.this.shell.getDisplay(), new Runnable() {

                        public void run() {
                            setSubtreeChecked(element, RepositoryBrowserEditorPage.this.searchResultTableViewer.getChecked(element), false);
                        }
                    });
                }
            }
        });
        registerContextMenu(this.searchResultTableViewer);

        data = new GridData(GridData.FILL_BOTH);
        data.heightHint = 120;
        this.searchResultTable.setLayoutData(data);
        this.searchResultTable.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                ISelection selection = RepositoryBrowserEditorPage.this.searchResultTableViewer.getSelection();
                if (selection instanceof IStructuredSelection) {
                    Object element = ((IStructuredSelection) selection).getFirstElement();
                    if (element instanceof IArtefact) {
                        RepositoryBrowserEditorPage.this.anaylseButton.setEnabled(true);
                        RepositoryBrowserEditorPage.this.licenseButton.setEnabled(true);
                    } else {
                        RepositoryBrowserEditorPage.this.anaylseButton.setEnabled(false);
                        RepositoryBrowserEditorPage.this.licenseButton.setEnabled(false);
                    }
                }
            }
        });

        Composite buttonComposite = new Composite(leftComposite, SWT.NONE);
        buttonComposite.setLayout(new GridLayout(1, true));
        data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
        buttonComposite.setLayoutData(data);

        data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = 100;
        Button selectAllButton = toolkit.createButton(buttonComposite, Messages.RepositoryBrowserEditorPage_SelectAll, SWT.PUSH);
        selectAllButton.setLayoutData(data);
        selectAllButton.setToolTipText(Messages.RepositoryBrowserEditorPage_SelectAllBundlesAndLibraries);
        selectAllButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                RepositoryBrowserEditorPage.this.searchResultTableViewer.setCheckedElements(
                    RepositoryBrowserEditorPage.this.searchResultContentProvider.getElements(
                        RepositoryBrowserEditorPage.this.searchResultTableViewer.getInput()));
            }
        });

        Button deselectAllButton = toolkit.createButton(buttonComposite, Messages.RepositoryBrowserEditorPage_DeselectAllBundlesAndLibraries,
            SWT.PUSH);
        deselectAllButton.setToolTipText(Messages.RepositoryBrowserEditorPage_9);
        deselectAllButton.setLayoutData(data);
        deselectAllButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                RepositoryBrowserEditorPage.this.searchResultTableViewer.setCheckedElements(new Object[0]);
            }
        });

        // insert vertical space to make the download button stand out
        toolkit.createLabel(buttonComposite, Messages.RepositoryBrowserEditorPage_10);

        this.anaylseButton = toolkit.createButton(buttonComposite, Messages.RepositoryBrowserEditorPage_Analyse, SWT.PUSH);
        this.anaylseButton.setEnabled(false);
        this.anaylseButton.setLayoutData(data);
        this.anaylseButton.setToolTipText(Messages.RepositoryBrowserEditorPage_AnalyseSelected);
        this.anaylseButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                ISelection selection = RepositoryBrowserEditorPage.this.searchResultTableViewer.getSelection();
                if (selection instanceof IStructuredSelection) {
                    Object element = ((IStructuredSelection) selection).getFirstElement();
                    if (element instanceof IArtefact) {
                        WebUiUtils.openUrl(RepositoryUtils.getRepositoryUrl((IArtefact) element));
                    }
                }
            }
        });

        this.licenseButton = toolkit.createButton(buttonComposite, Messages.RepositoryBrowserEditorPage_ViewLicense, SWT.PUSH);
        this.licenseButton.setEnabled(false);
        this.licenseButton.setLayoutData(data);
        this.licenseButton.setToolTipText(Messages.RepositoryBrowserEditorPage_OpenLicense);
        this.licenseButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                ISelection selection = RepositoryBrowserEditorPage.this.searchResultTableViewer.getSelection();
                if (selection instanceof IStructuredSelection) {
                    Object element = ((IStructuredSelection) selection).getFirstElement();
                    if (element instanceof LibraryArtefact) {
                        WebUiUtils.openUrl(RepositoryUtils.getResourceUrl((LibraryArtefact) element, RepositoryUtils.DOWNLOAD_TYPE_LICENSE));
                    } else if (element instanceof BundleArtefact) {
                        WebUiUtils.openUrl(RepositoryUtils.getResourceUrl((BundleArtefact) element, RepositoryUtils.DOWNLOAD_TYPE_LICENSE));
                    }
                }
            }
        });

        // insert vertical space to make the download button stand out
        toolkit.createLabel(buttonComposite, ""); //$NON-NLS-1$

        this.downloadButton = toolkit.createButton(buttonComposite, Messages.RepositoryBrowserEditorPage_Download, SWT.PUSH);
        this.downloadButton.setEnabled(false);
        this.downloadButton.setLayoutData(data);
        this.downloadButton.setToolTipText(Messages.RepositoryBrowserEditorPage_DownloadSelected);
        this.downloadButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                Set<Artefact> artifacts = new LinkedHashSet<Artefact>();
                Object[] selections = RepositoryBrowserEditorPage.this.searchResultTableViewer.getCheckedElements();
                for (Object selection : selections) {
                    if (selection instanceof IArtefact) {
                        artifacts.add((Artefact) selection);
                    }
                }

                boolean showDialog = ServerUiPlugin.getDefault().getPreferenceStore().getBoolean(ServerUiPlugin.PLUGIN_ID + ".download.message"); //$NON-NLS-1$

                if (!showDialog) {
                    MessageDialogWithToggle dialog = MessageDialogWithToggle.openOkCancelConfirm(RepositoryBrowserEditorPage.this.shell,
                        Messages.RepositoryBrowserEditorPage_DownloadBundlesAndLibraries, Messages.RepositoryBrowserEditorPage_DownloadTriggerMessage,
                        Messages.RepositoryBrowserEditorPage_DontShowDialog, false, ServerUiPlugin.getDefault().getPreferenceStore(),
                        ServerUiPlugin.PLUGIN_ID + ".download.message"); //$NON-NLS-1$
                    if (dialog.getReturnCode() != Window.OK) {
                        return;
                    } else {
                        ServerUiPlugin.getDefault().getPreferenceStore().setValue(ServerUiPlugin.PLUGIN_ID + ".download.message", //$NON-NLS-1$
                            new Boolean(dialog.getToggleState()));
                    }
                }

                Set<IRuntime> runtimes = new HashSet<IRuntime>();
                runtimes.add(getServer().getRuntime());
                RepositoryProvisioningJob operation = new RepositoryProvisioningJob(runtimes, RepositoryUtils.resolveDependencies(artifacts, false),
                    RepositoryBrowserEditorPage.this.downloadSourcesCheckbox.getSelection());
                operation.setProperty(IProgressConstants.ICON_PROPERTY, ServerUiImages.DESC_OBJ_BUNDLE);
                operation.schedule();

                // reset checked state
                RepositoryBrowserEditorPage.this.searchResultTableViewer.setCheckedElements(new Object[0]);
                RepositoryBrowserEditorPage.this.downloadButton.setEnabled(false);
            }
        });

        TableWrapLayout twLayout = new TableWrapLayout();
        twLayout.bottomMargin = 0;
        twLayout.topMargin = 0;
        twLayout.leftMargin = 0;
        twLayout.rightMargin = 0;

        Composite wrappedComposite = toolkit.createComposite(leftComposite);
        wrappedComposite.setLayout(twLayout);
        GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(wrappedComposite);

        this.downloadSourcesCheckbox = toolkit.createButton(wrappedComposite, Messages.RepositoryBrowserEditorPage_DownloadSourceJars,
            SWT.CHECK | SWT.WRAP);
        this.downloadSourcesCheckbox.setSelection(true);
        this.downloadSourcesCheckbox.setLayoutData(new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP));

        Link repoLink = new Link(wrappedComposite, SWT.WRAP);
        repoLink.setText(Messages.RepositoryBrowserEditorPage_SourceReposMessage);
        repoLink.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                WebUiUtils.openUrl(Messages.RepositoryBrowserEditorPage_SourceRepos);
            }
        });
        repoLink.setLayoutData(new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP));

        this.update = new Link(wrappedComposite, SWT.WRAP);
        this.update.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (MessageDialog.openQuestion(RepositoryBrowserEditorPage.this.shell, Messages.RepositoryBrowserEditorPage_UpdateLocalBundles,
                    Messages.RepositoryBrowserEditorPage_ConfirmIndexMessage)) {
                    ServerCorePlugin.getArtefactRepositoryManager().update();
                }

            }
        });
        setRepositoryDateString();
        this.update.setLayoutData(new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP));

        Hyperlink disclaimer = toolkit.createHyperlink(wrappedComposite, Messages.RepositoryBrowserEditorPage_FirewallConfigureMessage, SWT.WRAP);
        disclaimer.addHyperlinkListener(new HyperlinkAdapter() {

            @Override
            public void linkActivated(HyperlinkEvent e) {
                PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(null, PROXY_PREF_PAGE_ID, null, null);
                dialog.open();
            }
        });
        disclaimer.setLayoutData(new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP));

        return leftSection;
    }

    protected void registerContextMenu(StructuredViewer viewer) {
        MenuManager searchResultManager = new MenuManager();
        Menu searchResultPopup = searchResultManager.createContextMenu(viewer.getControl());
        viewer.getControl().setMenu(searchResultPopup);
        getSite().registerContextMenu(searchResultManager, viewer);
    }

    private void setRepositoryDateString() {
        Date date = ServerCorePlugin.getArtefactRepositoryManager().getArtefactRepositoryDate();
        String dateString = dateFormat.format(date);
        this.update.setText(Messages.RepositoryBrowserEditorPage_UpdateURL + dateString + ")"); //$NON-NLS-1$
    }

    protected String getServerName() {
        return Messages.RepositoryBrowserEditorPage_ServerName;
    }

    private Section createRightSection(FormToolkit toolkit, ScrolledForm form) {
        GridLayout layout;
        Section rightSection = toolkit.createSection(form.getBody(), ExpandableComposite.TITLE_BAR | Section.DESCRIPTION);
        rightSection.setText(Messages.RepositoryBrowserEditorPage_BundlesAndLibraries);
        rightSection.setDescription(Messages.RepositoryBrowserEditorPage_BundlesAndLibrariesMessage + getServerName() + ". " //$NON-NLS-1$
            + Messages.RepositoryBrowserEditorPage_BundlesAndLibrariesProviso);
        rightSection.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite leftComposite = toolkit.createComposite(rightSection);
        layout = new GridLayout();
        layout.numColumns = 1;
        leftComposite.setLayout(layout);
        leftComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        toolkit.paintBordersFor(leftComposite);
        rightSection.setClient(leftComposite);

        GridData data = new GridData(GridData.FILL_HORIZONTAL);

        Composite composite2 = toolkit.createComposite(leftComposite);
        layout = new GridLayout();
        layout.numColumns = 2;
        layout.marginHeight = 5;
        composite2.setLayout(layout);
        composite2.setLayoutData(new GridData(GridData.FILL_BOTH));
        toolkit.paintBordersFor(composite2);

        this.repositoryTableViewer = new CommonViewer(ServerUiPlugin.ARTEFACTS_BROWSER_VIEW_ID, composite2, SWT.BORDER | SWT.SINGLE);
        this.repositoryTableViewer.setSorter(new RepositoryViewerSorter());

        registerContextMenu(this.repositoryTableViewer);

        data = new GridData(GridData.FILL_BOTH);
        data.heightHint = 120;
        this.repositoryTableViewer.getControl().setLayoutData(data);

        Composite buttonComposite = new Composite(composite2, SWT.NONE);
        buttonComposite.setLayout(new GridLayout(1, true));
        data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
        buttonComposite.setLayoutData(data);

        data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = 100;
        this.refreshButton = toolkit.createButton(buttonComposite, Messages.RepositoryBrowserEditorPage_Refresh, SWT.PUSH);
        this.refreshButton.setLayoutData(data);
        this.refreshButton.setToolTipText(Messages.RepositoryBrowserEditorPage_RefreshMessage);
        this.refreshButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                refreshBundleRepository();
            }
        });

        this.downloadSourcesButton = toolkit.createButton(buttonComposite, Messages.RepositoryBrowserEditorPage_InstallSources, SWT.PUSH);
        this.downloadSourcesButton.setLayoutData(data);
        this.downloadSourcesButton.setToolTipText(Messages.RepositoryBrowserEditorPage_InstallSourcesMessage);
        this.downloadSourcesButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                downloadSources();
            }
        });

        // insert vertical space to make the download button stand out
        toolkit.createLabel(buttonComposite, Messages.RepositoryBrowserEditorPage_40);

        this.openManifestButton = toolkit.createButton(buttonComposite, Messages.RepositoryBrowserEditorPage_OpenManifest, SWT.PUSH);
        this.openManifestButton.setLayoutData(data);
        this.openManifestButton.setToolTipText(Messages.RepositoryBrowserEditorPage_OpenManifestMessage);
        this.openManifestButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                ISelection selection = RepositoryBrowserEditorPage.this.repositoryTableViewer.getSelection();
                if (selection instanceof IStructuredSelection) {
                    Object element = ((IStructuredSelection) selection).getFirstElement();
                    if (element instanceof IPackageFragmentRoot) {
                        IPackageFragmentRoot fragment = (IPackageFragmentRoot) element;
                        BundleManifestEditor.openExternalPlugin(fragment.getPath().toFile(), "META-INF/MANIFEST.MF"); //$NON-NLS-1$
                    }
                }
            }
        });

        this.openManifestButton.setEnabled(false);
        this.repositoryTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                ISelection selection = event.getSelection();
                if (selection instanceof IStructuredSelection) {
                    Object element = ((IStructuredSelection) selection).getFirstElement();
                    if (element instanceof IPackageFragmentRoot) {
                        RepositoryBrowserEditorPage.this.openManifestButton.setEnabled(true);
                    } else {
                        RepositoryBrowserEditorPage.this.openManifestButton.setEnabled(false);
                    }
                }
            }
        });

        TableWrapLayout twLayout = new TableWrapLayout();
        twLayout.bottomMargin = 0;
        twLayout.topMargin = 0;
        twLayout.leftMargin = 0;
        twLayout.rightMargin = 0;

        Composite wrappedComposite = toolkit.createComposite(leftComposite);
        wrappedComposite.setLayout(twLayout);
        GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(wrappedComposite);

        Link repoLink = new Link(wrappedComposite, SWT.WRAP);
        repoLink.setText(Messages.RepositoryBrowserEditorPage_NewBundlesMessage);
        repoLink.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                refreshBundleRepository();
            }

        });
        repoLink.setLayoutData(new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP));

        return rightSection;
    }

    private void refreshBundleRepository() {
        RefreshBundleJob.execute(this.shell, getServer().getRuntime());
    }

    protected void downloadSources() {
        Set<Artefact> artifacts = new LinkedHashSet<Artefact>();
        ArtefactRepository repository = RepositoryUtils.getRepositoryContents(this.server.getRuntime());
        for (IArtefactTyped bundle : repository.getBundles()) {
            if (bundle instanceof LocalBundleArtefact) {
                if (!((LocalBundleArtefact) bundle).isSourceDownloaded()) {
                    artifacts.add((LocalBundleArtefact) bundle);
                }
            }
        }
        Set<IRuntime> runtimes = new HashSet<IRuntime>();
        runtimes.add(getServer().getRuntime());
        RepositorySourceProvisiongJob operation = new RepositorySourceProvisiongJob(runtimes, artifacts);
        operation.setProperty(IProgressConstants.ICON_PROPERTY, ServerUiImages.DESC_OBJ_BUNDLE);
        operation.schedule();
    }

    private void handleCheckStateChange(final CheckStateChangedEvent event) {
        BusyIndicator.showWhile(this.shell.getDisplay(), new Runnable() {

            public void run() {
                boolean state = event.getChecked();
                setSubtreeChecked(event.getElement(), state, true);
                updateParentState(event.getElement());
            }
        });
    }

    private void setSubtreeChecked(Object container, boolean state, boolean checkExpandedState) {
        // checked state is set lazily on expand, don't set it if container is
        // collapsed

        Object[] members = this.searchResultContentProvider.getChildren(container);
        for (int i = members.length - 1; i >= 0; i--) {
            Object element = members[i];
            boolean elementGrayChecked = this.searchResultTableViewer.getGrayed(element) || this.searchResultTableViewer.getChecked(element);
            if (state) {
                this.searchResultTableViewer.setChecked(element, true);
                this.searchResultTableViewer.setGrayed(element, false);
            } else {
                this.searchResultTableViewer.setGrayChecked(element, false);
            } // unchecked state only
              // needs
            if (state || elementGrayChecked) {
                setSubtreeChecked(element, state, true);
            }
        }
    }

    private void updateParentState(Object child) {
        if (child == null || this.searchResultContentProvider.getParent(child) == null) {
            return;
        }
        Object parent = this.searchResultContentProvider.getParent(child);
        boolean childChecked = false;
        Object[] members = this.searchResultContentProvider.getChildren(parent);
        for (int i = members.length - 1; i >= 0; i--) {
            if (this.searchResultTableViewer.getChecked(members[i]) || this.searchResultTableViewer.getGrayed(members[i])) {
                childChecked = true;
                break;
            }
        }
        this.searchResultTableViewer.setGrayChecked(parent, childChecked);
        updateParentState(parent);
    }

    protected void addListeners() {
        this.propertyListener = new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent event) {
            }
        };

        this.serverWC.addConfigurationChangeListener(this.propertyListener);
        Job.getJobManager().addJobChangeListener(this.jobListener);

        this.repositoryListener = new IBundleRepositoryChangeListener() {

            public void bundleRepositoryChanged(IRuntime runtime) {
                refreshViewers();
            }
        };
        ServerCorePlugin.getArtefactRepositoryManager().addBundleRepositoryChangeListener(this.repositoryListener);

    }

    protected void handleSearch() {
        String search = this.searchText.getText();
        this.searchResultTableViewer.setInput(RepositoryUtils.searchForArtifacts(search));
        this.searchResultTableViewer.expandToLevel(2);
    }

    protected void initialize() {
        if (this.searchResultTable == null) {
            return;
        }
        this.searchResultTable.removeAll();
        setErrorMessage(null);
        initializeViewers();
    }

    protected void initializeViewers() {
        this.shell.getDisplay().asyncExec(new Runnable() {

            public void run() {
                if (RepositoryBrowserEditorPage.this.repositoryTableViewer.getControl() != null
                    && !RepositoryBrowserEditorPage.this.repositoryTableViewer.getControl().isDisposed()) {
                    RepositoryBrowserEditorPage.this.repositoryTableViewer.setInput(getServer());
                    RepositoryBrowserEditorPage.this.repositoryTableViewer.expandToLevel(2);
                }
                if (RepositoryBrowserEditorPage.this.searchResultTableViewer.getControl() != null
                    && !RepositoryBrowserEditorPage.this.searchResultTableViewer.getControl().isDisposed()) {
                    RepositoryBrowserEditorPage.this.searchResultTableViewer.refresh(true);
                    RepositoryBrowserEditorPage.this.searchResultTableViewer.expandToLevel(2);
                }
            }
        });
    }

    protected void refreshViewers() {
        this.shell.getDisplay().asyncExec(new Runnable() {

            public void run() {
                if (RepositoryBrowserEditorPage.this.repositoryTableViewer.getControl() != null
                    && !RepositoryBrowserEditorPage.this.repositoryTableViewer.getControl().isDisposed()) {
                    ISelection selection = RepositoryBrowserEditorPage.this.repositoryTableViewer.getSelection();
                    RepositoryBrowserEditorPage.this.repositoryTableViewer.refresh();
                    RepositoryBrowserEditorPage.this.repositoryTableViewer.setSelection(selection);
                    RepositoryBrowserEditorPage.this.searchResultTableViewer.refresh(true);
                }
            }
        });
    }

    private final class ArtefactOperationJobListener extends JobChangeAdapter {

        @Override
        public void done(IJobChangeEvent event) {
            if (event.getJob() instanceof RepositoryProvisioningJob) {
                if (((RepositoryProvisioningJob) event.getJob()).getRuntimes().contains(getServer().getRuntime())) {
                    refreshViewers();
                }
            } else if (event.getJob() instanceof ArtefactRepositoryManager.ArtefactRepositoryUpdateJob) {
                RepositoryBrowserEditorPage.this.shell.getDisplay().asyncExec(new Runnable() {

                    public void run() {
                        if (RepositoryBrowserEditorPage.this.update != null && !RepositoryBrowserEditorPage.this.update.isDisposed()) {
                            setRepositoryDateString();
                        }
                    }
                });
            }
        }
    }

    @Override
    public Object getAdapter(Class adapter) {
        if (adapter == ISelectionChangedListener.class) {
            return this;
        }
        return super.getAdapter(adapter);
    }

    /**
     * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
     */
    public void selectionChanged(SelectionChangedEvent event) {
        IStructuredSelection sel = (IStructuredSelection) event.getSelection();
        this.repositoryTableViewer.setSelection(sel, true);
        this.repositoryTableViewer.expandToLevel(sel.getFirstElement(), AbstractTreeViewer.ALL_LEVELS);
    }

    private class ColoredRespositoryLabelProvider extends RuntimeLabelProvider implements IColorProvider {

        public Color getBackground(Object element) {
            return null;
        }

        public Color getForeground(Object element) {
            ArtefactRepository repository = RepositoryUtils.getRepositoryContents(RepositoryBrowserEditorPage.this.server.getRuntime());
            if (repository != null && element instanceof Artefact && repository.contains((IArtefact) element)) {
                return Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY);
            }
            return null;
        }
    }

}
