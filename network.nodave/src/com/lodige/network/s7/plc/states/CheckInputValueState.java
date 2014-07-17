/**
 * GGZ Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.network.s7.plc.states;

import java.io.IOException;
import java.util.Arrays;

import com.lodige.network.s7.plc.IInput;
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
public class CheckInputValueState extends AState
{

	private final IInput toTest;
	private final Object test;


	/**
	 * @param id
	 * @param type
	 * @param parent
	 */
	public CheckInputValueState(String id, Object test, IInput toTest, IHasStates parent)
	{
		super(id, Type.BIT, parent);
		this.toTest = toTest;
		this.test = test;
		this.toTest.plc().addListener(IPLCAPI.EVENT_INPUT_CHANGED, (e) ->
		{
			if (e.getData() == this.toTest)
				this.parent.postEvent(IStateAPI.EVENT_STATE_CHANGED, this);
		});
		this.parent._registerState(this);
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean bitValue() throws StateReadException
	{
		try
		{
			switch (this.toTest.type())
			{
				case FLOAT:
					return ((Float) this.test).floatValue() == this.toTest.floatValue();
				case GENERIC:
					return Arrays.equals((byte[]) this.test, this.toTest.genericValue());
				case INT:
					return ((Integer) this.test).floatValue() == this.toTest.intValue();
				case SHORT:
					return ((Short) this.test).floatValue() == this.toTest.shortValue();
				case UBYTE:
					return ((Short) this.test).floatValue() == this.toTest.ubyteValue();
				case UINT:
					return ((Long) this.test).floatValue() == this.toTest.uintValue();
				case USHORT:
					return ((Integer) this.test).floatValue() == this.toTest.ushortValue();
				default:
					throw new StateReadException();
			}
		}
		catch (IOException ex)
		{
			throw new StateReadException(ex);
		}
	}
	
}
