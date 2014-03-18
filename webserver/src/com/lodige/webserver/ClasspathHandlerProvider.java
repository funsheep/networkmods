/**
 * webserver Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.webserver;

import github.javaappplatform.commons.collection.SmallMap;
import github.javaappplatform.platform.extension.Extension;
import github.javaappplatform.platform.extension.ExtensionRegistry;

import java.util.Set;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.ContextHandler;


/**
 * TODO javadoc
 * @author renken
 */
public class ClasspathHandlerProvider implements IHandlerProvider
{

	public static final String EXT_POINT_CLASSPATH = "com.lodige.webserver.classpath";
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Handler[] load()
	{
		SmallMap<String, ContextHandler> handlers = new SmallMap<>();
		
		Set<Extension> cpExts = ExtensionRegistry.getExtensions(EXT_POINT_CLASSPATH);
		for (Extension ext : cpExts)
		{
			String context = ext.getProperty("context", "/");
			ContextHandler handler = handlers.get(context);
			if (handler == null)
			{
				handler = new ContextHandler(context);
				handlers.put(context, handler);
			}

			ClasspathHandler classpathHandler = new ClasspathHandler();
//			classpathHandler.setCacheControl("no-store");

			handler.setHandler(classpathHandler);
		}
		return handlers.values().toArray(new Handler[handlers.size()]);
	}

}
