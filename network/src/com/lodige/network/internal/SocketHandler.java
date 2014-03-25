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
			try (InputStream in = SocketHandler.this.con._socket().getInputStream();)
			{
				while (SocketHandler.this.isConnected())
				{
					try 
					{
						final Message msg = (Message) con._protocol().read(in);
						if (msg != null)
						{
							assert LOGGER.trace("Received {}", msg);
							SocketHandler.this.con._put(msg);
						}
					}
					catch (final IOException e)
					{
						//ignore - maybe we simply have a timeout because we didn't read something in a while
					}
				}
			}
			catch (final IOException ex)
			{
				LOGGER.debug("TCP MSG Handler shutdown.", ex);
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
			try (OutputStream out = SocketHandler.this.con._socket().getOutputStream();)
			{
				while (SocketHandler.this.isConnected())
				{
					final Message msg = (Message) SocketHandler.this.con._take();
					if (msg != null)
					{
						if (msg.type() != IInternalNetworkConnection.T_CLOSED_SEND_QUEUE)
						{
							assert LOGGER.trace("Send {}", msg);
							con._protocol().send(msg, out);
						}
						SocketHandler.this.con._msgSend(msg);
						msg.dispose();
					}
				}
			}
			catch (IOException ex)
			{
				LOGGER.debug("TCP MSG Handler shutdown.", ex);
			}
			SocketHandler.this.shutdown();
		}
	};


	private final IInternalNetworkConnection con;
	private final AtomicBoolean isShutdown = new AtomicBoolean(false);


	/**
	 *
	 */
	public SocketHandler(IInternalNetworkConnection con)
	{
		this.con = con;
		
		if (!this.isConnected())
			return;

		this._receiver.setName("TCPReceiver for: " + this.con._socket().getLocalAddress());
		this._sender.setName("TCPSender for: " + this.con._socket().getLocalAddress());
		this._receiver.start();
		this._sender.start();
	}


	public boolean isConnected()
	{
		return this.con._socket().isConnected() && !this.con._socket().isClosed();
	}


	void shutdown()
	{
		if (this.isShutdown.compareAndSet(false, true))
		{
			Close.close(this.con._socket());
			this._receiver.interrupt();
			this._sender.interrupt();
			this.con.shutdown();
		}
	}

}
