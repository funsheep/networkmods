/**
 * network.nodave Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.network.plc.protocol;

import github.javaappplatform.commons.collection.SemiDynamicByteArray;
import github.javaappplatform.commons.util.Strings;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import com.lodige.network.internal.InternalNetTools;
import com.lodige.network.internal.Message;
import com.lodige.network.msg.IMessage;
import com.lodige.network.plc.INodaveAPI;
import com.lodige.network.plc.util.Converter;
import com.lodige.network.plc.util.NodaveTools;

/**
 * TODO javadoc
 * @author renken
 */
public class TCPProtocol extends S7Protocol
{

	private static final int HEADER_LENGTH = 4;
	private static final int TCP_START_IN  = 7;
	//	public static final int TCP_START_OUT = 3;


	private final byte[] b4;
	private final byte rack;
	private final byte slot;
	private final byte commType;
	private int tPDUsize;


	public TCPProtocol()
	{
		this((byte) 0, (byte) 2, (byte) 1);
	}
	
	public TCPProtocol(byte rack, byte slot, byte commType)
	{
		super(TCPProtocol.TCP_START_IN);
		this.rack = rack;
		this.slot = slot;
		this.commType = commType;
		this.b4 = new byte[]
		{
			(byte)0x11,(byte)0xE0,(byte)0x00,
			(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,
			(byte)0xC1,2,1,0,
			(byte)0xC2,2,
			this.commType,
			(byte)(this.slot | (this.rack << 5)), // hope I got it right this time...
			(byte)0xC0,1,(byte)0x9
		};
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onConnect(Socket socket) throws IOException
	{
		assert LOGGER.trace("daveConnectPLC() step 1. rack: {} slot: {}", Integer.valueOf(this.rack), Integer.valueOf(this.slot)); //$NON-NLS-1$
		this.send(Message.create(INodaveAPI.MSG_OTHER, this.b4), socket.getOutputStream());

		IMessage msg = this.read(socket.getInputStream());
		assert LOGGER.trace("daveConnectPLC() step 1 - got {}", msg); //$NON-NLS-1$
		if (msg == null)
			throw new IOException("Could not connect to PLC."); //$NON-NLS-1$
		
		if (msg.size() == 22)
		{
			final byte[] header = new byte[msg.size()-2-6];
			msg.data(header, 6);
			for (int i = 0; i < header.length; i++)
			{
				if (header[i] == (byte)0xc0)
				{
					this.tPDUsize = 128 << (header[i+2] - 7);
					assert LOGGER.trace("tPDU size: {}", Integer.valueOf(this.tPDUsize)); //$NON-NLS-1$
				}
			}
		}
		this.negPDUlengthRequest(socket);
	}


	private static final byte[] PDU_HEADER = { (byte)0x02, (byte)0xf0, (byte)0x80 };
	private final byte[] sendBuffer = new byte[400];
	private final byte[] pduHeader = new byte[400];


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void send(IMessage msg, OutputStream out) throws IOException
	{
		LOGGER.debug("Send Msg: {}", msg); //$NON-NLS-1$
		final int headerSize = HEADER_LENGTH + (((msg.type() & INodaveAPI.MSG_PDU) != 0) ? 3 : 0);
		if (this.maxPDUlength > 0 && headerSize + msg.size() > this.maxPDUlength)
			throw new IOException("Could not send message, as message size "+(headerSize+msg.size())+" excedes maxPDUlength " + this.maxPDUlength + ".");
		
		out.write(0x03);
		out.write(0x0);
		out.write((msg.size()+headerSize) / 0x100);
		out.write((msg.size()+headerSize) % 0x100);

		
		InputStream dataIn = msg.data();
		assert LOGGER.trace("send packet header: {} ", Strings.toHexString(new byte[] { 0x03, 0, (byte)((msg.size()+headerSize) / 0x100), (byte)((msg.size()+headerSize) % 0x100)}, 0, 4)); //$NON-NLS-1$
		if ((msg.type() & INodaveAPI.MSG_PDU) != 0)
		{
			LOGGER.trace("send PDU header: {} ", Strings.toHexString(PDU_HEADER, 0, PDU_HEADER.length)); //$NON-NLS-1$
			out.write(PDU_HEADER);
			int len = dataIn.read(this.pduHeader);
			if (len == -1)
				throw new IOException("Unexpected end of stream"); //$NON-NLS-1$
			Converter.setUSBEWord(this.pduHeader, 4, (int) ((Message) msg).sendID());
			out.write(this.pduHeader, 0, len);
		}
		
		int len;
		assert LOGGER.trace("payload:"); //$NON-NLS-1$
		while ((len = dataIn.read(this.sendBuffer)) != -1)
		{
			out.write(this.sendBuffer, 0, len);
			assert LOGGER.trace(Strings.toHexString(this.sendBuffer, 0, len));
		}
		out.flush();
	}
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IMessage read(InputStream in) throws IOException
	{
		SemiDynamicByteArray array = new SemiDynamicByteArray();

		final byte[] header = new byte[4];
		if (!InternalNetTools.readData(in, header, 0, header.length))	//header
			return null;
		
		array.putAll(header);
	
		int length = header[3]+0x100*header[2];
		if (length < HEADER_LENGTH)
			throw new IOException("Message body has negative size " + (length - HEADER_LENGTH)); //$NON-NLS-1$

		byte[] data = new byte[length - HEADER_LENGTH];
		if (data.length > 0 && !InternalNetTools.readData(in, data, 0, data.length))
			throw new EOFException("Unexpected end of stream."); //$NON-NLS-1$
		array.putAll(data);
		
		boolean follow = (0xff & data[1]) == 0xf0 && (data[2] & 0x80) == 0;
		while (follow)
		{
			assert LOGGER.trace("read more data: {}", Byte.valueOf(header[2])); //$NON-NLS-1$
			final byte[] lheader = new byte[7];
			if (!InternalNetTools.readData(in, lheader, 0, lheader.length))
				break;
			
			length = lheader[3]+0x100*lheader[2];
			assert LOGGER.trace("read more data length: {}", Integer.valueOf(length)); //$NON-NLS-1$
			data = new byte[length-7];
			if (!InternalNetTools.readData(in, data, 0, data.length))
				break;
			array.putAll(data);
			
			assert LOGGER.trace("Read payload:", Strings.toHexString(data)); //$NON-NLS-1$
			follow = (0xff & lheader[5]) == 0xf0 && (lheader[6] & 0x80) == 0;
		}
		
		assert LOGGER.trace("read message of {} bytes: {}", Integer.valueOf(array.size()), Strings.toHexString(array.getData())); //$NON-NLS-1$
		
		Message msg = Message.create(NodaveTools.msgType(array, TCP_START_IN), array, array.size(), null);
		LOGGER.debug("Read Msg: {}", msg); //$NON-NLS-1$
		return msg;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onDisconnect(Socket socket)
	{
		//do nothing
	}

}
