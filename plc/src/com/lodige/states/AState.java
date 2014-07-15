/**
 * GGZ Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.states;

import github.javaappplatform.commons.util.Strings;

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
			throw new IllegalStateException("Type of this State does not match."); //$NON-NLS-1$
		throw new StateReadException("Not implemented."); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public short shortValue() throws StateReadException
	{
		if (this.type() != Type.SHORT)
			throw new IllegalStateException("Type of this State "+this.type+" does not match " + Type.SHORT); //$NON-NLS-1$
		throw new StateReadException("Not implemented."); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int intValue() throws StateReadException
	{
		if (this.type() != Type.INT)
			throw new IllegalStateException("Type of this State does not match."); //$NON-NLS-1$
		throw new StateReadException("Not implemented."); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public float floatValue() throws StateReadException
	{
		if (this.type() != Type.FLOAT)
			throw new IllegalStateException("Type of this State does not match."); //$NON-NLS-1$
		throw new StateReadException("Not implemented."); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public short ubyteValue() throws StateReadException
	{
		if (this.type() != Type.UBYTE)
			throw new IllegalStateException("Type of this State does not match."); //$NON-NLS-1$
		throw new StateReadException("Not implemented."); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int ushortValue() throws StateReadException
	{
		if (this.type() != Type.USHORT)
			throw new IllegalStateException("Type of this State does not match."); //$NON-NLS-1$
		throw new StateReadException("Not implemented."); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long uintValue() throws StateReadException
	{
		if (this.type() != Type.UINT)
			throw new IllegalStateException("Type of this State does not match."); //$NON-NLS-1$
		throw new StateReadException("Not implemented."); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String stringValue() throws StateReadException
	{
		if (this.type() != Type.STRING)
			throw new IllegalStateException("Type of this State does not match."); //$NON-NLS-1$
		throw new StateReadException("Not implemented."); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte[] genericValue() throws StateReadException
	{
		if (this.type() != Type.GENERIC)
			throw new IllegalStateException("Type of this State does not match."); //$NON-NLS-1$
		throw new StateReadException("Not implemented."); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <O> O objectValue() throws StateReadException
	{
		if (this.type() != Type.OBJECT)
			throw new IllegalStateException("Type of this State does not match."); //$NON-NLS-1$
		throw new StateReadException("Not implemented."); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(20);
		for (int i = this.type.name().length(); i < 6; i++)
			sb.append(' ');
		sb.append('[');
		sb.append(this.type.name());
		sb.append(']');
		sb.append(this.id);
		sb.append(" : "); //$NON-NLS-1$
		try
		{
			switch (this.type)
			{
				case BIT:
					sb.append(this.bitValue());
					break;
				case FLOAT:
					sb.append(this.floatValue());
					break;
				case GENERIC:
					sb.append(Strings.toHexString(this.genericValue()));
					break;
				case INT:
					sb.append(this.intValue());
					break;
				case OBJECT:
					sb.append(String.valueOf(this.objectValue()));
					break;
				case SHORT:
					sb.append(this.shortValue());
					break;
				case STRING:
					sb.append(this.stringValue());
					break;
				case UBYTE:
					sb.append(this.ubyteValue());
					break;
				case UINT:
					sb.append(this.uintValue());
					break;
				case USHORT:
					sb.append(this.ushortValue());
					break;
			}
		}
		catch (StateReadException e)
		{
			sb.append(e.getMessage());
			if (e.getCause() != null)
				sb.append(e.getCause());
		}
		return sb.toString();
	}
}
