package com.lodige.network.s7.plc;

import java.io.IOException;

import com.lodige.network.s7.plc.IPLCAPI.Type;
import com.lodige.network.s7.plc.IPLCAPI.UpdateFrequency;

/**
 * TODO javadoc
 * @author renken
 */
public interface IInput
{

	public String id();
	public Type type();
	public IPLC plc();

	public void delete();
	

	public void resetUpdateMethod();

	public void setUpdateMethod(UpdateFrequency frequency);
	

	public default short shortValue() throws IOException
	{
		return this.shortValue(false);
	}

	public default int intValue() throws IOException
	{
		return this.intValue(false);
	}

	public default float floatValue() throws IOException
	{
		return this.floatValue(false);
	}

	public default short ubyteValue() throws IOException
	{
		return this.ubyteValue(false);
	}

	public default int ushortValue() throws IOException
	{
		return this.ushortValue(false);
	}

	public default long uintValue() throws IOException
	{
		return this.uintValue(false);
	}
	
	public default byte[] genericValue() throws IOException
	{
		return this.genericValue(false);
	}

	public short shortValue(boolean forceUpdate) throws IOException;

	public int intValue(boolean forceUpdate) throws IOException;

	public float floatValue(boolean forceUpdate) throws IOException;

	public short ubyteValue(boolean forceUpdate) throws IOException;

	public int ushortValue(boolean forceUpdate) throws IOException;

	public long uintValue(boolean forceUpdate) throws IOException;
	
	public byte[] genericValue(boolean forceUpdate) throws IOException;

}
