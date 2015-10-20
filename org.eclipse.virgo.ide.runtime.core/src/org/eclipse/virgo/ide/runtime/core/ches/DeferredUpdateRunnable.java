
package org.eclipse.virgo.ide.runtime.core.ches;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class DeferredUpdateRunnable extends AbstractRunnable {

    private static final int MAX_ATTEMPTS = 60;

    private static final int TIMEOUT = 5000;

    private final Map<IUpdate, Integer> updateToAttempts;

    public DeferredUpdateRunnable() {
        this.updateToAttempts = new HashMap<>();
    }

    public void enqueueUpdate(IUpdate update) {
        updateToAttempts.put(update, 0);
    }

    @Override
    public void run() {
        while (!terminated) {
            for (IUpdate update : new HashSet<>(updateToAttempts.keySet())) {
                int attempts = updateToAttempts.get(update);

                if (attempts > MAX_ATTEMPTS) {
                    updateToAttempts.remove(update);
                } else {
                    if (update.isApplicable()) {
                        update.apply();
                        updateToAttempts.remove(update);
                    } else {
                        updateToAttempts.put(update, attempts + 1);
                    }
                }
            }

            try {
                Thread.sleep(TIMEOUT);
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }

    @Override
    public String getName() {
        return "CHES Deferred Update Runnable";
    }

}
