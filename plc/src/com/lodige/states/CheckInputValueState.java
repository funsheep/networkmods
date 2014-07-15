/**
 * GGZ Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.states;

import github.javaappplatform.commons.events.IInnerTalker;

import java.io.IOException;
import java.util.Arrays;

import com.lodige.plc.IInput;
import com.lodige.plc.IPLCAPI;
import com.lodige.states.IStateAPI.Type;

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
				((IInnerTalker) this.parent).postEvent(IStateAPI.EVENT_STATE_CHANGED, this);
		});
		((IHasStates.Internal) this.parent)._registerState(this);
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
