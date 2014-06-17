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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.locks.ReentrantLock;

import com.lodige.faults.Fault;
import com.lodige.faults.IFaultDB;
import com.lodige.faults.IHasFaults;
import com.lodige.plc.IInput;
import com.lodige.plc.IPLC;
import com.lodige.plc.IPLCAPI;
import com.lodige.plc.IPLCAPI.ConnectionState;
import com.lodige.plc.IPLCAPI.Type;
import com.lodige.plc.nodave.NodavePLC;

/**
 * TODO javadoc
 * @author renken
 */
public abstract class ANodavePLCHasFaults implements IHasFaults
{
	
	private static final Logger LOGGER = Logger.getLogger();
	
	public static final String FAULT_NOT_CONNECTED = "PLC is not Connected.";


	private final IFaultDB faultDB;
	private final HashSet<IInput> knownInputs = new HashSet<>();
	private final HashMap<String, Fault> activeFaults = new HashMap<>();
	private final ReentrantLock activeFaultsLock = new ReentrantLock();
	private final IListener inputListener = new IListener()
	{
		
		@Override
		public void handleEvent(Event e)
		{
			try
			{
				IInput input = e.getData();
				if (ANodavePLCHasFaults.this.faultDB.knownFaults().contains(input.id()))
					ANodavePLCHasFaults.this.checkInput(input);
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
	public ANodavePLCHasFaults(IFaultDB faultDB)
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
				ANodavePLCHasFaults parent = ANodavePLCHasFaults.this;
				parent.activeFaultsLock.lock();
				try
				{
					if (!parent.activeFaults.containsKey(FAULT_NOT_CONNECTED) && e.<IPLC>getSource().connectionState() != IPLCAPI.ConnectionState.CONNECTED)
					{
						Fault f = parent.faultDB.instantiateFault(FAULT_NOT_CONNECTED);
						parent.clearFaults();
						parent._activateFault(f);
					}
					else if (parent.activeFaults.containsKey(FAULT_NOT_CONNECTED) && e.<IPLC>getSource().connectionState() == IPLCAPI.ConnectionState.CONNECTED)
					{
						parent._deactivateFault(FAULT_NOT_CONNECTED);
						parent.checkInputs(plc);
					}
				}
				finally
				{
					parent.activeFaultsLock.unlock();
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
		if (plc.connectionState() != ConnectionState.CONNECTED && plc.connectionState() != ConnectionState.CONNECTING)
			return;
		for (IInput in : this.knownInputs)
		{
			this.activeFaultsLock.lock();
			try
			{
				this.checkInput(in);
			}
			catch (IOException ex)
			{
				LOGGER.warn("Could not read plc data.", ex);
			}
			finally
			{
				this.activeFaultsLock.unlock();
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
				Fault f = this.faultDB.instantiateFault(faultID);
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
		this.activeFaultsLock.lock();
		try
		{
			for (Fault f : new ArrayList<>(this.activeFaults.values()))
				this._deactivateFault(f);
		}
		finally
		{
			this.activeFaultsLock.unlock();
		}
	}

	private void _activateFault(Fault fault)
	{
		this.activeFaultsLock.lock();
		try
		{
			this.activeFaults.put(fault.id, fault);
			JobPlatform.runJob(() -> this.activateFault(fault) , JobPlatform.MAIN_THREAD);
		}
		finally
		{
			this.activeFaultsLock.unlock();
		}
	}

	private void _deactivateFault(String faultID)
	{
		this.activeFaultsLock.lock();
		try
		{
			Fault f = this.activeFaults.remove(faultID);
			if (f != null)
				JobPlatform.runJob(() -> this.deactivateFault(f) , JobPlatform.MAIN_THREAD);
		}
		finally
		{
			this.activeFaultsLock.unlock();
		}
	}

	private void _deactivateFault(Fault fault)
	{
		this.activeFaultsLock.lock();
		try
		{
			if (this.activeFaults.remove(fault.id) != null)
				JobPlatform.runJob(() -> this.deactivateFault(fault) , JobPlatform.MAIN_THREAD);
		}
		finally
		{
			this.activeFaultsLock.unlock();
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<Fault> activeFaults()
	{
		this.activeFaultsLock.lock();
		try
		{
			return new ArrayList<>(this.activeFaults.values());
		}
		finally
		{
			this.activeFaultsLock.unlock();
		}
	}


	protected abstract void activateFault(Fault fault);
	
	protected abstract void deactivateFault(Fault fault);

}
