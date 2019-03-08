
package org.eclipse.virgo.ide.runtime.core.ches;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.virgo.ide.runtime.core.ServerCorePlugin;
import org.osgi.framework.Bundle;

/**
 * Tray icon for showing the grunt status.
 *
 * @author stefan.zugal
 *
 */
public class GruntTrayIcon {

    public static synchronized GruntTrayIcon getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new GruntTrayIcon();
        }

        return INSTANCE;
    }

    private static GruntTrayIcon INSTANCE;

    /**
     * Image to be shown when grunt is idle.
     */
    private Image statusIdleImage;

    /**
     * Image to be shown when grunt is running.
     */
    private Image statusGruntImage;

    private Image loadImage(Display display, String imageName) {
        try {
            Bundle bundle = Platform.getBundle(ServerCorePlugin.PLUGIN_ID);
            URL entry = bundle.getEntry("icons/" + imageName);
            return new Image(display, entry.openStream());
        } catch (IOException e) {
            VirgoToolingHook.logError("Could not load image " + imageName, e);
        }

        return null;
    }

    /**
     * Initializes the images.
     *
     * @param shell the shell of the workbench
     */
    private void initializeImages(Shell shell) {
        if (statusGruntImage != null) {
            return;
        }

        Image[] images = shell.getImages();
        for (Image image : images) {
            if (statusIdleImage == null || statusIdleImage.getBounds().height < image.getBounds().height) {
                statusIdleImage = image;
            }
        }

        statusGruntImage = loadImage(shell.getDisplay(), "status-grunt.png");
    }

    private void setIcon(final Image icon) {
        Display display = Display.getDefault();
        display.syncExec(new Runnable() {

            public void run() {
                Shell shell = PlatformUI.getWorkbench().getWorkbenchWindows()[0].getShell();
                initializeImages(shell);
                shell.setImage(icon);
            }
        });
    }

    public void showStatusIdle() {
        setIcon(statusIdleImage);
    }

    public void showStatusGrunt() {
        setIcon(statusGruntImage);
    }

}
