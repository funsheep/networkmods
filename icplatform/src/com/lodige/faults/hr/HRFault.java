/**
 * icplatform Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.faults.hr;

import java.time.Instant;


/**
 * TODO javadoc
 * @author renken
 */
public class HRFault extends com.lodige.faults.Fault
{

	public final String deviceID;
	public final String description;

	
	public HRFault(String id, String deviceID, String description, Instant occurrence)
	{
		super(id, occurrence);
		this.deviceID = deviceID;
		this.description = description;
	}
	
}
