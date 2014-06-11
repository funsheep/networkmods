/**
 * network.nodave Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.network.plc;

import github.javaappplatform.commons.concurrent.Compute;
import github.javaappplatform.commons.events.Event;
import github.javaappplatform.commons.events.IListener;
import github.javaappplatform.platform.job.JobPlatform;

import java.io.IOException;

import com.lodige.network.INetworkAPI;
import com.lodige.network.client.ClientConnection;
import com.lodige.network.msg.IMessage;
import com.lodige.network.plc.INodaveAPI.Area;
import com.lodige.network.plc.msg.PDUWriteBuilder;
import com.lodige.network.plc.msg.PDUWriteResult;
import com.lodige.network.plc.protocol.S7Protocol;
import com.lodige.network.plc.util.NodaveTools;

/**
 * TODO javadoc
 * @author renken
 */
public class Write
{
	
	private final class PDUResultListener implements IListener
	{
		
		private final long msgID;
		private final IListener callback;
		
		public PDUResultListener(long msgID, IListener callback)
		{
			this.msgID = msgID;
			this.callback = callback;
		}

		@Override
		public void handleEvent(Event e)
		{
			IMessage msg = e.getData();
			if (msg.type() == INodaveAPI.MSG_PDU_WRITE)
			{
				int gotID = NodaveTools.getPDUNumber(msg, Write.this.headerSize());
				if (gotID == this.msgID)
					this.callback.handleEvent(new Event(e.getSource(), INodaveAPI.E_PDU_RESULT_RECIEVED, new PDUWriteResult(msg, Write.this.headerSize())));
			}
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
			JobPlatform.runJob(() ->
			{
				try
				{
					long msgID = Write.this.connection.asyncSend(Write.this.wb.compile(null));
					Write.this.connection.addListener(INetworkAPI.E_MSG_RECEIVED, new PDUResultListener(msgID, (e) ->
					{
						getter.put(e.getData());
					}));
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
		}

		public long andInformMeOnResult(IListener listener) throws IOException
		{
			Write.this.addToWB();
			Compute getter = new Compute();
			JobPlatform.runJob(() ->
			{
				try
				{
					long msgID = Write.this.connection.asyncSend(Write.this.wb.compile(null));
					Write.this.connection.addListener(INetworkAPI.E_MSG_RECEIVED, new PDUResultListener(msgID, listener));
					getter.put(Long.valueOf(msgID));
				}
				catch (Exception e)
				{
					getter.error(e);
					
				}
			}, INetworkAPI.NETWORK_THREAD);
			try
			{
				return getter.<Long>get().longValue();
			}
			catch (Exception e)
			{
				if (e instanceof IOException)
					throw (IOException) e;
				throw new IOException(e);
			}
		}

	}


	private final ClientConnection connection;
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

}
