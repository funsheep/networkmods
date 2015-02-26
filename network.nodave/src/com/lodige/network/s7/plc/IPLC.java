package com.lodige.network.s7.plc;

import github.javaappplatform.commons.events.ITalker;

import java.io.IOException;
import java.util.Collection;

import com.lodige.network.client.ClientConnection;
import com.lodige.network.s7.plc.IPLCAPI.ConnectionState;
import com.lodige.network.s7.protocol.INodaveAPI.Area;

/**
 * TODO javadoc
 * @author renken
 */
public interface IPLC extends ITalker
{

	public String id();

	public default IInput getDBInput(int database, int offset)
	{
		return this.getNonDBInput(Area.DB, database, offset);
	}
	public default IInput getNonDBInput(Area area, int database, int offset)
	{
		return this.getInput(area.name() + database + ":" + offset);
	}
	public IInput getInput(String id);
	public Collection<IInput> inputs();
	public default IOutput getDBOutput(int database, int offset)
	{
		return this.getNonDBOutput(Area.DB, database, offset);
	}
	public default IOutput getNonDBOutput(Area area, int database, int offset)
	{
		return this.getOutput(area.name() + database + ":" + offset);
	}
	public IOutput getOutput(String id);
	public Collection<IOutput> outputs();

	public void beginTransaction();
	public boolean transactionActive();
	public void endTransaction() throws IOException;

	public ConnectionState connectionState();
	
	public interface Internal extends IPLC
	{
		public ClientConnection connection();
	}
}
