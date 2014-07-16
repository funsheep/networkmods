/**
 * network.nodave Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.plc.nodave;

import github.javaappplatform.commons.events.IInnerTalker;
import github.javaappplatform.commons.log.Logger;
import github.javaappplatform.platform.Platform;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.lodige.network.INetworkAPI;
import com.lodige.network.plc.INodaveAPI.Area;
import com.lodige.plc.IInput;
import com.lodige.plc.IPLC;
import com.lodige.plc.IPLCAPI;

/**
 * TODO javadoc
 * @author renken
 */
class Input implements IInput, IPLCAPI
{
	
	private static final Logger LOGGER = Logger.getLogger();

	
	protected final String id;
	protected final Area area;
	protected final int database;
	protected final int offset;
	protected final int length;
	protected final Type type;
	protected final NodavePLC parent;
	
	private final ReentrantLock lock = new ReentrantLock();
	private final Condition waitForUpdate = this.lock.newCondition();
	private UpdateFrequency frequency = null;
	private boolean triggerUpdate = false;
	
	private Object value;
	private long lastUpdate = 0;


	/**
	 * 
	 */
	protected Input(String id, Area area, int database, int offset, int length, Type type, NodavePLC parent)
	{
		this.id = id;
		this.area = area;
		this.database = database;
		this.offset = offset;
		this.length = length;
		this.type = type;
		this.parent = parent;
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
			return Platform.currentTime() - this.lastUpdate > this.frequency().schedule || this.triggerUpdate;
		}
		finally
		{
			this.lock.unlock();
		}
	}
	
	void updateNoValue()
	{
		this.setValue(null);
	}
	
	void update(byte[] data, int offset)
	{
		Object val = null;
		switch (this.type)
		{
			case GENERIC:
				val = data;
				break;
			case SHORT:
				final short s = (short) (((data[0] & 0xFF) << 8) | (data[1] & 0xFF));
				val = Short.valueOf(s);
				break;
			case INT:
				final int i = ByteBuffer.wrap(data).getInt();
				val = Integer.valueOf(i);
				break;
			case FLOAT:
				final float f = ByteBuffer.wrap(data).getFloat();
				val = Float.valueOf(f);
				break;
			case UBYTE:
				final short ub = (short) (data[0] & 0xFF);
				val = Short.valueOf(ub);
				break;
			case USHORT:
				final int us = Short.toUnsignedInt((short) (((data[0] & 0xFF) << 8) | (data[1] & 0xFF)));
				val = Integer.valueOf(us);
				break;
			case UINT:
				final long ui = Integer.toUnsignedLong(ByteBuffer.wrap(data).getInt());
				val = Long.valueOf(ui);
				break;
			default:
				throw new RuntimeException("Should not happen."); //$NON-NLS-1$
		}
		this.setValue(val);
	}
	
	private void setValue(Object newValue)
	{
		boolean postEvent = false;
		this.lock.lock();
		try
		{
			Object old = this.value;
			this.value = newValue;
			this.lastUpdate = Platform.currentTime();
			this.triggerUpdate = false;
			this.waitForUpdate.signalAll();
			postEvent = !this._equals(old);
		}
		finally
		{
			this.lock.unlock();
		}
		if (postEvent)
		{
			LOGGER.debug("Value of Input {} changed to {}.", this.id, newValue); //$NON-NLS-1$
			((IInnerTalker) this.parent).postEvent(IPLCAPI.EVENT_INPUT_CHANGED, this);
		}
	}
	
	private boolean _equals(Object old)
	{
		if (this.type == Type.GENERIC)
			return Arrays.equals((byte[]) old, (byte[]) this.value);
		return this.value == old || old != null && old.equals(this.value);	
	}
	
	private void triggerInternalUpdate(boolean forceUpdate) throws IOException
	{
		if (forceUpdate)
		{
			if (this.parent.connectionState() != ConnectionState.CONNECTED)
				throw new IOException("No Connection to PLC."); //$NON-NLS-1$
			this.triggerUpdate = true;
			try
			{
				if (!this.waitForUpdate.await(INetworkAPI.CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS))
					throw new IOException("Timeout: Did not get update for input " + this.id + " for NodavePLC " + this.parent.id() + " in time."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			catch (InterruptedException e)
			{
				throw new RuntimeException(e);
			}
		}
		else if (!forceUpdate && this.value == null)
			this.triggerUpdate = true;
		if (this.value == null)
			throw new IOException("No value from input " + this.id + " available."); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public short shortValue(boolean forceUpdate) throws IOException
	{
		this.lock.lock();
		try
		{
			this.triggerInternalUpdate(forceUpdate);
			return ((Short) this.value).shortValue();
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
	public int intValue(boolean forceUpdate) throws IOException
	{
		this.lock.lock();
		try
		{
			this.triggerInternalUpdate(forceUpdate);
			return ((Integer) this.value).intValue();
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
	public float floatValue(boolean forceUpdate) throws IOException
	{
		this.lock.lock();
		try
		{
			this.triggerInternalUpdate(forceUpdate);
			return ((Float) this.value).floatValue();
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
		this.lock.lock();
		try
		{
			this.triggerInternalUpdate(forceUpdate);
			return ((Long) this.value).longValue();
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
	public byte[] genericValue(boolean forceUpdate) throws IOException
	{
		this.lock.lock();
		try
		{
			this.triggerInternalUpdate(forceUpdate);
			return (byte[]) this.value;
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
	public String toString()
	{
		return this.id;
	}
}
