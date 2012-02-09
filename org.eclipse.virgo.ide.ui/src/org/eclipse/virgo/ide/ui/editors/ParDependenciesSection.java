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
package org.eclipse.virgo.ide.ui.editors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.edit.command.CreateChildCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.pde.internal.ui.parts.TablePart;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.virgo.ide.eclipse.editors.DependenciesSection;
import org.eclipse.virgo.ide.facet.core.FacetUtils;
import org.eclipse.virgo.ide.par.Bundle;
import org.eclipse.virgo.ide.par.Par;


/**
 * @author Christian Dupuis
 * @author Leo Dos Santos
 */
public class ParDependenciesSection extends DependenciesSection {

	private final ParXmlEditorPage page;

	private Par par;

	public ParDependenciesSection(ParXmlEditorPage page, Composite parent, String[] buttonLabels) {
		super(page, parent, buttonLabels);
		this.page = page;
		initialize();
		getSection().setText("Nested Bundles");
		getSection().setDescription("Add or remove bundle dependencies to the PAR.");
	}

	@Override
	protected void enableButtons() {
		if (getTableViewer() != null) {
			Object[] selected = ((IStructuredSelection) getTableViewer().getSelection()).toArray();
			int size = selected.length;
			TablePart tablePart = getTablePart();
			tablePart.setButtonEnabled(getRemoveIndex(), size > 0);
		}
	}

	@Override
	protected void entryModified(Object entry, String value) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void handleAdd() {
		// get list of facet projects that are not in the model
		List<IProject> facetProjects = new ArrayList<IProject>(Arrays.asList(FacetUtils.getBundleProjects()));
		for (Iterator<IProject> it = facetProjects.iterator(); it.hasNext();) {
			IProject workspaceProject = it.next();
			for (Bundle bundle : par.getBundle()) {
				if (bundle.getSymbolicName() != null
						&& bundle.getSymbolicName().equals(ParUtils.getSymbolicName(workspaceProject))) {
					it.remove();
					break;
				}
			}
		}

		ProjectSelectionDialog dialog = new ProjectSelectionDialog(getSection().getShell());
		dialog.setElements(facetProjects.toArray(new IProject[0]));

		if (dialog.open() == Window.OK) {
			// add selected facet projects to the model
			IProject[] workspaceProjects = dialog.getSelectedProjects();
			for (IProject workspaceProject : workspaceProjects) {
				Collection<?> newChildDescriptors = page.getModel().getNewChildDescriptors(par, null);
				Object descriptor = newChildDescriptors.iterator().next();
				Command command = CreateChildCommand.create(page.getModel(), par, descriptor, Collections
						.singleton(par));
				page.getModel().getCommandStack().execute(command);
				if (command.getResult() != null && !command.getResult().isEmpty()) {
					Bundle project = (Bundle) command.getResult().iterator().next();
					project.setSymbolicName(ParUtils.getSymbolicName(workspaceProject));
				}
			}
		}
	}

	@Override
	protected void handleDoubleClick(IStructuredSelection selection) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void handleDown() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void handleOpenProperties() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void handleRemove() {
		IStructuredSelection selection = (IStructuredSelection) getTableViewer().getSelection();
		if (!selection.isEmpty()) {
			Command command = RemoveCommand.create(page.getModel(), par, null, selection.toList());
			page.getModel().getCommandStack().execute(command);
		}
	}

	@Override
	protected void handleUp() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void initialize() {
		getTableViewer().setContentProvider(new AdapterFactoryContentProvider(page.getModel().getAdapterFactory()));
		getTableViewer().setLabelProvider(new AdapterFactoryLabelProvider(page.getModel().getAdapterFactory()));
		par = page.getPar();
		getTableViewer().setInput(par);
	}

	@Override
	protected void selectionChanged(IStructuredSelection selection) {
		enableButtons();
	}

}
