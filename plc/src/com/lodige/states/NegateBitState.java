/**
 * icplatform Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.states;

import github.javaappplatform.commons.events.IInnerTalker;

import com.lodige.states.IStateAPI.Type;

/**
 * TODO javadoc
 * @author renken
 */
public class NegateBitState extends AState
{
	
	private final IState state;


	public NegateBitState(String id, IState state)
	{
		this(id, state, state.parent());
	}

	/**
	 * @param id
	 * @param type
	 * @param parent
	 */
	public NegateBitState(String id, IState state, IHasStates parent)
	{
		super(id, Type.BIT, parent);
		this.state = state;
		parent.addListener(IStateAPI.EVENT_STATE_CHANGED, (e) ->
		{
			if (e.getData() == this.state)
				((IInnerTalker) this.parent).postEvent(e.type(), this);
		});
		((IHasStates.Internal) this.parent)._registerState(this);
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean bitValue() throws StateReadException
	{
		return !this.state.bitValue();
	}
}
