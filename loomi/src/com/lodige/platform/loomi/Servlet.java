/**
 * loomi Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.platform.loomi;

import github.javaappplatform.commons.events.IListener;
import github.javaappplatform.platform.extension.ExtensionRegistry;

import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import com.lodige.loomi.ILoomiAPI;
import com.lodige.loomi.annotation.LoomiConfiguration;
import com.lodige.loomi.engine.LoomiServlet;

/**
 * @author renken
 *
 */
@WebServlet(value = "/*", asyncSupported = true)
@LoomiConfiguration(ModelThreadProvider="com.lodige.platform.loomi.PlatformThreadProvider", listeners={ })
public class Servlet extends LoomiServlet
{

	public static final String EP_UI_LIFECYCLE = "com.lodige.platform.loomi.ui.lifecycle";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void servletInitialized() throws ServletException
	{
		Set<IListener> listeners = ExtensionRegistry.getServices(EP_UI_LIFECYCLE);
		for (IListener li : listeners)
		{
			this.addListener(ILoomiAPI.E_UISESSION_STARTED, li);
			this.addListener(ILoomiAPI.E_UISESSION_CLOSED, li);
		}
		super.servletInitialized();
	}
	
	private static final long serialVersionUID = 1L;

}
