/**
 * icplatform Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.sys.states;

import github.javaappplatform.commons.events.ITalker;
import github.javaappplatform.commons.util.StringID;

/**
 * TODO javadoc
 * @author renken
 */
public interface ISystemState extends ITalker
{
	
	public enum State
	{
		STOPPED, RUNNING, ERROR, WARNING, INFO, UNKNOWN
	}

	public static final int EVENT_STATE_CHANGED = StringID.id("EVENT STATE CHANGED"); //$NON-NLS-1$
	public static final String EXTPOINT_SYSTEM_STATE = "com.lodige.states.SystemState"; //$NON-NLS-1$


	public String name();
	
	public State state();

}
