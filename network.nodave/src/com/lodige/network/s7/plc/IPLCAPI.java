package com.lodige.network.s7.plc;

import github.javaappplatform.commons.util.StringID;

/**
 * TODO javadoc
 * @author renken
 */
public interface IPLCAPI
{

	public enum ConnectionState
	{
		NOT_CONNECTED, CONNECTING, CONNECTED, SHUTTING_DOWN, UNKNOWN;
	}

	public enum Type
	{
		/** Signed short, 2 bytes, NodavePLC-Term: 'Integer' */
		SHORT(false, 2),
		/** Signed int, 4 bytes, NodavePLC-Term: 'Double-Integer' */
		INT(false, 4),
		/** Floating Point, 4 bytes, NodavePLC-Term: 'Real' */
		FLOAT(false, 4),
		/** Unsigned byte, 1 byte, NodavePLC-Term: 'Byte' */
		UBYTE(true, 1),
		/** Unsigned short, 2 bytes, NodavePLC-Term: 'Word' */
		USHORT(true, 2),
		/** Unsigned int, 4 bytes, NodavePLC-Term: 'Double-Word' */
		UINT(true, 4),
		/** Generic data area with arbitrary size. */
		GENERIC(false, -1);
		
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
	
	public static final int BACKGROUND_UPDATE_FRQUENCY = 30000;
	
	public static final int EVENT_INPUT_CHANGED = StringID.id("EVENT_INPUT_CHANGED"); //int=2041127352 //$NON-NLS-1$
	public static final int EVENT_CONNECTION_STATE_CHANGED = StringID.id("EVENT_CONNECTION_STATE_CHANGED"); //$NON-NLS-1$
	
	public static final String PLC_UPDATE_THREAD = "Update PLC"; //$NON-NLS-1$
}
