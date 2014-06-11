/**
 * icplatform Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.faults;

import java.time.Instant;

/**
 * TODO javadoc
 * @author renken
 */
public class Fault
{

	public final String id;
	public final Instant occurrence;


	/**
	 * 
	 */
	public Fault(String id, Instant occurrence)
	{
		this.id = id;
		this.occurrence = occurrence;
	}
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		return this.id + " Occurred At " + this.occurrence;
	}
}
