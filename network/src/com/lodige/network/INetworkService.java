/**
 * network Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.network;

import github.javaappplatform.commons.events.ITalker;

import java.io.Closeable;
import java.net.InetAddress;
import java.util.Collection;

/**
 * TODO javadoc
 * @author renken
 */
public interface INetworkService extends ITalker, Closeable
{
	
	public String name();
	
	public int state();

	public void shutdown();

	
	public INetworkConnection getConnection(InetAddress address);

	public INetworkConnection getConnection(String alias);

	public Collection<INetworkConnection> getAllConnections();

}
