/**
 * icplatform Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.network;

import github.javaappplatform.commons.collection.SmallMap;
import github.javaappplatform.platform.job.JobPlatform;
import github.javaappplatform.platform.job.JobbedTalkerStub;

import java.util.ArrayList;
import java.util.Collection;

import com.lodige.states.IHasStates;
import com.lodige.states.IState;
import com.lodige.states.IStateAPI.Type;

/**
 * TODO javadoc
 * @author renken
 */
public class ConnectionOverview extends JobbedTalkerStub implements IHasStates
{

	private final SmallMap<String, IState> states = new SmallMap<>();
	
	
	/**
	 * 
	 */
	public ConnectionOverview()
	{
		super(JobPlatform.MAIN_THREAD);
	}

	
	public synchronized void registerState(IState state)
	{
		if (state.type() != Type.BIT)
			throw new IllegalArgumentException("Only bit based types are valid.");
		if (this.states.containsKey(state.id()))
			throw new IllegalArgumentException("State with id " + state.id() + " is already known.");
		this.states.put(state.id(), state);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized IState getState(String id)
	{
		return this.states.get(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized Collection<IState> states()
	{
		return new ArrayList<>(this.states.values());
	}

}
