
package org.eclipse.virgo.ide.runtime.core.ches;

import java.util.Set;

import org.eclipse.core.resources.IProject;

/**
 * Listener that is called when grunt is running.
 *
 * @author stefan.zugal
 *
 */
public interface IGruntListener {

    /**
     * Is invoked before grunt is started.
     *
     * @param projects the projects for which grunt is executed
     */
    void beforeRun(Set<IProject> projects);

    /**
     * Is invoked after grunt has finished.
     *
     * @param projects the projects for which grunt is executed
     */
    void afterRun(Set<IProject> projects);
}
