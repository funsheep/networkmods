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

import com.lodige.network.msg.IMessage;

public class PDUResult
{

	private final S7Message msg;
	
	private int header; // the position of the header;
	private int param; // the position of the parameters;
//	private int hlen;
//	private int plen;
	private int dlen;
//	private int udlen;
	private int data;
//	private int udata;


	public PDUResult(IMessage msg)
	{
		if (!(msg instanceof S7Message) || ((S7Message) msg).headerSize() == 0)
			throw new IllegalArgumentException("This message was not recieved over a S7 protocol.");
		this.msg = (S7Message) msg;
		this.header = this.msg.headerSize();
		this.param = this.header + getHeaderLength(this.msg);
		this.data  = this.param + getParamLength(this.msg);
		this.dlen  = getDataLength(this.msg);
		
	}


	private static final int getHeaderLength(S7Message msg)
	{
		byte[] tmp = new byte[1];
		msg.data(tmp, msg.headerSize()+1);
		int headerLength = 10;
		if (tmp[0] == 2 || tmp[0] == 3)
			headerLength = 12;
		return headerLength;
	}
	
	private static final int getParamLength(S7Message msg)
	{
		byte[] tmp = new byte[2];
		msg.data(tmp, msg.headerSize() + 6);
		return Nodave.USBEWord(tmp, 0);
	}
	
	private static final int getDataLength(S7Message msg)
	{
		byte[] tmp = new byte[2];
		msg.data(tmp, msg.headerSize() + 8);
		return Nodave.USBEWord(tmp, 0);
	}
	

	/**
	 * return the number of the PDU
	 */
	public int getNumber()
	{
		byte[] tmp = new byte[2];
		this.msg.data(tmp, this.header + 4);
		return Nodave.USBEWord(tmp, 0);
	}

	/**
	 * return the function code of the PDU
	 */
	public int getFunc()
	{
		byte[] tmp = new byte[1];
		this.msg.data(tmp, this.param);
		return Nodave.USByte(tmp, 0);
	}


	public int getError()
	{
		if (this.param - this.header == 12)
		{
			byte[] tmp = new byte[2];
			this.msg.data(tmp, this.header + 10);
			return Nodave.USBEWord(tmp, 0);
		}
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

//	/*		
//			
//	*/
//	int testResultData()
//	{
//		int res = Nodave.RESULT_CANNOT_EVALUATE_PDU; // just assume the worst
//		if ((mem[data] == (byte)255) && (dlen > 4))
//		{
//			res = Nodave.RESULT_OK;
//			udata = data + 4;
//			// udlen=data[2]*0x100+data[3];
//			udlen = Nodave.USBEWord(mem, data + 2);
//			if (mem[data + 1] == 4)
//			{
//				udlen >>= 3; /* len is in bits, adjust */
//			}
//			else if (mem[data + 1] == 9)
//			{
//				/* len is already in bytes, ok */
//			}
//			else if (mem[data + 1] == 3)
//			{
//				/* len is in bits, but there is a byte per result bit, ok */
//			}
//			else
//			{
//				if ((Nodave.Debug & Nodave.DEBUG_PDU) != 0)
//					System.out.println("fixme: what to do with data type " + mem[data + 1]);
//				res = Nodave.RESULT_UNKNOWN_DATA_UNIT_SIZE;
//			}
//		}
//		else
//		{
//			res = mem[data];
//		}
//		return res;
//	}
//
//	int testReadResult()
//	{
//		if (mem[param] != INoDave.FUNC_READ)
//			return Nodave.RESULT_UNEXPECTED_FUNC;
//		return testResultData();
//	}
//
//	public int testPGReadResult()
//	{
//		if (mem[param] != 0)
//			return Nodave.RESULT_UNEXPECTED_FUNC;
//		return testResultData();
//	}
//
//	int testWriteResult()
//	{
//		int res = Nodave.RESULT_CANNOT_EVALUATE_PDU;
//		if (mem[param] != INoDave.FUNC_WRITE)
//			return Nodave.RESULT_UNEXPECTED_FUNC;
//		if ((mem[data] == 255))
//		{
//			res = Nodave.RESULT_OK;
//		}
//		else
//			res = mem[data];
//		if ((Nodave.Debug & Nodave.DEBUG_PDU) != 0)
//		{
//			dump();
//		}
//		return res;
//	}

}
