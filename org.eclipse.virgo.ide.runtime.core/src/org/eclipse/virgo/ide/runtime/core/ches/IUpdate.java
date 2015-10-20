package org.eclipse.virgo.ide.runtime.core.ches;

public interface IUpdate {

	/**
	 * Conduct the update.
	 */
	void apply();

	/**
	 * Return the timestamp when this update was created.
	 *
	 * @return
	 */
	long getTimestamp();

	/**
	 * Compute whether the update can be performed.
	 *
	 * @return
	 */
	boolean isApplicable();

}
