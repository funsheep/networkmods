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


	private void set(int type, IListener callback)
	{
		this.type = type;
		this.callback = callback;
	}

	private void set(byte[] body, int off, int len)
	{
		this.bodyType = BODYTYPE_BYTEARRAY;
		this.body = body;
		this.off = off;
		this.len = len;
	}

	private void set(SemiDynamicByteArray body, int len)
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
	
//	public int bodyType()
//	{
//		return this.bodyType;
//	}
//	
//	public <O> O body()
//	{
//		return GenericsToolkit.<O>convertUnchecked(this.body);
//	}

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
	public InputStream data()
	{
		if (this.bodyType == BODYTYPE_BYTEARRAY)
			return new ByteArrayInputStream((byte[]) this.body, this.off, this.len);
		return new SemiDynamicByteArrayInputStream((SemiDynamicByteArray) this.body, this.len);
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
		return "Message["+this.type()+"] '" + Strings.toHexString(this.bodyType == BODYTYPE_BYTEARRAY ? (byte[]) this.body : ((SemiDynamicByteArray) this.body).getData(), this.off, this.len) + "'";
	}

	private static final Message get()
	{
		final Message m = CACHE.poll();
		if (m != null)
			return m;
		return new Message();
	}


	public static IMessage create(int type, byte[] body)
	{
		return create(type, body, 0, body.length, null);
	}

	public static IMessage create(int type, byte[] body, int off, int len, IListener callback)
	{
		final Message m = get();
		m.set(type, callback);
		m.set(body, off, len);
		return m;
	}

	public static IMessage create(int type, SemiDynamicByteArray body, int len, IListener callback)
	{
		final Message m = get();
		m.set(type, callback);
		m.set(body, len);
		return m;
	}

}
