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


public class INodaveAPI
{
	
	public static final int MSG_OTHER = 0;
	public static final int MSG_PDU = 1 << 1;
	public static final int MSG_PDU_READ = 1 << 2 | MSG_PDU;
	public static final int MSG_PDU_WRITE = 1 << 3 | MSG_PDU;

	public static final int E_PDU_RESULT_RECIEVED = 1;

//	public final static int OrderCodeSize = 21;
//	public final static int MPIReachable = 0x30;
//	public final static int MPIunused = 0x10;
//	public final static int PartnerListSize = 126;

	/**
	 * known function codes
	 */
	public enum Func
	{
		READ(4),
		WRITE(5),
		UNKNOWN(Integer.MIN_VALUE);

	
		public final int code;
		
		private Func(int code)
		{
			this.code = code;
		}
		
		public static final Func convert(int code)
		{
			for (Func a : Func.values())
				if (a.code == code)
					return a;
			return UNKNOWN;
		}

	}

	/*
		Use these constants for parameter "area" in daveReadBytes and daveWriteBytes
	*/
	public enum Area
	{
		
		SYSINFO_200(3),			// System info of 200 family
		SYSTEMFLAGS_200(5),		// System flags of 200 family
		ANALOGINPUTS_200(6),	// analog inputs of 200 family
		ANALOGOUTPUTS_200(7),	// analog outputs of 200 family
		P(0x80),				//Peripheral I/O
		INPUTS(0x81),
		OUTPUTS(0x82),
		FLAGS(0x83),
		DB(0x84),				//data blocks
		DI(0x85),				// instance data blocks
		LOCAL(0x86),			//not tested
		V(0x87),				// local of caller
		COUNTER(28),			// S7 counters
		TIMER(29),				// S7 timers 
		COUNTER_200(30),		// IEC counters (200 family)
		TIMER_200(31),			// IEC timers (200 family)
		UNKNOWN(-1);

	
		public final int code;
		
		private Area(int code)
		{
			this.code = code;
		}
		
		public static final Area convert(int code)
		{
			for (Area a : Area.values())
				if (a.code == code)
					return a;
			return UNKNOWN;
		}
	}

	public enum Protocol
	{
		
		 PPI(0),
		 MPI(1),
		 PROTOCOL_MPI2(2),
		 MPI3(3),
		 ISOTCP(4),
		 ISOTCP243(5),
		 MPI_IBH(223),		// MPI with IBH NetLink MPI to ethernet gateway
		 PPI_IBH(224),		// PPI with IBH NetLink MPI to ethernet gateway
		 MPI_NLPRO(230),	// MPI with IBH NetLink MPI to ethernet gateway
		 NLPRO(230),		// MPI with IBH NetLink MPI to ethernet gateway
		 USER_TRANSPORT(255);
		 // Libnodave will pass the PDUs of S7 Communication to user defined call back functions.
		
		public final int id;
		
		private Protocol(int id)
		{
			this.id = id;
		}
	}

	public enum Result
	{
		OK(0), /* means all ok */
		NO_PERIPHERAL_AT_ADDRESS(1),
		/* CPU tells there is no peripheral at address */
		MULTIPLE_BITS_NOT_SUPPORTED(6),
		/* CPU tells it does not support to read a bit block with a */
		/* length other than 1 bit. */
		ITEM_NOT_AVAILABLE200(3),
		/* means a a piece of data is not available in the CPU, e.g. */
		/* when trying to read a non existing DB or bit bloc of length<>1 */
		/* This code seems to be specific to 200 family. */
	
		ITEM_NOT_AVAILABLE(10),
		/* means a a piece of data is not available in the CPU, e.g. */
		/* when trying to read a non existing DB */
	
		ADDRESS_OUT_OF_RANGE(5),
		/* means the data address is beyond the CPUs address range */
		WRITE_DATA_SIZE_MISMATCH(7),
		/* means the write data size doesn't fit item size */
		CANNOT_EVALUATE_PDU(-123),
		CPU_RETURNED_NO_DATA(-124),
	
		UNKNOWN_ERROR(-125),
		EMPTY_RESULT(-126),
		EMPTY_RESULTSET(-127),
		UNEXPECTED_FUNC(-128),
		UNKNOWN_DATA_UNIT_SIZE(-129),
	
		SHORT_PACKET(-1024),
		TIMEOUT(-1025),

		FUNCTION_ALREADY_OCCUPIED(0x8000),
		CURRENTLY_NOT_ALLOWED(0x8001),
		HARDWARE_FAULT(0x8101),
		ACCESS_NOT_ALLOWED(0x8103),
		CONTEXT_UNSUPPORTED(0x8104),
		INVALID_ADDRESS(0x8105),
		DATATYPE_UNSUPPORTED(0x8106),
		DATATYPE_INCONSITENT(0x8107),
		OBJECT_NOT_EXISTENT(0x810A),
		INCORRECT_PDU_SIZE(0x8500),
		INVALID_ADDRESS2(0x8702),
		BLOCKNAME_SYNTAX_ERROR(0xd201),
		FUNCTION_PARAMETER_SYNTAX_ERROR(0xd202),
		BLOCKTYPE_SYNTAX_ERROR(0xd203),
		NO_BLOCK_ON_STORAGE(0xd204),
		OBJECT_ALREADY_EXISTS(0xd205),
		OBJECT_ALREADY_EXISTS2(0xd206),
		BLOCK_ALREADY_EXISTS(0xd207),
		BLOCK_NOT_EXISTENT(0xd209),
		BLOCK_ALREADY_EXISTS2(0xd20e),
		BLOCK_NUMBER_TOO_BIG(0xd210),
		UNFINISHED_BLOCK_TRANSFER(0xd240),
		PASSWORD_PROTECTED(0xd241);

		
		public final int code;
		
		private Result(int code)
		{
			this.code = code;
		}

		public static final Result convert(int code)
		{
			for (Result r : Result.values())
				if (r.code == code)
					return r;
			return UNKNOWN_ERROR;
		}
	}

}