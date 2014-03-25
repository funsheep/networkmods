/**
 * network Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.lodige.network.internal.VarMSGProtocol;
import com.lodige.network.msg.IMessage;

/**
 * TODO javadoc
 * @author renken
 */
public interface IProtocol
{
	
	public static final IProtocol VAR_MSG_PROTOCOL = new VarMSGProtocol();

	
	public void send(IMessage msg, OutputStream out) throws IOException;

	public IMessage read(InputStream in) throws IOException;

}
