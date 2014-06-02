/**
 * network.nodave Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.plc.impl;

import github.javaappplatform.commons.events.TalkerStub;
import github.javaappplatform.commons.log.Logger;
import github.javaappplatform.platform.Platform;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.lodige.network.INetworkAPI;
import com.lodige.network.plc.INodaveAPI.Area;
import com.lodige.plc.IInput;
import com.lodige.plc.IPLC;
import com.lodige.plc.IPLCAPI.Type;
import com.lodige.plc.IPLCAPI.UpdateFrequency;

/**
 * TODO javadoc
 * @author renken
 */
class Input extends TalkerStub implements IInput
{
	
	private static final Logger LOGGER = Logger.getLogger();

	
	protected final String id;
	protected final Area area;
	protected final int database;
	protected final int offset;
	protected final int bitnr;
	protected final Type type;
	protected final PLC parent;
	
	private final ReentrantLock lock = new ReentrantLock();
	private final Condition waitForUpdate = this.lock.newCondition();
	private Boolean onTrigger = null;
	private UpdateFrequency frequency = null;
	private boolean updateScheduled = false;
	
	private Object value;
	private long lastUpdate = 0;


	/**
	 * 
	 */
	protected Input(String id, Area area, int database, int offset, int bitnr, Type type, PLC parent)
	{
		this.id = id;
		this.area = area;
		this.database = database;
		this.offset = offset;
		this.bitnr = bitnr;
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
			this.onTrigger = null;
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
	public void setUpdateMethod(UpdateFrequency frequency, boolean onTrigger)
	{
		this.lock.lock();
		try
		{
			this.onTrigger = Boolean.valueOf(onTrigger);
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

	private boolean onTrigger()
	{
		return this.onTrigger != null ? this.onTrigger.booleanValue() : this.parent.onTrigger();
	}

	boolean startExternalUpdate()
	{
		this.lock.lock();
		try
		{
			if (!this.updateScheduled && (this.value == null || this.frequency() != UpdateFrequency.OFF && (Platform.currentTime() - this.lastUpdate > this.frequency().schedule)))
			{
				this.updateScheduled = true;
				return true;
			}
			return false;
		}
		finally
		{
			this.lock.unlock();
		}
	}
	
	boolean startInternalUpdate()
	{
		this.lock.lock();
		try
		{
			if (!this.updateScheduled && (this.value == null || this.onTrigger() && this.frequency() != UpdateFrequency.HIGH && Platform.currentTime() - this.lastUpdate > UpdateFrequency.HIGH.schedule))
			{
				this.updateScheduled = true;
				return true;
			}
			return false;
		}
		finally
		{
			this.lock.unlock();
		}

	}
	
	void update(byte[] data, int offset)
	{
		Object val = null;
		switch (this.type)
		{
			case BIT:
				final boolean b = (data[0] & (1 << this.bitnr)) != 0;
				val = Boolean.valueOf(b);
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
		}
		
		this.lock.lock();
		try
		{
			this.value = val;
			this.lastUpdate = Platform.currentTime();
			this.updateScheduled = false;
			this.waitForUpdate.signalAll();
			LOGGER.debug("Input {} updated.", this.id);
		}
		finally
		{
			this.lock.unlock();
		}
	}
	
	private void triggerInternalUpdate() throws IOException
	{
		if (this.startInternalUpdate())
		{
			this.lock.lock();
			try
			{
				new InputTriggering(this);
				if (this.updateScheduled)
					try
					{
						if (!this.waitForUpdate.await(INetworkAPI.CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS))
							throw new IOException("Timeout: Did not get update for input " + this.id + " for PLC " + this.parent.id() + " in time.");
					}
					catch (InterruptedException e)
					{
						throw new RuntimeException(e);
					}
			}
			finally
			{
				this.lock.unlock();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean bitValue() throws IOException
	{
		this.triggerInternalUpdate();
		return ((Boolean) this.value).booleanValue();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public  short shortValue() throws IOException
	{
		this.triggerInternalUpdate();
		return ((Short) this.value).shortValue();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public  int intValue() throws IOException
	{
		this.triggerInternalUpdate();
		return ((Integer) this.value).intValue();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public  float floatValue() throws IOException
	{
		this.triggerInternalUpdate();
		return ((Float) this.value).floatValue();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public  short ubyteValue() throws IOException
	{
		return this.shortValue();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public  int ushortValue() throws IOException
	{
		return this.intValue();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public  long uintValue() throws IOException
	{
		this.triggerInternalUpdate();
		return ((Long) this.value).longValue();
	}

}
