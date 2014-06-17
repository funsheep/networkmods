/**
 * lodige.platform Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.plc.dummy;

import java.io.IOException;

import com.lodige.plc.IOutput;
import com.lodige.plc.IPLC;
import com.lodige.plc.IPLCAPI;

/**
 * TODO javadoc
 * @author renken
 */
public abstract class DummyOutput implements IOutput, IPLCAPI
{
	
	private final String id;
	private final Type type;
	private final DummyPLC parent; 

	
	/**
	 * 
	 */
	public DummyOutput(String id, Type type, DummyPLC parent)
	{
		this.id = id;
		this.type = type;
		this.parent = parent;
		this.parent.registerOutput(this);
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

	
//	/**
//	 * {@inheritDoc}
//	 */
//	@Override
//	public void writeBit(boolean bit) throws IOException
//	{
//		if (this.type != Type.BIT)
//			throw new IllegalStateException("DummyOutput is of type " + this.type);
//		byte b = (byte) (bit ? 1 << this.bitnr : 0);
//		this.parent.writeOutput(this, new byte[] { b });
//	}
//
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeShort(short value) throws IOException
	{
		if (this.type != Type.SHORT)
			throw new IllegalStateException("DummyOutput is of type " + this.type);
		throw new UnsupportedOperationException("Not implemented");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeInt(int value) throws IOException
	{
		if (this.type != Type.INT)
			throw new IllegalStateException("DummyOutput is of type " + this.type);
		throw new UnsupportedOperationException("Not implemented");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeFloat(float fvalue) throws IOException
	{
		if (this.type != Type.FLOAT)
			throw new IllegalStateException("DummyOutput is of type " + this.type);
		throw new UnsupportedOperationException("Not implemented");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeUByte(short value) throws IOException
	{
		if (this.type != Type.UBYTE)
			throw new IllegalStateException("DummyOutput is of type " + this.type);
		throw new UnsupportedOperationException("Not implemented");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeUShort(int value) throws IOException
	{
		if (this.type != Type.USHORT)
			throw new IllegalStateException("DummyOutput is of type " + this.type);
		throw new UnsupportedOperationException("Not implemented");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeUInt(long value) throws IOException
	{
		if (this.type != Type.UINT)
			throw new IllegalStateException("DummyOutput is of type " + this.type);
		throw new UnsupportedOperationException("Not implemented");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeGeneric(byte... data) throws IOException
	{
		if (this.type != Type.GENERIC)
			throw new IllegalStateException("DummyOutput is of type " + this.type);
		throw new UnsupportedOperationException("Not implemented");
	}

}
