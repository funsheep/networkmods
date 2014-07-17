/**
 * icplatform Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.network.s7.plc.states;

import github.javaappplatform.commons.events.IInnerTalker;
import github.javaappplatform.commons.util.GenericsToolkit;

import com.lodige.network.s7.plc.IPLC;
import com.lodige.network.s7.plc.IPLCAPI;
import com.lodige.states.IHasStates;
import com.lodige.states.IStateAPI;
import com.lodige.states.IStateAPI.Type;
import com.lodige.states.impl.AState;
import com.lodige.states.impl.StateReadException;

/**
 * TODO javadoc
 * @author renken
 */
public class PLCConnectionState extends AState
{

	private final IPLC plc;
	

	/**
	 * @param id
	 * @param type
	 * @param parent
	 */
	public PLCConnectionState(IPLC plc, IHasStates parent)
	{
		super("Connection State", Type.OBJECT, parent); //$NON-NLS-1$
		this.plc = plc;
		this.parent._registerState(this);
		this.plc.addListener(IPLCAPI.EVENT_CONNECTION_STATE_CHANGED, (e) -> ((IInnerTalker) parent).postEvent(IStateAPI.EVENT_STATE_CHANGED, this));
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public <O> O objectValue() throws StateReadException
	{
		if (this.type() != Type.OBJECT)
			throw new IllegalStateException("Type of this State does not match."); //$NON-NLS-1$
		return GenericsToolkit.convertUnchecked(this.plc.connectionState());
	}
	
}
