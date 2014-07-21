/**
 * network.nodave Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.network.s7.plc.util;

import com.lodige.network.s7.protocol.INodaveAPI.Area;

/**
 * TODO javadoc
 * @author renken
 */
public class PLCTools
{

	public static final String dbUID(int database, int offset)
	{
		return uID(Area.DB, database, offset);
	}

	public static final String uID(Area area, int database, int offset)
	{
		return area.name() + database + ":" + offset;
	}


	/**
	 * 
	 */
	private PLCTools()
	{
		//no instance
	}

}
