
package org.eclipse.virgo.ide.runtime.core.ches;

import java.io.IOException;
import java.net.URL;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;
import org.eclipse.virgo.ide.runtime.core.ServerCorePlugin;
import org.osgi.framework.Bundle;

/**
 * Listens to the status of the grunt task and shows visual feedback to the user.
 *
 * @author stefan.zugal
 *
 */
public class GruntStatusListener implements IGruntListener {

    private TrayItem trayIcon;

    private Image statusIdleImage;

    private Image statusGruntImage;

    public GruntStatusListener() {
        Display.getDefault().syncExec(new Runnable() {

            public void run() {
                Display display = Display.getDefault();
                Tray tray = display.getSystemTray();
                trayIcon = new TrayItem(tray, SWT.NONE);

                statusIdleImage = loadImage(display, "status-idle.png");
                statusGruntImage = loadImage(display, "status-grunt.png");
                trayIcon.setImage(statusIdleImage);
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
        Display.getDefault().syncExec(new Runnable() {

            public void run() {
                trayIcon.setImage(icon);
            }
        });
    }

    @Override
    public void beforeRun(Set<IProject> projects) {
        setIcon(statusGruntImage);
    }

    @Override
    public void afterRun(Set<IProject> projects) {
        setIcon(statusIdleImage);
    }

}
