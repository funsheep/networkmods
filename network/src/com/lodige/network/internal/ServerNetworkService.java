/*
	This file is part of the d3fact network library.
	Copyright (C) 2007-2012 d3fact Project Team

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package com.lodige.network.internal;

import github.javaappplatform.platform.job.AComputeDoJob;
import github.javaappplatform.platform.job.ADoJob;
import github.javaappplatform.platform.job.JobPlatform;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

import com.lodige.network.INetworkAPI;
import com.lodige.network.server.IServerNetworkService;
import com.lodige.network.server.ServerConnection;


/**
 * TODO javadoc
 * @author funsheep
 */
public class ServerNetworkService extends ANetworkService implements IServerNetworkService
{

	private final AtomicInteger state = new AtomicInteger(INetworkAPI.S_NOT_STARTED);
	private final SocketUnit unit;
	
	
	public ServerNetworkService(String name, int localPort, String protocolClass)
	{
		super("Server Network Service", protocolClass);
		this.unit = new SocketUnit(localPort);
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
	public void start() throws IOException
	{
		if (!this.state.compareAndSet(INetworkAPI.S_NOT_STARTED, INetworkAPI.S_STARTING))
			return;
		
		ServerNetworkService.this.postEvent(INetworkAPI.E_STATE_CHANGED);

		AComputeDoJob job = new AComputeDoJob("Start Server Unit", INetworkAPI.NETWORK_THREAD) //$NON-NLS-1$
		{
			
			@Override
			public void doJob()
			{
				try
				{
					ServerNetworkService.this.unit.start(ServerNetworkService.this);
				}
				catch (IOException e)
				{
					ServerNetworkService.this.state.set(INetworkAPI.S_NOT_CONNECTED);
					this.finishedWithError(e);
				}
				ServerNetworkService.this.state.set(INetworkAPI.S_RUNNING);
				this.finished(null);
			}
		};
		try
		{
			job.get();
		}
		catch (Exception e)
		{
			throw (IOException) e;
		}
		finally
		{
			this.postEvent(INetworkAPI.E_STATE_CHANGED);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close()
	{
		if (!this.state.compareAndSet(INetworkAPI.S_RUNNING, INetworkAPI.S_CLOSING))
			return;

		ServerNetworkService.this.postEvent(INetworkAPI.E_STATE_CHANGED);

		JobPlatform.runJob(new ADoJob("Closing Networkservice") //$NON-NLS-1$
		{
			
			@Override
			public void doJob()
			{
				ServerNetworkService.this.unit.shutdown();
				
				ServerNetworkService.this.closeAllConnections();

				ServerNetworkService.this.state.set(INetworkAPI.S_NOT_CONNECTED);
				ServerNetworkService.this.postEvent(INetworkAPI.E_STATE_CHANGED);
			}
		}, INetworkAPI.NETWORK_THREAD);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void shutdown()
	{
		if (this.state.getAndSet(INetworkAPI.S_NOT_CONNECTED) == INetworkAPI.S_NOT_CONNECTED)
			return;
		
		ServerNetworkService.this.postEvent(INetworkAPI.E_STATE_CHANGED);

		JobPlatform.runJob(new ADoJob("Shutdown Networkconnections") //$NON-NLS-1$
		{
			
			@Override
			public void doJob()
			{
				ServerNetworkService.this.unit.shutdown();

				ServerNetworkService.this.shutdownAllConnections();
				
			}
		}, INetworkAPI.NETWORK_THREAD);
	}


	void register(Socket clientSocket) throws IOException
	{
		this._register(new ServerConnection(this, clientSocket));
	}

}
