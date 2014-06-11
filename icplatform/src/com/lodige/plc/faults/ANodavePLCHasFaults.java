/**
 * icplatform Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.plc.faults;

import github.javaappplatform.commons.events.Event;
import github.javaappplatform.commons.events.IListener;
import github.javaappplatform.commons.log.Logger;
import github.javaappplatform.platform.job.JobPlatform;

import java.io.IOException;
import java.util.HashMap;

import com.lodige.faults.Fault;
import com.lodige.faults.IFaultDB;
import com.lodige.plc.IInput;
import com.lodige.plc.IPLCAPI;
import com.lodige.plc.IPLCAPI.Type;
import com.lodige.plc.nodave.NodavePLC;

/**
 * TODO javadoc
 * @author renken
 */
public abstract class ANodavePLCHasFaults<F extends Fault>
{
	
	private static final Logger LOGGER = Logger.getLogger();


	private final IFaultDB<F> faultDB;
	private final HashMap<String, F> activeFaults = new HashMap<>();
	private final IListener inputListener = new IListener()
	{
		
		@Override
		public void handleEvent(Event e)
		{
			try
			{
				ANodavePLCHasFaults.this.checkInput(e.getData());
			}
			catch (IOException ex)
			{
				LOGGER.warn("Could not read plc data.", ex);
			}
		}
	};


	/**
	 * 
	 */
	public ANodavePLCHasFaults(IFaultDB<F> faultDB)
	{
		this.faultDB = faultDB;
	}


	public void initInputs(NodavePLC plc)
	{
		JobPlatform.runJob(() ->
		{
			for (String faultID : this.faultDB.knownFaults())
			{
				final String[] address = faultID.split("[.]");
				final String inputID = address[0] + "." + address[1];
				final int db = Integer.parseInt(address[0]);
				final int offset = Integer.parseInt(address[1]);
	
				IInput in = plc.getInput(inputID);
				if (in == null)
					in = plc.createDBInput(inputID, db, offset, Type.UBYTE);
			}
			plc.addListener(IPLCAPI.EVENT_INPUT_CHANGED, this.inputListener);
		}, IPLCAPI.PLC_UPDATE_THREAD);
	}
	
	private void checkInput(IInput input) throws IOException
	{
		final short inValue = input.ubyteValue();
		for (int i = 0; i < 8; i++)	//8 bit
		{
			final String faultID = input.id()+"."+i;
			if (!this.faultDB.knownFaults().contains(faultID))
				continue;

			final boolean active = (inValue & (1 << i)) != 0;
			if (active && !this.activeFaults.containsKey(faultID))
			{
				F f = this.faultDB.instantiateFault(faultID);
				this.activeFaults.put(faultID, f);
				this.activateFault(f);
			}
			else if (!active)
			{
				Fault f = this.activeFaults.remove(faultID);
				if (f != null)
					this.deactivateFault(f);
			}
		}
	}

	public void clearActiveFaults()
	{
		JobPlatform.runJob(() -> this.activeFaults.clear(), IPLCAPI.PLC_UPDATE_THREAD);
	}
	
	protected abstract void activateFault(F fault);
	
	protected abstract void deactivateFault(Fault fault);

}
