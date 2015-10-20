
package org.eclipse.virgo.ide.runtime.core.ches;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class UpdateRunnable extends AbstractRunnable {

    private class TerminationUpdate implements IUpdate {

        @Override
        public void apply() {
            // do nothing
        }

        @Override
        public long getTimestamp() {
            return 0;
        }

        @Override
        public boolean isApplicable() {
            return true;
        }

    }

    private final BlockingQueue<IUpdate> scheduledUpdates;

    private final List<IUpdate> allUpdates;

    private final DeferredUpdateRunnable deferredUpdateRunnable;

    private final Thread deferredUpdateThread;

    public UpdateRunnable() {
        this.scheduledUpdates = new LinkedBlockingQueue<>();
        this.allUpdates = new LinkedList<>();

        this.deferredUpdateRunnable = new DeferredUpdateRunnable();
        deferredUpdateThread = new Thread(deferredUpdateRunnable, deferredUpdateRunnable.getName());
        deferredUpdateThread.start();
    }

    public void enqueueUpdate(IUpdate update) {
        scheduledUpdates.add(update);
        allUpdates.add(update);

        // heuristic: keep at most 1000 updates
        if (allUpdates.size() > 1000) {
            allUpdates.remove(0);
        }
    }

    public void replayUpdates() {
        long approximateMaximumDurationForVirgoStart = 5 * 60 * 1000;

        for (IUpdate update : allUpdates) {
            if (System.currentTimeMillis() - update.getTimestamp() < approximateMaximumDurationForVirgoStart) {
                if (update.isApplicable()) {
                    update.apply();
                }
            }
        }
    }

    @Override
    public void run() {
        try {
            while (!terminated) {
                try {
                    IUpdate update = scheduledUpdates.take();
                    if (update instanceof TerminationUpdate) {
                        break;
                    }
                    if (update.isApplicable()) {
                        update.apply();
                    } else {
                        // virgo may not be started yet, defer the update
                        deferredUpdateRunnable.enqueueUpdate(update);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            try {
                deferredUpdateRunnable.terminate();
                deferredUpdateThread.join();
            } catch (InterruptedException e) {
                // ignore
            }
        }

    }

    @Override
    public String getName() {
        return "CHES Update Runnable";
    }

    @Override
    public void terminate() {
        super.terminate();
        if (scheduledUpdates != null) {
            scheduledUpdates.add(new TerminationUpdate());
        }
    }

}
