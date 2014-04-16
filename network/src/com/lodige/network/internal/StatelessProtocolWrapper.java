/**
 * network Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.network.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import com.lodige.network.IProtocol.Stateless;
import com.lodige.network.msg.IMessage;

/**
 * TODO javadoc
 * @author renken
 */
public class StatelessProtocolWrapper implements Stateless
{


	private final Stateful wrapped;


	public StatelessProtocolWrapper(Stateful toWrap)
	{
		this.wrapped = toWrap;
	}
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onConnect(Socket socket) throws IOException
	{
		this.wrapped.onConnect(socket);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void send(IMessage msg, OutputStream out) throws IOException
	{
		this.wrapped.send(msg);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IMessage read(InputStream in) throws IOException
	{
		return this.wrapped.read();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onDisconnect(Socket socket)
	{
		this.wrapped.onDisconnect();
	}

}
