/**
 * icplatform Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.states;

import github.javaappplatform.commons.collection.SmallSet;
import github.javaappplatform.commons.events.IInnerTalker;

import com.lodige.states.IStateAPI.Type;

/**
 * TODO javadoc
 * @author renken
 */
public class OrBitState extends AState
{

	private final IState[] states;


	public OrBitState(String id, IState... states)
	{
		this(id, states[0].parent(), states);
	}

	/**
	 * @param id
	 * @param type
	 * @param parent
	 */
	public OrBitState(String id, IHasStates parent, IState... states)
	{
		super(id, Type.BIT, parent);
		this.states = states;
		SmallSet<IHasStates> parents = new SmallSet<>();
		for (IState state : this.states)
			parents.add(state.parent());
		for (IHasStates par : parents)
			par.addListener(IStateAPI.EVENT_STATE_CHANGED, (e) ->
		{
			if (contains(e.getData(), this.states))
				((IInnerTalker) this.parent).postEvent(e.type(), this);
		});
		((IHasStates.Internal) this.parent)._registerState(this);
	}

	private static final boolean contains(Object o, Object[] arr)
	{
		for (Object a : arr)
		{
			if (a == o)
				return true;
			if (o != null && o.equals(a))
				return true;
		}
		return false;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean bitValue() throws StateReadException
	{
		for (IState s : this.states)
			if (s.bitValue())
				return true;
		return false;
	}

}
