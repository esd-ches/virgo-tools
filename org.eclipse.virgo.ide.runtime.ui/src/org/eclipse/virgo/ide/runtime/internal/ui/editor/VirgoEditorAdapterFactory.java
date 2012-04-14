/**
 * <copyright>
 *
 * TODO Copyright
 *
 * </copyright>
 *
 */
package org.eclipse.virgo.ide.runtime.internal.ui.editor;

import java.util.List;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.ui.ServerUICore;
import org.eclipse.wst.server.ui.editor.ServerEditorPart;
import org.eclipse.wst.server.ui.internal.editor.IServerEditorPartFactory;
import org.eclipse.wst.server.ui.internal.editor.ServerEditor;
import org.eclipse.wst.server.ui.internal.editor.ServerEditorCore;
import org.eclipse.wst.server.ui.internal.editor.ServerEditorInput;
import org.eclipse.wst.server.ui.internal.editor.ServerEditorPartFactory;

/**
 * 
 * @author Miles Parker
 * 
 */
public class VirgoEditorAdapterFactory implements IAdapterFactory {

	/**
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object,
	 *      java.lang.Class)
	 */
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adapterType == IContentOutlinePage.class && adaptableObject instanceof ServerEditor) {
			ServerEditor serverEditor = (ServerEditor) adaptableObject;
			String partName = serverEditor.getPartName();
			IWorkbenchPartSite site = serverEditor.getSite();
//			IWorkbenchPart part = serverEditor.getEditorSite().getPart();
//			IServerEditorPartFactory pageFactory = serverEditor.getPageFactory((ServerEditorPart) part);
//			List<ServerEditorPartFactory> sef = ServerEditorCore.getServerEditorPageFactories();
//			for (ServerEditorPartFactory serverEditorPartFactory : sef) {
//				String id = serverEditorPartFactory.getId();
//				System.err.println(id);
//			}
			//serverEditor.getSite()PageSite(0);
			//input.get
//			if (serverEditor.getPageSite(0).)
			return new VirgoRepositoryOutlinePage();
		}
		return null;
	}

	/**
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	public Class[] getAdapterList() {
		return new Class[] { IContentOutlinePage.class};
	}

}
