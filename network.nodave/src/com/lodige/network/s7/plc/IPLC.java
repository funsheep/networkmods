package com.lodige.network.s7.plc;

import github.javaappplatform.commons.events.ITalker;

import java.io.IOException;
import java.util.Collection;

import com.lodige.network.s7.plc.IPLCAPI.ConnectionState;
import com.lodige.network.s7.plc.IPLCAPI.UpdateFrequency;

/**
 * TODO javadoc
 * @author renken
 */
public interface IPLC extends ITalker
{

	public String id();
	
	public IInput getInput(String id);
	public Collection<IInput> inputs();
	public IOutput getOutput(String id);
	public Collection<IOutput> outputs();

	public void setUpdateMethod(UpdateFrequency frequency);

	public void beginTransaction();
	public boolean transactionActive();
	public void endTransaction() throws IOException;

	public ConnectionState connectionState();
	
}
