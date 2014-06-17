/**
 * icplatform Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.plc.dummy;

import github.javaappplatform.platform.job.JobbedTalkerStub;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import com.lodige.plc.IInput;
import com.lodige.plc.IOutput;
import com.lodige.plc.IPLC;
import com.lodige.plc.IPLCAPI;
import com.lodige.plc.IPLCAPI.ConnectionState;
import com.lodige.plc.IPLCAPI.UpdateFrequency;

/**
 * TODO javadoc
 * @author renken
 */
public class DummyPLC extends JobbedTalkerStub implements IPLC
{

	private final String id;
	private final HashMap<String, IInput> inputs = new HashMap<>();
	private final HashMap<String, IOutput> outputs = new HashMap<>();
	private boolean updateOnTrigger = false;
	private UpdateFrequency frequency = UpdateFrequency.MEDIUM;
	private boolean transactionActive = false;


	/**
	 * 
	 */
	public DummyPLC(String id)
	{
		super(IPLCAPI.PLC_UPDATE_THREAD);
		this.id = id;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public String id()
	{
		return this.id;
	}

	synchronized void registerInput(DummyInput input)
	{
		if (this.inputs.containsKey(input.id()))
			throw new IllegalArgumentException("An input with ID " + input.id() + " is already registered.");
		this.inputs.put(input.id(), input);
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

	synchronized void delete(IInput input)
	{
		this.inputs.remove(input.id());
	}

	synchronized void registerOutput(DummyOutput output)
	{
		if (this.outputs.containsKey(output.id()))
			throw new IllegalArgumentException("An output with ID " + output.id() + " is already registered.");
		this.outputs.put(output.id(), output);
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

	synchronized void delete(IOutput output)
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
	public synchronized void endTransaction() throws IOException
	{
		this.transactionActive = false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ConnectionState connectionState()
	{
		return ConnectionState.CONNECTED;
	}

}
