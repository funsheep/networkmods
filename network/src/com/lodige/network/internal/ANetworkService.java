/*
	This file is part of the d3fact network library.
	Copyright (C) 2007-2012 d3fact Project Team

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package com.lodige.network.internal;

import github.javaappplatform.commons.log.Logger;
import github.javaappplatform.commons.util.Close;
import github.javaappplatform.platform.extension.ServiceInstantiationException;
import github.javaappplatform.platform.job.AComputeDoJob;
import github.javaappplatform.platform.job.ADoJob;
import github.javaappplatform.platform.job.JobPlatform;
import github.javaappplatform.platform.job.JobbedTalkerStub;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

import com.lodige.network.INetworkAPI;
import com.lodige.network.INetworkConnection;
import com.lodige.network.IProtocol;


/**
 * TODO javadoc
 * @author funsheep
 */
public abstract class ANetworkService extends JobbedTalkerStub implements IInternalNetworkService
{

	protected static final Logger LOGGER = Logger.getLogger();

	private final ReentrantLock connectionLock = new ReentrantLock();
	private final HashMap<InetAddress, INetworkConnection> connectionsByAddr  = new HashMap<>(1);
	private final HashMap<String, INetworkConnection> connectionsByAlias = new HashMap<>(1);
	private final String name;
	private final String protocolClass;

	
	public ANetworkService(String name, String protocolClass)
	{
		super(INetworkAPI.NETWORK_THREAD);
		this.name = name;
		this.protocolClass = protocolClass;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String name()
	{
		return this.name;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public IProtocol _getProtocol()
	{
		try
		{
			return instantiateProtocol(this.protocolClass);
		}
		catch (ServiceInstantiationException ex)
		{
			throw new RuntimeException("Could not instantiate protocol. Should not happen.", ex);
		}
	}


	protected void closeAllConnections()
	{
		JobPlatform.runJob(new AComputeDoJob("Closing Networkconnections", INetworkAPI.NETWORK_THREAD) //$NON-NLS-1$
		{
			
			@Override
			public void doJob()
			{
				for (INetworkConnection con : ANetworkService.this.getAllConnections())
					Close.close(con);
			}
		}, INetworkAPI.NETWORK_THREAD);
	}

	protected void shutdownAllConnections()
	{
		JobPlatform.runJob(new ADoJob("Shutdown Networkconnections") //$NON-NLS-1$
		{
			
			@Override
			public void doJob()
			{
				for (INetworkConnection con : ANetworkService.this.getAllConnections())
					con.shutdown();
			}
		}, INetworkAPI.NETWORK_THREAD);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public INetworkConnection getConnection(InetAddress address)
	{
		this.connectionLock.lock();
		try
		{
			return this.connectionsByAddr.get(address);
		}
		finally
		{
			this.connectionLock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public INetworkConnection getConnection(String alias)
	{
		this.connectionLock.lock();
		try
		{
			return this.connectionsByAlias.get(alias);
		}
		finally
		{
			this.connectionLock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<INetworkConnection> getAllConnections()
	{
		this.connectionLock.lock();
		try
		{
			return new ArrayList<>(this.connectionsByAddr.values());
		}
		finally
		{
			this.connectionLock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void _register(INetworkConnection connection)
	{
		this.connectionLock.lock();
		try
		{
			if (connection.state() == INetworkAPI.S_NOT_CONNECTED)
				return;
			if (this.getConnection(connection.address()) != null)
			{
				throw new IllegalStateException("Should never happen."); //$NON-NLS-1$
			}
			
			this.connectionsByAddr.put(connection.address(), connection);
			if (connection.alias() != null)
				this.connectionsByAlias.put(connection.alias(), connection);
		}
		finally
		{
			this.connectionLock.unlock();
		}
		this.postEvent(INetworkAPI.E_CLIENT_CONNECTED, connection);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void _unregister(final INetworkConnection connection)
	{
		if (connection.state() == INetworkAPI.S_NOT_CONNECTED)
		{
			this.connectionLock.lock();
			try
			{
				this.connectionsByAddr.remove(connection.address());
				if (connection.alias() != null)
					this.connectionsByAlias.remove(connection.alias());
			}
			finally
			{
				this.connectionLock.unlock();
			}
			this.postEvent(INetworkAPI.E_CLIENT_DISCONNECTED, connection);
		}
	}


	private static final IProtocol instantiateProtocol(String protocol) throws ServiceInstantiationException
	{
		try
		{
			Class<?> clazz = Class.forName(protocol);
			return (IProtocol) clazz.newInstance();
		}
		catch (ClassNotFoundException | InstantiationException | IllegalAccessException e)
		{
			throw new ServiceInstantiationException(e);
		}
	}
}
