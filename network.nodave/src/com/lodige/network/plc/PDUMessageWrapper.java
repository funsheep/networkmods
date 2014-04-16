/**
 * network.nodave Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.network.plc;

import java.io.IOException;
import java.io.InputStream;

import com.lodige.network.msg.IMessage;

/**
 * TODO javadoc
 * @author renken
 */
class PDUMessageWrapper implements IMessage
{

	private final byte[] pduHeader;
	private final IMessage msg;


	/**
	 * 
	 */
	public PDUMessageWrapper(byte[] pduHeader, IMessage msg)
	{
		this.pduHeader = pduHeader;
		this.msg = msg;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void data(byte[] dest, int off)
	{
		int remaining = dest.length;
		if (off < this.pduHeader.length)
		{
			System.arraycopy(this.pduHeader, off, dest, 0, this.pduHeader.length - off);
			remaining -= (this.pduHeader.length - off);
		}
		if (remaining > 0)
		{
			byte[] tmp = new byte[remaining];
			this.msg.data(tmp, off - this.pduHeader.length);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InputStream data()
	{
		return new InputStream()
		{
			private InputStream wrapped = msg.data();
			private int position = 0;
			
			@Override
			public int read() throws IOException
			{
				if (this.position < pduHeader.length)
					return pduHeader[this.position++];
				return this.wrapped.read();
			}
		};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int size()
	{
		return this.pduHeader.length + this.msg.size();
	}

}
