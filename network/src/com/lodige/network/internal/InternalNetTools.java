/*
	This file is part of the d3fact network library.
	Copyright (C) 2007-2012 d3fact Project Team

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package com.lodige.network.internal;

import github.javaappplatform.commons.util.FastMersenneTwister;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

import com.lodige.network.INetworkAPI;
import com.lodige.network.msg.Converter;
import com.lodige.network.server.PortRange;

/**
 * TODO javadoc
 * @author funsheep
 */
public class InternalNetTools
{

	public static final int TIMEOUT = 10 * 1000;	//sec
	public static final boolean KEEP_ALIVE = true;
	public static final boolean LINGER_ON = true;
	public static final int LINGER_TIMEOUT = TIMEOUT;
	public static final boolean BUFFERING_DELAY = false;


	public static final void configureSocket(Socket socket) throws SocketException
	{
		socket.setKeepAlive(KEEP_ALIVE);
		socket.setSoLinger(LINGER_ON, LINGER_TIMEOUT);
		socket.setTcpNoDelay(!BUFFERING_DELAY);
	}


	public static final ServerSocket newServerSocket(PortRange range) throws IOException
	{
		final ServerSocket socket = new ServerSocket();
		PortRange.bind(socket, range);
		return socket;
	}

	public static final Socket newSocket(PortRange range) throws IOException
	{
		final Socket socket = new Socket();
		configureSocket(socket);
		PortRange.bind(socket, range);
		return socket;
	}

	public static final DatagramSocket newDatagramSocket(PortRange range) throws IOException
	{
		final DatagramSocket socket = new DatagramSocket();
		PortRange.bind(socket, range);
		return socket;
	}

	@SuppressWarnings("deprecation")
	public static final void sendMSG(final OutputStream out, Message msg) throws IOException
	{
		byte[] _long = new byte[8];
		final int length = msg.bodyType() == Message.BODYTYPE_BYTEARRAY ? msg.len() : msg.body2().size();
		Converter.putIntBig(_long, 0, length+InternalMessageAPI.LENGTH_TCP_HEADER);
		out.write(_long, 0, 4);
		Converter.putIntBig(_long, 0, msg.session());
		out.write(_long, 0, 4);
		Converter.putLongBig(_long, 0, msg.orderID());
		out.write(_long, 0, 8);
		Converter.putIntBig(_long, 0, msg.type());
		out.write(_long, 0, 4);
		if (msg.bodyType() == Message.BODYTYPE_BYTEARRAY)
			out.write(msg.body(), msg.off(), msg.len());
		else
		{
			int round = 0;
			int write = msg.body2().size();
			for (byte[] raw : msg.body2().getRAWData())
			{
				if (write <= 0)
					break;
				round = Math.min(raw.length, write);
				out.write(raw, 0, round);
				write -= round;
			}
		}
		out.flush();
	}

	public static final Message readTCPMSG(final InputStream in, final long timeout) throws IOException
	{
		final FutureTask<Message> _task = new FutureTask<Message>(new Callable<Message>()
		{
			@Override
			public Message call() throws Exception
			{
				return InternalNetTools.readTCPMSG(in);
			}
		});
		try
		{
			_task.run();
			return _task.get(timeout, TimeUnit.MILLISECONDS);
		}
		catch (ExecutionException e)
		{
			throw new IOException(e.getCause());
		}
		catch (InterruptedException e)
		{
			throw new IOException(e);
		}
		catch (TimeoutException e)
		{
			throw new IOException(e);
		}
	}

