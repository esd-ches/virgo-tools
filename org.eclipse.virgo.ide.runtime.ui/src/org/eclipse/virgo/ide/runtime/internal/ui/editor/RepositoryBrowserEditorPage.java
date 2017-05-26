/*******************************************************************************
 * Copyright (c) 2009, 2010 SpringSource, a divison of VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource, a division of VMware, Inc. - initial API and implementation
 *     GianMaria Romanato - remove SpringSource repository browser section
 *******************************************************************************/

package org.eclipse.virgo.ide.runtime.internal.ui.editor;

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
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.progress.IProgressConstants;
import org.eclipse.virgo.ide.runtime.core.ServerCorePlugin;
import org.eclipse.virgo.ide.runtime.core.artefacts.Artefact;
import org.eclipse.virgo.ide.runtime.core.artefacts.ArtefactRepository;
import org.eclipse.virgo.ide.runtime.core.artefacts.ArtefactRepositoryManager;
import org.eclipse.virgo.ide.runtime.core.artefacts.IArtefactTyped;
import org.eclipse.virgo.ide.runtime.core.artefacts.LocalBundleArtefact;
import org.eclipse.virgo.ide.runtime.core.provisioning.IBundleRepositoryChangeListener;
import org.eclipse.virgo.ide.runtime.core.provisioning.RepositoryProvisioningJob;
import org.eclipse.virgo.ide.runtime.core.provisioning.RepositorySourceProvisiongJob;
import org.eclipse.virgo.ide.runtime.core.provisioning.RepositoryUtils;
import org.eclipse.virgo.ide.runtime.internal.ui.ServerUiImages;
import org.eclipse.virgo.ide.runtime.internal.ui.ServerUiPlugin;
import org.eclipse.virgo.ide.runtime.internal.ui.repository.RefreshBundleJob;
import org.eclipse.virgo.ide.runtime.internal.ui.sorters.RepositoryViewerSorter;
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

    private final IJobChangeListener jobListener = new ArtefactOperationJobListener();

    private Button refreshButton;

    private CommonViewer repositoryTableViewer;

    private Button downloadSourcesButton;

    private Tree searchResultTable;

    private Shell shell;

    private Link update;

    private IBundleRepositoryChangeListener repositoryListener;

    @Override
    public void createPartControl(Composite parent) {

        this.shell = parent.getShell();

        FormToolkit toolkit = getFormToolkit(parent.getDisplay());

        ScrolledForm form = toolkit.createScrolledForm(parent);
        toolkit.decorateFormHeading(form.getForm());
        form.setText(Messages.RepositoryBrowserEditorPage_BundleBrowserLabel);
        form.setImage(getFormImage());
        GridLayout layout = GridLayoutFactory.swtDefaults().numColumns(1).create();
        form.getBody().setLayout(layout);

        Section section = createSection(toolkit, form);
        form.setContent(section);
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

        Job.getJobManager().removeJobChangeListener(this.jobListener);
        ServerCorePlugin.getArtefactRepositoryManager().removeBundleRepositoryChangeListener(this.repositoryListener);
    }

    @Override
    public void init(IEditorSite site, IEditorInput input) {
        super.init(site, input);
        addListeners();
        initialize();
    }

    @Override
    public void setFocus() {
        if (this.searchResultTable != null) {
            this.searchResultTable.setFocus();
        }
    }

    private void setRepositoryDateString() {
        Date date = ServerCorePlugin.getArtefactRepositoryManager().getArtefactRepositoryDate();
        String dateString = dateFormat.format(date);
        this.update.setText(Messages.RepositoryBrowserEditorPage_UpdateURL + dateString + ")"); //$NON-NLS-1$
    }

    protected String getServerName() {
        return Messages.RepositoryBrowserEditorPage_ServerName;
    }

    private Section createSection(FormToolkit toolkit, ScrolledForm form) {
        GridLayout layout;
        Section section = toolkit.createSection(form.getBody(), ExpandableComposite.TITLE_BAR | Section.DESCRIPTION);
        section.setText(Messages.RepositoryBrowserEditorPage_BundlesAndLibraries);
        section.setDescription(Messages.RepositoryBrowserEditorPage_BundlesAndLibrariesMessage + getServerName() + ". " //$NON-NLS-1$
            + Messages.RepositoryBrowserEditorPage_BundlesAndLibrariesProviso);
        section.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite leftComposite = toolkit.createComposite(section);
        layout = new GridLayout();
        layout.numColumns = 1;
        leftComposite.setLayout(layout);
        leftComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        toolkit.paintBordersFor(leftComposite);
        section.setClient(leftComposite);

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

        data = new GridData(GridData.FILL_BOTH);
        // data.heightHint = 120;
        this.repositoryTableViewer.getControl().setLayoutData(data);

        Composite buttonComposite = new Composite(composite2, SWT.NONE);
        buttonComposite.setLayout(new GridLayout(1, true));
        data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
        buttonComposite.setLayoutData(data);

        data = new GridData(GridData.FILL_HORIZONTAL);
        // data.widthHint = 100;
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

        return section;
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

    protected void addListeners() {
        Job.getJobManager().addJobChangeListener(this.jobListener);

        this.repositoryListener = new IBundleRepositoryChangeListener() {

            public void bundleRepositoryChanged(IRuntime runtime) {
                refreshViewers();
            }
        };
        ServerCorePlugin.getArtefactRepositoryManager().addBundleRepositoryChangeListener(this.repositoryListener);

    }

    protected void initialize() {
        if (this.repositoryTableViewer == null) {
            return;
        }
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

    @SuppressWarnings({ "unchecked", "rawtypes" })
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

}
