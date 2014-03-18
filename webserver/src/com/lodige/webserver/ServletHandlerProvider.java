/**
 * webserver.servlets Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.webserver;

import github.javaappplatform.commons.collection.SmallMap;
import github.javaappplatform.platform.extension.Extension;
import github.javaappplatform.platform.extension.ExtensionRegistry;

import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * TODO javadoc
 * 
 * @author renken
 */
public class ServletHandlerProvider implements IHandlerProvider
{

	public static final String EXT_POINT_SERVLET = "com.lodige.webserver.servlet";
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Handler[] load()
	{
		SmallMap<String, ServletContextHandler> handlers = new SmallMap<>();
		
		Set<Extension> servletExts = ExtensionRegistry.getExtensions(EXT_POINT_SERVLET);
		for (Extension ext : servletExts)
		{
			String context = ext.getProperty("context", "/");
			ServletContextHandler handler = handlers.get(context);
			if (handler == null)
			{
				handler = new ServletContextHandler(null, context, ServletContextHandler.SESSIONS);
				handlers.put(context, handler);
			}
			
			ServletHolder holder = new ServletHolder();
			parseServletExtension(ext, holder);
			parseServletAnnotations(holder);
			for (String url : urlPatterns(ext))
				handler.addServlet(holder, url);
		}
		return handlers.values().toArray(new Handler[handlers.size()]);
	}
	
	private static void parseServletExtension(Extension ext, ServletHolder holder)
	{
		for (Entry<String, Object> entry : ext.getProperties())
		{
			switch (entry.getKey())
			{
				case "asyncSupported": 
					holder.setAsyncSupported(ext.getProperty("asyncSupported", false));
					break;
				case "displayName":
					holder.setDisplayName(ext.getProperty("displayName", ""));
					break;
				case "loadOnStartup":
					holder.setInitOrder(ext.getProperty("loadOnStartup", -1));
					break;
				case "name":
					holder.setName(ext.getProperty("name", ""));
					break;
				case "class":
					holder.setClassName(ext.getProperty("class", ""));
					break;
				case "urlPatterns":
				case "value":
					break;
				default:
					holder.setInitParameter(entry.getKey(), String.valueOf(entry.getValue()));
					break;
			}
		}
	}

	private static void parseServletAnnotations(ServletHolder holder)
	{
		Class< ? > type;
		try
		{
			type = Class.forName(holder.getClassName());
		}
		catch (ClassNotFoundException e)
		{
			throw new RuntimeException("Should not happen.", e);
		}
		WebServlet info = type.getAnnotation(WebServlet.class);
		if (info != null)
		{
			if (info.asyncSupported())
				holder.setAsyncSupported(info.asyncSupported());
			if (!"".equals(info.displayName()))
				holder.setDisplayName(info.displayName());
			if (info.loadOnStartup() != -1)
				holder.setInitOrder(info.loadOnStartup());
			if (!"".equals(info.name()))
				holder.setName(info.name());
			for (WebInitParam param : info.initParams())
				holder.setInitParameter(param.name(), param.value());
		}
	}
	
	private static String[] urlPatterns(Extension ext)
	{
		String[] urlPatterns = {};
		Class< ? > type;
		try
		{
			type = Class.forName(ext.getProperty("class", ""));
		}
		catch (ClassNotFoundException e)
		{
			throw new RuntimeException("Should not happen.", e);
		}
		WebServlet info = type.getAnnotation(WebServlet.class);
		if (info != null)
		{
			urlPatterns = info.urlPatterns();
			if (urlPatterns == null || urlPatterns.length == 0)
				urlPatterns = info.value();
		}
		
		if (urlPatterns == null || urlPatterns.length == 0)
		{
			Object o = ext.getProperty("urlPatterns");
			if (o instanceof String)
				urlPatterns = new String[] { String.valueOf(o) };
			else if (o instanceof String[])
				urlPatterns = (String[]) o;
		}
		return urlPatterns;
	}

}
