/**
 * lodige.platform Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.plc.impl;

import github.javaappplatform.commons.log.Logger;
import github.javaappplatform.platform.job.ADoJob;

import java.io.IOException;
import java.util.ArrayList;

import com.lodige.network.plc.Read;
import com.lodige.network.plc.msg.PDUResultException;
import com.lodige.network.plc.msg.Variable;
import com.lodige.plc.IInput;
import com.lodige.plc.IPLCAPI;
import com.lodige.plc.IPLCAPI.UpdateFrequency;

/**
 * TODO javadoc
 * @author renken
 */
class InputPolling extends ADoJob
{
	
	private static final Logger LOGGER = Logger.getLogger();
	private final PLC plc;


	/**
	 * @param name
	 */
	public InputPolling(PLC plc)
	{
		super("Input Polling for " + plc.id());
		this.plc = plc;
		this.schedule(IPLCAPI.PLC_UPDATE_THREAD, true, UpdateFrequency.HIGH.schedule / 2);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doJob()
	{
		if (!this.plc.connected())
		{
			this.shutdown();
			return;
		}
		
		Read r = Read.fromPLC(this.plc.cc);
		Read.Read3 r3 = null;
		final ArrayList<Input> inputs = new ArrayList<>();
		for (IInput input : this.plc.inputs())
		{
			Input in = (Input) input;
			if (in.startUpdate())
			{
				if (r3 != null)
					r = r3.andRead();
				r3 = r.bytes(in.length).from(in.area).andDatabase(in.database).startAt(in.offset);
				inputs.add(in);
			}
		}
		
		if (inputs.size() == 0)
			return;
		
		try
		{
			int i = 0;
			Variable[] vars = r3.andWaitForResult().getResults();
			if (vars.length != inputs.size())
				throw new IOException("Number of results does not match number of requests.");
			for (Input input : inputs)
				try
				{
					input.update(vars[i++].data(), 0);
				}
				catch (PDUResultException e)
				{
					LOGGER.severe("Could not read data from input {} from plc {}.", input.id, this.plc.id(), e);
				}
		}
		catch (IOException e)
		{
			LOGGER.severe("Could not read data from plc {}.", this.plc.id(), e);
		}
	}

}
