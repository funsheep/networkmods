/**
 * network.nodave Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.network.plc;

/**
 * TODO javadoc
 * @author renken
 */
public interface INoDave
{

	public static final int TCP_START_IN  = 7;
//	public static final int TCP_START_OUT = 3;
	/**
	 * known function codes
	 */
	public final static byte FUNC_READ = 4;
	public final static byte FUNC_WRITE = 5;

}
