/**
 * lodige.platform Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.plc.impl;

import github.javaappplatform.platform.job.AComputeDoJob;

import java.io.IOException;

import com.lodige.network.INetworkAPI;
import com.lodige.network.plc.Read;
import com.lodige.network.plc.msg.PDUReadResult;
import com.lodige.network.plc.msg.PDUResultException;
import com.lodige.network.plc.msg.Variable;

/**
 * TODO javadoc
 * @author renken
 */
class InputTriggering extends AComputeDoJob
{
	
	private final Input input;

	public InputTriggering(Input input)
	{
		super("Update Trigger for Input " + input.id() + " for PLC " + input.plc().id());
		this.input = input;
		this.schedule(INetworkAPI.NETWORK_THREAD, false, 0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doJob()
	{
		try
		{
			PDUReadResult rr = Read.fromPLC(this.input.parent.cc).bytes(this.input.type.size).from(this.input.area).andDatabase(this.input.database).startAt(this.input.offset).andWaitForResult();
			Variable[] vars = rr.getResults();
			this.finished(vars[0].data());
		}
		catch (IOException | PDUResultException ex)
		{
			this.finishedWithError(ex);
		}
	}

}
