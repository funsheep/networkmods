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
public interface IFaultDB<F extends Fault>
{

	public F instantiateFault(String faultID);

	public Set<String> knownFaults();

}
