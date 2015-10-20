
package org.eclipse.virgo.ide.runtime.core.ches;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.WatchService;

public abstract class AbstractWatchServiceRunnable extends AbstractRunnable {

    protected WatchService watchService;

    @Override
    final public void run() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            runInternal();
        } catch (ClosedWatchServiceException e) {
            if (terminated) {
                // ignore - exception is expected
            } else {
                VirgoToolingHook.logError(getName() + "was unexpectedly closed.", e);
            }
        } catch (Exception e) {
            VirgoToolingHook.logError("An unexpected error occurred.", e);
        } finally {
            closeWatchService();
        }
    }

    abstract protected void runInternal() throws Exception;

    @Override
    public void terminate() {
        super.terminate();
        closeWatchService();
    }

    private void closeWatchService() {
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

}
