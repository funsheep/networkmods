/**
 * icplatform Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.sys.states;

import github.javaappplatform.platform.extension.ExtensionRegistry;
import github.javaappplatform.platform.job.JobPlatform;
import github.javaappplatform.platform.job.JobbedTalkerStub;

import com.lodige.network.INetworkAPI;
import com.lodige.network.INetworkConnection;

/**
 * TODO javadoc
 * @author renken
 */
public class NetworkConnectionState extends JobbedTalkerStub implements ISystemState
{

	private final String name;
	private final INetworkConnection connection;


	public NetworkConnectionState(INetworkConnection connection)
	{
		this(connection.alias(), connection);
	}
	
	/**
	 * 
	 */
	public NetworkConnectionState(String name, INetworkConnection connection)
	{
		super(JobPlatform.MAIN_THREAD);
		this.name = name;
		this.connection = connection;
		this.connection.addListener(INetworkAPI.E_STATE_CHANGED, (e) -> this.postEvent(EVENT_STATE_CHANGED));
		ExtensionRegistry.registerSingleton(name, EXTPOINT_SYSTEM_STATE, this, null);
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
	public State state()
	{
		switch (this.connection.state())
		{
			case INetworkAPI.S_NOT_CONNECTED:
				return State.ERROR;
			case INetworkAPI.S_CONNECTION_PENDING:
				return State.INFO;
			case INetworkAPI.S_CONNECTED:
				return State.RUNNING;
			case INetworkAPI.S_CLOSING:
				return State.INFO;
			default:
				return State.UNKNOWN;
		}
	}

}
