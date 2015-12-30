
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

    private Image statusIdleImage;

    private Image statusGruntImage;

    private GruntTrayIcon() {
        Display.getDefault().syncExec(new Runnable() {

            public void run() {
                Display display = Display.getDefault();

                Image[] images = display.getActiveShell().getImages();
                for (Image image : images) {
                    if (statusIdleImage == null || statusIdleImage.getBounds().height < image.getBounds().height) {
                        statusIdleImage = image;
                    }
                }

                statusGruntImage = loadImage(display, "status-grunt.png");
            }

            private Image loadImage(Display display, String imageName) {
                Bundle bundle = Platform.getBundle(ServerCorePlugin.PLUGIN_ID);
                URL entry = bundle.getEntry("icons/" + imageName);

                try {
                    return new Image(display, entry.openStream());
                } catch (IOException e) {
                    VirgoToolingHook.logError("Could not load image " + imageName, e);
                }

                return null;
            }
        });
    }

    private void setIcon(final Image icon) {
        Display display = Display.getDefault();
        display.syncExec(new Runnable() {

            public void run() {
                Shell shell = PlatformUI.getWorkbench().getWorkbenchWindows()[0].getShell();
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
