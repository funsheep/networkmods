/*
 Part of Libnodave, a free communication libray for Siemens S7
 
 (C) Thomas Hergenhahn (thomas.hergenhahn@web.de) 2005.

 Libnodave is free software; you can redistribute it and/or modify
 it under the terms of the GNU Library General Public License as published by
 the Free Software Foundation; either version 2, or (at your option)
 any later version.

 Libnodave is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU Library General Public License
 along with this; see the file COPYING.  If not, write to
 the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.  
*/

package com.lodige.network.plc;


public class INodave
{
	
	/**
	 * known function codes
	 */
	public final static byte FUNC_READ = 4;
	public final static byte FUNC_WRITE = 5;

	
	public static final int MSG_OTHER = 0;
	public static final int MSG_PDU = 1 << 1;
	public static final int MSG_PDU_READ = 1 << 2 | MSG_PDU;
	public static final int MSG_PDU_WRITE = 1 << 3 | MSG_PDU;


//	public final static int OrderCodeSize = 21;
//	public final static int MPIReachable = 0x30;
//	public final static int MPIunused = 0x10;
//	public final static int PartnerListSize = 126;

	/*
	    Use these constants for parameter "area" in daveReadBytes and daveWriteBytes
	*/

	/*
		Use these constants for parameter "area" in daveReadBytes and daveWriteBytes
	*/    
	public final static int SYSINFO = 3;	// System info of 200 family
	public final static int SYSTEMFLAGS = 5;	// System flags of 200 family
	public final static int ANALOGINPUTS200 = 6; // analog inputs of 200 family
	public final static int ANALOGOUTPUTS200 = 7;	// analog outputs of 200 family
	public final static int P = 0x80;	//Peripheral I/O
	public final static int INPUTS = 0x81;
	public final static int OUTPUTS = 0x82;
	public final static int FLAGS = 0x83;
	public final static int DB = 0x84; //data blocks
	public final static int DI = 0x85;	// instance data blocks
	public final static int LOCAL = 0x86; //not tested
	public final static int V = 0x87; // local of caller
	public final static int COUNTER = 28;	// S7 counters
	public final static int TIMER = 29;	// S7 timers 
	public final static int COUNTER200 = 30;	// IEC counters (200 family)
	public final static int TIMER200 = 31;	// IEC timers (200 family) 

	public final static int DEBUG_PDU = 0x10;
	public final static int DEBUG_IFACE = 0x20;
	public final static int DEBUG_CONN = 0x40;
	public final static int DEBUG_EXCHANGE = 0x80;
	public final static int DEBUG_PRINT_ERRORS = 0x100;
	public final static int DEBUG_SPECIALCHARS = 0x200;
	public final static int DEBUG_RAWREAD = 0x400;
	public final static int DEBUG_INITADAPTER = 0x800;
	public final static int DEBUG_CONNECT = 0x1000;
	public final static int DEBUG_LIST_REACHABLES = 0x2000;
	public final static int DEBUG_RAWSEND = 0x4000;
	public final static int DEBUG_ERROR_REPORTIMG =0x8000;
	public final static int DEBUG_CONVERSIONS = 0x1000;
	public final static int DEBUG_ALL = 0xFFFFFF;

	public final static int PROTOCOL_PPI = 0;
	public final static int PROTOCOL_MPI = 1;
	public final static int PROTOCOL_MPI2 = 2;
	public final static int PROTOCOL_MPI3 = 3;
	public final static int PROTOCOL_ISOTCP = 4;
	public final static int PROTOCOL_ISOTCP243 = 5;
	public final static int PROTOCOL_MPI_IBH = 223; // MPI with IBH NetLink MPI to ethernet gateway
	public final static int PROTOCOL_PPI_IBH = 224; // PPI with IBH NetLink MPI to ethernet gateway
	public final static int PROTOCOL_MPI_NLPRO = 230; // MPI with IBH NetLink MPI to ethernet gateway
	public final static int PROTOCOL_NLPRO = 230; // MPI with IBH NetLink MPI to ethernet gateway
	public final static int PROTOCOL_USER_TRANSPORT= 255;	
// Libnodave will pass the PDUs of S7 Communication to user defined call back functions.

	public final static int RESULT_OK = 0; /* means all ok */
	public final static int RESULT_NO_PERIPHERAL_AT_ADDRESS = 1;
	/* CPU tells there is no peripheral at address */
	public final static int RESULT_MULTIPLE_BITS_NOT_SUPPORTED = 6;
	/* CPU tells it does not support to read a bit block with a */
	/* length other than 1 bit. */
	public final static int RESULT_ITEM_NOT_AVAILABLE200 = 3;
	/* means a a piece of data is not available in the CPU, e.g. */
	/* when trying to read a non existing DB or bit bloc of length<>1 */
	/* This code seems to be specific to 200 family. */

	public final static int RESULT_ITEM_NOT_AVAILABLE = 10;
	/* means a a piece of data is not available in the CPU, e.g. */
	/* when trying to read a non existing DB */

	public final static int RESULT_ADDRESS_OUT_OF_RANGE = 5;
	/* means the data address is beyond the CPUs address range */
	public final static int RESULT_WRITE_DATA_SIZE_MISMATCH = 7;
	/* means the write data size doesn't fit item size */
	public final static int RESULT_CANNOT_EVALUATE_PDU = -123;
	public final static int RESULT_CPU_RETURNED_NO_DATA = -124;

	public final static int RESULT_UNKNOWN_ERROR = -125;
	public final static int RESULT_EMPTY_RESULT_ERROR = -126;
	public final static int RESULT_EMPTY_RESULT_SET_ERROR = -127;
	public final static int RESULT_UNEXPECTED_FUNC = -128;
	public final static int RESULT_UNKNOWN_DATA_UNIT_SIZE = -129;

	public final static int RESULT_SHORT_PACKET = -1024;
	public final static int RESULT_TIMEOUT = -1025;

}