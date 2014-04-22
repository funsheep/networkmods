/**
 * network Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.network.internal;

import com.lodige.network.INetworkConnection;
import com.lodige.network.INetworkService;
import com.lodige.network.IProtocol;

/**
 * TODO javadoc
 * @author renken
 */
public interface IInternalNetworkService extends INetworkService
{
	
	public IProtocol _getProtocol();

	public void _register(INetworkConnection connection);

	public void _unregister(INetworkConnection connection);

}
