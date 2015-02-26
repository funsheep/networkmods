/**
 * network.nodave Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.network.s7.protocol;

import github.javaappplatform.commons.concurrent.Compute;
import github.javaappplatform.commons.events.Event;
import github.javaappplatform.commons.events.IListener;
import github.javaappplatform.platform.job.JobPlatform;

import java.io.IOException;

import com.lodige.network.INetworkAPI;
import com.lodige.network.client.ClientConnection;
import com.lodige.network.msg.IMessage;
import com.lodige.network.s7.plc.IPLC;
import com.lodige.network.s7.plc.IPLC.Internal;
import com.lodige.network.s7.protocol.INodaveAPI.Area;
import com.lodige.network.s7.protocol.impl.S7Protocol;
import com.lodige.network.s7.protocol.msg.PDUWriteBuilder;
import com.lodige.network.s7.protocol.msg.PDUWriteResult;

/**
 * TODO javadoc
 * @author renken
 */
public class Write
{
	
	private final class PDUResultListener implements IListener
	{
		
		private long msgID;
		private Compute compute;
		
		public void setup(long msgID, Compute compute)
		{
			this.msgID = msgID;
			this.compute = compute;
			Write.this.connection.addListener(INetworkAPI.E_MSG_RECEIVED, this);
		}
		
		public void setdown()
		{
			Write.this.connection.removeListener(this);
		}

		@Override
		public void handleEvent(Event e)
		{
			IMessage msg = e.getData();
			if (msg.type() == INodaveAPI.MSG_PDU_WRITE && msg.sendID() == this.msgID)
				this.compute.put(new PDUWriteResult(msg, Write.this.headerSize()));
		}
	}


	public class Write1
	{
		private Write1() {}
		
		public Write2 to(Area area)
		{
			Write.this.area = area;
			return new Write2();
		}
	}
	
	public class Write2
	{
		private Write2() {}
		
		public Write3 andDatabase(int dbNum)
		{
			Write.this.dbNum = dbNum;
			return new Write3();
		}
	}

	public class Write3
	{
		private Write3() {}
		
		public Write3 startAt(int start)
		{
			Write.this.start = start;
			return this;
		}
		
		public Write andWrite()
		{
			Write.this.addToWB();
			return Write.this;
		}
		
		public void execute() throws IOException
		{
			Write.this.addToWB();
			Write.this.connection.asyncSend(Write.this.wb.compile(null));
		}

		public PDUWriteResult andWaitForResult() throws IOException
		{
			return this.andWaitForResult(0);
		}

		public PDUWriteResult andWaitForResult(int timeout) throws IOException
		{
			Write.this.addToWB();
			Compute getter = new Compute();
			JobPlatform.runJob("Write Request to " + Write.this.connection.alias(), () ->
			{
				try
				{
					long msgID = Write.this.connection.asyncSend(Write.this.wb.compile(null));
					Write.this.resultListener.setup(msgID, getter);
				}
				catch (Exception e)
				{
					getter.error(e);
					
				}
			}, INetworkAPI.NETWORK_THREAD);
			try
			{
				return getter.<PDUWriteResult>get(timeout);
			}
			catch (Exception e)
			{
				if (e instanceof IOException)
					throw (IOException) e;
				throw new IOException(e);
			}
			finally
			{
				Write.this.resultListener.setdown();
			}
		}

	
		//FIXME this will not work correctly, for example, for the user it is not possible to abort this operation after some time.
//		public long andInformMeOnResult(IListener listener) throws IOException
//		{
//			Write.this.addToWB();
//			Compute getter = new Compute();
//			JobPlatform.runJob(() ->
//			{
//				try
//				{
//					long msgID = Write.this.connection.asyncSend(Write.this.wb.compile(null));
//					Write.this.connection.addListener(INetworkAPI.E_MSG_RECEIVED, new PDUResultListener(msgID, listener));
//					getter.put(Long.valueOf(msgID));
//				}
//				catch (Exception e)
//				{
//					getter.error(e);
//					
//				}
//			}, INetworkAPI.NETWORK_THREAD);
//			try
//			{
//				return getter.<Long>get().longValue();
//			}
//			catch (Exception e)
//			{
//				if (e instanceof IOException)
//					throw (IOException) e;
//				throw new IOException(e);
//			}
//		}

	}


	private final ClientConnection connection;
	private final PDUResultListener resultListener = new PDUResultListener();
	private final PDUWriteBuilder wb = new PDUWriteBuilder();
	private byte[] buffer;
	private Area area;
	private int dbNum;
	private int start = 0;
	

	private Write(ClientConnection connection)
	{
		this.connection = connection;
	}

	
	public Write1 data(byte[] buffer, int off, int len)
	{
		this.buffer = new byte[len];
		System.arraycopy(buffer, off, this.buffer, 0, len);
		return new Write1();
	}

	public Write1 data(byte... buffer)
	{
		this.buffer = buffer;
		return new Write1();
	}


	private void addToWB()
	{
		this.wb.addVarToWriteRequest(this.area, this.dbNum, this.start, this.buffer.length, this.buffer);
		this.area = null;
		this.buffer = null;
		this.dbNum = 0;
		this.start = 0;
	}
	
	private int headerSize()
	{
		return ((S7Protocol) Write.this.connection._protocol()).pduInHeaderSize();
	}

	public static final Write toPLC(ClientConnection connection)
	{
		return new Write(connection);
	}

	public static final Write toPLC(IPLC plc)
	{
		return new Write(((Internal) plc).connection());
	}

}
