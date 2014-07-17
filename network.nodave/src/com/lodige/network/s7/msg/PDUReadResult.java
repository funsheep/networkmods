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
package com.lodige.network.s7.msg;

import github.javaappplatform.commons.util.Strings;

import com.lodige.network.msg.IMessage;
import com.lodige.network.s7.INodaveAPI.Func;
import com.lodige.network.s7.INodaveAPI.Result;
import com.lodige.network.s7.util.Converter;
import com.lodige.network.s7.util.NodaveTools;

public class PDUReadResult extends PDUResult
{


	private final int udata;
	private final int udlen;
	private Variable[] results = null;

	private int cursor = 0;
	

	public PDUReadResult(IMessage msg, int protocolHeaderSize)
	{
		super(msg, protocolHeaderSize);
		this.udata = this.data + 4;
		this.udlen = getResultDataLength();
	}

	private final int getResultDataLength()
	{
		int _udlen = Converter.USBEWord(this.word(this.data + 2), 0);
		// udlen=data[2]*0x100+data[3];
		final int type = this.date(this.data+1)[0];
		if (type == 4)
		{
			_udlen >>= 3; /* len is in bits, adjust */
		}
		else if (type == 9)
		{
			/* len is already in bytes, ok */
		}
		else if (type == 3)
		{
			/* len is in bits, but there is a byte per result bit, ok */
		}
		else
		{
			LOGGER.debug("fixme: what to do with data type {}", Integer.valueOf(type)); //$NON-NLS-1$
//			throw new PDUResultException(Nodave.RESULT_UNKNOWN_DATA_UNIT_SIZE);
		}
		return _udlen;
	}
	
	public byte[] resultData()
	{
		byte[] dest = new byte[this.udlen];
		this.msg.data(dest, this.udata);
		return dest;
	}

	public void checkReadResult() throws PDUResultException
	{
		if (this.date(this.param)[0] != Func.READ.code)
			throw new IllegalStateException(NodaveTools.strerror(Result.UNEXPECTED_FUNC));
		checkResultData();
	}

	private void checkResultData() throws PDUResultException
	{
		if ((this.date(this.data)[0] != (byte)255) || (this.dlen <= 4))
			throw new PDUResultException(this.date(this.data)[0]);
	}
	

	public void checkPGReadResult() throws PDUResultException
	{
		if (this.date(this.param)[0] != 0)
			throw new PDUResultException(Result.UNEXPECTED_FUNC);
		checkResultData();
	}

	
	public void setCursor(int position)
	{
		if (position < 0 || position >= this.resultDataLength())
			throw new ArrayIndexOutOfBoundsException(position);
		this.cursor = position;
	}
	
	public int cursor()
	{
		return this.cursor;
	}
	
	public int resultDataLength()
	{
		return this.udlen;
	}
	
	public byte readByte()
	{
		return this.date(this.cursor++)[0];
	}
	
	public short readShort()
	{
		return com.lodige.network.msg.Converter.getShortLittle(this.word(this.cursor), 0);
	}
	
	public int readUnsignedShort()
	{
		short value = this.readShort();
		return value >= 0 ? value : 0x10000 + value;
	}
	
	public Variable[] getResults()
	{
		if (this.results == null)
			try
			{
				this.results = this.parseResults();
			}
			catch (PDUResultException e)
			{
				LOGGER.debug("Could not parse pdu results", e); //$NON-NLS-1$
				this.results = new Variable[0];
			}
		return this.results;
	}
	
	/*
	 * Read a predefined set of values from the PLC. Return ok or an error state If a buffer pointer
	 * is provided, data will be copied into this buffer. If it's NULL you can get your data from
	 * the resultPointer in daveConnection long as you do not send further requests.
	 */
	private Variable[] parseResults() throws PDUResultException
	{
		this.checkReadResult();

		final int numResults = this.date(this.param + 1)[0];
		Variable[] results = new Variable[numResults];
		int pos = this.data;
		for (int i = 0; i < numResults; i++)
		{
			Variable r;
			final int error = Converter.USByte(this.date(pos), 0);
			if (error == 255)
			{
				int type = Converter.USByte(this.date(pos + 1), 0);
				int len = Converter.USBEWord(this.word(pos + 2), 0);
				if (type == 4)
					len /= 8;
//				else if (type == 3)
//					; // length is ok

				r = new Variable(len, pos + 4, this.msg);
				pos += len;
				if ((len % 2) != 0)
					pos++;
			}
			else
			{
				r = new Variable(error);
				LOGGER.debug("Error {}", Integer.valueOf(r.error)); //$NON-NLS-1$
			}

			pos += 4;
			results[i] = r;
		}
		return results;
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		if (this.udlen > 0)
		{
			sb.append("Result Data "); //$NON-NLS-1$
			sb.append(Strings.toHexString(this.resultData()));
			sb.append('\n');
		}
		return sb.toString();
	}
	
}
