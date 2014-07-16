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

import github.javaappplatform.commons.collection.SemiDynamicByteArray;
import github.javaappplatform.commons.events.IListener;
import github.javaappplatform.commons.log.Logger;
import github.javaappplatform.commons.util.GenericsToolkit;
import github.javaappplatform.commons.util.Strings;

import com.lodige.network.internal.Message;
import com.lodige.network.msg.IMessage;
import com.lodige.network.plc.INodaveAPI;
import com.lodige.network.plc.util.Converter;

public class PDUBuilder
{
	
	protected static final Logger LOGGER = Logger.getLogger();
	
	/*
	 * typedef struct { uc P; // allways 0x32 uc type; // a type? type 2 and 3 headers are two bytes
	 * longer. uc a,b; // currently unknown us number; // Number, can be used to identify answers
	 * corresponding to requests us this.plen; // length of parameters which follow this header us this.dlen;
	 * // length of this.data which follows the parameters uc x[2]; // only present in type 2 and 3
	 * headers. This may contain error information. } PDUHeader;
	 */
	protected final SemiDynamicByteArray mem = new SemiDynamicByteArray(100);
	
	protected int param; // the position of the parameters;
	protected int hlen;
	protected int plen;
	protected int dlen;
	protected int udlen;
	protected int data;
	protected int udata;

	private final int pduType;


	public PDUBuilder()
	{
		this(1, INodaveAPI.MSG_PDU);
	}

	protected PDUBuilder(int type, int pduType)
	{
		this.initHeader(type);
		this.pduType = pduType;
	}


	/**
	 * reserve space for the header of a new PDU
	 */
	protected void initHeader(int type)
	{
		if (type == 2 || type == 3)
			this.hlen = 12;
		else
			this.hlen = 10;
		this.mem.cursor(0);
		for (int i = 0; i < this.hlen; i++)
		{
			this.mem.put((byte) 0);
		}
		this.param = this.hlen;
		this.mem.cursor(0);
		this.mem.put((byte)0x32);
		this.mem.put((byte)type);
		this.dlen = 0;
		this.plen = 0;
		this.udlen = 0;
		this.data = 0;
		this.udata = 0;
	}

	
	public int type()
	{
		return this.pduType;
	}

	public void addParam(byte[] pa)
	{
		this.plen = pa.length;
		this.mem.ensureSize(this.param + pa.length);
		this.mem.putAt(pa, 0, pa.length, this.param);
		Converter.setUSBEWord(this.mem, 6, this.plen);
		// this.mem[header + 6] = (byte) (pa.length / 256);
		// this.mem[header + 7] = (byte) (pa.length % 256);
		this.data = this.param + this.plen;
		this.dlen = 0;
	}

	/**
	 * Add this.data after parameters, set this.dlen as needed. Needs valid header and parameters
	 */
	protected void addData(byte[] newData)
	{
		this.addData(newData, newData.length);
	}

	/**
	 * Add len bytes of len after parameters from a maybe longer block of bytes. Set this.dlen as needed.
	 * Needs valid header and parameters
	 */
	protected void addData(byte[] newData, int len)
	{
		int appPos = this.data + this.dlen; // append to this position
		this.dlen += len;
		this.mem.ensureSize(appPos + len);
		this.mem.putAt(newData, 0, len, appPos);
		Converter.setUSBEWord(this.mem, 8, this.dlen);
	}

	/**
	 * Add values after value header in this.data, adjust this.dlen and this.data count. Needs valid
	 * header,parameters,this.data,this.dlen
	 */
	protected void addValue(byte[] values)
	{
		int valCount = 0x100 * this.mem.getDateFrom(this.data + 2) + this.mem.getDateFrom(this.data + 3);
		LOGGER.debug("valCount: {}", Integer.valueOf(valCount)); //$NON-NLS-1$

		final int type = this.mem.getDateFrom(this.data + 1);
		if (type == 4)
		{ // bit this.data, length is in bits
			valCount += 8 * values.length;
		}
		else if (type == 9 || type == 3)
		{ // byte this.data, length is in bytes
			valCount += values.length;
		}
		else
		{
			LOGGER.debug("unknown this.data type/length: {}", Integer.valueOf(type)); //$NON-NLS-1$
		}
		if (this.udata == 0)
			this.udata = this.data + 4;
		this.udlen += values.length;
		
		LOGGER.debug("valCount: {}", Integer.valueOf(valCount)); //$NON-NLS-1$
		Converter.setUSBEWord(this.mem, this.data + 2, valCount);
		addData(values);
	}

	/*
	 * add data in user data. Add a user data header, if not yet present.
	 */
	public void addUserData(byte[] da)
	{
		if (this.dlen == 0)
		{
			LOGGER.debug("adding user data header."); //$NON-NLS-1$
			final byte udh[] = { (byte)0xff, 9, 0, 0 };
			addData(udh);
		}
		addValue(da);
	}


	public final IMessage compile(IListener callback)
	{
		IMessage m = Message.create(this.type(), this.mem, this.hlen + this.plen + this.dlen, -1, callback);
		return GenericsToolkit.convertUnchecked(m);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(this.hlen + this.plen + this.dlen + this.udlen);
		sb.append("PDU header "); //$NON-NLS-1$
		sb.append(Strings.toHexString(this.mem.getDataFrom(new byte[this.hlen], 0)));
		sb.append('\n');
		
		sb.append("plen: "); sb.append(this.plen); sb.append(" dlen: "); sb.append(this.dlen); sb.append('\n'); //$NON-NLS-1$ //$NON-NLS-2$
		
		sb.append("Parameter "); //$NON-NLS-1$
		sb.append(Strings.toHexString(this.mem.getDataFrom(new byte[this.plen], this.param)));
		sb.append('\n');
		if (this.dlen > 0)
		{
			sb.append("Data     "); //$NON-NLS-1$
			sb.append(Strings.toHexString(this.mem.getDataFrom(new byte[this.dlen], this.data)));
			sb.append('\n');
		}
		if (this.udlen > 0)
		{
			sb.append("Result Data "); //$NON-NLS-1$
			sb.append(Strings.toHexString(this.mem.getDataFrom(new byte[this.udlen], this.udata)));
			sb.append('\n');
		}
		return sb.toString();
	}

}
