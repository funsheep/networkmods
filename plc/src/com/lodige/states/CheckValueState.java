/**
 * GGZ Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.states;

import github.javaappplatform.commons.events.IInnerTalker;

import java.util.Arrays;

import com.lodige.states.IStateAPI.Type;

/**
 * TODO javadoc
 * @author renken
 */
public class CheckValueState extends AState
{

	private final IState toTest;
	private final Object test;


	public CheckValueState(String id, Object test, IState toTest)
	{
		this(id, test, toTest, toTest.parent());
	}
	
	/**
	 * @param id
	 * @param type
	 * @param parent
	 */
	public CheckValueState(String id, Object test, IState toTest, IHasStates parent)
	{
		super(id, Type.BIT, parent);
		this.toTest = toTest;
		this.test = test;
		parent.addListener(IStateAPI.EVENT_STATE_CHANGED, (e) ->
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
		switch (this.toTest.type())
		{
			case BIT:
				return ((Boolean) this.test).booleanValue() == this.toTest.bitValue();
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
			case OBJECT:
				return this.test == this.toTest.objectValue() || this.test.equals(this.toTest.objectValue());
			case STRING:
				return this.test == this.toTest.stringValue() || this.test.equals(this.toTest.stringValue());
			default:
				throw new StateReadException();
		}
	}
	
}
