/*
	This file is part of the d3fact network library.
	Copyright (C) 2007-2012 d3fact Project Team

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package com.lodige.network.msg;

import java.io.IOException;
import java.io.InputStream;

public class MessageReader
{

	private final byte[] four  = new byte[4];
	private final byte[] eight = new byte[8];

	private IMessage msg;
	private InputStream data;


	public MessageReader()
	{
		//empty constructor
		//first call would have to be to reset.
	}

	public MessageReader(IMessage msg)
	{
		this.reset(msg);
	}


	public void reset(IMessage _msg)
	{
		this.msg = _msg;
		this.data = _msg.data();
	}

	public IMessage current()
	{
		return this.msg;
	}

	public int readInt()
	{
		this.readStream(this.four);
		return Converter.getIntBig(this.four, 0);
	}


	public long readLong()
	{
		this.readStream(this.eight);
		return Converter.getLongBig(this.eight, 0);
	}


	public byte[] readByteArray(int size)
	{
		byte[] read = new byte[size];
		this.readStream(read);
		return read;
	}


	public boolean readBoolean()
	{
		this.readStream(this.four, 1);
		return Converter.getBooleanBig(this.four, 0);
	}


	public byte readByte()
	{
		this.readStream(this.four, 1);
		return this.four[0];
	}

	public double readDouble()
	{
		this.readStream(this.eight);
		return Converter.getDoubleBig(this.eight, 0);
	}

	public float readFloat()
	{
		this.readStream(this.four);
		return Converter.getFloatBig(this.four, 0);
	}

	public short readShort()
	{
		throw new UnsupportedOperationException();
	}


	public String readString()
	{
		int length = this.readInt();
		byte[] str = this.readByteArray(length);
		return new String(str, Converter.DEFAULT_CHARSET);
	}

	private final void readStream(byte[] arr)
	{
		this.readStream(arr, arr.length);
	}
	
	private final void readStream(byte[] arr, int len)
	{
		try
		{
			int amount = this.data.read(arr, 0, len);
			if (amount != len)
				throw new RuntimeException("Wrong message format. Tried to read " + len + " bytes, but read " + amount); //$NON-NLS-1$ //$NON-NLS-2$
		}
		catch (IOException e)
		{
			throw new RuntimeException("Should not happen.", e); //$NON-NLS-1$
		}
	}

}
