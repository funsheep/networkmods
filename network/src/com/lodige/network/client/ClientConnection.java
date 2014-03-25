package com.lodige.network.client;

import github.javaappplatform.platform.extension.ExtensionRegistry;
import github.javaappplatform.platform.job.AComputeDoJob;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import com.lodige.network.INetworkAPI;
import com.lodige.network.INetworkService;
import com.lodige.network.internal.IInternalNetworkService;
import com.lodige.network.internal.InternalNetTools;
import com.lodige.network.internal.ANetworkConnection;
import com.lodige.network.server.PortRange;

/**
 * @author renken
 */
public class ClientConnection extends ANetworkConnection
{

	private final InetSocketAddress remoteAddress;
	private final PortRange localRange;


	public ClientConnection(String remoteHost, int remotePort, PortRange localPortRange, String networkservice) throws IOException
	{
		this(remoteHost, remotePort, localPortRange, ExtensionRegistry.<INetworkService>getService(networkservice));
	}
	
	public ClientConnection(String remoteHost, int remotePort, PortRange localPortRange, INetworkService service) throws IOException
	{
		super((IInternalNetworkService) service);
		this.remoteAddress = new InetSocketAddress(remoteHost, remotePort);
		this.localRange = localPortRange;
		this.register();
	}


	public void connect() throws IOException
	{
		AComputeDoJob job = new AComputeDoJob("Connect to address " + this.remoteAddress, INetworkAPI.NETWORK_THREAD)
		{
			
			@Override
			public void doJob()
			{
				ClientConnection.this.state.set(INetworkAPI.S_CONNECTION_PENDING);
				ClientConnection.this.postEvent(INetworkAPI.E_STATE_CHANGED);
				try
				{
					final Socket socket = InternalNetTools.newSocket(ClientConnection.this.localRange);
					socket.connect(ClientConnection.this.remoteAddress, INetworkAPI.CONNECTION_TIMEOUT);
					ClientConnection.this.setSocket(socket);
					ClientConnection.this.state.set(INetworkAPI.S_CONNECTED);
				}
				catch (IOException e)
				{
					LOGGER.warn("Could not establish connection.", e);
					ClientConnection.this.state.set(INetworkAPI.S_SHUTDOWN);
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
		sb.append("ClientUnit: ");
		switch (this.state())
		{
			case INetworkAPI.S_NOT_CONNECTED:
				sb.append("Not Connected");
				break;
			case INetworkAPI.S_CONNECTED:
				sb.append("Connected");
				break;
			case INetworkAPI.S_CLOSING:
				sb.append("Closing");
				break;
			case INetworkAPI.S_CONNECTION_PENDING:
				sb.append("Connection Pending");
				break;
		}
		sb.append('\n');
		sb.append(super.toString());
		return sb.toString();
	}

}
