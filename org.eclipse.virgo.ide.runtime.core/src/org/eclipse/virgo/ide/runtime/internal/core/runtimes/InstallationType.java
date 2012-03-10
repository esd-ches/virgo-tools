/**
 * <copyright>
 *
 * TODO Copyright
 *
 * </copyright>
 *
 */
package org.eclipse.virgo.ide.runtime.internal.core.runtimes;

/**
 * A particular distribution of Virgo.
 * @author Miles Parker
 *
 */
public enum InstallationType {
	TOMCAT("Virgo Tomcat Server"), JETTY("Virgo Jetty Server"), KERNEL("Virgo Kernel"), NANO("Virgo Nano");
	
	private final String name;

	InstallationType(String name) {
		this.name = name;
    }
	
	public String getName() {
		return name;
	}
}
