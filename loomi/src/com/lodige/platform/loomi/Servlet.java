/**
 * loomi Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.platform.loomi;

import github.javaappplatform.commons.events.IListener;
import github.javaappplatform.platform.extension.ExtensionRegistry;

import java.util.Arrays;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import com.lodige.loomi.ILoomiAPI;
import com.lodige.loomi.engine.ALoomiServlet;
import com.vaadin.server.UIProvider;

/**
 * @author renken
 *
 */
@WebServlet(value = "/*", asyncSupported = true)
public class Servlet extends ALoomiServlet
{

	public static final String EXTPOINT_UI_LIFECYCLE = "com.lodige.platform.loomi.ui.lifecycle";
	public static final String EXTPOINT_UI_PROVIDER = "com.lodige.platform.loomi.ui.provider";

	private static UIProvider[] PROVIDER = null;
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void servletInitialized() throws ServletException
	{
		super.servletInitialized();
		Set<IListener> listeners = ExtensionRegistry.getServices(EXTPOINT_UI_LIFECYCLE);
		for (IListener li : listeners)
		{
			this.addListener(ILoomiAPI.E_UISESSION_STARTING, li);
			this.addListener(ILoomiAPI.E_UISESSION_CLOSING, li);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected synchronized UIProvider[] getUIProvider()
	{
		if (PROVIDER == null)
		{
			PROVIDER = ExtensionRegistry.getServices(EXTPOINT_UI_PROVIDER).stream().toArray(UIProvider[]::new);
			System.out.println("Found UIProvider: " + Arrays.toString(PROVIDER));
		}
		return PROVIDER;
	}
	
	private static final long serialVersionUID = 1L;

}
