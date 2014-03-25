/**
 * network Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.network.internal;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.lodige.network.IProtocol;
import com.lodige.network.msg.Converter;
import com.lodige.network.msg.IMessage;

/**
 * TODO javadoc
 * @author renken
 */
public class VarMSGProtocol implements IProtocol
{

	public static final int HEADER_LENGTH = 8;


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void send(IMessage _msg, OutputStream out) throws IOException
	{
		Message msg = (Message) _msg;
		byte[] _long = new byte[8];
		Converter.putIntBig(_long, 0, msg.length()+HEADER_LENGTH);
		out.write(_long, 0, 4);
		Converter.putIntBig(_long, 0, msg.type());
		out.write(_long, 0, 4);
		InputStream in = _msg.data();

		byte[] buffer = new byte[8096];
		int len;
		while ((len = in.read(buffer)) != -1)
		{
			out.write(buffer, 0, len);
			if (Thread.interrupted())
				break;
		}
		out.flush();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IMessage read(InputStream in) throws IOException
	{
		final byte[] buffer = new byte[8];
		if (!InternalNetTools.readData(in, buffer, 0, 4))	//length (int)
			return null;
		final int length = Converter.getIntBig(buffer, 0);
		if (length < HEADER_LENGTH)
			throw new IOException("Message body has negative size " + (length - HEADER_LENGTH));
		if (!InternalNetTools.readData(in, buffer, 0, 4))	//type (int)
			throw new EOFException("Unexpected end of stream.");
		final int msgType = Converter.getIntBig(buffer, 0);
		final byte[] data = new byte[length - HEADER_LENGTH];
		if (data.length > 0 && !InternalNetTools.readData(in, data))
			throw new EOFException("Unexpected end of stream.");
		return Message.create(msgType, data);
	}

}
