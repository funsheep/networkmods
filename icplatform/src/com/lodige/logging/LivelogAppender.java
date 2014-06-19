/**
 * icplatform Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.logging;

import github.javaappplatform.platform.extension.ExtensionRegistry;
import github.javaappplatform.platform.job.JobPlatform;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

/**
 * TODO javadoc
 * @author renken
 */
public class LivelogAppender extends AppenderBase<ILoggingEvent>
{
	
	public static final String EXTPOINT_LIVELOG = "com.lodige.logging.Livelog"; //$NON-NLS-1$
	public static final String LOG_THREAD = "Log Thread"; //$NON-NLS-1$
	
	private final ObservableFixedSizeOrderedSet<ILoggingEvent> eventSet = ExtensionRegistry.getService(EXTPOINT_LIVELOG);


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void append(ILoggingEvent eventObject)
	{
		JobPlatform.runJob(() -> this.eventSet.put(eventObject), LOG_THREAD);
	}

}
