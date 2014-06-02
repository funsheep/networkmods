/**
 * network.nodave Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.plc.impl;

import github.javaappplatform.commons.events.TalkerStub;
import github.javaappplatform.commons.log.Logger;
import github.javaappplatform.platform.Platform;

import java.nio.ByteBuffer;

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
	private Boolean onTrigger = null;
	private UpdateFrequency frequency = null;
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
	public synchronized void resetUpdateMethod()
	{
		this.onTrigger = null;
		this.frequency = null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void setUpdateMethod(UpdateFrequency frequency, boolean onTrigger)
	{
		this.onTrigger = Boolean.valueOf(onTrigger);
		this.frequency = frequency;
	}
	
	public synchronized UpdateFrequency frequency()
	{
		return this.frequency != null ? this.frequency : this.parent.frequency();
	}

	synchronized boolean externalUpdateNeeded()
	{
		UpdateFrequency freq = this.frequency();
		return freq != UpdateFrequency.OFF && (Platform.currentTime() - this.lastUpdate > freq.schedule);
	}
	
	synchronized void update(byte[] data, int offset)
	{
		switch (this.type)
		{
			case BIT:
				final boolean b = (data[0] & (1 << this.bitnr)) != 0;
				this.value = Boolean.valueOf(b);
				break;
			case SHORT:
				final short s = (short) (((data[0] & 0xFF) << 8) | (data[1] & 0xFF));
				this.value = Short.valueOf(s);
				break;
			case INT:
				final int i = ByteBuffer.wrap(data).getInt();
				this.value = Integer.valueOf(i);
				break;
			case FLOAT:
				final float f = ByteBuffer.wrap(data).getFloat();
				this.value = Float.valueOf(f);
				break;
			case UBYTE:
				final short ub = (short) (data[0] & 0xFF);
				this.value = Short.valueOf(ub);
				break;
			case USHORT:
				final int us = Short.toUnsignedInt((short) (((data[0] & 0xFF) << 8) | (data[1] & 0xFF)));
				this.value = Integer.valueOf(us);
				break;
			case UINT:
				final long ui = Integer.toUnsignedLong(ByteBuffer.wrap(data).getInt());
				this.value = Long.valueOf(ui);
				break;
		}
		this.lastUpdate = Platform.currentTime();
	}
	
	private void triggerUpdate()
	{
		final boolean triggerFlag = this.onTrigger != null ? this.onTrigger.booleanValue() : this.parent.updateOnTrigger();
		final boolean timerExpired = this.frequency() != UpdateFrequency.HIGH && Platform.currentTime() - this.lastUpdate > UpdateFrequency.HIGH.schedule;
		if (triggerFlag && timerExpired)
		{
			try
			{
				byte[] data = (new InputTriggering(this)).get();
				this.update(data, 0);
			}
			catch (Exception e)
			{
				LOGGER.severe("Could not read data from input {} from plc {}.", this.id, this.parent.id(), e);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized boolean bitValue()
	{
		this.triggerUpdate();
		return ((Boolean) this.value).booleanValue();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized short shortValue()
	{
		this.triggerUpdate();
		return ((Short) this.value).shortValue();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized int intValue()
	{
		this.triggerUpdate();
		return ((Integer) this.value).intValue();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized float floatValue()
	{
		this.triggerUpdate();
		return ((Float) this.value).floatValue();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized short ubyteValue()
	{
		return this.shortValue();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized int ushortValue()
	{
		return this.intValue();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized long uintValue()
	{
		this.triggerUpdate();
		return ((Long) this.value).longValue();
	}

}
