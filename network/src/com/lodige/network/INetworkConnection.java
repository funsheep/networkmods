/*
	This file is part of the d3fact network library.
	Copyright (C) 2007-2012 d3fact Project Team

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package com.lodige.network;

import github.javaappplatform.commons.events.ITalker;

import java.io.Closeable;
import java.net.InetAddress;

import com.lodige.network.msg.IMessage;

/**
 * TODO javadoc
 * @author renken
 */
public interface INetworkConnection extends ITalker, Closeable
{
	
	public InetAddress address();
	
	public String alias();

	public int state();

	public void shutdown();

	
	public long asyncSend(IMessage msg) throws InterruptedException;
	
	
	public boolean hasReceivedMSGs();

	public IMessage receiveMSG();

}
