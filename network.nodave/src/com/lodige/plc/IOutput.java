package com.lodige.plc;

import com.lodige.plc.IPLCAPI.Type;

/**
 * TODO javadoc
 * @author renken
 */
public interface IOutput
{

	public String id();
	public Type type();
	public IPLC plc();

	public void delete();
	

	public void writeBit(boolean bit);

	public void writeShort(short value);

	public void writeInt(int value);

	public void writeFloat(float value);

	public void writeUByte(short value);

	public void writeUShort(int value);

	public void writeUInt(long value);
}
