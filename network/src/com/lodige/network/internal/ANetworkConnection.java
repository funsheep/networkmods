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
import github.javaappplatform.commons.util.Close;
import github.javaappplatform.platform.job.ADoJob;
import github.javaappplatform.platform.job.JobPlatform;
import github.javaappplatform.platform.job.JobbedTalkerStub;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.lodige.network.INetworkAPI;
import com.lodige.network.IProtocol;
import com.lodige.network.msg.IMessage;

/**
 * @author funsheep
 */
public abstract class ANetworkConnection extends JobbedTalkerStub implements IInternalNetworkConnection
{
	
	private class DispatchJob extends ADoJob
	{
		private boolean hasFinished = true;
		private int counter = 0;

		
		public DispatchJob()
		{
			super("Message Dispatcher");
		}
	
		@Override
		public synchronized void schedule(String thread, boolean loop, long delay)
		{
			this.hasFinished = false;
			super.schedule(thread, loop, delay);
		}
	
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void doJob()
		{
			ANetworkConnection.this.postEvent(INetworkAPI.E_MSG_RECEIVED, ANetworkConnection.this.receiveQueue.take());
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public long absoluteProgress()
		{
			return this.counter;
		}

		public synchronized boolean isRunning()
		{
			return !this.hasFinished;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public synchronized boolean isfinished()
		{
			boolean isFinished = this.shutdown || ANetworkConnection.this.receiveQueue.isClosed() || ANetworkConnection.this.receiveQueue.size() == 0;
			if (isFinished)
				this.hasFinished = true;
			return isFinished;
		}

	}


	protected static final Logger LOGGER = Logger.getLogger();
	private static final AtomicLong SENDIDS = new AtomicLong(0);


	private Socket socket;
	private SocketHandler handler;
	private final IInternalNetworkService service;
	private final IProtocol protocol;
	private final CloseableQueue sendQueue = new CloseableQueue(INetworkAPI.MAX_MESSAGE_COUNTER);
	private final CloseableQueue receiveQueue = new CloseableQueue(INetworkAPI.MAX_MESSAGE_COUNTER);
	private final DispatchJob dispatcher = new DispatchJob();


	protected final AtomicInteger state = new AtomicInteger(INetworkAPI.S_NOT_CONNECTED);
	private String alias;


	public ANetworkConnection(IInternalNetworkService service)
	{
		super(INetworkAPI.NETWORK_THREAD);
		this.service = service;
		this.protocol = this.service._getProtocol();
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
		this.socket = socket;
		this.handler = new SocketHandler(this);
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
		if (this.state.get() == INetworkAPI.S_SHUTDOWN)
			throw new IllegalStateException("Connection is shutdown.");
		Message m = (Message) msg;
		long sendID = SENDIDS.getAndIncrement();
		m.setSendID(sendID);
		try
		{
			this.sendQueue.put(m);
		}
		catch (IllegalStateException | InterruptedException e)
		{
			throw new IOException("Could not send message", e);
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
			this.receiveQueue.close();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Socket _socket()
	{
		return this.socket;
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
		try
		{
			if (this.receiveQueue.put(msg) && !this.dispatcher.isRunning())
				this.dispatcher.schedule(INetworkAPI.NETWORK_THREAD, false, 0);
		}
		catch (InterruptedException e)
		{
			LOGGER.warn("Dropping received message: {}", msg);
		}
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
				ANetworkConnection.this.sendQueue.putVIP(Message.create(IInternalNetworkConnection.T_CLOSED_SEND_QUEUE, null));
				ANetworkConnection.this.sendQueue.close();
			}
		}, INetworkAPI.NETWORK_THREAD);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void shutdown()
	{
		if (this.state.getAndSet(INetworkAPI.S_SHUTDOWN) == INetworkAPI.S_SHUTDOWN)
			return;
		
		this.handler.shutdown();
		Close.close(this.socket);
		this.postEvent(INetworkAPI.E_STATE_CHANGED);
		this.service._unregister(this);
	}

}
