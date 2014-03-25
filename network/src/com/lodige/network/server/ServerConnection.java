/**
 * network Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.network.server;

import java.net.InetAddress;
import java.net.Socket;

import com.lodige.network.INetworkAPI;
import com.lodige.network.internal.IInternalNetworkService;
import com.lodige.network.internal.ANetworkConnection;

/**
 * TODO javadoc
 * @author renken
 */
public class ServerConnection extends ANetworkConnection
{

	private final InetAddress remoteAddress;
	

	/**
	 * @param service
	 */
	public ServerConnection(IInternalNetworkService service, Socket socket)
	{
		super(service);
		this.remoteAddress = socket.getInetAddress();
		this.setSocket(socket);
		this.state.set(INetworkAPI.S_CONNECTED);
		this.register();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InetAddress address()
	{
		return this.remoteAddress;
	}

}
