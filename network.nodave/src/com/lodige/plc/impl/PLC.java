/**
 * network.nodave Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.plc.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import com.lodige.network.INetworkAPI;
import com.lodige.network.client.ClientConnection;
import com.lodige.network.client.ClientNetworkService;
import com.lodige.network.plc.INodaveAPI.Area;
import com.lodige.network.plc.Write;
import com.lodige.plc.IInput;
import com.lodige.plc.IOutput;
import com.lodige.plc.IPLC;
import com.lodige.plc.IPLCAPI.Type;
import com.lodige.plc.IPLCAPI.UpdateFrequency;


/**
 * TODO javadoc
 * @author renken
 */
public class PLC implements IPLC
{

	protected final ClientConnection cc;
	private final HashMap<String, Input> inputs = new HashMap<>();
	private final HashMap<String, IOutput> outputs = new HashMap<>();
	private boolean updateOnTrigger = false;
	private UpdateFrequency frequency = UpdateFrequency.MEDIUM;
	private boolean transactionActive = false;


	/**
	 * 
	 */
	public PLC(String host, ClientNetworkService service) throws IOException
	{
		this.cc = new ClientConnection(host, 102, null, service);
		this.cc.connect();
		new InputPolling(this);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public String id()
	{
		return this.cc.alias() != null ? this.cc.alias() : this.cc.address().toString();
	}

	
	public IInput createBitInput(String id, int database, int offset, int bitnr)
	{
		return this.createInput(id, Area.DB, database, offset, bitnr, Type.BIT);
	}
	
	public IInput createInput(String id, int database, int offset, Type type)
	{
		return this.createInput(id, Area.DB, database, offset, 0, type);
	}
	
	public IInput createInput(String id, Area area, int offset, Type type)
	{
		return this.createInput(id, area, 0, offset, 0, type);
	}
	
	private synchronized Input createInput(String id, Area area, int database, int offset, int bitNr, Type type)
	{
		if (this.inputs.containsKey(id))
			throw new IllegalArgumentException("Input with id " + id + " is already known.");
		Input input = new Input(id, area, database, offset, bitNr, type, this);
		this.inputs.put(id, input);
		return input;
	}
	
	protected synchronized void delete(IInput input)
	{
		this.inputs.remove(input.id());
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized IInput getInput(String id)
	{
		return this.inputs.get(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized Collection<IInput> inputs()
	{
		return new ArrayList<>(this.inputs.values());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized IOutput getOutput(String id)
	{
		return this.outputs.get(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized Collection<IOutput> outputs()
	{
		return new ArrayList<>(this.outputs.values());
	}

	protected synchronized void delete(IOutput output)
	{
		this.outputs.remove(output.id());
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void setUpdateMethod(UpdateFrequency frequency, boolean updateOnTrigger)
	{
		this.updateOnTrigger = updateOnTrigger;
		this.frequency = frequency;
	}
	
	synchronized UpdateFrequency frequency()
	{
		return this.frequency;
	}

	synchronized boolean onTrigger()
	{
		return this.updateOnTrigger;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void beginTransaction()
	{
		if (!this.transactionActive)
		{
			//FIXME initiate transaction
		}
		this.transactionActive = true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized boolean transactionActive()
	{
		return this.transactionActive;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void endTransaction()
	{
		if (this.transactionActive)
		{
			//FIXME write data
		}
		this.transactionActive = false;
	}
	
	void writeOutput(Output out, byte... data)
	{
		Write.toPLC(this.cc).data(data).to(out.area).andDatabase(out.database).startAt(out.offset);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean connected()
	{
		return this.cc.state() == INetworkAPI.S_CONNECTED;
	}

}
