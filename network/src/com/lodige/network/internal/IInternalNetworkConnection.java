/**
 * network Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.network.internal;

import java.net.Socket;

import com.lodige.network.INetworkConnection;
import com.lodige.network.IProtocol;

/**
 * TODO javadoc
 * @author renken
 */
public interface IInternalNetworkConnection extends INetworkConnection
{
	
	public static final int T_CLOSED_SEND_QUEUE = Integer.MIN_VALUE;

	public Socket _socket();

	public IProtocol.Stateless _protocol();


	public void _put(Message msg);


	public Message _take();
	
	public void _msgSend(Message msg);

}
