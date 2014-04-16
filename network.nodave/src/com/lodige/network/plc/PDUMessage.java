/**
 * network.nodave Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.network.plc;

import github.javaappplatform.commons.collection.SemiDynamicByteArray;
import github.javaappplatform.commons.events.IListener;

import com.lodige.network.internal.Message;

/**
 * Identifies PDU request messages.
 * PDU request messages need a special, protocol specific header, before the message payload.
 * Usually such a request is build, using the PDU*RequestBuilder classes.
 * @author renken
 */
class PDUMessage extends Message
{

	public static PDUMessage create(byte[] body)
	{
		return create(body, 0, body.length, null);
	}

	public static PDUMessage create(byte[] body, int off, int len, IListener callback)
	{
		final PDUMessage m = new PDUMessage();
		m.set(-1, callback);
		m.set(body, off, len);
		return m;
	}

	public static PDUMessage create(SemiDynamicByteArray body, int len, IListener callback)
	{
		final PDUMessage m = new PDUMessage();
		m.set(-1, callback);
		m.set(body, len);
		return m;
	}

}
