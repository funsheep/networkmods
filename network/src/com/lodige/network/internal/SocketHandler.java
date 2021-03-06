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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TODO javadoc
 * @author funsheep
 */
class SocketHandler
{

	protected static final Logger LOGGER = Logger.getLogger();


	private final Thread _receiver = new Thread()
	{

		{
			this.setDaemon(true);
		}

		@Override
		public void run()
		{
			try (InputStream in = SocketHandler.this.socket.getInputStream())
			{
				while (SocketHandler.this.isConnected())
				{
					try 
					{
						final Message msg = (Message) SocketHandler.this.con._protocol().read(in);
						if (msg == null)	//we got an EOF
							break;

						assert LOGGER.trace("Received {}", msg); //$NON-NLS-1$
						SocketHandler.this.con._put(msg);
					}
					catch (final IOException e)
					{
						//ignore - maybe we simply have a timeout because we didn't read something in a while
					}
				}
			}
			catch (final Exception ex)
			{
				LOGGER.severe("TCP MSG Receiver {} shutdown.", this.getName(), ex); //$NON-NLS-1$
			}
			SocketHandler.this.shutdown();
		}
	};


	private final Thread _sender = new Thread()
	{
		{
			this.setDaemon(true);
		}

		@Override
		public void run()
		{
			try (OutputStream out = SocketHandler.this.socket.getOutputStream())
			{
				while (SocketHandler.this.isConnected())
				{
					final Message msg = SocketHandler.this.con._take();
					if (msg != null)
					{
						if (msg.type() != IInternalNetworkConnection.T_CLOSED_SEND_QUEUE)
						{
							assert LOGGER.trace("Send {}", msg); //$NON-NLS-1$
							SocketHandler.this.con._protocol().send(msg, out);
						}
						SocketHandler.this.con._msgSend(msg);
						msg.dispose();
					}
				}
			}
			catch (Exception ex)
			{
				LOGGER.severe("TCP MSG Sender {} shutdown.", this.getName(), ex); //$NON-NLS-1$
			}
			SocketHandler.this.shutdown();
		}
	};


	private final IInternalNetworkConnection con;
	private final Socket socket;
	private final AtomicBoolean isShutdown = new AtomicBoolean(false);


	/**
	 *
	 */
	public SocketHandler(Socket socket, IInternalNetworkConnection con) throws IOException
	{
		this.con = con;
		this.socket = socket;
		if (!this.isConnected())
			return;
		this.con._protocol().onConnect(this.socket);

		this._receiver.setName("TCPReceiver for: " + this.socket.getLocalAddress()); //$NON-NLS-1$
		this._sender.setName("TCPSender for: " + this.socket.getLocalAddress()); //$NON-NLS-1$
		this._receiver.start();
		this._sender.start();
	}


	public boolean isConnected()
	{
		return this.socket.isConnected() && !this.socket.isClosed();
	}


	void shutdown()
	{
		if (this.isShutdown.compareAndSet(false, true))
		{
			try
			{
				this.con._protocol().onDisconnect(this.socket);
			}
			catch (Exception e)
			{
				LOGGER.debug("Could not correctly shutdown sockethandler.", e);
			}
			this._receiver.interrupt();
			this._sender.interrupt();
			Close.close(this.socket);
			this.con.shutdown();
		}
	}

}
