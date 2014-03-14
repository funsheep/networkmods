/**
 * webserver Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.webserver;

import org.eclipse.jetty.server.Handler;

/**
 * TODO javadoc
 * @author renken
 */
public interface IHandlerProvider
{
	
	public static final String EXT_POINT = "com.lodige.webserver.handler";

	public Handler[] load();
}
