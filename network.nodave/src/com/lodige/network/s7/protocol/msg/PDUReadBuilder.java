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
package com.lodige.network.s7.protocol.msg;

import com.lodige.network.s7.protocol.INodaveAPI;
import com.lodige.network.s7.protocol.INodaveAPI.Area;
import com.lodige.network.s7.protocol.INodaveAPI.Func;
import com.lodige.network.s7.protocol.util.Converter;


public class PDUReadBuilder extends PDUBuilder
{

	private static byte READ_HEADER[] = { (byte) Func.READ.code, (byte) 0x00 };

	/**
	 * prepare a read request with no item.
	 */
	public PDUReadBuilder()
	{
		super(1, INodaveAPI.MSG_PDU_READ);
		addParam(READ_HEADER);
		LOGGER.debug("{}", this); //$NON-NLS-1$
	}


	public void addVarToReadRequest(Area area, int DBnum, int start, int len)
	{
		this.addToReadRequest(area, DBnum, start, len, false);
	}
	
	public void addToReadRequest(Area area, int DBnum, int start, int len, boolean isBit)
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

		if ((area == Area.ANALOGINPUTS_200) || (area == Area.ANALOGOUTPUTS_200))
		{
			pa[3] = 4;
			start *= 8; /* bits */
		}
		else if ((area == Area.TIMER) || (area == Area.COUNTER) || (area == Area.TIMER_200) || (area == Area.COUNTER_200))
		{
			pa[3] = (byte) area.code;
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
		Converter.setUSByte(pa, 8, area.code);

		this.mem.putAt((byte) (this.mem.getDateFrom(this.param+1)+1), this.param+1);
		
		this.mem.putAt(pa, 0, pa.length, this.param + this.plen);
		this.plen += pa.length;
		Converter.setUSBEWord(this.mem, 6, this.plen);
		LOGGER.debug("{}", this); //$NON-NLS-1$
	}

	public void addBitVarToReadRequest(Area area, int DBnum, int start, int len)
	{
		this.addToReadRequest(area, DBnum, start, len, true);
	}

}
