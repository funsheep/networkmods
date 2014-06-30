/**
 * icplatform Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.logging;

import java.io.PrintStream;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import github.javaappplatform.commons.collection.IObservableSet;
import github.javaappplatform.platform.console.ICommand;
import github.javaappplatform.platform.extension.ExtensionRegistry;

/**
 * TODO javadoc
 * @author renken
 */
public class LivelogCommand implements ICommand
{

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run(String[] args, PrintStream out) throws Exception
	{
		IObservableSet<ILoggingEvent> events = ExtensionRegistry.getService(LivelogAppender.EXTPOINT_LIVELOG);
		for (Object event : events.toArray())
		{
			ILoggingEvent e = (ILoggingEvent) event;
			out.println('['+e.getThreadName()+"] " + e.getLoggerName() + " - " + e.getFormattedMessage()); //$NON-NLS-1$ //$NON-NLS-2$
			this.printThrowable(e.getThrowableProxy(), true, out);
		}
	}

	private void printThrowable(IThrowableProxy proxy, boolean first, PrintStream out)
	{
		if (proxy == null)
			return;

		if (!first)
			out.println("Caused By:"); //$NON-NLS-1$
		out.println(proxy.getClassName() + " - " + proxy.getMessage()); //$NON-NLS-1$
		for (StackTraceElementProxy trace : proxy.getStackTraceElementProxyArray())
		{
			out.println(trace.getSTEAsString());
		}
		this.printThrowable(proxy.getCause(), false, out);
	}
}
