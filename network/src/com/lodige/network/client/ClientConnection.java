package com.lodige.network.client;

import github.javaappplatform.platform.extension.ExtensionRegistry;
import github.javaappplatform.platform.extension.ServiceInstantiationException;
import github.javaappplatform.platform.job.AComputeDoJob;
import github.javaappplatform.platform.job.ADoJob;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import com.lodige.network.INetworkAPI;
import com.lodige.network.INetworkService;
import com.lodige.network.internal.ANetworkConnection;
import com.lodige.network.internal.IInternalNetworkService;
import com.lodige.network.internal.InternalNetTools;
import com.lodige.network.server.PortRange;

/**
 * @author renken
 */
public class ClientConnection extends ANetworkConnection
{
	
	private class Reconnecter extends ADoJob
	{
		public Reconnecter()
		{
			super("AutoReconnecter for " + ClientConnection.this.alias()); //$NON-NLS-1$
		}

	
		@Override
		public void doJob()
		{
			LOGGER.debug(ClientConnection.this.toString()); //$NON-NLS-1$
			if (this.isfinished() || ClientConnection.this.state() != INetworkAPI.S_NOT_CONNECTED)
				return;
			try
			{
				LOGGER.info("Attempting a reconnect to {}.", ClientConnection.this.remoteAddress); //$NON-NLS-1$
				ClientConnection.this.connect();
			}
			catch (IOException e)
			{
				LOGGER.info("Could not reconnect to {}. Exception: {}", ClientConnection.this.remoteAddress, e.getMessage()); //$NON-NLS-1$
			}
		}

	}

	private final InetSocketAddress remoteAddress;
	private final PortRange localRange;
	private Reconnecter autoReconnect = null;
	

	public ClientConnection(String remoteHost, int remotePort, PortRange localPortRange, String networkservice) throws ServiceInstantiationException
	{
		this(remoteHost, remotePort, localPortRange, ExtensionRegistry.getExtensionByName(networkservice).<INetworkService>getService());
	}
	
	public ClientConnection(String remoteHost, int remotePort, PortRange localPortRange, INetworkService service)
	{
		super((IInternalNetworkService) service);
		this.remoteAddress = new InetSocketAddress(remoteHost, remotePort);
		this.localRange = localPortRange;
		this.register();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String alias()
	{
		String alias = super.alias();
		if (alias != null)
			return alias;
		return this.remoteAddress.toString();
	}



	public void connect() throws IOException
	{
		AComputeDoJob job = new AComputeDoJob("Connect to address " + this.remoteAddress, INetworkAPI.NETWORK_THREAD) //$NON-NLS-1$
		{
			
			@Override
			public void doJob()
			{
				if (!ClientConnection.this.state.compareAndSet(INetworkAPI.S_NOT_CONNECTED, INetworkAPI.S_CONNECTION_PENDING))
					this.finishedWithError(new IllegalStateException("Client Already Connected.")); //$NON-NLS-1$
				ClientConnection.this.postEvent(INetworkAPI.E_STATE_CHANGED);
				try
				{
					final Socket socket = InternalNetTools.newSocket(ClientConnection.this.localRange);
					socket.connect(ClientConnection.this.remoteAddress, INetworkAPI.CONNECTION_TIMEOUT);
					ClientConnection.this.setSocket(socket);
					ClientConnection.this.state.set(INetworkAPI.S_CONNECTED);
					LOGGER.info(ClientConnection.this.toString()); //$NON-NLS-1$
				}
				catch (IOException e)
				{
//					LOGGER.warn("Could not establish connection.", e);
					ClientConnection.this.state.set(INetworkAPI.S_NOT_CONNECTED);
					this.finishedWithError(e);
				}
				ClientConnection.this.postEvent(INetworkAPI.E_STATE_CHANGED);
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
	}

	public synchronized void setAutoReconnect(boolean autoReconnect)
	{
		if (!this.doesAutoReconnect() && autoReconnect)
		{
			this.autoReconnect = new Reconnecter();
			this.autoReconnect.schedule(INetworkAPI.NETWORK_THREAD, true, 10 * 1000);
		}
		else if (this.doesAutoReconnect() && !autoReconnect)
		{
			this.autoReconnect.shutdown();
			this.autoReconnect = null;
		}
	}
	
	public boolean doesAutoReconnect()
	{
		return this.autoReconnect != null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InetAddress address()
	{
		return this.remoteAddress.getAddress();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("ClientUnit "); //$NON-NLS-1$
		sb.append(this.alias());
		sb.append(": ");
		switch (this.state())
		{
			case INetworkAPI.S_NOT_CONNECTED:
				sb.append("Not Connected"); //$NON-NLS-1$
				break;
			case INetworkAPI.S_CONNECTED:
				sb.append("Connected"); //$NON-NLS-1$
				break;
			case INetworkAPI.S_CLOSING:
				sb.append("Closing"); //$NON-NLS-1$
				break;
			case INetworkAPI.S_CONNECTION_PENDING:
				sb.append("Connection Pending"); //$NON-NLS-1$
				break;
			default:
				sb.append("Unkown");
				break;
		}
		return sb.toString();
	}

}
