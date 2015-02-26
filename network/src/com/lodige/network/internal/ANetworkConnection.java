/*
	This file is part of the d3fact network library.
	Copyright (C) 2007-2012 d3fact Project Team

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package com.lodige.network.internal;

import github.javaappplatform.commons.events.Event;
import github.javaappplatform.commons.log.Logger;
import github.javaappplatform.platform.job.ADoJob;
import github.javaappplatform.platform.job.JobPlatform;
import github.javaappplatform.platform.job.JobbedTalkerStub;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.lodige.network.INetworkAPI;
import com.lodige.network.IProtocol;
import com.lodige.network.msg.IMessage;

/**
 * @author funsheep
 */
public abstract class ANetworkConnection extends JobbedTalkerStub implements IInternalNetworkConnection
{
	
	private class DispatchMSG extends ADoJob
	{
		private final IMessage msg;
		

		public DispatchMSG(IMessage msg)
		{
			super("Dispatch " + msg); //$NON-NLS-1$
			this.msg = msg;
		}
	
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void doJob()
		{
			ANetworkConnection.this.postEvent(INetworkAPI.E_MSG_RECEIVED, this.msg);
			super.shutdown();
		}
		
	}


	protected static final Logger LOGGER = Logger.getLogger();


	private SocketHandler handler;
	private final IInternalNetworkService service;
	private IProtocol protocol;
	private CloseableQueue sendQueue;
	private final AtomicBoolean receiveClosed = new AtomicBoolean(false);


	protected final AtomicInteger state = new AtomicInteger(INetworkAPI.S_NOT_CONNECTED);
	private String alias;


	public ANetworkConnection(IInternalNetworkService service)
	{
		super(INetworkAPI.NETWORK_THREAD);
		this.service = service;
	}
	
	protected void register()
	{
		this.service._register(this);
	}

	
	protected void setAlias(String alias)
	{
		if (this.alias != null)
			throw new IllegalStateException();
		this.alias = alias;
	}

	protected void setSocket(Socket socket) throws IOException
	{
		this.sendQueue = new CloseableQueue(INetworkAPI.MAX_MESSAGE_COUNTER);
		this.protocol = this.loadProtocol();
		this.handler = new SocketHandler(socket, this);
		this.receiveClosed.set(false);
	}
	
	protected IProtocol loadProtocol()
	{
		return this.service._getProtocol();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public String alias()
	{
		return this.alias;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int state()
	{
		return this.state.get();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public long asyncSend(IMessage msg) throws IOException
	{
		if (this.state.get() != INetworkAPI.S_CONNECTED)
			throw new IllegalStateException("Connection is shutdown."); //$NON-NLS-1$
		Message m = (Message) msg;
		long sendID = this.protocol.nextSendID();
		m.setSendID(sendID);
		try
		{
			this.sendQueue.put(m);
		}
		catch (IllegalStateException | InterruptedException e)
		{
			throw new IOException("Could not send message", e); //$NON-NLS-1$
		}
		return sendID;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void _msgSend(final Message msg)
	{
		if (msg.callback() != null)
		{
			JobPlatform.runJob(new Runnable()
			{
				
				@Override
				public void run()
				{
					msg.callback().handleEvent(new Event(ANetworkConnection.this, INetworkAPI.E_MSG_SEND, Long.valueOf(msg.sendID())));
				}
			}, INetworkAPI.NETWORK_THREAD);
		}
		else if (msg.type() == IInternalNetworkConnection.T_CLOSED_SEND_QUEUE)
		{
			this.receiveClosed.set(true);
			this._shutdown();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IProtocol _protocol()
	{
		return this.protocol;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Message _take()
	{
		return (Message) this.sendQueue.take();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void _put(Message msg)
	{
		if (!this.receiveClosed.get())
			JobPlatform.runJob(new DispatchMSG(msg), INetworkAPI.NETWORK_THREAD);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws IOException
	{
		if (!this.state.compareAndSet(INetworkAPI.S_CONNECTED, INetworkAPI.S_CLOSING))
			return;
		this.postEvent(INetworkAPI.E_STATE_CHANGED);
		
		JobPlatform.runJob(new Runnable()
		{
			
			@Override
			public void run()
			{
				ANetworkConnection.this.sendQueue.putVIP(Message.create(IInternalNetworkConnection.T_CLOSED_SEND_QUEUE, new byte[0]));
				ANetworkConnection.this.sendQueue.close();
			}
		}, INetworkAPI.NETWORK_THREAD);
	}

	private void _shutdown()
	{
		JobPlatform.runJob(new Runnable()
		{
			
			@Override
			public void run()
			{
				if (ANetworkConnection.this.state.getAndSet(INetworkAPI.S_NOT_CONNECTED) == INetworkAPI.S_NOT_CONNECTED)
					return;
				
				ANetworkConnection.this.handler.shutdown();
				ANetworkConnection.this.handler = null;
				ANetworkConnection.this.postEvent(INetworkAPI.E_STATE_CHANGED);
			}
		}, INetworkAPI.NETWORK_THREAD);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void shutdown()
	{
		this._shutdown();
		this.service._unregister(this);
	}

}
