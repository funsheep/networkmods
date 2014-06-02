package com.lodige.plc;

import java.io.IOException;

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
	

	public void writeBit(boolean bit) throws IOException;

	public void writeShort(short value) throws IOException;

	public void writeInt(int value) throws IOException;

	public void writeFloat(float value) throws IOException;

	public void writeUByte(short value) throws IOException;

	public void writeUShort(int value) throws IOException;

	public void writeUInt(long value) throws IOException;
}
