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

import github.javaappplatform.commons.log.Logger;
import github.javaappplatform.commons.util.Strings;

import com.lodige.network.msg.IMessage;
import com.lodige.network.s7.util.Converter;

public class PDUResult
{
	
	protected static final Logger LOGGER = Logger.getLogger();


	protected final IMessage msg;
	
	protected final int header; // the position of the header;
	protected final int param; // the position of the parameters;
//	private int hlen;
//	private int plen;
	protected final int data;
	protected final int dlen;


	public PDUResult(IMessage msg, int protocolHeaderSize)
	{
		this.msg = msg;
		this.header = protocolHeaderSize;
		this.param = this.header + this.getHeaderLength();
		this.data  = this.param + this.getParamLength();
		this.dlen  = this.getDataLength();
		LOGGER.debug("Got PDU result with number {}", Integer.valueOf(this.getNumber())); //$NON-NLS-1$
	}


	private final int getHeaderLength()
	{
		byte[] tmp = new byte[1];
		this.msg.data(tmp, this.header+1);
		int headerLength = 10;
		if (tmp[0] == 2 || tmp[0] == 3)
			headerLength = 12;
		return headerLength;
	}
	
	private final int getParamLength()
	{
		byte[] tmp = new byte[2];
		this.msg.data(tmp, this.header + 6);
		return Converter.USBEWord(tmp, 0);
	}
	
	private final int getDataLength()
	{
		byte[] tmp = new byte[2];
		this.msg.data(tmp, this.header + 8);
		return Converter.USBEWord(tmp, 0);
	}
	
	/**
	 * return the number of the PDU
	 */
	public int getNumber()
	{
		return Converter.USBEWord(this.word(this.header + 4), 0);
	}

	/**
	 * return the function code of the PDU - read/write differentiation.
	 */
	public int getFunc()
	{
		return Converter.USByte(this.date(this.param), 0);
	}


	public int getError()
	{
		if (this.param - this.header == 12)
			return Converter.USBEWord(this.word(this.header + 10), 0);

		return 0;
	}


	public byte[] header()
	{
		byte[] dest = new byte[this.param - this.header];
		this.msg.data(dest, this.header);
		return dest;
	}
	
	public byte[] params()
	{
		byte[] dest = new byte[this.data - this.param];
		this.msg.data(dest, this.param);
		return dest;
	}

	public byte[] data()
	{
		byte[] dest = new byte[this.dlen];
		this.msg.data(dest, this.data);
		return dest;
	}


	private final byte[] one = new byte[1];
	protected byte[] date(int position)
	{
		this.msg.data(this.one, position);
		return this.one;
	}

	private final byte[] two = new byte[2];
	protected byte[] word(int position)
	{
		this.msg.data(this.two, position);
		return this.two;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(this.data + this.dlen);
		sb.append("PDU header "); //$NON-NLS-1$
		sb.append(Strings.toHexString(this.header()));
		sb.append('\n');
		
		sb.append("plen: "); sb.append(this.getParamLength()); sb.append(" dlen: "); sb.append(this.dlen); sb.append('\n'); //$NON-NLS-1$ //$NON-NLS-2$
		
		sb.append("Parameter "); //$NON-NLS-1$
		sb.append(Strings.toHexString(this.params()));
		sb.append('\n');
		if (this.dlen > 0)
		{
			sb.append("Data     "); //$NON-NLS-1$
			sb.append(Strings.toHexString(this.data()));
			sb.append('\n');
		}
		return sb.toString();
	}

}
