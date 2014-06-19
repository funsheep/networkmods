/*
	This file is part of the d3fact network library.
	Copyright (C) 2007-2012 d3fact Project Team

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package com.lodige.network.internal;

import github.javaappplatform.commons.collection.SemiDynamicByteArray;
import github.javaappplatform.commons.events.IListener;
import github.javaappplatform.commons.util.Strings;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.LinkedBlockingDeque;

import com.lodige.network.msg.IMessage;

/**
 * TODO javadoc
 * @author funsheep
 */
public class Message implements IMessage
{

	public static final int BODYTYPE_BYTEARRAY = 0;
	public static final int BODYTYPE_SEMIARRAY = 1;


	private static final LinkedBlockingDeque<Message> CACHE = new LinkedBlockingDeque<>(200);


	private int type;
	private long sendID;
	private int bodyType;
	private Object body;
	private int off;
	private int len;

	private IListener callback;
//	private Object source;


	protected void set(int type, IListener callback)
	{
		this.type = type;
		this.callback = callback;
	}

	protected void set(byte[] body, int off, int len)
	{
		this.bodyType = BODYTYPE_BYTEARRAY;
		this.body = body;
		this.off = off;
		this.len = len;
	}

	protected void set(SemiDynamicByteArray body, int len)
	{
		this.bodyType = BODYTYPE_SEMIARRAY;
		this.body = body;
		this.off = 0;
		this.len = len;
	}


	public long sendID()
	{
		return this.sendID;
	}

	void setSendID(long sendID)
	{
		this.sendID = sendID;
	}

	public IListener callback()
	{
		return this.callback;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void data(byte[] dest, int off)
	{
		if (this.bodyType == BODYTYPE_BYTEARRAY)
		{
			System.arraycopy(this.body, off, dest, 0, dest.length);
			return;
		}
		((SemiDynamicByteArray) this.body).getDataFrom(dest, off);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int type()
	{
		return this.type;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int size()
	{
		return this.len;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InputStream data()
	{
		if (this.bodyType == BODYTYPE_BYTEARRAY)
			return new ByteArrayInputStream((byte[]) this.body, this.off, this.len);
		return new SemiDynamicByteArrayInputStream((SemiDynamicByteArray) this.body, this.off, this.len);
	}
	
	public byte date(int pos)
	{
		if (this.bodyType == BODYTYPE_BYTEARRAY)
			return ((byte[]) this.body)[this.off+pos];
		((SemiDynamicByteArray) this.body).cursor(this.off+pos);
		return ((SemiDynamicByteArray) this.body).getDate();
	}

	public void dispose()
	{
		this.body = null;
		this.callback = null;
		this.sendID = -1;
		CACHE.offer(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		return "Message["+ //$NON-NLS-1$
				this.type()+
				"] '"+ //$NON-NLS-1$
				Strings.toHexString(this.bodyType == BODYTYPE_BYTEARRAY ? (byte[]) this.body : ((SemiDynamicByteArray) this.body).getData(), this.off, this.len) + '\'';
	}

	private static final Message get()
	{
		final Message m = CACHE.poll();
		if (m != null)
			return m;
		return new Message();
	}


	public static Message create(int type, byte[] body)
	{
		return create(type, body, 0, body.length, null);
	}

	public static Message create(int type, byte[] body, int off, int len, IListener callback)
	{
		final Message m = get();
		m.set(type, callback);
		m.set(body, off, len);
		return m;
	}

	public static Message create(int type, SemiDynamicByteArray body, int len, IListener callback)
	{
		final Message m = get();
		m.set(type, callback);
		m.set(body, len);
		return m;
	}

}
