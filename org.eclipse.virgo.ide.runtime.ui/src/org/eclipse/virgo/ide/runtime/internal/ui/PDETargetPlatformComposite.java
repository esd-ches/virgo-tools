/*******************************************************************************
 *  Copyright (c) 2015 GianMaria Romanato
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     GianMaria Romanato - initial API and implementation
 *******************************************************************************/

package org.eclipse.virgo.ide.runtime.internal.ui;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.virgo.ide.runtime.internal.core.utils.StatusUtil;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;

/**
 * Gather required data for setting up a PDE target platform.
 */
public class PDETargetPlatformComposite extends Composite {

    class DirectoryLabelProvider extends BaseLabelProvider implements ITableLabelProvider {

        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        public String getColumnText(Object element, int columnIndex) {
            return element != null ? element.toString() : null;
        }

    }

    private List<File> folders;

    private final Button enablePDEDevelopmentButton;

    private final TableViewer foldersTableViewer;

    private final Button addButton;

    private final Button removeButton;

    private final Button editButton;

    private final Label comment;

    private final IRunnableContext runnableContext;

    private final IRuntimeWorkingCopy runtimeWorkingCopy;

    private boolean repositoryConfigurationChanged;

    public PDETargetPlatformComposite(Composite parent, IRunnableContext runnableContext, IRuntimeWorkingCopy iRuntimeWorkingCopy) {
        super(parent, SWT.NONE);

        this.runnableContext = runnableContext;
        this.runtimeWorkingCopy = iRuntimeWorkingCopy;

        setLayout(GridLayoutFactory.swtDefaults().numColumns(2).create());
        setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

        enablePDEDevelopmentButton = new Button(this, SWT.CHECK);
        enablePDEDevelopmentButton.setSelection(false);
        enablePDEDevelopmentButton.setText(Messages.PDETargetPlatformComposite_enable_checkbox);
        enablePDEDevelopmentButton.setLayoutData(GridDataFactory.swtDefaults().span(2, SWT.DEFAULT).create());

        foldersTableViewer = new TableViewer(this);
        foldersTableViewer.setContentProvider(ArrayContentProvider.getInstance());
        foldersTableViewer.setSorter(new ViewerSorter());
        foldersTableViewer.setLabelProvider(new DirectoryLabelProvider());
        foldersTableViewer.getTable().setLayoutData(GridDataFactory.fillDefaults().grab(true, true).span(1, 3).create());
        foldersTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                int size = foldersTableViewer.getStructuredSelection().size();
                editButton.setEnabled(size == 1);
                removeButton.setEnabled(size > 0);
            }
        });

        addButton = new Button(this, SWT.PUSH);
        addButton.setLayoutData(GridDataFactory.fillDefaults().create());
        addButton.setText(Messages.PDETargetPlatformComposite_add);
        addButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                PDETargetPlatformComposite.this.addPressed(e);
            }
        });

        removeButton = new Button(this, SWT.PUSH);
        removeButton.setText(Messages.PDETargetPlatformComposite_remove);
        removeButton.setLayoutData(GridDataFactory.fillDefaults().create());
        removeButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                PDETargetPlatformComposite.this.removePressed(e);
            }
        });

        editButton = new Button(this, SWT.PUSH);
        editButton.setText(Messages.PDETargetPlatformComposite_edit);
        editButton.setLayoutData(GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).create());
        editButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                PDETargetPlatformComposite.this.editPressed(e);
            }
        });

        comment = new Label(this, SWT.WRAP);
        comment.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).span(2, SWT.DEFAULT).create());
        comment.setText(Messages.PDETargetPlatformComposite_note);

        if (PDEHelper.existsTargetDefinition(runtimeWorkingCopy.getName())) {
            enableTargetPlatform(true);
            enablePDEDevelopmentButton.setSelection(true);
        } else {
            enableTargetPlatform(false);
        }

        enablePDEDevelopmentButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                PDETargetPlatformComposite.this.enableButtonSelected(e);
            }
        });
    }

    protected void editPressed(SelectionEvent e) {
        DirectoryDialog dlg = new DirectoryDialog(e.display.getActiveShell());
        dlg.setText(Messages.PDETargetPlatformComposite_edit_dialog_title);
        dlg.setMessage(Messages.PDETargetPlatformComposite_edit_dialog_message);
        String path = dlg.open();
        if (path != null) {
            LinkedHashSet<File> folders2 = new LinkedHashSet<File>(getTargetPlatformFolders());
            folders2.remove(this.foldersTableViewer.getStructuredSelection().getFirstElement());
            folders2.add(new File(path));
            List<File> folders3 = new ArrayList<File>(folders2);
            setTableInput(folders3);
            repositoryConfigurationChanged = true;
        }
    }

    protected void removePressed(SelectionEvent e) {
        LinkedHashSet<File> folders2 = new LinkedHashSet<File>(getTargetPlatformFolders());
        folders2.removeAll(this.foldersTableViewer.getStructuredSelection().toList());
        List<File> folders3 = new ArrayList<File>(folders2);
        setTableInput(folders3);
        repositoryConfigurationChanged = true;
    }

    protected void addPressed(SelectionEvent e) {
        DirectoryDialog dlg = new DirectoryDialog(e.display.getActiveShell());
        dlg.setText(Messages.PDETargetPlatformComposite_add_dialog_title);
        dlg.setMessage(Messages.PDETargetPlatformComposite_add_dialog_message);
        String path = dlg.open();
        if (path != null) {
            LinkedHashSet<File> folders2 = new LinkedHashSet<File>(getTargetPlatformFolders());
            folders2.add(new File(path));
            List<File> folders3 = new ArrayList<File>(folders2);
            setTableInput(folders3);
            repositoryConfigurationChanged = true;
        }
    }

    protected void enableButtonSelected(SelectionEvent e) {
        enableTargetPlatform(enablePDEDevelopmentButton.getSelection());
    }

    protected void enableTargetPlatform(boolean enabled) {
        comment.setEnabled(enabled);
        foldersTableViewer.getTable().setEnabled(enabled);
        addButton.setEnabled(enabled);
        removeButton.setEnabled(enabled);
        editButton.setEnabled(enabled);

        if (enabled) {
            try {
                runnableContext.run(true, false, new IRunnableWithProgress() {

                    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                        folders = PDEHelper.getFoldersForTargetDefinition(runtimeWorkingCopy);
                        monitor.done();
                        if (foldersTableViewer.getTable() != null && !foldersTableViewer.getTable().isDisposed()) {
                            foldersTableViewer.getTable().getDisplay().asyncExec(new Runnable() {

                                public void run() {
                                    foldersTableViewer.setInput(folders);
                                }
                            });
                        }

                    }
                });
            } catch (Exception e) {
                StatusUtil.error(Messages.PDETargetPlatformComposite_error_message, e, StatusManager.LOG | StatusManager.SHOW);
            }
        } else {
            foldersTableViewer.setInput(Collections.emptyList());
        }
    }

    public Button getEnablePDEDevelopmentButton() {
        return enablePDEDevelopmentButton;
    }

    public void setTableInput(List<File> targetPlatformFolders) {
        this.folders = targetPlatformFolders;
        foldersTableViewer.setInput(targetPlatformFolders);
    }

    public List<File> getTargetPlatformFolders() {
        return folders;
    }

    public boolean isRepositoryConfigurationChanged() {
        return repositoryConfigurationChanged;
    }

    public void performFinish(IProgressMonitor monitor) {
        final List<File> folders = getTargetPlatformFolders();
        boolean changed = isRepositoryConfigurationChanged();

        Status st = PDEHelper.createTargetDefinition(monitor, runtimeWorkingCopy, folders);
        if (!st.isOK()) {
            StatusManager.getManager().handle(st, StatusManager.SHOW | StatusManager.LOG);
        } else {
            if (changed) {
                getDisplay().syncExec(new Runnable() {

                    public void run() {
                        boolean applyToRepository = MessageDialog.openQuestion(getShell(),
                            PDEUIMessages.PDETargetPlatformWizardFragment_update_dialog_title,
                            PDEUIMessages.PDETargetPlatformWizardFragment_update_dialog_message);
                        if (applyToRepository) {
                            try {
                                PDEHelper.updateRepositoryConfiguration(runtimeWorkingCopy, folders);
                            } catch (IOException e) {
                                StatusUtil.error(PDEUIMessages.PDETargetPlatformWizardFragment_update_dialog_error, e,
                                    StatusManager.SHOW | StatusManager.LOG);
                            }
                        }
                    }
                });
            }
        }
    }

}
