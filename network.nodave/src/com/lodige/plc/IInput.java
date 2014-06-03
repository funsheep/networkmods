package com.lodige.plc;

import java.io.IOException;

import github.javaappplatform.commons.events.ITalker;

import com.lodige.plc.IPLCAPI.Type;
import com.lodige.plc.IPLCAPI.UpdateFrequency;

/**
 * TODO javadoc
 * @author renken
 */
public interface IInput extends ITalker
{

	public String id();
	public Type type();
	public IPLC plc();

	public void delete();
	

	public void resetUpdateMethod();

	public void setUpdateMethod(UpdateFrequency frequency, boolean onTrigger);
	
//	public boolean bitValue() throws IOException;
//
	public short shortValue() throws IOException;

	public int intValue() throws IOException;

	public float floatValue() throws IOException;

	public short ubyteValue() throws IOException;

	public int ushortValue() throws IOException;

	public long uintValue() throws IOException;
	
	public byte[] genericValue() throws IOException;

}
