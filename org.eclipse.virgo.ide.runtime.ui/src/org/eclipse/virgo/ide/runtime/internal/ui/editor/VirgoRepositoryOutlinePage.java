/**
 * <copyright>
 *
 * TODO Copyright
 *
 * </copyright>
 *
 */
package org.eclipse.virgo.ide.runtime.internal.ui.editor;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;


/**
 * 
 * @author Miles Parker
 *
 */
public class VirgoRepositoryOutlinePage extends ContentOutlinePage {

	private TreeViewer contentOutlineViewer;
	protected Object input;

	public void createControl(Composite parent) {
		super.createControl(parent);
		contentOutlineViewer = getTreeViewer();
		contentOutlineViewer.addSelectionChangedListener(this);

		// Set up the tree viewer.
		//
		contentOutlineViewer.setContentProvider(new IContentProvider() {
			
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				input = newInput;
			}
			
			public void dispose() {
			}
		});
		contentOutlineViewer.setLabelProvider(new ILabelProvider() {
			
			public void removeListener(ILabelProviderListener listener) {
			}
			
			public boolean isLabelProperty(Object element, String property) {
				return false;
			}
			
			public void dispose() {
			}
			
			public void addListener(ILabelProviderListener listener) {
			}
			
			public String getText(Object element) {
				return null;
			}
			
			public Image getImage(Object element) {
				return null;
			}
		});
		//contentOutlineViewer.setInput(parResource);
	}
}
