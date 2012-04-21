/**
 * <copyright>
 *
 * TODO Copyright
 *
 * </copyright>
 *
 */
package org.eclipse.virgo.ide.runtime.internal.ui.editor;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.virgo.ide.runtime.internal.ui.ServerUiPlugin;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.ui.internal.editor.ServerEditor;
import org.eclipse.wst.server.ui.internal.editor.ServerEditorInput;

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
			ServerEditorInput editorInput = (ServerEditorInput) serverEditor.getEditorInput();
			IServer server = ServerCore.findServer(editorInput
					.getServerId());
			IEditorPart[] findEditors = serverEditor.findEditors(editorInput);
			IServerType serverType = server.getServerType();
			if (serverType.getId().equals(ServerUiPlugin.VIRGO_SERVER_ID )) {
				return new RepositoryOutlinePage(serverEditor);
			}
		}
		return null;
	}

	/**
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	public Class[] getAdapterList() {
		return new Class[] { IContentOutlinePage.class };
	}

}
