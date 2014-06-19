package com.lodige.network.internal;

import java.io.Closeable;
import java.util.ArrayDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.lodige.network.INetworkAPI;
import com.lodige.network.msg.IMessage;

/**
 * TODO javadoc
 * @author funsheep
 */
class CloseableQueue implements Closeable
{

	private final int maxcapacity;
	private final ArrayDeque<IMessage> queue;
	private final ReentrantLock lock = new ReentrantLock();
	private final Condition isEmpty = this.lock.newCondition();
	private final Condition isFull = this.lock.newCondition();
	private volatile boolean closed = false;


	/**
	 * 
	 */
	public CloseableQueue(int maxcapacity)
	{
		this.maxcapacity = maxcapacity;
		this.queue = new ArrayDeque<>(this.maxcapacity);
	}

	
	public IMessage take()
	{
		return this.poll(0);
	}

	public IMessage poll()
	{
		return this.poll(INetworkAPI.CONNECTION_TIMEOUT);
	}
	
	private IMessage poll(int wait)
	{
		this.lock.lock();
		try
		{
			if (wait == 0 && this.queue.isEmpty())
				return null;
			
			while (this.queue.isEmpty())
				if (!this.isEmpty.await(wait, TimeUnit.MILLISECONDS))
					return null;
			
			IMessage msg = this.queue.removeFirst();
			this.isFull.signalAll();
			return msg;
		}
		catch (InterruptedException e)
		{
			return null;
		}
		finally
		{
			this.lock.unlock();
		}
	}

	public boolean put(IMessage msg) throws InterruptedException, IllegalStateException
	{
		this.lock.lock();
		try
		{
			while (this.queue.size() == this.maxcapacity && !this.isClosed())
			{
				if (!this.isFull.await(INetworkAPI.CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS))
					throw new InterruptedException("Timeout"); //$NON-NLS-1$
			}
			
			if (this.closed)
				throw new IllegalStateException("Queue is closed."); //$NON-NLS-1$

			this.queue.add(msg);
			this.isEmpty.signalAll();
			return this.queue.size() == 1;
		}
		finally
		{
			this.lock.unlock();
		}
	}
	
	void putVIP(IMessage msg)
	{
		this.lock.lock();
		try
		{
			this.queue.add(msg);
			this.isEmpty.signalAll();
		}
		finally
		{
			this.lock.unlock();
		}
	}

	public int size()
	{
		this.lock.lock();
		try
		{
			return this.queue.size();
		}
		finally
		{
			this.lock.unlock();
		}
	}

	public boolean isClosed()
	{
		this.lock.lock();
		try
		{
			return this.closed;
		}
		finally
		{
			this.lock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close()
	{
		this.lock.lock();
		try
		{
			this.isFull.signalAll();
			this.isEmpty.signalAll();
			this.closed = true;
		}
		finally
		{
			this.lock.unlock();
		}
	}
}
