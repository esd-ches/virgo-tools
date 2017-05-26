
package org.eclipse.virgo.ide.ui.internal.actions;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.virgo.ide.ui.internal.actions.messages"; //$NON-NLS-1$

    public static String ConvertPDE2VirgoProject_message;

    public static String ConvertPDE2VirgoProject_title;
    public static String ConvertPlugInProject_message;

    public static String ConvertPlugInProject_title;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
