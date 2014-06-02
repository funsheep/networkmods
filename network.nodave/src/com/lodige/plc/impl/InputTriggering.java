/**
 * lodige.platform Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.plc.impl;

import github.javaappplatform.commons.log.Logger;
import github.javaappplatform.platform.job.ADoJob;

import java.io.IOException;

import com.lodige.network.plc.Read;
import com.lodige.network.plc.msg.PDUReadResult;
import com.lodige.network.plc.msg.PDUResultException;
import com.lodige.network.plc.msg.Variable;
import com.lodige.plc.IPLCAPI;

/**
 * TODO javadoc
 * @author renken
 */
class InputTriggering extends ADoJob
{
	
	private static final Logger LOGGER = Logger.getLogger();
	
	
	private final Input input;

	public InputTriggering(Input input)
	{
		super("Update Trigger for Input " + input.id() + " for PLC " + input.plc().id());
		this.input = input;
		this.schedule(IPLCAPI.PLC_UPDATE_THREAD, false, 0);
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
			try
			{
				this.input.update(vars[0].data(), 0);
			}
			catch (PDUResultException e)
			{
				LOGGER.severe("Could not read data from input {} from plc {}.", this.input.id, this.input.parent.id(), e);
			}
		}
		catch (IOException e)
		{
			LOGGER.severe("Could not read data from plc {}.", this.input.parent.id(), e);
		}
		this.shutdown();
	}

}
