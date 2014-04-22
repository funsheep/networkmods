/**
 * network.nodave Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.network.plc.protocol;

import github.javaappplatform.commons.log.Logger;

import java.io.IOException;
import java.net.Socket;

import com.lodige.network.IProtocol;
import com.lodige.network.msg.IMessage;
import com.lodige.network.plc.INodave;
import com.lodige.network.plc.msg.PDUBuilder;
import com.lodige.network.plc.msg.PDUResult;
import com.lodige.network.plc.util.Converter;
import com.lodige.network.plc.util.NodaveTools;

/**
 * TODO javadoc
 * @author renken
 */
public abstract class S7Protocol implements IProtocol
{

	protected static final Logger LOGGER = Logger.getLogger();


	protected final int pduInHeaderSize;
	protected int maxPDUlength;

	
	public S7Protocol(int pduInHeaderSize)
	{
		this.pduInHeaderSize = pduInHeaderSize;
	}


	public int pduInHeaderSize()
	{
		return this.pduInHeaderSize;
	}
	
	/*
		build the PDU for a PDU length negotiation    
	*/
	private static final byte PA_NEG_LENGTH[] = { (byte) 0xF0, 0, 0x00, 0x01, 0x00, 0x01, 0x03, (byte) 0xC0, }; //this.maxPDUlength / 0x100, //3,  this.maxPDUlength % 0x100, //0xC0,
	protected void negPDUlengthRequest(Socket socket) throws IOException
	{
		PDUBuilder p = new PDUBuilder();
		p.addParam(PA_NEG_LENGTH);
		
		this.send(p.compile(null), socket.getOutputStream());
		IMessage resmsg = this.read(socket.getInputStream());

		PDUResult result = new PDUResult(resmsg, this.pduInHeaderSize);
		if (result.getError() != INodave.RESULT_OK)
			throw new IOException(NodaveTools.strerror(result.getError()));
		this.maxPDUlength = Converter.USBEWord(result.params(), 6);
		LOGGER.debug("*** Partner offered PDU length: {}", Integer.valueOf(this.maxPDUlength));
	}

}
