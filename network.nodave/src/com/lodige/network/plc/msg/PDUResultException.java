/**
 * network.nodave Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.network.plc.msg;

import com.lodige.network.plc.Nodave;

/**
 * TODO javadoc
 * @author renken
 */
public class PDUResultException extends Exception
{

	private final int error;

	/**
	 * 
	 */
	public PDUResultException(int error)
	{
		super(Nodave.strerror(error));
		this.error = error;
	}

	
	public int error()
	{
		return this.error;
	}

}
