/**
 * icplatform Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.logging;

import github.javaappplatform.commons.collection.IObservableSet;
import github.javaappplatform.platform.console.ICommand;
import github.javaappplatform.platform.extension.ExtensionRegistry;

import java.io.PrintStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;

/**
 * TODO javadoc
 * @author renken
 */
public class LivelogCommand implements ICommand
{
	
	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

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
			out.println(formatTime(e.getTimeStamp())+" ["+ensureLength(e.getThreadName(), 10)+"] " + e.getLoggerName() + " - " + e.getFormattedMessage()); //$NON-NLS-1$ //$NON-NLS-2$
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
	
	private static final String formatTime(long millis)
	{
		return LocalDate.from(Instant.ofEpochMilli(millis)).format(TIME_FORMATTER);
	}
	
	private static final String ensureLength(String text, int length)
	{
		int diff = length - text.length();
		if (diff > 0)
		{
			StringBuilder sb = new StringBuilder(length);
			for (int i = 0; i < diff/2; i++)
				sb.append(' ');
			sb.append(text);
			for (int i = diff/2; i < diff; i++)
				sb.append(' ');
			return sb.toString();
		}
		return text;
	}

}
