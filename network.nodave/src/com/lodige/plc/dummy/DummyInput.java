/**
 * network.nodave Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.plc.dummy;

import github.javaappplatform.commons.events.IInnerTalker;
import github.javaappplatform.commons.log.Logger;
import github.javaappplatform.platform.Platform;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.lodige.network.INetworkAPI;
import com.lodige.plc.IInput;
import com.lodige.plc.IPLC;
import com.lodige.plc.IPLCAPI;

/**
 * TODO javadoc
 * @author renken
 */
public abstract class DummyInput implements IInput, IPLCAPI
{
	
	private static final Logger LOGGER = Logger.getLogger();

	
	protected final String id;
	protected final Type type;
	protected final DummyPLC parent;
	
	private final ReentrantLock lock = new ReentrantLock();
	private final Condition waitForUpdate = this.lock.newCondition();
	private UpdateFrequency frequency = null;
	private boolean triggerUpdate = false;
	
	private Object value;
	private long lastUpdate = 0;


	/**
	 * 
	 */
	protected DummyInput(String id, Type type, DummyPLC parent)
	{
		this.id = id;
		this.type = type;
		this.parent = parent;
		this.parent.registerInput(this);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public String id()
	{
		return this.id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Type type()
	{
		return this.type;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IPLC plc()
	{
		return this.parent;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void delete()
	{
		this.parent.delete(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void resetUpdateMethod()
	{
		this.lock.lock();
		try
		{
			this.frequency = null;
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
	public void setUpdateMethod(UpdateFrequency frequency)
	{
		this.lock.lock();
		try
		{
			this.frequency = frequency;
		}
		finally
		{
			this.lock.unlock();
		}
	}
	
	private UpdateFrequency frequency()
	{
		return this.frequency != null ? this.frequency : this.parent.frequency();
	}

	boolean startUpdate()
	{
		this.lock.lock();
		try
		{
			return Platform.currentTime() - this.lastUpdate > this.frequency().schedule ||
				   this.triggerUpdate ||
				   this.value == null;
		}
		finally
		{
			this.lock.unlock();
		}
	}
	
	abstract Object computeUpdate();
	
	void update()
	{
		Object val = this.computeUpdate();
		
		boolean postEvent = false;
		this.lock.lock();
		try
		{
			Object old = this.value;
			this.value = val;
			this.lastUpdate = Platform.currentTime();
			this.triggerUpdate = false;
			this.waitForUpdate.signalAll();
			postEvent = this.type != Type.GENERIC && !this.value.equals(old) ||
						this.type == Type.GENERIC && !Arrays.equals((byte[]) old, (byte[]) this.value);
			LOGGER.debug("DummyInput {} updated.", this.id);
		}
		finally
		{
			this.lock.unlock();
		}
		if (postEvent)
		{
			LOGGER.info("Value of DummyInput {} changed.", this.id);
			((IInnerTalker) this.parent).postEvent(IPLCAPI.EVENT_INPUT_CHANGED, this);
		}
	}
	
	private void triggerInternalUpdate(boolean forceUpdate) throws IOException
	{
		this.lock.lock();
		try
		{
			if (this.value == null && !forceUpdate)
			{
				this.triggerUpdate = true;
				throw new IOException("No values available.");
			}
			if (forceUpdate || this.value == null)
			{
				this.triggerUpdate = true;
				if (!this.waitForUpdate.await(INetworkAPI.CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS))
					throw new IOException("Timeout: Did not get update for input " + this.id + " for NodavePLC " + this.parent.id() + " in time.");
			}
		}
		catch (InterruptedException e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			this.triggerUpdate = false;
			this.lock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public short shortValue(boolean forceUpdate) throws IOException
	{
		this.triggerInternalUpdate(forceUpdate);
		return ((Short) this.value).shortValue();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int intValue(boolean forceUpdate) throws IOException
	{
		this.triggerInternalUpdate(forceUpdate);
		return ((Integer) this.value).intValue();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public float floatValue(boolean forceUpdate) throws IOException
	{
		this.triggerInternalUpdate(forceUpdate);
		return ((Float) this.value).floatValue();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public short ubyteValue(boolean forceUpdate) throws IOException
	{
		return this.shortValue(forceUpdate);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int ushortValue(boolean forceUpdate) throws IOException
	{
		return this.intValue(forceUpdate);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long uintValue(boolean forceUpdate) throws IOException
	{
		this.triggerInternalUpdate(forceUpdate);
		return ((Long) this.value).longValue();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte[] genericValue(boolean forceUpdate) throws IOException
	{
		this.triggerInternalUpdate(forceUpdate);
		return (byte[]) this.value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		return this.id;
	}
}
