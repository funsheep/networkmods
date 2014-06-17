/**
 * icplatform Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.faults;

import java.util.Set;

/**
 * TODO javadoc
 * @author renken
 */
public interface IFaultDB
{

	public Fault instantiateFault(String faultID);

	public Set<String> knownFaults();

}
