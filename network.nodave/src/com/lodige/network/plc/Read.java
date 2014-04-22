/**
 * network.nodave Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.network.plc;

import github.javaappplatform.commons.events.Event;
import github.javaappplatform.commons.events.IListener;
import github.javaappplatform.commons.util.GenericsToolkit;
import github.javaappplatform.platform.job.JobPlatform;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.lodige.network.INetworkAPI;
import com.lodige.network.client.ClientConnection;
import com.lodige.network.msg.IMessage;
import com.lodige.network.plc.INodaveAPI.Area;
import com.lodige.network.plc.msg.PDUReadBuilder;
import com.lodige.network.plc.msg.PDUReadResult;
import com.lodige.network.plc.protocol.S7Protocol;
import com.lodige.network.plc.util.NodaveTools;

/**
 * TODO javadoc
 * @author renken
 */
public class Read
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
			if (msg.type() == INodaveAPI.MSG_PDU_READ)
			{
				int gotID = NodaveTools.getPDUNumber(msg, Read.this.headerSize());
				if (gotID == this.msgID)
					this.callback.handleEvent(new Event(e.getSource(), INodaveAPI.E_PDU_RESULT_RECIEVED, new PDUReadResult(msg, Read.this.headerSize())));
			}
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
		
		private class Getter
		{
			private final ReentrantLock lock = new ReentrantLock();
			private final Condition waitForResult = this.lock.newCondition();
			private Object result = null;
			
			
			public void put(Object result)
			{
				if (result == null)
					throw new IllegalArgumentException();
				this.lock.lock();
				try
				{
					this.result = result;
					this.waitForResult.signalAll();
				}
				finally
				{
					this.lock.unlock();
				}
			}
			
			public void error(Exception e)
			{
				this.put(e);
			}
			
			public <O extends Object> O get() throws Exception
			{
				this.lock.lock();
				try
				{
					while (this.result == null)
						if (!this.waitForResult.await(INetworkAPI.CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS))
							throw new TimeoutException();
					if (this.result instanceof Exception)
						throw (Exception) this.result;
					return GenericsToolkit.convertUnchecked(this.result);
				}
				finally
				{
					this.lock.unlock();
				}
			}
		}
		
		public PDUReadResult andWaitForResult() throws IOException
		{
			Read.this.addToRB();
			Getter getter = new Getter();
			JobPlatform.runJob(() ->
			{
				try
				{
					long msgID = Read.this.connection.asyncSend(Read.this.rb.compile(null));
					Read.this.connection.addListener(INetworkAPI.E_MSG_RECEIVED, new PDUResultListener(msgID, (e) ->
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
				return getter.<PDUReadResult>get();
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
			Read.this.addToRB();
			Getter getter = new Getter();
			JobPlatform.runJob(() ->
			{
				try
				{
					long msgID = Read.this.connection.asyncSend(Read.this.rb.compile(null));
					Read.this.connection.addListener(INetworkAPI.E_MSG_RECEIVED, new PDUResultListener(msgID, listener));
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
