/**
 * lodige.platform Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.plc.impl;

import java.io.IOException;

import com.lodige.network.plc.INodaveAPI.Area;
import com.lodige.plc.IOutput;
import com.lodige.plc.IPLCAPI.Type;

/**
 * TODO javadoc
 * @author renken
 */
class Output implements IOutput
{
	
	private final String id;
	private final Type type;
	protected final Area area;
	protected final int database;
	protected final int offset;
	private final int bitnr;
	private final PLC parent; 

	
	/**
	 * 
	 */
	public Output(String id, Area area, int database, int offset, int bitnr, Type type, PLC parent)
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
	public PLC plc()
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
	public void writeBit(boolean bit) throws IOException
	{
		if (this.type != Type.BIT)
			throw new IllegalStateException("Output is of type " + this.type);
		byte b = (byte) (bit ? 1 << this.bitnr : 0);
		this.parent.writeOutput(this, new byte[] { b });
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeShort(short value) throws IOException
	{
		if (this.type != Type.SHORT)
			throw new IllegalStateException("Output is of type " + this.type);
		byte[] data = { (byte) ((value >> 8) & 0xFF), (byte) (value & 0xFF) };
		this.parent.writeOutput(this, data);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeInt(int value) throws IOException
	{
		if (this.type != Type.INT)
			throw new IllegalStateException("Output is of type " + this.type);
		byte[] data = { (byte)(value >>> 24), (byte)(value >>> 16), (byte)(value >>> 8), (byte)value };
		this.parent.writeOutput(this, data);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeFloat(float fvalue) throws IOException
	{
		if (this.type != Type.FLOAT)
			throw new IllegalStateException("Output is of type " + this.type);
		final int value = Float.floatToRawIntBits(fvalue);
		byte[] data = { (byte)(value >>> 24), (byte)(value >>> 16), (byte)(value >>> 8), (byte)value };
		this.parent.writeOutput(this, data);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeUByte(short value) throws IOException
	{
		if (this.type != Type.UBYTE)
			throw new IllegalStateException("Output is of type " + this.type);
		byte b = (byte) (value & 0xFF);
		this.parent.writeOutput(this, new byte[] { b });
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeUShort(int value) throws IOException
	{
		if (this.type != Type.USHORT)
			throw new IllegalStateException("Output is of type " + this.type);
		byte[] data = { (byte) ((value >> 8) & 0xFF), (byte) (value & 0xFF) };
		this.parent.writeOutput(this, data);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeUInt(long value) throws IOException
	{
		if (this.type != Type.UINT)
			throw new IllegalStateException("Output is of type " + this.type);
		byte[] data = { (byte) (value >>> 24), (byte) (value >>> 16), (byte) (value >>> 8), (byte) value };
		this.parent.writeOutput(this, data);
	}

}
