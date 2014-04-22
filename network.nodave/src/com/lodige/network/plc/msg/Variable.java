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

import java.nio.ByteBuffer;

import com.lodige.network.msg.IMessage;
import com.lodige.network.plc.INodaveAPI.Result;

/**
 * 
 */
public class Variable
{
	public final int error;
	public final int length;
	private int bufferStart;
	private IMessage msg;
	
	
	public Variable(int error)
	{
		this(error, -1, -1);
	}

	public Variable(int length, int bufferStart, IMessage msg)
	{
		this(Result.OK.code, length, bufferStart);
		this.msg = msg;
	}

	private Variable(int error, int length, int bufferStart)
	{
		this.error = error;
		this.length = length;
		this.bufferStart = bufferStart;
	}

	
	public byte[] data() throws PDUResultException
	{
		if (this.error != Result.OK.code)
			throw new PDUResultException(this.error);
		byte[] data = new byte[this.length];
		this.msg.data(data, this.bufferStart);
		return data;
	}

	public byte getByte() throws PDUResultException
	{
		return this.data()[0];
	}

	public float getInt() throws PDUResultException
	{
		return ByteBuffer.allocate(4).put(this.data()).getInt(0);
	}


	public float getFloat() throws PDUResultException
	{
		return ByteBuffer.allocate(4).put(this.data()).getFloat(0);
	}


}
