/**
 * network.nodave Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.plc.nodave;

import github.javaappplatform.commons.events.TalkerStub;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import com.lodige.network.INetworkAPI;
import com.lodige.network.client.ClientConnection;
import com.lodige.network.client.ClientNetworkService;
import com.lodige.network.plc.INodaveAPI.Area;
import com.lodige.network.plc.Write;
import com.lodige.network.plc.Write.Write3;
import com.lodige.plc.IInput;
import com.lodige.plc.IOutput;
import com.lodige.plc.IPLC;
import com.lodige.plc.IPLCAPI.Type;
import com.lodige.plc.IPLCAPI.UpdateFrequency;


/**
 * TODO javadoc
 * @author renken
 */
public class NodavePLC extends TalkerStub implements IPLC
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
	public NodavePLC(String host, ClientNetworkService service) throws IOException
	{
		this.cc = new ClientConnection(host, 102, null, service);
		this.cc.connect();
		new InputPolling(this);
	}

	public NodavePLC(ClientConnection connection)
	{
		this.cc = connection;
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

	
	public IInput createDBInput(String id, int database, int offset, Type type)
	{
		return this.createInput(id, Area.DB, database, offset, type.size, type);
	}
	
	public IInput createGenericDBInput(String id, int database, int offset, int length)
	{
		return this.createInput(id, Area.DB, database, offset, length, Type.GENERIC);
	}
	
	public IInput createNonDBInput(String id, Area area, int offset, Type type)
	{
		return this.createInput(id, area, 0, offset, type.size, type);
	}
	
	public IInput createGenericNonDBInput(String id, Area area, int offset, int length)
	{
		return this.createInput(id, area, 0, offset, length, Type.GENERIC);
	}
	
	private synchronized Input createInput(String id, Area area, int database, int offset, int length, Type type)
	{
//		if (type == Type.BIT)
//			throw new UnsupportedOperationException("Use FlagInput wrapper instead.");
		if (this.inputs.containsKey(id))
			throw new IllegalArgumentException("Input with id " + id + " is already known.");
		Input input = new Input(id, area, database, offset, length, type, this);
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

	public IOutput createDBOutput(String id, int database, int offset, Type type)
	{
		return this.createOutput(id, Area.DB, database, offset, type.size, type);
	}
	
	public IOutput createGenericDBOutput(String id, int database, int offset, int length)
	{
		return this.createOutput(id, Area.DB, database, offset, length, Type.GENERIC);
	}
	
	public IOutput createNonDBOutput(String id, Area area, int offset, Type type)
	{
		return this.createOutput(id, area, 0, offset, type.size, type);
	}
	
	public IOutput createGenericNonDBOutput(String id, Area area, int offset, int length)
	{
		return this.createOutput(id, area, 0, offset, length, Type.GENERIC);
	}
	
	private synchronized Output createOutput(String id, Area area, int database, int offset, int length, Type type)
	{
//		if (type == Type.BIT)
//			throw new UnsupportedOperationException();
		if (this.outputs.containsKey(id))
			throw new IllegalArgumentException("Output with id " + id + " is already known.");
		Output output = new Output(id, area, database, offset, length, type, this);
		this.outputs.put(id, output);
		return output;
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
		if (this.transactionActive && this.writer != null)
		{
			this.writer.execute();
		}
		this.transactionActive = false;
	}
	
	private Write3 writer;
	synchronized void writeOutput(Output out, byte... data) throws IOException
	{
		Write base = (this.writer == null) ? Write.toPLC(this.cc) : this.writer.andWrite();
		this.writer = base.data(data).to(out.area).andDatabase(out.database).startAt(out.offset);
		
		if (!this.transactionActive)
		{
			this.writer.execute();
			this.writer = null;
			return;
		}
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