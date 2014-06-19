/**
 * lodige.platform Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.plc.dummy;

import github.javaappplatform.platform.job.ADoJob;

import com.lodige.plc.IInput;
import com.lodige.plc.IPLCAPI;
import com.lodige.plc.IPLCAPI.UpdateFrequency;

/**
 * TODO javadoc
 * @author renken
 */
class InputPolling extends ADoJob
{
	
	private final DummyPLC plc;


	/**
	 * @param name
	 */
	public InputPolling(DummyPLC plc)
	{
		super("DummyInput Polling for " + plc.id()); //$NON-NLS-1$
		this.plc = plc;
		this.schedule(IPLCAPI.PLC_UPDATE_THREAD, true, UpdateFrequency.HIGH.schedule);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doJob()
	{
		for (IInput input : this.plc.inputs())
		{
			DummyInput in = (DummyInput) input;
			if (in.startUpdate())
				in.update();
		}
	}

}
