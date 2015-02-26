/**
 * lodige.platform Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.network.s7.plc.impl;

import github.javaappplatform.commons.log.Logger;
import github.javaappplatform.commons.util.Close;
import github.javaappplatform.platform.job.ADoJob;
import github.javaappplatform.platform.job.JobPlatform;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.lodige.network.INetworkAPI;
import com.lodige.network.s7.plc.IInput;
import com.lodige.network.s7.plc.IPLCAPI;
import com.lodige.network.s7.plc.IPLCAPI.ConnectionState;
import com.lodige.network.s7.protocol.Read;
import com.lodige.network.s7.protocol.impl.S7Protocol;
import com.lodige.network.s7.protocol.msg.PDUResultException;
import com.lodige.network.s7.protocol.msg.Variable;

/**
 * TODO javadoc
 * @author renken
 */
class InputPolling extends ADoJob
{
	
	private static final Logger LOGGER = Logger.getLogger();
	
	private static final int MAX_FAILS_BEFORE_RECONNECT = 3;
	private static final int MAX_INPUTS_PER_POLL = 10;
	private static final int POLLING_FREQUENCY = 1000;
	
	private final NodavePLC plc;
	private int fails = 0;

	private static final AtomicInteger pollcount = new AtomicInteger();

	static
	{
		JobPlatform.loopJob(new ADoJob("Input Poll Count")
			{

				@Override
				public void doJob()
				{
					int count = pollcount.getAndSet(0);
					LOGGER.debug("#PPS {}", (count / 10f));
				}
			}, IPLCAPI.PLC_UPDATE_THREAD, 10 * 1000);
	}

	/**
	 * @param name
	 */
	public InputPolling(NodavePLC plc)
	{
		super("Input Polling for " + plc.id()); //$NON-NLS-1$
		this.plc = plc;
//		this.schedule(IPLCAPI.PLC_UPDATE_THREAD, true, POLLING_FREQUENCY);
		this.schedule(IPLCAPI.PLC_UPDATE_THREAD + " " + plc.id(), true, POLLING_FREQUENCY);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doJob()
	{
		if (this.plc.connectionState() != ConnectionState.CONNECTED)
			return;

		final int maxLength = ((S7Protocol) this.plc.cc._protocol()).maxPDULength();
		try
		{
			Read r = Read.fromPLC(this.plc.cc);
			Read.Read3 r3 = null;
			ArrayList<Input> inputs = new ArrayList<>();
			int length = 0;
			for (IInput input : this.plc.inputs())
			{
				Input in = (Input) input;
				if (maxLength <= 0 && inputs.size() == MAX_INPUTS_PER_POLL || length + 25 + in.length > maxLength)
				{
					this.poll(r3, inputs);
					inputs.clear();
					length = 0;
					r = Read.fromPLC(this.plc.cc);
					r3 = null;
				}

				if (in.startUpdate())
				{
					if (r3 != null)
						r = r3.andRead();
					r3 = r.bytes(in.length).from(in.area).andDatabase(in.database).startAt(in.offset);
					inputs.add(in);
					length += 25 + in.length;
				}
			}

			this.poll(r3, inputs);
		}
		catch (IOException e)
		{
			LOGGER.debug("Could not read data from plc {}.", this.plc.id(), e); //$NON-NLS-1$
			this.fails++;
		}
	
		if (this.fails >= MAX_FAILS_BEFORE_RECONNECT)
		{
			LOGGER.severe("The last {} read requests to plc {} failed. Attempting a reconnect.", Integer.valueOf(this.fails), this.plc.id());
			Close.close(this.plc.cc);
			this.fails = 0;
		}
	}

	private void poll(Read.Read3 r3, List<Input> inputs) throws IOException
	{
		if (inputs.size() == 0)
			return;
		
		pollcount.addAndGet(inputs.size());
		
		int i = 0;
		Variable[] vars = r3.andWaitForResult(INetworkAPI.CONNECTION_TIMEOUT).getResults();
		if (vars.length != inputs.size())
		{
			for (Input in : inputs)
				in.updateNoValue();
			throw new IOException("Number of results does not match number of requests."); //$NON-NLS-1$
		}
		for (Input input : inputs)
			try
			{
				input.update(vars[i++].data(), 0);
			}
			catch (PDUResultException e)
			{
				LOGGER.debug("Could not read data from input {} from plc {}.", input.id, this.plc.id(), e); //$NON-NLS-1$
				input.updateNoValue();
			}
		LOGGER.trace("Successfully updated inputs: {}", Arrays.toString(inputs.toArray()));
	}
}
