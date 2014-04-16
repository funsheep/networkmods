/**
 * network.nodave Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.network.plc;

import github.javaappplatform.commons.collection.SemiDynamicByteArray;
import github.javaappplatform.commons.util.Strings;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import com.lodige.network.internal.InternalNetTools;
import com.lodige.network.internal.Message;
import com.lodige.network.msg.IMessage;

/**
 * TODO javadoc
 * @author renken
 */
public class TCPProtocol extends S7Protocol
{

	private static final int HEADER_LENGTH = 4;


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
//		super(INoDave.TCP_START_IN, INoDave.TCP_START_OUT);
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
		super.onConnect(socket);
		LOGGER.debug("daveConnectPLC() step 1. rack: {} slot: {}", Integer.valueOf(this.rack), Integer.valueOf(this.slot));
		this.send(Message.create(0, this.b4));

		IMessage msg = this.read();
		LOGGER.debug("daveConnectPLC() step 1 - got {}", msg);
		
		if (msg.size() != 22)
			throw new IOException("Could not retreive tPDUsize.");

		final byte[] header = new byte[msg.size()-2-6];
		msg.data(header, 6);
		for (int i = 0; i < header.length; i++)
		{
			if (header[i] == (byte)0xc0)
			{
				this.tPDUsize = 128 << (header[i+2] - 7);
				LOGGER.debug("tPDU size: {}", Integer.valueOf(this.tPDUsize));
			}
		}
		this.negPDUlengthRequest();
	}


	private static final byte[] SEND_BUFFER = new byte[400];
	private static final byte[] SEND_HEADER = { (byte) 0x03, (byte)0x0, 0, 0, (byte)0x02, (byte)0xf0, (byte)0x80 };
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void send(IMessage msg) throws IOException
	{
		final byte[] header = new byte[SEND_HEADER.length];
		System.arraycopy(SEND_HEADER, 0, header, 0, SEND_HEADER.length);
		
		final int headerSize = (msg instanceof PDUMessage) ? SEND_HEADER.length : SEND_HEADER.length-3;
		header[2] = (byte) ((msg.size()+headerSize) / 0x100);
		header[3] = (byte) ((msg.size()+headerSize) % 0x100);
		LOGGER.debug("send packet header: {} ", Strings.toHexString(header, 0, headerSize));
		this.out.write(header, 0, headerSize);
		
		InputStream dataIn = msg.data();
		int len;
		LOGGER.debug("payload:");
		while ((len = dataIn.read(SEND_BUFFER)) != -1)
		{
			this.out.write(SEND_BUFFER, 0, len);
			LOGGER.debug(Strings.toHexString(SEND_BUFFER, 0, len));
			if (Thread.interrupted())
				break;
		}
		this.out.flush();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public S7Message read() throws IOException
	{
		SemiDynamicByteArray array = new SemiDynamicByteArray();

		final byte[] header = new byte[4];
		if (!InternalNetTools.readData(this.in, header, 0, header.length))	//header
			return null;
		array.putAll(header);
	
		int length = header[3]+0x100*header[2];
		if (length < HEADER_LENGTH)
			throw new IOException("Message body has negative size " + (length - HEADER_LENGTH));

		byte[] data = new byte[length - HEADER_LENGTH];
		if (data.length > 0 && !InternalNetTools.readData(this.in, data, 0, data.length))
			throw new EOFException("Unexpected end of stream.");
		LOGGER.debug("read package of {} bytes: {}", Integer.valueOf(data.length), Strings.toHexString(data));
		array.putAll(data);
		
		boolean follow = ((data[1]==0xf0)&& ((data[2] & 0x80)==0) );
		while (follow)
		{
			LOGGER.debug("read more data: {}", Byte.valueOf(header[2]));
			final byte[] lheader = new byte[7];
			if (!InternalNetTools.readData(this.in, lheader, 0, lheader.length))
				break;
			
			length = lheader[3]+0x100*lheader[2];
			LOGGER.debug("read more data length: {}", Integer.valueOf(length));
			data = new byte[length-7];
			if (!InternalNetTools.readData(this.in, data, 0, data.length))
				break;
			array.putAll(data);
			
			LOGGER.debug("Read payload:", Strings.toHexString(data));
			follow=((lheader[5]==0xf0) && ((lheader[6] & 0x80)==0) );
		}
		
		return S7Message.create(INoDave.TCP_START_IN, array, array.size(), null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected S7Message exchange(PDUMessage req) throws IOException
	{
		LOGGER.debug(" enter TCP.Exchange");
		this.send(req);
		return this.read();
	}

}
