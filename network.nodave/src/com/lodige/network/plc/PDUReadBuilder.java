/*
 Part of Libnodave, a free communication libray for Siemens S7 300/400 via
 the MPI adapter 6ES7 972-0CA22-0XAC
 or  MPI adapter 6ES7 972-0CA33-0XAC
 or  MPI adapter 6ES7 972-0CA11-0XAC.
 
 (C) Thomas Hergenhahn (thomas.hergenhahn@web.de) 2002.

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


public class PDUReadBuilder extends PDUBuilder
{

	private static byte READ_HEADER[] = { INoDave.FUNC_READ, (byte) 0x00 };

	/**
	 * prepare a read request with no item.
	 */
	public PDUReadBuilder()
	{
		super(1);
		addParam(READ_HEADER);
		LOGGER.debug("{}", this);
	}


	/**
	 * return the number of the PDU
	 */
	public int getNumber()
	{
		return Nodave.USBEWord(this.mem, this.header + 4);
	}

	public void addVarToReadRequest(int area, int DBnum, int start, int len, boolean isBit)
	{
		this.addToReadRequest(area, DBnum, start, len, false);
	}
	
	public void addToReadRequest(int area, int DBnum, int start, int len, boolean isBit)
	{
	    byte[] pa =
	    {
	    	0x12, 0x0a, 0x10,
	    	0x02,		/* 1=single bit, 2=byte, 4=word */
	    	0,0,		/* length in bytes */
	    	0,0,		/* DB number */
	    	0,		/* area code */
	    	0,0,0		/* start address in bits */
	    };

		if ((area == Nodave.ANALOGINPUTS200) || (area == Nodave.ANALOGOUTPUTS200))
		{
			pa[3] = 4;
			start *= 8; /* bits */
		}
		else if ((area == Nodave.TIMER) || (area == Nodave.COUNTER) || (area == Nodave.TIMER200) || (area == Nodave.COUNTER200))
		{
			pa[3] = (byte) area;
		}
		else if (isBit)
		{
			pa[3] = 1;
		}
		else
		{
			start *= 8; /* bits */
		}

		Nodave.setUSBEWord(pa, 4, len);
		Nodave.setUSBEWord(pa, 6, DBnum);
		Nodave.setUSBELong(pa, 8, start);
		Nodave.setUSByte(pa, 8, area);

		this.mem.putAt((byte) (this.mem.getDateFrom(this.param+1)+1), this.param+1);
		
		this.mem.putAt(pa, 0, pa.length, this.param + this.plen);
		this.plen += pa.length;
		Nodave.setUSBEWord(this.mem, this.header + 6, this.plen);
		LOGGER.debug("{}", this);
	}

	public void addBitVarToReadRequest(int area, int DBnum, int start, int len)
	{
		this.addToReadRequest(area, DBnum, start, len, true);
	}

//	void testReadResult()
//	{
//		if (this.mem.getDateFrom(this.param) != INoDave.FUNC_READ)
//			throw new IllegalStateException(Nodave.strerror(Nodave.RESULT_UNEXPECTED_FUNC));
//		testResultData();
//	}
//
//	/*
//	 * This method adjusts udlen - for a test method, this should not happen!
//	*/
//	void testResultData()
//	{
//		if ((this.mem.getDateFrom(this.data) != (byte)255) || (this.dlen <= 4))
//			throw new IllegalStateException(Nodave.strerror(this.mem.getDateFrom(this.data)));
//
//		this.udata = this.data + 4;
//		// udlen=data[2]*0x100+data[3];
//		this.udlen = Nodave.USBEWord(this.mem, this.data + 2);
//		final int type = this.mem.getDateFrom(this.data+1);
//		if (type == 4)
//		{
//			this.udlen >>= 3; /* len is in bits, adjust */
//		}
//		else if (type == 9)
//		{
//			/* len is already in bytes, ok */
//		}
//		else if (type == 3)
//		{
//			/* len is in bits, but there is a byte per result bit, ok */
//		}
//		else
//		{
//			LOGGER.debug("fixme: what to do with data type {}", Integer.valueOf(type));
//			throw new IllegalStateException(Nodave.strerror(Nodave.RESULT_UNKNOWN_DATA_UNIT_SIZE));
//		}
//	}


}
