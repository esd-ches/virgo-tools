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
package org.eclipse.virgo.ide.runtime.internal.ui;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;


/**
 * The images provided by the Spring Beans UI plugin.
 * Initialize the image registry by declaring all of the required
 * graphics. This involves creating JFace image descriptors describing
 * how to create/find the image should it be needed.
 * The image is not actually allocated until requested.
 * Prefix conventions
 * Wizard Banners			WIZBAN_
 * Preference Banners		PREF_BAN_
 * Property Page Banners	PROPBAN_
 * Color toolbar			CTOOL_
 * Enable toolbar			ETOOL_
 * Disable toolbar			DTOOL_
 * Local enabled toolbar	ELCL_
 * Local Disable toolbar	DLCL_
 * Object large			OBJL_
 * Object small			OBJS_
 * View 					VIEW_
 * Product images			PROD_
 * Misc images				MISC_
 * Where are the images?
 * The images (typically gifs) are found in the same location as this
 * plugin class. This may mean the same package directory as the
 * package holding this class. The images are declared using
 * <code>this.getClass()</code> to ensure they are looked up via
 * this plugin class.
 * @author Christian Dupuis
 * @see org.eclipse.jface.resource.ImageRegistry
 */
public class ServerUiImages {

	private static final String ICON_PATH_PREFIX = "icons/full/";
	private static final String NAME_PREFIX = ServerUiPlugin.PLUGIN_ID + '.';
	private static final int NAME_PREFIX_LENGTH = NAME_PREFIX.length();

	/* Declare Common paths */
	private static URL ICON_BASE_URL = null;

	static {
		try {
			ICON_BASE_URL = new URL(ServerUiPlugin.getDefault().getBundle()
					.getEntry("/"), ICON_PATH_PREFIX);
		} catch (MalformedURLException e) {
			ServerUiPlugin.log(e);
		}
	}
	
	/** A table of all the <code>ImageDescriptor</code>s. */
	private static Map<String, ImageDescriptor> imageDescriptors;

	public static final String IMG_WIZB_SERVER = NAME_PREFIX + "springsource_wiz.png";
	public static final String IMG_WIZB_VIRGO_SERVER = NAME_PREFIX + "virgo_wiz.png";   

	public static final String IMG_OBJ_PORT = NAME_PREFIX + "port_obj.gif";
	public static final String IMG_OBJ_SPRINGSOURCE = NAME_PREFIX + "springsource_obj.png";
	public static final String IMG_OBJ_VIRGO = NAME_PREFIX + "virgo_obj.png";
	public static final String IMG_OBJ_BUNDLE = NAME_PREFIX + "bundle_obj.gif";
	public static final String IMG_OBJ_BUNDLE_SRC = NAME_PREFIX + "bundle_src_obj.gif";
	public static final String IMG_OBJ_LIB = NAME_PREFIX + "lib_obj.gif";
	public static final String IMG_OBJ_FILE = NAME_PREFIX + "file_obj.gif";
	
	
	// Use IPath and toOSString to build the names to ensure they have the slashes correct
	private final static String OBJECT = "obj16/"; //basic colors - size 16x16
	private final static String WIZBAN = "wizban/"; //basic colors - size 16x16
	//private final static String OVR = "ovr16/"; //basic colors - size 7x8

	public static final ImageDescriptor DESC_WIZB_SERVER = createManaged(WIZBAN, IMG_WIZB_SERVER);
	public static final ImageDescriptor DESC_WIZB_VIRGO_SERVER = createManaged(WIZBAN, IMG_WIZB_VIRGO_SERVER);
	
	public static final ImageDescriptor DESC_OBJ_PORT = createManaged(OBJECT, IMG_OBJ_PORT);
	public static final ImageDescriptor DESC_OBJ_SPRINGSOURCE = createManaged(OBJECT, IMG_OBJ_SPRINGSOURCE);
	public static final ImageDescriptor DESC_OBJ_VIRGO = createManaged(OBJECT, IMG_OBJ_VIRGO);
	public static final ImageDescriptor DESC_OBJ_BUNDLE = createManaged(OBJECT, IMG_OBJ_BUNDLE);
	public static final ImageDescriptor DESC_OBJ_BUNDLE_SRC = createManaged(OBJECT, IMG_OBJ_BUNDLE_SRC);
	public static final ImageDescriptor DESC_OBJ_LIB = createManaged(OBJECT, IMG_OBJ_LIB);
	public static final ImageDescriptor DESC_OBJ_FILE = createManaged(OBJECT, IMG_OBJ_FILE);

	/*
	 * Helper method to initialize the image registry from the BeansUIPlugin
	 * class.
	 */
	/* package */ static void initializeImageRegistry(ImageRegistry registry) {
		for (String key : imageDescriptors.keySet()) {
			registry.put(key, imageDescriptors.get(key));
		}
	}

	/**
	 * Returns the {@link Image} identified by the given key,
	 * or <code>null</code> if it does not exist.
	 */
	public static Image getImage(String key) {
		return ServerUiPlugin.getDefault().getImageRegistry().get(key);
	}
	
	/**
	 * Sets the three image descriptors for enabled, disabled, and hovered to
	 * an action. The actions are retrieved from the *tool16 folders.
	 */
	public static void setToolImageDescriptors(IAction action,
			String iconName) {
		setImageDescriptors(action, "tool16", iconName);
	}

	/**
	 * Sets the three image descriptors for enabled, disabled, and hovered to
	 * an action. The actions are retrieved from the *lcl16 folders.
	 */
	public static void setLocalImageDescriptors(IAction action,
			String iconName) {
		setImageDescriptors(action, "lcl16", iconName);
	}

	//---- Helper methods to access icons on the file system -------------------

	private static void setImageDescriptors(IAction action, String type,
			String relPath) {
		action.setImageDescriptor(create("e" + type, relPath));
		try {
			ImageDescriptor id = ImageDescriptor.createFromURL(makeIconFileURL(
					"d" + type, relPath));
			if (id != null) {
				action.setDisabledImageDescriptor(id);
			}
		} catch (MalformedURLException e) {
			ServerUiPlugin.log(e);
		}
	}

	private static ImageDescriptor createManaged(String prefix, String name) {
		try {
			ImageDescriptor result = ImageDescriptor.createFromURL(
				   makeIconFileURL(prefix, name.substring(NAME_PREFIX_LENGTH)));
			if (imageDescriptors == null) {
				imageDescriptors = new HashMap<String, ImageDescriptor>();
			}
			imageDescriptors.put(name, result);
			return result;
		} catch (MalformedURLException e) {
			ServerUiPlugin.log(e);
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}

	private static ImageDescriptor create(String prefix, String name) {
		try {
			return ImageDescriptor.createFromURL(makeIconFileURL(prefix,
					name));
		} catch (MalformedURLException e) {
			ServerUiPlugin.log(e);
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}

	private static URL makeIconFileURL(String prefix, String name)
			throws MalformedURLException {
		if (ICON_BASE_URL == null) {
			throw new MalformedURLException();
		}

		StringBuffer buffer = new StringBuffer(prefix);
		buffer.append('/').append(name);
		return new URL(ICON_BASE_URL, buffer.toString());
	}
}
