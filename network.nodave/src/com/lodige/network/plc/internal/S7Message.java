/**
 * network.nodave Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.network.plc.internal;

import github.javaappplatform.commons.collection.SemiDynamicByteArray;
import github.javaappplatform.commons.events.IListener;

import com.lodige.network.internal.Message;

/**
 * Identifies PDU request messages.
 * PDU request messages need a special, protocol specific header, before the message payload.
 * Usually such a request is build, using the PDU*RequestBuilder classes.
 * @author renken
 */
public class S7Message extends Message
{

	private int headerSize;
	
	
	private S7Message(int headerSize)
	{
		this.headerSize = headerSize;
	}


	public int headerSize()
	{
		return this.headerSize;
	}



	public static S7Message create(int headerSize, byte[] body)
	{
		return create(headerSize, body, 0, body.length, null);
	}

	public static S7Message create(int headerSize, byte[] body, int off, int len, IListener callback)
	{
		final S7Message m = new S7Message(headerSize);
		m.set(-1, callback);
		m.set(body, off, len);
		return m;
	}

	public static S7Message create(int headerSize, SemiDynamicByteArray body, int len, IListener callback)
	{
		final S7Message m = new S7Message(headerSize);
		m.set(-1, callback);
		m.set(body, len);
		return m;
	}

}
