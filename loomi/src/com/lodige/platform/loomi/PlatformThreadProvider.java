/**
 * loomi.demo Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.platform.loomi;

import github.javaappplatform.commons.util.Strings;
import github.javaappplatform.platform.job.AComputeDoJob;
import github.javaappplatform.platform.job.JobPlatform;

import com.lodige.loomi.provider.IModelThreadProvider;

/**
 * @author renken
 *
 */
public class PlatformThreadProvider implements IModelThreadProvider
{

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run(final Runnable runnable, boolean synchronously)
	{
		if (!synchronously)
			JobPlatform.runJob(runnable, JobPlatform.MAIN_THREAD);
		else
		{
			try
			{
				new AComputeDoJob(Strings.random(10), JobPlatform.MAIN_THREAD)
				{
					
					@Override
					public void doJob()
					{
						runnable.run();
						this.finished(null);
					}
				}.get();
			}
			catch (Exception e)
			{
				throw new RuntimeException("Running " + runnable + " caused an exception.", e);
			}
		}
	}

}
