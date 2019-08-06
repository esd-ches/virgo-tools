
package org.eclipse.virgo.ide.runtime.core.ches;

import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.wst.server.core.IModule;

/**
 * Listener that allows clients to listen to the deployment events, such as deploying and undeploying.
 *
 * @author stefan.zugal
 *
 */
public interface IDeployListener {

    /**
     * Called before a module is deployed.
     *
     * @param module the module to be deployed (i.e., the plan)
     * @param projects the projects associated with the module to be deployed
     */
    void beforeDeploy(IModule module, Set<IProject> projects);

    /**
     * Called after a module is deployed.
     *
     * @param module the module to be deployed (i.e., the plan)
     * @param projects the projects associated with the module to be deployed
     */
    void afterDeploy(IModule module, Set<IProject> projects);

    /**
     * Called before the virgo is stopped.
     */
    void beforeStop();

    /**
     * Called before a module is undeployed.
     *
     * @param module the module to be undeployed (i.e., the plan)
     * @param projects the projects associated with the module to be deployed
     */
    void beforeUndeploy(IModule module, Set<IProject> projects);

}
