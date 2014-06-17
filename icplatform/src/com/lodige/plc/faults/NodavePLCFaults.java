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
import java.util.HashSet;

import com.lodige.faults.Fault;
import com.lodige.faults.IFaultDB;
import com.lodige.plc.IInput;
import com.lodige.plc.IPLC;
import com.lodige.plc.IPLCAPI;
import com.lodige.plc.IPLCAPI.Type;
import com.lodige.plc.nodave.NodavePLC;

/**
 * TODO javadoc
 * @author renken
 */
public abstract class NodavePLCFaults<F extends Fault>
{
	
	private static final Logger LOGGER = Logger.getLogger();
	
	public static final String FAULT_NOT_CONNECTED = "PLC is not Connected.";


	private final IFaultDB<F> faultDB;
	private final HashSet<IInput> knownInputs = new HashSet<>();
	private final HashMap<String, F> activeFaults = new HashMap<>();
	private final IListener inputListener = new IListener()
	{
		
		@Override
		public void handleEvent(Event e)
		{
			try
			{
				IInput input = e.getData();
				if (NodavePLCFaults.this.faultDB.knownFaults().contains(input.id()))
					NodavePLCFaults.this.checkInput(input);
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
	public NodavePLCFaults(IFaultDB<F> faultDB)
	{
		this.faultDB = faultDB;
	}


	public void initFaults(NodavePLC plc)
	{
		this.initInputs(plc);
		plc.addListener(IPLCAPI.EVENT_INPUT_CHANGED, this.inputListener);
		plc.addListener(IPLCAPI.EVENT_CONNECTION_STATE_CHANGED, new IListener()
		{
			@Override
			public void handleEvent(Event e)
			{
				NodavePLCFaults<F> parent = NodavePLCFaults.this;
				if (!parent.activeFaults.containsKey(FAULT_NOT_CONNECTED) && e.<IPLC>getSource().connectionState() != IPLCAPI.ConnectionState.CONNECTED)
				{
					F f = parent.faultDB.instantiateFault(FAULT_NOT_CONNECTED);
					parent.clearFaults();
					parent._activateFault(f);
				}
				else if (parent.activeFaults.containsKey(FAULT_NOT_CONNECTED) && e.<IPLC>getSource().connectionState() == IPLCAPI.ConnectionState.CONNECTED)
				{
					parent._deactivateFault(FAULT_NOT_CONNECTED);
					parent.checkInputs(plc);
				}
			}
		});
	}

	private void initInputs(NodavePLC plc)
	{
		for (String faultID : this.faultDB.knownFaults())
		{
			final String[] address = faultID.split("[.]");
			if (address.length == 3)
			{
				final String inputID = address[0] + "." + address[1];
				IInput in = plc.getInput(inputID);
				if (in == null)
				{
					final int db = Integer.parseInt(address[0]);
					final int offset = Integer.parseInt(address[1]);
					in = plc.createDBInput(inputID, db, offset, Type.UBYTE);
				}
				this.knownInputs.add(in);
			}
		}
		this.checkInputs(plc);
	}

	private void checkInputs(NodavePLC plc)
	{
		for (IInput in : this.knownInputs)
		{
			try
			{
				this.checkInput(in);
			}
			catch (IOException ex)
			{
				LOGGER.warn("Could not read plc data.", ex);
			}
		}
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
				this._activateFault(f);
			}
			else if (!active)
			{
				this._deactivateFault(faultID);
			}
		}
	}
	
	private void clearFaults()
	{
		for (Fault f : this.activeFaults.values())
			this._deactivateFault(f);
		this.activeFaults.clear();
	}

	private void _activateFault(F fault)
	{
		this.activeFaults.put(fault.id, fault);
		JobPlatform.runJob(() -> this.activateFault(fault) , JobPlatform.MAIN_THREAD);
	}

	private void _deactivateFault(String faultID)
	{
		Fault f = this.activeFaults.remove(faultID);
		if (f != null)
			JobPlatform.runJob(() -> this.deactivateFault(f) , JobPlatform.MAIN_THREAD);
	}

	private void _deactivateFault(Fault fault)
	{
		if (this.activeFaults.remove(fault.id) != null)
			JobPlatform.runJob(() -> this.deactivateFault(fault) , JobPlatform.MAIN_THREAD);
	}

	
	protected abstract void activateFault(F fault);
	
	protected abstract void deactivateFault(Fault fault);

}