	public static final Message readTCPMSG(InputStream in) throws IOException
	{
		final byte[] buffer = new byte[8];
		if (!readData(in, buffer, 0, 4))	//length (int)
			return null;
		final int length = Converter.getIntBig(buffer, 0);
		if (length < InternalMessageAPI.LENGTH_TCP_HEADER)
			throw new IOException("Message body has negative size " + (length-InternalMessageAPI.LENGTH_TCP_HEADER));
		if (!readData(in, buffer, 0, 4))	//sessionID (int)
			throw new EOFException("Unexpected end of stream.");
		final int sessionID = Converter.getIntBig(buffer, 0);
		if (!readData(in, buffer, 0, 8))	//orderID (long)
			throw new EOFException("Unexpected end of stream.");
		final long orderID = Converter.getLongBig(buffer, 0);
		if (!readData(in, buffer, 0, 4))	//type (int)
			throw new EOFException("Unexpected end of stream.");
		final int msgType = Converter.getIntBig(buffer, 0);
		final byte[] data = new byte[length-InternalMessageAPI.LENGTH_TCP_HEADER];
		if (data.length > 0 && !readData(in, data))
			throw new EOFException("Unexpected end of stream.");
		return Message.receiveTCP(sessionID, orderID, msgType, data, 0, data.length);
	}

	public static final byte[] readRawMSG(final InputStream in, final long timeout) throws IOException
	{
		final FutureTask<byte[]> _task = new FutureTask<>(new Callable<byte[]>()
		{
			@Override
			public byte[] call() throws Exception
			{
				return InternalNetTools.readRawMSG(in);
			}
		});
		try
		{
			_task.run();
			return _task.get(timeout, TimeUnit.MILLISECONDS);
		}
		catch (ExecutionException e)
		{
			throw new IOException(e.getCause());
		}
		catch (InterruptedException e)
		{
			throw new IOException(e);
		}
		catch (TimeoutException e)
		{
			throw new IOException(e);
		}
	}

	public static final byte[] readRawMSG(final InputStream in) throws IOException
	{
		final byte[] _length = new byte[4];
		if (!readData(in, _length))
			return null;
		final int length = Converter.getIntBig(_length, 0);
		if (length < 0)
			throw new IOException("Message has negative size " + length);
		final byte[] data = new byte[length];
		if (length == 0)
			return data;
		if (!readData(in, data))
			throw new EOFException("Unexpected end of stream.");
		return data;
	}

	public static final boolean readData(final InputStream in, final byte[] buffer, final long timeout) throws IOException
	{
		final FutureTask<Boolean> _task = new FutureTask<>(new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return Boolean.valueOf(InternalNetTools.readData(in, buffer));
			}
		});
		try
		{
			_task.run();
			return _task.get(timeout, TimeUnit.MILLISECONDS).booleanValue();
		}
		catch (ExecutionException e)
		{
			throw new IOException(e.getCause());
		}
		catch (InterruptedException e)
		{
			throw new IOException(e);
		}
		catch (TimeoutException e)
		{
			throw new IOException(e);
		}
	}

	public static final boolean readData(InputStream in, byte[] buffer) throws IOException
	{
		return readData(in, buffer, 0, buffer.length);
	}

	public static final boolean readData(final InputStream in, final byte[] buffer, final int off, final int len, final long timeout) throws IOException
	{
		final FutureTask<Boolean> _task = new FutureTask<Boolean>(new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return Boolean.valueOf(InternalNetTools.readData(in, buffer, off, len));
			}
		});
		try
		{
			_task.run();
			return _task.get(timeout, TimeUnit.MILLISECONDS).booleanValue();
		}
		catch (ExecutionException e)
		{
			throw new IOException(e.getCause());
		}
		catch (InterruptedException e)
		{
			throw new IOException(e);
		}
		catch (TimeoutException e)
		{
			throw new IOException(e);
		}
	}

	public static final boolean readData(InputStream in, byte[] buffer, int off, int len) throws IOException
	{
		int eof = in.read(buffer, off, len);
		if (eof == -1)
			return false;
		off += eof;
		len -= eof;
		while (len > 0)
		{
			eof = in.read(buffer, off, len);
			if (eof == -1)
				throw new EOFException("Unexpected stream end detected.");
			off += eof;
			len -= eof;
		}
		return true;
	}


	/**
	 *
	 */
	private InternalNetTools()
	{
		//no instance
	}

}
