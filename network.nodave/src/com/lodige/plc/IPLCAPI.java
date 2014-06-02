package com.lodige.plc;

import github.javaappplatform.commons.util.StringID;

/**
 * TODO javadoc
 * @author renken
 */
public interface IPLCAPI
{

	public enum Type
	{
		/** Bit Flag, stored in a byte somewhere. */
		BIT(true, -1),
		/** Signed short, 2 bytes, PLC-Term: 'Integer' */
		SHORT(false, 2),
		/** Signed int, 4 bytes, PLC-Term: 'Double-Integer' */
		INT(false, 4),
		/** Floating Point, 4 bytes, PLC-Term: 'Real' */
		FLOAT(false, 4),
		/** Unsigned byte, 1 byte, PLC-Term: 'Byte' */
		UBYTE(true, 1),
		/** Unsigned short, 2 bytes, PLC-Term: 'Word' */
		USHORT(true, 2),
		/** Unsigned int, 4 bytes, PLC-Term: 'Double-Word' */
		UINT(true, 4);
		
		/** Flag, whether the type is signed or unsigned. */
		public final boolean unsigned;
		/** Size of the data type in byte on the plc. Bit Type is set to -1. */
		public final int size;
		
		private Type(boolean unsigned, int size)
		{
			this.unsigned = unsigned;
			this.size = size;
		}
		
	}
	
	public enum UpdateFrequency
	{
		HIGH(1000),
		MEDIUM(4000),
		LOW(8000),
		OFF(0);
		
		public final int schedule;
		
		private UpdateFrequency(int schedule)
		{
			this.schedule = schedule;
		}
	}
	
	public static final int EVENT_VALUE_CHANGED = StringID.id("EVENT_VALUE_CHANGED");
	
	public static final String PLC_UPDATE_THREAD = "PLC UPDATE_THREAD";

}
