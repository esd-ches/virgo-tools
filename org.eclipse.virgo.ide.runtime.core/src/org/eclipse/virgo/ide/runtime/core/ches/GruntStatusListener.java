
package org.eclipse.virgo.ide.runtime.core.ches;

import java.util.Set;

import org.eclipse.core.resources.IProject;

/**
 * Listens to the status of the grunt task and shows visual feedback to the user.
 *
 * @author stefan.zugal
 *
 */
public class GruntStatusListener implements IGruntListener {

    @Override
    public void beforeRun(Set<IProject> projects) {
        GruntTrayIcon.getInstance().showStatusGrunt();
    }

    @Override
    public void afterRun(Set<IProject> projects) {
        GruntTrayIcon.getInstance().showStatusIdle();
    }

}
