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
package org.eclipse.virgo.ide.runtime.internal.ui.console;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonImages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.internal.browser.ImageResource;
import org.eclipse.virgo.ide.runtime.internal.ui.AbstractBundleEditorPage;
import org.eclipse.virgo.ide.runtime.internal.ui.SearchTextHistory;
import org.eclipse.virgo.ide.runtime.internal.ui.ServerUiImages;
import org.eclipse.virgo.ide.runtime.internal.ui.model.ManagementConnectorClient;


/**
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public class ServerConsoleEditorPage extends AbstractBundleEditorPage {

	private StyledText manifestText;

	private IToolBarManager toolBarManager;

	private Action backAction;

	private Action forwardAction;

	private Action refreshAction;

	private final SearchTextHistory history = new SearchTextHistory();

	@Override
	protected void createBundleContent(Composite parent) {

		mform = new ManagedForm(parent);
		setManagedForm(mform);
		sform = mform.getForm();
		FormToolkit toolkit = mform.getToolkit();
		sform.setText("Server Console");
		sform.setImage(ServerUiImages.getImage(ServerUiImages.IMG_OBJ_SPRINGSOURCE));
		sform.setExpandHorizontal(true);
		sform.setExpandVertical(true);
		toolkit.decorateFormHeading(sform.getForm());

		Composite body = sform.getBody();
		GridLayout layout = new GridLayout(1, false);
		layout.marginLeft = 6;
		layout.marginTop = 6;
		layout.marginRight = 6;
		body.setLayout(layout);

		Section manifestSection = toolkit.createSection(sform.getBody(), ExpandableComposite.TITLE_BAR
				| Section.DESCRIPTION);
		manifestSection.setText("Commands");
		manifestSection.setDescription("Execute commands on server.");
		layout = new GridLayout();
		manifestSection.setLayout(layout);
		manifestSection.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite manifestComposite = toolkit.createComposite(manifestSection);
		layout = new GridLayout();
		layout.marginLeft = 6;
		layout.marginTop = 6;
		layout.numColumns = 3;
		manifestComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		manifestComposite.setLayout(layout);
		manifestSection.setClient(manifestComposite);

		Label commandLabel = toolkit.createLabel(manifestComposite, "Command:");
		commandLabel.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(commandLabel);
		final Text commandText = toolkit.createText(manifestComposite, "", SWT.CANCEL | SWT.SEARCH);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(commandText);

		commandText.addKeyListener(new KeyListener() {

			public void keyPressed(KeyEvent e) {
				if (e.character == SWT.CR || e.character == SWT.LF) {
					history.add(commandText.getText());
					forwardAction.setEnabled(history.canForward());
					backAction.setEnabled(history.canBack());
					toolBarManager.update(true);
					manifestText.append("osgi> " + commandText.getText() + "\n");
					manifestText.append((ManagementConnectorClient.execute(getServer().getOriginal(), commandText
							.getText()))
							+ "\n");
					manifestText.setTopIndex(manifestText.getLineCount() - 1);
					commandText.setText("");
				}
				else if (e.keyCode == SWT.ARROW_UP) {
					String command = history.back();
					commandText.setText(command);
					commandText.setSelection(command.length());
					e.doit = false;
				}
				else if (e.keyCode == SWT.ARROW_DOWN) {
					String command = history.forward();
					commandText.setText(command);
					commandText.setSelection(command.length());
					e.doit = false;
				}
			}

			public void keyReleased(KeyEvent e) {
			}
		});

		Button commandButton = toolkit.createButton(manifestComposite, "Execute", SWT.PUSH);
		commandButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				history.add(commandText.getText());
				forwardAction.setEnabled(history.canForward());
				backAction.setEnabled(history.canBack());
				toolBarManager.update(true);
				manifestText.append("osgi> " + commandText.getText() + "\n");
				manifestText.append((ManagementConnectorClient
						.execute(getServer().getOriginal(), commandText.getText()))
						+ "\n");
				manifestText.setTopIndex(manifestText.getLineCount() - 1);
				commandText.setText("");
			}
		});
		Button clearButton = toolkit.createButton(manifestComposite, "Clear", SWT.PUSH);
		clearButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				manifestText.setText("");
			}
		});

		manifestText = new StyledText(manifestComposite, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		manifestText.setWordWrap(false);
		manifestText.setFont(JFaceResources.getTextFont());
		GridData data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 3;
		manifestText.setLayoutData(data);

		Label helpLabel = toolkit.createLabel(manifestComposite, "Type 'help' to get a list of supported commands.");
		GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(helpLabel);

		toolBarManager = sform.getToolBarManager();

		backAction = new Action("Back") {
			@Override
			public void run() {
				commandText.setText(history.back());
				manifestText.append("osgi> " + commandText.getText() + "\n");
				manifestText.append((ManagementConnectorClient
						.execute(getServer().getOriginal(), commandText.getText()))
						+ "\n");
				forwardAction.setEnabled(history.canForward());
				backAction.setEnabled(history.canBack());
				toolBarManager.update(true);
				manifestText.setTopIndex(manifestText.getLineCount() - 1);
				commandText.setText("");
			}
		};
		backAction.setImageDescriptor(ImageResource
				.getImageDescriptor(org.eclipse.ui.internal.browser.ImageResource.IMG_ELCL_NAV_BACKWARD));
		backAction.setHoverImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_CLCL_NAV_BACKWARD));
		backAction.setDisabledImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_DLCL_NAV_BACKWARD));
		backAction.setEnabled(false);
		toolBarManager.add(backAction);

		forwardAction = new Action("Forward", CommonImages.EXPAND_ALL) {
			@Override
			public void run() {
				commandText.setText(history.forward());
				manifestText.append("osgi> " + commandText.getText() + "\n");
				manifestText.append((ManagementConnectorClient
						.execute(getServer().getOriginal(), commandText.getText()))
						+ "\n");
				forwardAction.setEnabled(history.canForward());
				backAction.setEnabled(history.canBack());
				toolBarManager.update(true);
				manifestText.setTopIndex(manifestText.getLineCount() - 1);
				commandText.setText("");
			}
		};
		forwardAction.setImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_ELCL_NAV_FORWARD));
		forwardAction.setHoverImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_CLCL_NAV_FORWARD));
		forwardAction.setDisabledImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_DLCL_NAV_FORWARD));
		forwardAction.setEnabled(false);
		toolBarManager.add(forwardAction);

		refreshAction = new Action("Refresh from server", CommonImages.REFRESH) {

			@Override
			public void run() {
				String cmdLine = history.current();
				manifestText.append("osgi> " + cmdLine + "\n");
				manifestText.append((ManagementConnectorClient.execute(getServer().getOriginal(), cmdLine)) + "\n");
				forwardAction.setEnabled(history.canForward());
				backAction.setEnabled(history.canBack());
				toolBarManager.update(true);
				manifestText.setTopIndex(manifestText.getLineCount() - 1);
				commandText.setText("");
			}

		};
		toolBarManager.add(refreshAction);
		sform.updateToolBar();
	}

}
