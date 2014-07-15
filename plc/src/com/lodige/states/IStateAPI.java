/**
 * GGZ Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.states;

import com.lodige.plc.IPLCAPI;

/**
 * TODO javadoc
 * @author renken
 */
public interface IStateAPI
{

	public enum Type
	{
		BIT, UBYTE, SHORT, USHORT, INT, UINT, FLOAT, STRING, GENERIC, OBJECT
	}


	public interface IStateMappable
	{
		public boolean fits(int code);
	}


	public static final int EVENT_STATE_CHANGED = IPLCAPI.EVENT_INPUT_CHANGED;
}
