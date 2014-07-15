/**
 * GGZ Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.faults.hr;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.lodige.faults.Fault;
import com.lodige.faults.IFaultDB;

/**
 * TODO javadoc
 * @author renken
 */
public final class HRFaultDB implements IFaultDB
{

	private final Map<String, String> deviceIDByFaultID = new HashMap<>();
	private final Map<String, String> descriptionByFaultID = new HashMap<>();
	

	/**
	 * 
	 */
	HRFaultDB()
	{
		//nothing to do.
	}

	void defineFault(String faultID, String deviceID, String description)
	{
		this.deviceIDByFaultID.put(faultID, deviceID);
		this.descriptionByFaultID.put(faultID, description);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Fault instantiateFault(String faultID)
	{
		return new HRFault(faultID, this.deviceIDByFaultID.get(faultID), this.descriptionByFaultID.get(faultID), Instant.now());
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<String> knownFaults()
	{
		return Collections.unmodifiableSet(this.deviceIDByFaultID.keySet());
	}

}
