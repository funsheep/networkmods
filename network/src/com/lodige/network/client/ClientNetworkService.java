/*
	This file is part of the d3fact network library.
	Copyright (C) 2007-2012 d3fact Project Team

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package com.lodige.network.client;

import github.javaappplatform.platform.job.ADoJob;
import github.javaappplatform.platform.job.JobPlatform;

import java.util.concurrent.atomic.AtomicInteger;

import com.lodige.network.INetworkAPI;
import com.lodige.network.IProtocol;
import com.lodige.network.internal.ANetworkService;


/**
 * TODO javadoc
 * @author funsheep
 */
public class ClientNetworkService extends ANetworkService
{

	private final AtomicInteger state = new AtomicInteger(INetworkAPI.S_RUNNING);

	
	public ClientNetworkService(String name, IProtocol protocol)
	{
		super(name, protocol);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int state()
	{
		return this.state.get();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close()
	{
		if (!this.state.compareAndSet(INetworkAPI.S_RUNNING, INetworkAPI.S_CLOSING))
			return;

		ClientNetworkService.this.postEvent(INetworkAPI.E_STATE_CHANGED);

		JobPlatform.runJob(new ADoJob("Closing Networkservice")
		{
			
			@Override
			public void doJob()
			{
				ClientNetworkService.this.closeAllConnections();

				ClientNetworkService.this.state.set(INetworkAPI.S_SHUTDOWN);
				ClientNetworkService.this.postEvent(INetworkAPI.E_STATE_CHANGED);
			}
		}, INetworkAPI.NETWORK_THREAD);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void shutdown()
	{
		if (this.state.getAndSet(INetworkAPI.S_SHUTDOWN) == INetworkAPI.S_SHUTDOWN)
			return;
		
		ClientNetworkService.this.postEvent(INetworkAPI.E_STATE_CHANGED);

		JobPlatform.runJob(new ADoJob("Shutdown Networkconnections")
		{
			
			@Override
			public void doJob()
			{
				ClientNetworkService.this.shutdownAllConnections();
				
			}
		}, INetworkAPI.NETWORK_THREAD);
	}

}
