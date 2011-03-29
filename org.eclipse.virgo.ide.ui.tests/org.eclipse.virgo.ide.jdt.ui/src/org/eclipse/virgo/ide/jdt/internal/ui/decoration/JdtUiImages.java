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
package org.eclipse.virgo.ide.jdt.internal.ui.decoration;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.virgo.ide.jdt.ui.JdtUiPlugin;


/**
 * The images provided by the Spring UI plugin. Initialize the image registry by
 * declaring all of the required graphics. This involves creating JFace image
 * descriptors describing how to create/find the image should it be needed. The
 * image is not actually allocated until requested.
 * Prefix conventions Wizard Banners WIZBAN_ Preference Banners PREF_BAN_
 * Property Page Banners PROPBAN_ Color toolbar CTOOL_ Enable toolbar ETOOL_
 * Disable toolbar DTOOL_ Local enabled toolbar ELCL_ Local Disable toolbar
 * DLCL_ Object large OBJL_ Object small OBJS_ View VIEW_ Product images PROD_
 * Misc images MISC_
 * Where are the images? The images (typically gifs) are found in the same
 * location as this plugin class. This may mean the same package directory as
 * the package holding this class. The images are declared using
 * <code>this.getClass()</code> to ensure they are looked up via this plugin
 * class.
 * @author Christian Dupuis
 * @see org.eclipse.jface.resource.ImageRegistry
 */
public class JdtUiImages {

	private static final String ICON_PATH_PREFIX = "icons/full/";

	private static final String NAME_PREFIX = JdtUiPlugin.PLUGIN_ID + '.';

	private static final int NAME_PREFIX_LENGTH = NAME_PREFIX.length();

	/* Declare Common paths */
	private static URL ICON_BASE_URL = null;

	static {
		try {
			ICON_BASE_URL = new URL(JdtUiPlugin.getDefault().getBundle().getEntry("/"),
					ICON_PATH_PREFIX);
		}
		catch (MalformedURLException e) {
		}
	}

	/** A table of all the <code>ImageDescriptor</code>s. */
	private static Map<String, ImageDescriptor> imageDescriptors;

	/** The image registry containing <code>Image</code>s. */
	private static ImageRegistry imageRegistry;

	public static final String IMG_OVR_LOCKED = NAME_PREFIX + "locked_ovr.gif";
	public static final String IMG_OVR_EXPORTED = NAME_PREFIX + "package_export_ovr.gif";

	private final static String OVR = "ovr16/"; // basic colors -

	// size 16x16

	public static final ImageDescriptor DESC_OVR_LOCKED = createManaged(OVR, IMG_OVR_LOCKED);
	public static final ImageDescriptor DESC_OVR_EXPORTED = createManaged(OVR, IMG_OVR_EXPORTED);

	/**
	 * Returns the <code>Image<code> identified by the given key,
	 * or <code>null</code> if it does not exist.
	 */
	public static Image getImage(String key) {
		return getImageRegistry().get(key);
	}

	/**
	 * Sets the three image descriptors for enabled, disabled, and hovered to an
	 * action. The actions are retrieved from the *tool16 folders.
	 */
	public static void setToolImageDescriptors(IAction action, String iconName) {
		setImageDescriptors(action, "tool16", iconName);
	}

	/**
	 * Sets the three image descriptors for enabled, disabled, and hovered to an
	 * action. The actions are retrieved from the *lcl16 folders.
	 */
	public static void setLocalImageDescriptors(IAction action, String iconName) {
		setImageDescriptors(action, "lcl16", iconName);
	}

	/*
	 * Helper method to access the image registry from the JavaPlugin class.
	 */
	/* package */static ImageRegistry getImageRegistry() {
		if (imageRegistry == null) {
			imageRegistry = new ImageRegistry();
			for (String string : imageDescriptors.keySet()) {
				String key = string;
				imageRegistry.put(key, imageDescriptors.get(key));
			}
			imageDescriptors = null;
		}
		return imageRegistry;
	}

	// ---- Helper methods to access icons on the file system
	// -------------------

	private static void setImageDescriptors(IAction action, String type, String relPath) {
		try {
			ImageDescriptor id = ImageDescriptor
					.createFromURL(makeIconFileURL("d" + type, relPath));
			if (id != null) {
				action.setDisabledImageDescriptor(id);
			}
		}
		catch (MalformedURLException e) {
		}
		/*
		 * try { ImageDescriptor id =
		 * ImageDescriptor.createFromURL(makeIconFileURL( "c" + type, relPath));
		 * if (id != null) { action.setHoverImageDescriptor(id); } } catch
		 * (MalformedURLException e) { BeansUIPlugin.log(e); }
		 */
		action.setImageDescriptor(create("e" + type, relPath));
	}

	private static ImageDescriptor createManaged(String prefix, String name) {
		try {
			ImageDescriptor result = ImageDescriptor.createFromURL(makeIconFileURL(prefix, name
					.substring(NAME_PREFIX_LENGTH)));
			if (imageDescriptors == null) {
				imageDescriptors = new HashMap<String, ImageDescriptor>();
			}
			imageDescriptors.put(name, result);
			if (imageRegistry != null) {
			}
			return result;
		}
		catch (MalformedURLException e) {
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}

	private static ImageDescriptor create(String prefix, String name) {
		try {
			return ImageDescriptor.createFromURL(makeIconFileURL(prefix, name));
		}
		catch (MalformedURLException e) {
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}

	private static URL makeIconFileURL(String prefix, String name) throws MalformedURLException {
		if (ICON_BASE_URL == null) {
			throw new MalformedURLException();
		}

		StringBuffer buffer = new StringBuffer(prefix);
		buffer.append('/');
		buffer.append(name);
		return new URL(ICON_BASE_URL, buffer.toString());
	}
}
