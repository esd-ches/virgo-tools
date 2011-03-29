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

import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerListener;
import org.eclipse.wst.server.core.ServerEvent;
import org.eclipse.wst.server.ui.editor.ServerEditorPart;
import org.eclipse.wst.server.ui.internal.ImageResource;
import org.eclipse.wst.server.ui.internal.Messages;
import org.eclipse.wst.server.ui.internal.view.servers.StartAction;
import org.eclipse.wst.server.ui.internal.view.servers.StopAction;

/**
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public abstract class AbstractBundleEditorPage extends ServerEditorPart {

	class PageEnablementServerListener implements IServerListener {

		public void serverChanged(final ServerEvent event) {
			if ((event.getKind() & ServerEvent.SERVER_CHANGE) != 0 && (event.getKind() & ServerEvent.STATE_CHANGE) != 0) {
				getSite().getShell().getDisplay().asyncExec(new Runnable() {

					public void run() {
						if (event.getState() == IServer.STATE_STARTED) {
							enablePage();
						}
						else {
							disablePage();
						}
						sform.getToolBarManager().update(true);
					}
				});
			}
		}
	}

	class StartServerAction extends Action {

		private final String launchMode;

		public StartServerAction(String launchMode) {
			this.launchMode = launchMode;
			if (launchMode == ILaunchManager.RUN_MODE) {
				setToolTipText(Messages.actionStartToolTip);
				setText(Messages.actionStart);
				setImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_ELCL_START));
				setHoverImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_CLCL_START));
				setDisabledImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_DLCL_START));
			}
			else if (launchMode == ILaunchManager.DEBUG_MODE) {
				setToolTipText(Messages.actionDebugToolTip);
				setText(Messages.actionDebug);
				setImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_ELCL_START_DEBUG));
				setHoverImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_CLCL_START_DEBUG));
				setDisabledImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_DLCL_START_DEBUG));
			}
		}

		@Override
		public void run() {
			if (getServer().getOriginal().canStart(launchMode).isOK()) {
				StartAction.start(getServer().getOriginal(), launchMode, getSite().getShell());
			}
		}

	}

	class StopServerAction extends Action {

		public StopServerAction() {
			setToolTipText(Messages.actionStopToolTip);
			setImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_ELCL_STOP));
			setHoverImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_CLCL_STOP));
			setDisabledImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_DLCL_STOP));
		}

		@Override
		public void run() {
			if (getServer().getOriginal().canStop().isOK()) {
				StopAction.stop(getServer().getOriginal(), getSite().getShell());
			}
		}

	}

	private StartServerAction debugAction;

	private StartServerAction runAction;

	private IServerListener serverListener;

	private StopServerAction stopAction;

	protected ManagedForm mform;

	protected ScrolledForm sform;

	public final void createPartControl(Composite parent) {

		createBundleContent(parent);

		IToolBarManager toolBarManager = sform.getToolBarManager();

		if (toolBarManager.getItems().length > 0) {
			toolBarManager.add(new Separator());
		}

		debugAction = new StartServerAction(ILaunchManager.DEBUG_MODE);
		toolBarManager.add(debugAction);
		runAction = new StartServerAction(ILaunchManager.RUN_MODE);
		toolBarManager.add(runAction);
		stopAction = new StopServerAction();
		toolBarManager.add(stopAction);
		toolBarManager.update(true);

		if (server.getOriginal().getServerState() != IServer.STATE_STARTED) {
			disablePage();
		}
		else {
			enablePage();
		}
	}

	public void dispose() {
		getServer().getOriginal().removeServerListener(serverListener);
		super.dispose();
	}

	public void init(IEditorSite site, IEditorInput input) {
		super.init(site, input);
		serverListener = new PageEnablementServerListener();
		getServer().getOriginal().addServerListener(serverListener);
	}

	public void setFocus() {
	}

	protected abstract void createBundleContent(Composite parent);

	protected void disablePage() {
		sform.getForm().setMessage("Server '" + getServer().getName() + "' is not running.",
				IMessageProvider.INFORMATION);
		setEnabled(sform.getForm().getBody(), false);
		runAction.setEnabled(true);
		debugAction.setEnabled(true);
		stopAction.setEnabled(false);
	}

	protected void enablePage() {
		sform.getForm().setMessage(null);
		setEnabled(sform.getForm().getBody(), true);
		runAction.setEnabled(false);
		debugAction.setEnabled(false);
		stopAction.setEnabled(true);
	}

	protected void setEnabled(Control control, boolean enabled) {
		control.setEnabled(enabled);
		if (control instanceof Composite) {
			for (Control childControl : ((Composite) control).getChildren()) {
				setEnabled(childControl, enabled);
			}
		}
	}

}
