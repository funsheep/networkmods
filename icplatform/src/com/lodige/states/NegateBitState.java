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


	/**
	 * @param id
	 * @param type
	 * @param parent
	 */
	public NegateBitState(String id, IState state)
	{
		super(id, Type.BIT, state.parent());
		this.state = state;
		this.parent.addListener(IStateAPI.EVENT_STATE_CHANGED, (e) ->
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
