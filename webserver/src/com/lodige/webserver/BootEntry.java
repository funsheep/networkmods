/**
 * webserver Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.webserver;

import github.javaappplatform.commons.log.Logger;
import github.javaappplatform.platform.PlatformException;
import github.javaappplatform.platform.boot.IBootEntry;
import github.javaappplatform.platform.extension.Extension;
import github.javaappplatform.platform.extension.ExtensionRegistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerCollection;

/**
 * TODO javadoc
 * @author renken
 */
public class BootEntry implements IBootEntry
{
	
	private static final Logger LOGGER = Logger.getLogger();


	private Server server;
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void startup(Extension e) throws PlatformException
	{
		this.server = new Server();
		//set http connection
		ServerConnector http = new ServerConnector(this.server);
		http.setHost(e.getProperty("host", "localhost"));
		http.setPort(e.getProperty("port", 80));
		http.setIdleTimeout(e.getProperty("idle_timeout", 180000));
		this.server.addConnector(http);
		
		//set handler for requests
		Set<IHandlerProvider> provider = ExtensionRegistry.getServices(IHandlerProvider.EXT_POINT);
		ArrayList<Handler> found = new ArrayList<>();
		for (IHandlerProvider prov : provider)
		{
			found.addAll(Arrays.asList(prov.load()));
		}
		Handler handler = null;
		if (found.size() == 1)
			handler = found.get(0);
		else
		{
			HandlerCollection col = new HandlerCollection();
			col.setHandlers(found.toArray(new Handler[found.size()]));
			handler = col;
		}
		this.server.setHandler(handler);
		
		try
		{
			this.server.start();
		}
		catch (Exception e1)
		{
			throw new PlatformException("Could not boot jetty properly.", e1);
		}
	}

	/**
	 * {@inheritDoc}
	 * @throws Exception 
	 */
	@Override
	public void shutdown()
	{
		if (this.server != null)
			try
			{
				this.server.stop();
			}
			catch (Exception e)
			{
				LOGGER.info("Could not properly shutdown jetty.", e);
			}
	}

}
