package com.lodige.plc;

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
	
	public boolean bitValue();

	public short shortValue();

	public int intValue();

	public float floatValue();

	public short ubyteValue();

	public int ushortValue();

	public long uintValue();
}
