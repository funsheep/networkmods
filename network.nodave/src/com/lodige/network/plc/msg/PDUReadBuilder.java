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
package com.lodige.network.plc.msg;

import com.lodige.network.plc.INodave;
import com.lodige.network.plc.util.Converter;


public class PDUReadBuilder extends PDUBuilder
{

	private static byte READ_HEADER[] = { INodave.FUNC_READ, (byte) 0x00 };

	/**
	 * prepare a read request with no item.
	 */
	public PDUReadBuilder()
	{
		super(1, INodave.MSG_PDU_READ);
		addParam(READ_HEADER);
		LOGGER.debug("{}", this);
	}


	public void addVarToReadRequest(int area, int DBnum, int start, int len)
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

		if ((area == INodave.ANALOGINPUTS200) || (area == INodave.ANALOGOUTPUTS200))
		{
			pa[3] = 4;
			start *= 8; /* bits */
		}
		else if ((area == INodave.TIMER) || (area == INodave.COUNTER) || (area == INodave.TIMER200) || (area == INodave.COUNTER200))
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

		Converter.setUSBEWord(pa, 4, len);
		Converter.setUSBEWord(pa, 6, DBnum);
		Converter.setUSBELong(pa, 8, start);
		Converter.setUSByte(pa, 8, area);

		this.mem.putAt((byte) (this.mem.getDateFrom(this.param+1)+1), this.param+1);
		
		this.mem.putAt(pa, 0, pa.length, this.param + this.plen);
		this.plen += pa.length;
		Converter.setUSBEWord(this.mem, 6, this.plen);
		LOGGER.debug("{}", this);
	}

	public void addBitVarToReadRequest(int area, int DBnum, int start, int len)
	{
		this.addToReadRequest(area, DBnum, start, len, true);
	}

}
