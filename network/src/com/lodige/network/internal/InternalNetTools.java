/*
	This file is part of the d3fact network library.
	Copyright (C) 2007-2012 d3fact Project Team

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package com.lodige.network.internal;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

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

	public static final boolean readData(InputStream in, byte[] buffer) throws IOException
	{
		return readData(in, buffer, 0, buffer.length);
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
