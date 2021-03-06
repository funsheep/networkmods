/**
 * network Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import com.lodige.network.msg.IMessage;

/**
 * TODO javadoc
 * @author renken
 */
public interface IProtocol
{
	
	public long nextSendID();

	
	public void onConnect(Socket socket) throws IOException;
	
	public void send(IMessage msg, OutputStream out) throws IOException;

	public IMessage read(InputStream in) throws IOException;

	public void onDisconnect(Socket socket);
	
}
