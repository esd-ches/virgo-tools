
package org.eclipse.virgo.ide.runtime.core.ches;

public abstract class AbstractRunnable implements Runnable {

    volatile protected boolean terminated = false;

    public void terminate() {
        terminated = true;
    }

    abstract public String getName();

    public boolean isTerminated() {
        return terminated;
    }

}
