package com.lodige.plc;

import java.io.IOException;
import java.util.Collection;

import com.lodige.plc.IPLCAPI.UpdateFrequency;

/**
 * TODO javadoc
 * @author renken
 */
public interface IPLC
{

	public String id();
	
	public IInput getInput(String id);
	public Collection<IInput> inputs();
	public IOutput getOutput(String id);
	public Collection<IOutput> outputs();

	public void setUpdateMethod(UpdateFrequency frequency, boolean updateOnTrigger);

	public void beginTransaction();
	public boolean transactionActive();
	public void endTransaction() throws IOException;

	public boolean connected();
}
