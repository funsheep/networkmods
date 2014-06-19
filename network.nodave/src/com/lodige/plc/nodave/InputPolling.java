/**
 * lodige.platform Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.plc.nodave;

import github.javaappplatform.commons.log.Logger;
import github.javaappplatform.platform.job.ADoJob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.lodige.network.INetworkAPI;
import com.lodige.network.plc.Read;
import com.lodige.network.plc.msg.PDUResultException;
import com.lodige.network.plc.msg.Variable;
import com.lodige.plc.IInput;
import com.lodige.plc.IPLCAPI;
import com.lodige.plc.IPLCAPI.ConnectionState;
import com.lodige.plc.IPLCAPI.UpdateFrequency;

/**
 * TODO javadoc
 * @author renken
 */
class InputPolling extends ADoJob
{
	
	private static final Logger LOGGER = Logger.getLogger();
	
	private static final int MAX_INPUTS_PER_POLL = 15;
	
	private final NodavePLC plc;


	/**
	 * @param name
	 */
	public InputPolling(NodavePLC plc)
	{
		super("Input Polling for " + plc.id()); //$NON-NLS-1$
		this.plc = plc;
		this.schedule(IPLCAPI.PLC_UPDATE_THREAD, true, UpdateFrequency.HIGH.schedule);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doJob()
	{
		if (this.plc.connectionState() != ConnectionState.CONNECTED)
			return;
		
		Read r = Read.fromPLC(this.plc.cc);
		Read.Read3 r3 = null;
		ArrayList<Input> inputs = new ArrayList<>();
		for (IInput input : this.plc.inputs())
		{
			Input in = (Input) input;
			if (in.startUpdate())
			{
				if (r3 != null)
					r = r3.andRead();
				r3 = r.bytes(in.length).from(in.area).andDatabase(in.database).startAt(in.offset);
				inputs.add(in);
				if (inputs.size() == MAX_INPUTS_PER_POLL)
				{
					try
					{
						this.poll(r3, inputs);
					}
					catch (IOException e)
					{
						for (Input i : inputs)
							i.updateNoValue();
						LOGGER.info("Could not read data from plc {}.", this.plc.id(), e); //$NON-NLS-1$
					}
					inputs = new ArrayList<>();
					r =  Read.fromPLC(this.plc.cc);
					r3 = null;
				}
			}
		}

		if (inputs.size() == 0)
			return;
		try
		{
			this.poll(r3, inputs);
		}
		catch (IOException e)
		{
			for (Input i : inputs)
				i.updateNoValue();
			LOGGER.info("Could not read data from plc {}.", this.plc.id(), e); //$NON-NLS-1$
		}
	}

	private void poll(Read.Read3 r3, List<Input> inputs) throws IOException
	{
		int i = 0;
		Variable[] vars = r3.andWaitForResult(INetworkAPI.CONNECTION_TIMEOUT).getResults();
		if (vars.length != inputs.size())
			throw new IOException("Number of results does not match number of requests."); //$NON-NLS-1$
		for (Input input : inputs)
			try
			{
				input.update(vars[i++].data(), 0);
			}
			catch (PDUResultException e)
			{
				LOGGER.severe("Could not read data from input {} from plc {}.", input.id, this.plc.id(), e); //$NON-NLS-1$
			}
	}
}
