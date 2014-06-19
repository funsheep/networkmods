/**
 * icplatform Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.faults.hr;


/**
 * TODO javadoc
 * @author renken
 */
public class DefineHRFaultDB
{

	private final HRFaultDB knownFaults = new HRFaultDB();

	
	public DefineHRFaultDB andWithFault(int db, int offset, int bit, String deviceID, String description)
	{
		final String id = String.valueOf(db) + '.' + offset + '.' + bit;
		this.knownFaults.defineFault(id, deviceID, description);
		return this;
	}
	
	public HRFaultDB compileDB()
	{
		return this.knownFaults;
	}
	
	public static final DefineHRFaultDB withFault(int db, int offset, int bit, String deviceID, String description)
	{
		DefineHRFaultDB faults = new DefineHRFaultDB();
		faults.andWithFault(db, offset, bit, deviceID, description);
		return faults;
	}
	
	
	private DefineHRFaultDB()
	{
		//no external instance
	}
}
