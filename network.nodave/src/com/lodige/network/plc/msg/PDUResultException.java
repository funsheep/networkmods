/**
 * network.nodave Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.network.plc.msg;

import com.lodige.network.plc.INodaveAPI.Result;
import com.lodige.network.plc.util.NodaveTools;

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
		super(NodaveTools.strerror(error));
		this.error = error;
	}

	public PDUResultException(Result result)
	{
		this(result.code);
	}

	
	public int error()
	{
		return this.error;
	}

	/**  */
	private static final long serialVersionUID = -2821623806688611512L;
}
