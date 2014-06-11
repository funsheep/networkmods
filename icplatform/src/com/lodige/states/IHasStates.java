/**
 * GGZ Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.states;

import github.javaappplatform.commons.events.ITalker;

import java.util.Collection;

/**
 * TODO javadoc
 * @author renken
 */
public interface IHasStates extends ITalker
{

	public IState getState(String id);

	public Collection<IState> states();
	
}
