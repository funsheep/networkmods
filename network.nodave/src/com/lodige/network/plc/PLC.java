/**
 * network.nodave Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.network.plc;

import github.javaappplatform.commons.util.GenericsToolkit;
import github.javaappplatform.platform.job.JobPlatform;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;

import java.io.IOException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.lodige.network.INetworkAPI;
import com.lodige.network.INetworkService;
import com.lodige.network.client.ClientConnection;
import com.lodige.network.internal.Message;
import com.lodige.network.msg.IMessage;
import com.lodige.network.plc.msg.PDUBuilder;
import com.lodige.network.plc.msg.PDUReadResult;
import com.lodige.network.plc.msg.PDUResult;
import com.lodige.network.plc.msg.PDUWriteResult;
import com.lodige.network.plc.protocol.S7Protocol;
import com.lodige.network.server.PortRange;

/**
 * TODO javadoc
 * 
 * @author renken
 */
public class PLC extends ClientConnection
{
	
	private final class GetPDUResult
	{

		private final ReentrantLock lock = new ReentrantLock();
		private final Condition waitForFinish = this.lock.newCondition();

		private final int pduType;
		private IMessage result;


		/**
		 * @param name
		 */
		public GetPDUResult(int pduType)
		{
			this.pduType = pduType;
		}


		protected final void _msgRecieved(IMessage msg)
		{
			this.lock.lock();
			try
			{
				this.result = msg;
				this.waitForFinish.signalAll();
			}
			finally
			{
				this.lock.unlock();
			}
		}


		public final <P extends PDUResult> P get() throws InterruptedException
		{
			this.lock.lock();
			try
			{
				if (!INetworkAPI.NETWORK_THREAD.equals(JobPlatform.getCurrentThread()))
				{
					while (this.result == null)
						this.waitForFinish.await();
				}
				PDUResult ret = null;
				switch (this.pduType)
				{
					default:
					case INodave.MSG_PDU:
						ret = new PDUResult(this.result, PLC.this._protocol().pduInHeaderSize());
						break;
					case INodave.MSG_PDU_READ:
						ret = new PDUReadResult(this.result, PLC.this._protocol().pduInHeaderSize());
						break;
					case INodave.MSG_PDU_WRITE:
						ret = new PDUWriteResult(this.result, PLC.this._protocol().pduInHeaderSize());
						break;
				}
				return GenericsToolkit.<P>convertUnchecked(ret);
			}
			finally
			{
				this.lock.unlock();
			}
		}

	}
	
	private final ReentrantLock waitLock = new ReentrantLock();
	private final TLongObjectMap<GetPDUResult> waiting = new TLongObjectHashMap<>();
	

	public PLC(String remoteHost, int remotePort, PortRange localPortRange, String networkservice) throws IOException
	{
		super(remoteHost, remotePort, localPortRange, networkservice);
	}

	public PLC(String remoteHost, int remotePort, PortRange localPortRange, INetworkService service) throws IOException
	{
		super(remoteHost, remotePort, localPortRange, service);
	}

	
	public <P extends PDUResult> P sendPDU(PDUBuilder builder) throws IOException
	{
		GetPDUResult get = null;
		this.waitLock.lock();
		try
		{
			long msgID = this.asyncSend(builder.compile(null));
			get = new GetPDUResult(builder.type());
			this.waiting.put(msgID, get);
		}
		finally
		{
			this.waitLock.unlock();
		}
		try
		{
			return get.get();
		}
		catch (InterruptedException e)
		{
			throw new IOException("Could not request data.", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void _put(Message msg)
	{
		if (PDUResult.isValidPDUResult(msg, this._protocol().pduInHeaderSize()))
		{
			PDUResult result = new PDUResult(msg, this._protocol().pduInHeaderSize());
			this.waitLock.lock();
			try
			{
				GetPDUResult get = this.waiting.remove(result.getNumber());
				if (get != null)
				{
					get._msgRecieved(msg);
					return;
				}
			}
			finally
			{
				this.waitLock.unlock();
			}
		}
		super._put(msg);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public S7Protocol _protocol()
	{
		return (S7Protocol) super._protocol();
	}

}
