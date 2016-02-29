
package org.eclipse.virgo.ide.runtime.internal.ui.editor;

import org.eclipse.osgi.util.NLS;

public class TargetPlatformSectionMessages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.virgo.ide.runtime.internal.ui.editor.tpsectionmessages"; //$NON-NLS-1$

    public static String TargetPlatformSection_description;

    public static String TargetPlatformSection_form_text;

    public static String TargetPlatformSection_InternalError;

    public static String TargetPlatformSection_not_configured_message;

    public static String TargetPlatformSection_not_configured_title;

    public static String TargetPlatformSection_title;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, TargetPlatformSectionMessages.class);
    }

    private TargetPlatformSectionMessages() {
    }
}
