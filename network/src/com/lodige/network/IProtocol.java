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
	
	public void onConnect(Socket socket) throws IOException;
	
	public interface Stateless extends IProtocol
	{
		
		public void send(IMessage msg, OutputStream out) throws IOException;
	
		public IMessage read(InputStream in) throws IOException;
	
		public void onDisconnect(Socket socket);
	}
	
	public interface Stateful extends IProtocol
	{
		
		public void send(IMessage msg) throws IOException;
	
		public IMessage read() throws IOException;
	
		public void onDisconnect();
	}

}
