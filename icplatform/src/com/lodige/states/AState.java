/**
 * GGZ Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.states;

import com.lodige.states.IStateAPI.Type;

/**
 * TODO javadoc
 * @author renken
 */
public abstract class AState implements IState
{

	protected final String id;
	protected final Type type;
	protected final IHasStates parent;


	/**
	 * 
	 */
	public AState(String id, Type type, IHasStates parent)
	{
		this.id = id;
		this.type = type;
		this.parent = parent;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public String id()
	{
		return this.id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Type type()
	{
		return this.type;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IHasStates parent()
	{
		return this.parent;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean bitValue() throws StateReadException
	{
		if (this.type() != Type.BIT)
			throw new IllegalStateException("Type of this State does not match.");
		throw new StateReadException("Not implemented.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public short shortValue() throws StateReadException
	{
		if (this.type() != Type.SHORT)
			throw new IllegalStateException("Type of this State does not match.");
		throw new StateReadException("Not implemented.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int intValue() throws StateReadException
	{
		if (this.type() != Type.INT)
			throw new IllegalStateException("Type of this State does not match.");
		throw new StateReadException("Not implemented.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public float floatValue() throws StateReadException
	{
		if (this.type() != Type.FLOAT)
			throw new IllegalStateException("Type of this State does not match.");
		throw new StateReadException("Not implemented.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public short ubyteValue() throws StateReadException
	{
		if (this.type() != Type.UBYTE)
			throw new IllegalStateException("Type of this State does not match.");
		throw new StateReadException("Not implemented.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int ushortValue() throws StateReadException
	{
		if (this.type() != Type.USHORT)
			throw new IllegalStateException("Type of this State does not match.");
		throw new StateReadException("Not implemented.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long uintValue() throws StateReadException
	{
		if (this.type() != Type.UINT)
			throw new IllegalStateException("Type of this State does not match.");
		throw new StateReadException("Not implemented.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String stringValue() throws StateReadException
	{
		if (this.type() != Type.STRING)
			throw new IllegalStateException("Type of this State does not match.");
		throw new StateReadException("Not implemented.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte[] genericValue() throws StateReadException
	{
		if (this.type() != Type.GENERIC)
			throw new IllegalStateException("Type of this State does not match.");
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <O> O objectValue() throws StateReadException
	{
		if (this.type() != Type.OBJECT)
			throw new IllegalStateException("Type of this State does not match.");
		throw new StateReadException("Not implemented.");
	}

}
