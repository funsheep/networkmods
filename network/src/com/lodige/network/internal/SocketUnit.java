/*
	This file is part of the d3fact network library.
	Copyright (C) 2007-2012 d3fact Project Team

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package com.lodige.network.internal;

import github.javaappplatform.commons.log.Logger;
import github.javaappplatform.commons.util.Close;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import com.lodige.network.INetworkAPI;

/**
 * TODO javadoc
 * @author funsheep
 */
public class SocketUnit extends Thread
{

	private static final Logger LOGGER = Logger.getLogger();

	private final int localPort;
	private ServerNetworkService serverNetworkService;
	private ServerSocket socket;


	public SocketUnit(int localPort)
	{
		super("Socketunit"); //$NON-NLS-1$
		this.localPort = localPort;
		this.setDaemon(true);
	}

	
	public InetAddress localAddress()
	{
		if (this.socket == null || !this.socket.isBound())
			return null;
		return this.socket.getInetAddress();
	}
	

	public void start(ServerNetworkService serverNetworkService) throws IOException
	{
		if (!this.isShutdown())
			return;

		this.serverNetworkService = serverNetworkService;
		this.socket = new ServerSocket();
		this.socket.bind(new InetSocketAddress(SocketUnit.this.localPort), INetworkAPI.MAX_SERVER_CONNECTION_QUEUE);
		this.start();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run()
	{
		try
		{
			while (!SocketUnit.this.isShutdown())
			{
				assert LOGGER.info("Accepting connections on {}", SocketUnit.this.socket); //$NON-NLS-1$
				SocketUnit.this.socketAccepted(SocketUnit.this.socket.accept());
			}
		}
		catch (Exception e)
		{
			if (!SocketUnit.this.isShutdown())
			{
				LOGGER.info("Socketunit shutdown on error.", e); //$NON-NLS-1$
				SocketUnit.this.shutdown();
			}
		}
		LOGGER.debug("Socketunit no longer accepting connections."); //$NON-NLS-1$
		this.serverNetworkService.shutdown();
	}


	private void socketAccepted(Socket clientSocket)
	{
		try
		{
			//error we are no longer running, send error and close socket
			if (!this.isShutdown())
			{
				InternalNetTools.configureSocket(clientSocket);
				LOGGER.info("Client {} connected.", clientSocket.getInetAddress()); //$NON-NLS-1$

				this.serverNetworkService.register(clientSocket);
				return;
			}
		}
		catch (IOException ioe)
		{
			LOGGER.warn("Accepting a client socket threw an exception.", ioe); //$NON-NLS-1$
		}
		finally
		{
			Close.close(clientSocket);
		}
	}
	

	public boolean isShutdown()
	{
		return this.isInterrupted() || this.socket == null || this.socket.isClosed();
	}

	public void shutdown()
	{
		if (this.isShutdown())
			return;

		this.interrupt();
		Close.close(this.socket);
	}

}
