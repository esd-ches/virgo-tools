
package org.eclipse.virgo.ide.runtime.core.ches;

import org.eclipse.wst.server.core.IModule;

public interface IVirgoToolingHook {

    public void beforeStop();

    public void beforeDeploy(IModule module);

    public void afterDeploy(IModule module);

    public void beforeUndeploy(IModule module);

}
