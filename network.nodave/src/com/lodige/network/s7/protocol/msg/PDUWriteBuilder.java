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


public class PDUWriteBuilder extends PDUBuilder
{
	
	private static byte WRITE_HEADER[] = { (byte) Func.WRITE.code, (byte) 0x00 };
	public PDUWriteBuilder()
	{
		super(1, INodaveAPI.MSG_PDU_WRITE);
		addParam(WRITE_HEADER);
		LOGGER.debug("{}", this); //$NON-NLS-1$
	}

	
	public void addByteToWriteRequest(Area area, int DBnum, int start, byte value)
	{
		this.addVarToWriteRequest(area, DBnum, start, 1, new byte[] { value });
	}

	public void addShortToWriteRequest(Area area, int DBnum, int start, short value)
	{
		this.addBitVarToWriteRequest(area, DBnum, start, 2, Converter.bswap_16(value));
	}
	
	public void addIntToWriteRequest(Area area, int DBnum, int start, int value)
	{
		this.addBitVarToWriteRequest(area, DBnum, start, 4, Converter.bswap_32(value));
	}
	
	public void addFloatToWriteRequest(Area area, int DBnum, int start, float value)
	{
		this.addBitVarToWriteRequest(area, DBnum, start, 4, Converter.toPLCfloat(value));
	}
	

	public void addVarToWriteRequest(Area area, int DBnum, int start, int byteCount, byte[] buffer)
	{
		byte da[] = { 0, 4, 0, 0, };
		byte pa[] =
		{
			0x12, 0x0a, 0x10, 0x02,
			/* unit (for count?, for consistency?) byte */
			0, 0, /* length in bytes */
			0, 0, /* DB number */
			0, /* area code */
			0, 0, 0 /* start address in bits */
		};
		this.addToWriteRequest(area, DBnum, start, byteCount, buffer, da, pa);
	}
	
	private void addToWriteRequest(Area area, int DBnum, int start, int byteCount, byte[] buffer, byte[] da, byte[] pa)
	{
		if ((area == Area.TIMER) || (area == Area.COUNTER) || (area == Area.TIMER_200) || (area == Area.COUNTER_200))
		{
			pa[3] = (byte)area.code;
			pa[4] = (byte)(((byteCount + 1) / 2) / 0x100);
			pa[5] = (byte)(((byteCount + 1) / 2) & 0xff);
		}
		else if ((area == Area.ANALOGINPUTS_200) || (area == Area.ANALOGOUTPUTS_200))
		{
			pa[3] = 4;
			pa[4] = (byte)(((byteCount + 1) / 2) / 0x100);
			pa[5] = (byte)(((byteCount + 1) / 2) & 0xff);
		}
		else
		{
			pa[4] = (byte)(byteCount / 0x100);
			pa[5] = (byte)(byteCount & 0xff);
		}

		pa[6] = (byte)(DBnum / 256);
		pa[7] = (byte)(DBnum & 0xff);
		pa[8] = (byte)(area.code);
		start *= 8; /* number of bits */
		pa[11] = (byte)(start & 0xff);
		pa[10] = (byte)((start / 0x100) & 0xff);
		pa[9] = (byte)(start / 0x10000);
		if ((this.dlen % 2) != 0)
		{
			addData(da, 1);
		}
		this.mem.putAt((byte) (this.mem.getDateFrom(this.param + 1) + 1), this.param + 1);
		if (this.dlen > 0)
		{
			byte[] saveData = new byte[this.dlen];
			this.mem.getDataFrom(saveData, this.data);
			this.mem.putAt(saveData, 0, saveData.length, this.data + pa.length);
		}
		this.mem.putAt(pa, 0, pa.length, this.param + this.plen);
		this.plen += pa.length;
		Converter.setUSBEWord(this.mem, 6, this.plen);
		this.data = this.param + this.plen;
		addData(da);
		addValue(buffer);
		LOGGER.debug("{}", this); //$NON-NLS-1$
	}

	public void addBitVarToWriteRequest(Area area, int DBnum, int start, int byteCount, byte[] buffer)
	{
		byte da[] = { 0, 3, 0, 0, };
		byte pa[] =
		{
			0x12, 0x0a, 0x10, 0x01, /* single bit */
			0, 0, /* insert length in bytes here */
			0, 0, /* insert DB number here */
			0, /* change this to real area code */
			0, 0, 0 /* insert start address in bits */
		};
		this.addToWriteRequest(area, DBnum, start, byteCount, buffer, da, pa);
	}

}
