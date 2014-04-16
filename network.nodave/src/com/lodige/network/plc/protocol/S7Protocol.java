/**
 * network.nodave Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.network.plc.protocol;

import github.javaappplatform.commons.log.Logger;

import java.io.IOException;

import com.lodige.network.internal.AStatefulProtocol;
import com.lodige.network.msg.IMessage;
import com.lodige.network.plc.Nodave;
import com.lodige.network.plc.internal.S7Message;
import com.lodige.network.plc.msg.PDUBuilder;
import com.lodige.network.plc.msg.PDUResult;

/**
 * TODO javadoc
 * @author renken
 */
public abstract class S7Protocol extends AStatefulProtocol
{

//	public final byte[] msgIn = new byte[Nodave.MAX_RAW_LEN];
//	public final byte[] msgOut = new byte[Nodave.MAX_RAW_LEN];
	
	protected static final Logger LOGGER = Logger.getLogger();
	
	// position in result data, incremented when variables are extracted without position
	private int dataPointer;
	// absolute begin of result data
	private final int udata = 0;

//	private final int startPDUin;
//	private final int startPDUout;
	protected int maxPDUlength;
//	protected Semaphore semaphore;


//	public S7Protocol(int startPDUin, int startPDUout)
//	{
//		this.startPDUin = startPDUin;
//		this.startPDUout = startPDUout;
//	}

//	public int readBytes(
//		int area,
//		int DBnum,
//		int start,
//		int len,
//		byte[] buffer) {
//		int res = 0;
//		semaphore.enter();
//		//		System.out.println("readBytes");
//		PDU p1 = new PDU(msgOut, PDUstartOut);
//		p1.initReadRequest();
//		p1.addVarToReadRequest(area, DBnum, start, len);
//
//		res = exchange(p1);
//		if (res != Nodave.RESULT_OK) {
//			semaphore.leave();
//			return res;
//		}
//		PDU p2 = new PDU(msgIn, PDUstartIn);
//		res = p2.setupReceivedPDU();
//		if ((Nodave.Debug & Nodave.DEBUG_CONN) != 0)
//			System.out.println(
//				"setupReceivedPDU() returned: " + res + Nodave.strerror(res));
//		if (res != Nodave.RESULT_OK) {
//			semaphore.leave();
//			return res;
//		}
//
//		res = p2.testReadResult();
//		if ((Nodave.Debug & Nodave.DEBUG_CONN) != 0)
//		System.out.println(
//			"testReadResult() returned: " + res + Nodave.strerror(res));
//		if (res != Nodave.RESULT_OK) {
//			semaphore.leave();
//			return res;
//		}
//		if (p2.udlen == 0) {
//			semaphore.leave();
//			return Nodave.RESULT_CPU_RETURNED_NO_DATA;
//		}
//		/*
//			copy to user buffer and setup internal buffer pointers:
//		*/
//		if (buffer != null)
//			System.arraycopy(p2.mem, p2.udata, buffer, 0, p2.udlen);
//
//		dataPointer = p2.udata;
//		udata = p2.udata;
////		answLen = p2.udlen;
//		semaphore.leave();
//		return res;
//	}
//
//	public PDU prepareReadRequest() {
//		int errorState = 0;
//		semaphore.enter();
//		PDU p1 = new PDU(msgOut, PDUstartOut);
//		p1.prepareReadRequest();
//		return p1;
//	}
//
//	/*
//	    Write len bytes to PLC memory area "area", data block DBnum. 
//	*/
//	public int writeBytes(
//		int area,
//		int DBnum,
//		int start,
//		int len,
//		byte[] buffer) {
//		int errorState = 0;
//		semaphore.enter();
//		PDU p1 = new PDU(msgOut, PDUstartOut);
//
//		//		p1.constructWriteRequest(area, DBnum, start, len, buffer);
//		p1.prepareWriteRequest();
//		p1.addVarToWriteRequest(area, DBnum, start, len, buffer);
//
//		errorState = exchange(p1);
//
//		if (errorState == 0) {
//			PDU p2 = new PDU(msgIn, PDUstartIn);
//			p2.setupReceivedPDU();
//
//			if (p2.mem[p2.param + 0] == PDU.FUNC_WRITE) {
//				if (p2.mem[p2.data + 0] == (byte) 0xFF) {
//					if ((Nodave.Debug & Nodave.DEBUG_CONN) != 0)
//						System.out.println("writeBytes: success");
//					semaphore.leave();
//					return 0;
//				}
//			} else {
//				errorState |= 4096;
//			}
//		}
//		semaphore.leave();
//		return errorState;
//	}
//
//	public class Semaphore {
//		private int value;
//
//		public Semaphore(int value) {
//			this.value = value;
//		}
//
//		public synchronized void enter() {
//			--value;
//			if (value < 0) {
//				try {
//					wait();
//				} catch (Exception e) {
//				}
//			}
//		}
//
//		public synchronized void leave() {
//			++value;
//			notify();
//		}
//	}
//
//	public int getResponse() {
//		return 0;
//	}
//
//	public int getPPIresponse() {
//		return 0;
//	}
//
//	public int sendMsg(PDU p) {
//		return 0;
//	}
//	public void sendRequestData(int alt) {
//	}
//
//	//	int numResults;
//	/*
//		class Result {
//			int error;
//			byte[] data;
//		}
//	*/
//	/*
//		Read a predefined set of values from the PLC. 
//		Return ok or an error state
//		If a buffer pointer is provided, data will be copied into this buffer.
//		If it's NULL you can get your data from the resultPointer in daveConnection long
//		as you do not send further requests.
//	*/
//	public ResultSet execReadRequest(PDU p) {
//		PDU p2;
//		int errorState;
//		errorState = exchange(p);
//
//		p2 = new PDU(msgIn, PDUstartIn);
//		p2.setupReceivedPDU();
//		/*		
//				if (p2.udlen == 0) {
//					dataPointer = 0;
//					answLen = 0;
//					return Nodave.RESULT_CPU_RETURNED_NO_DATA;
//				}
//		*/
//		ResultSet rs = new ResultSet();
//		if (p2.mem[p2.param + 0] == PDU.FUNC_READ) {
//			int numResults = p2.mem[p2.param + 1];
//			//			System.out.println("Results " + numResults);
//			rs.results = new Result[numResults];
//			int pos = p2.data;
//			for (int i = 0; i < numResults; i++) {
//				Result r = new Result();
//				r.error = Nodave.USByte(p2.mem, pos);
//				if (r.error == 255) {
//
//					int type = Nodave.USByte(p2.mem, pos + 1);
//					int len = Nodave.USBEWord(p2.mem, pos + 2);
//					r.error = 0;
//					//					System.out.println("Raw length " + len);
//					if (type == 4)
//						len /= 8;
//					else if (type == 3); //length is ok
//
//					//					System.out.println("Byte length " + len);
//					//					r.data = new byte[len];
//
//					//					System.arraycopy(p2.mem, pos + 4, r.data, 0, len);
//					//					Nodave.dump("Result " + i + ":", r.data, 0, len);
//					r.bufferStart = pos + 4;
//					pos += len;
//					if ((len % 2) == 1)
//						pos++;
//				} else {
//					System.out.println("Error " + r.error);
//				}
//				pos += 4;
//				rs.results[i] = r;
//			}
//			numResults = p2.mem[p2.param + 1];
//			rs.setNumResults(numResults);
//			dataPointer = p2.udata;
////			answLen = p2.udlen;
//			//		}    
//		} else {
//			errorState |= 2048;
//		}
//		semaphore.leave();
//		rs.setErrorState(errorState);
//		return rs;
//	}
//
//	public int useResult(ResultSet rs, int number) {
//		System.out.println(
//			"rs.getNumResults: " + rs.getNumResults() + " number: " + number);
//		if (rs.getNumResults() > number) {
//			dataPointer = rs.results[number].bufferStart;
//			return 0;
//			//			udata=rs.results[number].bufferStart;
//		}
//		return -33;
//	};
	
	protected abstract S7Message exchange(IMessage reg) throws IOException;

	/*
		build the PDU for a PDU length negotiation    
	*/
	private static final byte PA_NEG_LENGTH[] = { (byte) 0xF0, 0, 0x00, 0x01, 0x00, 0x01, 0x03, (byte) 0xC0, }; //this.maxPDUlength / 0x100, //3,  this.maxPDUlength % 0x100, //0xC0,
	protected void negPDUlengthRequest() throws IOException
	{
		PDUBuilder p = new PDUBuilder();
		p.addParam(PA_NEG_LENGTH);
		
		S7Message resmsg = this.exchange(p.compile());

		PDUResult result = new PDUResult(resmsg);
		if (result.getError() != Nodave.RESULT_OK)
			throw new IOException(Nodave.strerror(result.getError()));
		this.maxPDUlength = Nodave.USBEWord(result.params(), 6);
		LOGGER.debug("*** Partner offered PDU length: {}", Integer.valueOf(this.maxPDUlength));
	}

}
