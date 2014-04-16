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


public class PDUWriteBuilder extends PDUBuilder
{
	
	private static byte WRITE_HEADER[] = { INoDave.FUNC_WRITE, (byte) 0x00 };
	public PDUWriteBuilder()
	{
		super(1);
		addParam(WRITE_HEADER);
		LOGGER.debug("{}", this);
	}


	/**
	 * set the number of the PDU
	 */
	public void setNumber(int n)
	{
		Nodave.setUSBEWord(this.mem, this.header + 4, n);
	}


	public void addVarToWriteRequest(int area, int DBnum, int start, int byteCount, byte[] buffer)
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
	
	public void addToWriteRequest(int area, int DBnum, int start, int byteCount, byte[] buffer, byte[] da, byte[] pa)
	{
		if ((area == Nodave.TIMER) || (area == Nodave.COUNTER) || (area == Nodave.TIMER200) || (area == Nodave.COUNTER200))
		{
			pa[3] = (byte)area;
			pa[4] = (byte)(((byteCount + 1) / 2) / 0x100);
			pa[5] = (byte)(((byteCount + 1) / 2) & 0xff);
		}
		else if ((area == Nodave.ANALOGINPUTS200) || (area == Nodave.ANALOGOUTPUTS200))
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
		pa[8] = (byte)(area);
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
		System.arraycopy(pa, 0, this.mem, this.param+ this.plen, pa.length);
		this.plen += pa.length;
		Nodave.setUSBEWord(this.mem, this.header + 6, this.plen);
		this.data = this.param + this.plen;
		addData(da);
		addValue(buffer);
		LOGGER.debug("{}", this);
	}

	public void addBitVarToWriteRequest(int area, int DBnum, int start, int byteCount, byte[] buffer)
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

	void testWriteResult()
	{
		if (this.mem.getDateFrom(this.param) != INoDave.FUNC_WRITE)
			throw new IllegalStateException(Nodave.strerror(Nodave.RESULT_UNEXPECTED_FUNC));
		if (this.mem.getDateFrom(this.data) != 255)
			throw new IllegalStateException(Nodave.strerror(this.mem.getDateFrom(this.data)));
	}

}