
package org.eclipse.virgo.ide.runtime.core.ches;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;
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

    private TrayItem trayIcon;

    private Image statusIdleImage;

    private Image statusGruntImage;

    private GruntTrayIcon() {
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

        Thread thread = new Thread(new StandbyRunnable());
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Workaround for Windows 10: After sleep, duplicated icons are created. This runnable detects when the system was
     * in standby and restores the icons afterwards.
     *
     * @author stefan.zugal
     *
     */
    private class StandbyRunnable implements Runnable {

        @Override
        public void run() {
            long lastTimestamp = System.currentTimeMillis();
            while (true) {
                int sleepDuration = 5000;
                try {
                    Thread.sleep(sleepDuration);
                } catch (InterruptedException e) {
                    // ignore
                }

                // heuristic: if the difference is larger than 3 seconds, it is an indication that the system was
                // restarted
                long difference = System.currentTimeMillis() - lastTimestamp;
                if (difference > sleepDuration + 3000) {
                    Display.getDefault().syncExec(new Runnable() {

                        public void run() {
                            trayIcon.dispose();
                            Display display = Display.getDefault();
                            Tray tray = display.getSystemTray();
                            trayIcon = new TrayItem(tray, SWT.NONE);
                            trayIcon.setImage(statusIdleImage);
                        }
                    });
                }

                lastTimestamp = System.currentTimeMillis();
            }
        }
    }

    private void setIcon(final Image icon) {
        Display.getDefault().syncExec(new Runnable() {

            public void run() {
                trayIcon.setImage(icon);
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
