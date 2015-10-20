/*******************************************************************************
 * Copyright (c) 2015 GianMaria Romanato
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     GianMaria Romanato - initial API and implementation
 *******************************************************************************/

package org.eclipse.virgo.ide.runtime.internal.core.command;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.eclipse.core.resources.IProject;
import org.eclipse.osgi.util.ManifestElement;
import org.eclipse.virgo.ide.facet.core.FacetCorePlugin;
import org.eclipse.virgo.ide.runtime.core.IServerBehaviour;
import org.eclipse.virgo.ide.runtime.internal.core.DeploymentIdentity;
import org.eclipse.wst.server.core.IModule;
import org.osgi.framework.Constants;

/**
 * An {@link IServerCommand} for Virgo 3.5+ to check whether a given bundle is already deployed or not. Created to fix
 * #454015.
 *
 * @author GianMaria Romanato
 * @since 1.0.2
 */
class JmxServerCheckBundleDeployedCommand extends AbstractJmxServerCommand implements IServerCommand<DeploymentIdentity> {

    private static final String META_INF_MANIFEST_MF = "META-INF/MANIFEST.MF"; //$NON-NLS-1$

    private static final String VERSION_ATTRIBUTE = "Version"; //$NON-NLS-1$

    private static final String STATE_ATTRIBUTE = "State"; //$NON-NLS-1$

    private static final String STATE_ATTRIBUTE_ACTIVE = "ACTIVE"; //$NON-NLS-1$

    private static final String START_OPERATION = "start"; //$NON-NLS-1$

    private static final String[] START_OPERATION_SIGNATURE = new String[0];

    private static final Object[] START_OPERATION_PARAMETERS = new Object[0];

    private static final String OBJECT_NAME = "org.eclipse.virgo.kernel:type=ArtifactModel,artifact-type=bundle,version=*,region=org.eclipse.virgo.region.user,name="; //$NON-NLS-1$

    private final IModule module;

    /**
     * Creates a new {@link JmxServerCheckBundleDeployedCommand}.
     */
    JmxServerCheckBundleDeployedCommand(IServerBehaviour serverBehaviour, IModule module) {
        super(serverBehaviour);
        this.module = module;
    }

    /**
     * {@inheritDoc}
     */
    public DeploymentIdentity execute() throws IOException, TimeoutException {
        // if and only if the artifact is a module
        if (isBundle()) {
            // and we could determine its version number
            // enquiry the artifact repository for a MBean named after the bundle
            JmxServerCommandTemplate tpl = new JmxServerCommandTemplate() {

                public Object invokeOperation(MBeanServerConnection connection) throws Exception {
                    return checkIfBundleAlreadyDeployed(connection);
                }
            };
            // and return true if it was found
            return (DeploymentIdentity) execute(tpl);
        }

        // if not a bundle (plan) or the manifest could not be parsed
        // return null, the IDE tool will then assume the bundle was not already
        // deployed and this will make it behave the same way it did before
        return null;
    }

    protected DeploymentIdentity checkIfBundleAlreadyDeployed(MBeanServerConnection connection) throws MalformedObjectNameException {
        ObjectName name = javax.management.ObjectName.getInstance(OBJECT_NAME + this.module.getName());
        Set<ObjectName> candidates;
        try {
            candidates = connection.queryNames(name, null);
        } catch (Exception e1) {
            // silently ignore, this is a best effort tentative
            return null;
        }

        if (!candidates.isEmpty()) {
            final String localVersion = discoverBundleVersion();
            for (ObjectName objectName : candidates) {
                try {
                    AttributeList attributes = connection.getAttributes(objectName, new String[] { VERSION_ATTRIBUTE, STATE_ATTRIBUTE });
                    Attribute version = (Attribute) attributes.get(0);
                    Attribute state = (Attribute) attributes.get(1);

                    if (localVersion.equals(version.getValue())) {

                        if (!STATE_ATTRIBUTE_ACTIVE.equals(state.getValue())) {
                            // if bundle is not active, activate it, because this
                            // is the expected behaviour for a bundle that is added
                            // to a Virgo server instance in Eclipse
                            connection.invoke(objectName, START_OPERATION, START_OPERATION_PARAMETERS, START_OPERATION_SIGNATURE);
                        }
                        return new DeploymentIdentity(this.module.getName(), localVersion);
                    }
                } catch (Exception e) {
                    // silently ignore, this is a best effort tentative
                }
            }
        }
        return null;
    }

    /**
     * Parse the bundle manifest to extract the version information. Note that the manifest is taken from the deployment
     * folder (stage) as opposed to the {@link IProject} because the {@link IProject} may contain changes not yet
     * published.
     *
     * @return the version as a string or <code>null</code> if the manifest could not be found or could not be parsed
     */
    protected String discoverBundleVersion() {
        InputStream is = null;
        Map<String, String> manifest = Collections.<String, String> emptyMap();
        try {
            URI manifestURI = AbstractJmxServerDeployerCommand.getUri(
                this.serverBehaviour.getModuleDeployUri(this.module).append(META_INF_MANIFEST_MF));
            is = manifestURI.toURL().openStream();
            manifest = ManifestElement.parseBundleManifest(is, null);
        } catch (Exception e) {
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }

        return manifest.get(Constants.BUNDLE_VERSION);
    }

    protected boolean isBundle() {
        return this.module.getModuleType().getId().equals(FacetCorePlugin.BUNDLE_FACET_ID);
    }

}
