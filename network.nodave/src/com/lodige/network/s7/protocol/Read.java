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
import com.lodige.network.s7.protocol.INodaveAPI.Area;
import com.lodige.network.s7.protocol.impl.S7Protocol;
import com.lodige.network.s7.protocol.msg.PDUReadBuilder;
import com.lodige.network.s7.protocol.msg.PDUReadResult;

/**
 * TODO javadoc
 * @author renken
 */
public class Read
{
	
	private final class PDUResultListener implements IListener
	{
		
		private long msgID;
		private Compute compute;
		
		public void setup(long msgID, Compute compute)
		{
			this.msgID = msgID;
			this.compute = compute;
			Read.this.connection.addListener(INetworkAPI.E_MSG_RECEIVED, this);
		}
		
		public void setdown()
		{
			Read.this.connection.removeListener(this);
		}

		@Override
		public void handleEvent(Event e)
		{
			IMessage msg = e.getData();
			if (msg.type() == INodaveAPI.MSG_PDU_READ && msg.sendID() == this.msgID)
				this.compute.put(new PDUReadResult(msg, Read.this.headerSize()));
		}
	}
	
	public class Read1
	{
		private Read1() {}
		
		public Read2 from(Area area)
		{
			Read.this.area = area;
			return new Read2();
		}
	}
	
	public class Read2
	{
		private Read2() {}
		
		public Read3 andDatabase(int dbNum)
		{
			Read.this.dbNum = dbNum;
			return new Read3();
		}
	}

	public class Read3
	{
		private Read3() {}
		
		public Read3 startAt(int start)
		{
			Read.this.start = start;
			return this;
		}
		
		public Read andRead()
		{
			Read.this.addToRB();
			return Read.this;
		}
		

		public PDUReadResult andWaitForResult() throws IOException
		{
			return this.andWaitForResult(0);
		}

		public PDUReadResult andWaitForResult(int timeout) throws IOException
		{
			Read.this.addToRB();
			Compute getter = new Compute();
			JobPlatform.runJob(() ->
			{
				try
				{
					long msgID = Read.this.connection.asyncSend(Read.this.rb.compile(null));
					Read.this.resultListener.setup(msgID, getter);
				}
				catch (Exception e)
				{
					getter.error(e);
				}
			}, INetworkAPI.NETWORK_THREAD);
			try
			{
				return getter.<PDUReadResult>get(timeout);
			}
			catch (Exception e)
			{
				if (e instanceof IOException)
					throw (IOException) e;
				throw new IOException(e);
			}
			finally
			{
				Read.this.resultListener.setdown();
			}
		}

		//FIXME this will not work correctly, for example, for the user it is not possible to abort this operation after some time.
//		public long andInformMeOnResult(IListener listener) throws IOException
//		{
//			Read.this.addToRB();
//			Compute getter = new Compute();
//			JobPlatform.runJob(() ->
//			{
//				IListener resultListener = null;
//				try
//				{
//					long msgID = Read.this.connection.asyncSend(Read.this.rb.compile(null));
//					resultListener = new PDUResultListener(msgID, listener);
//					Read.this.connection.addListener(INetworkAPI.E_MSG_RECEIVED, resultListener);
//					getter.put(Long.valueOf(msgID));
//				}
//				catch (Exception e)
//				{
//					if (resultListener != null)
//						Read.this.connection.removeListener(listener);
//					getter.error(e);
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
	private final PDUReadBuilder rb = new PDUReadBuilder();
	private int byteCount;
	private Area area;
	private int dbNum;
	private int start = 0;
	

	private Read(ClientConnection connection)
	{
		this.connection = connection;
	}

	
	public Read1 bytes(int count)
	{
		this.byteCount = count;
		return new Read1();
	}

	private void addToRB()
	{
		this.rb.addVarToReadRequest(this.area, this.dbNum, this.start, this.byteCount);
		this.area = null;
		this.dbNum = 0;
		this.start = 0;
		this.byteCount = 0;
	}
	
	private int headerSize()
	{
		return ((S7Protocol) Read.this.connection._protocol()).pduInHeaderSize();
	}

	public static final Read fromPLC(ClientConnection connection)
	{
		return new Read(connection);
	}

}
