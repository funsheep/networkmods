/**
 * network Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.network.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import com.lodige.network.IProtocol.Stateful;

/**
 * TODO javadoc
 * @author renken
 */
public abstract class AStatefulProtocol implements Stateful
{

//	private Socket socket;
	protected InputStream in;
	protected OutputStream out;
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onConnect(Socket socket) throws IOException
	{
//		this.socket = socket;
		this.in = socket.getInputStream();
		this.out = socket.getOutputStream();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onDisconnect()
	{
//		this.socket = null;
		this.in = null;
		this.out = null;
	}

}
