/**
 * network.nodave Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.network.s7.plc.impl;

import github.javaappplatform.platform.job.JobPlatform;
import github.javaappplatform.platform.job.JobbedTalkerStub;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import com.lodige.network.INetworkAPI;
import com.lodige.network.client.ClientConnection;
import com.lodige.network.client.ClientNetworkService;
import com.lodige.network.s7.plc.IInput;
import com.lodige.network.s7.plc.IOutput;
import com.lodige.network.s7.plc.IPLC;
import com.lodige.network.s7.plc.IPLCAPI;
import com.lodige.network.s7.plc.IPLCAPI.ConnectionState;
import com.lodige.network.s7.plc.IPLCAPI.Type;
import com.lodige.network.s7.plc.util.PLCTools;
import com.lodige.network.s7.protocol.INodaveAPI.Area;
import com.lodige.network.s7.protocol.Write;
import com.lodige.network.s7.protocol.Write.Write3;


/**
 * TODO javadoc
 * @author renken
 */
public class NodavePLC extends JobbedTalkerStub implements IPLC.Internal
{

	protected final ClientConnection cc;
	private final HashMap<String, Input> inputs = new HashMap<>();
	private final HashMap<String, IOutput> outputs = new HashMap<>();
	private boolean transactionActive = false;


	/**
	 * 
	 */
	public NodavePLC(String host, ClientNetworkService service) throws IOException
	{
		super(JobPlatform.MAIN_THREAD);
		this.cc = new ClientConnection(host, 102, null, service);
		this.cc.connect();
		new InputPolling(this);
	}

	public NodavePLC(ClientConnection connection)
	{
		super(JobPlatform.MAIN_THREAD);
		this.cc = connection;
		this.cc.addListener(INetworkAPI.E_STATE_CHANGED, (e) -> this.postEvent(IPLCAPI.EVENT_CONNECTION_STATE_CHANGED));
		new InputPolling(this);
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String id()
	{
		return this.cc.alias();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ClientConnection connection()
	{
		return this.cc;
	}

	
	public IInput createDBInput(int database, int offset, Type type)
	{
		return this.createInput(Area.DB, database, offset, type.size, type);
	}
	
	public IInput createGenericDBInput(int database, int offset, int length)
	{
		return this.createInput(Area.DB, database, offset, length, Type.GENERIC);
	}
	
	public IInput createNonDBInput(Area area, int offset, Type type)
	{
		return this.createInput(area, 0, offset, type.size, type);
	}
	
	public IInput createGenericNonDBInput(Area area, int offset, int length)
	{
		return this.createInput(area, 0, offset, length, Type.GENERIC);
	}
	
	private synchronized Input createInput(Area area, int database, int offset, int length, Type type)
	{
		String id = PLCTools.uID(area, database, offset);
//		if (type == Type.BIT)
//			throw new UnsupportedOperationException("Use FlagInput wrapper instead.");
		if (this.inputs.containsKey(id))
			throw new IllegalArgumentException("Input with id " + id + " is already known."); //$NON-NLS-1$ //$NON-NLS-2$
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

	public IOutput createDBOutput(int database, int offset, Type type)
	{
		return this.createOutput(Area.DB, database, offset, type.size, type);
	}
	
	public IOutput createGenericDBOutput(int database, int offset, int length)
	{
		return this.createOutput(Area.DB, database, offset, length, Type.GENERIC);
	}
	
	public IOutput createNonDBOutput(Area area, int offset, Type type)
	{
		return this.createOutput(area, 0, offset, type.size, type);
	}
	
	public IOutput createGenericNonDBOutput(Area area, int offset, int length)
	{
		return this.createOutput(area, 0, offset, length, Type.GENERIC);
	}
	
	private synchronized Output createOutput(Area area, int database, int offset, int length, Type type)
	{
		String id = PLCTools.uID(area, database, offset);
//		if (type == Type.BIT)
//			throw new UnsupportedOperationException();
		if (this.outputs.containsKey(id))
			throw new IllegalArgumentException("Output with id " + id + " is already known."); //$NON-NLS-1$ //$NON-NLS-2$
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
		if (this.transactionActive)
		{
			this.transactionActive = false;
			if (this.writer != null)
			{
				this.writer.execute();
				this.writer = null;
			}
		}
	}
	
	private Write3 writer = null;
	synchronized void writeOutput(Output out, byte... data) throws IOException
	{
		Write base = (this.writer == null) ? Write.toPLC(this.cc) : this.writer.andWrite();
		this.writer = base.data(data).to(out.area).andDatabase(out.database).startAt(out.offset);
		
		if (!this.transactionActive)
		{
			this.writer.execute();
			this.writer = null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ConnectionState connectionState()
	{
		switch (this.cc.state())
		{
			case INetworkAPI.S_CONNECTED:
				return ConnectionState.CONNECTED;
			case INetworkAPI.S_NOT_CONNECTED:
				return ConnectionState.NOT_CONNECTED;
			case INetworkAPI.S_CONNECTION_PENDING:
				return ConnectionState.CONNECTING;
			case INetworkAPI.S_CLOSING:
				return ConnectionState.SHUTTING_DOWN;
			default:
				return ConnectionState.UNKNOWN;
		}
	}

}
