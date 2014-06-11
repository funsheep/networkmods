/**
 * GGZ Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.plc.states;

import github.javaappplatform.commons.events.IInnerTalker;
import github.javaappplatform.commons.util.GenericsToolkit;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.IOException;
import java.nio.charset.Charset;

import com.lodige.plc.IInput;
import com.lodige.plc.IPLCAPI;
import com.lodige.states.AState;
import com.lodige.states.IHasStates;
import com.lodige.states.IStateAPI;
import com.lodige.states.IStateAPI.IStateMappable;
import com.lodige.states.IStateAPI.Type;
import com.lodige.states.StateReadException;


/**
 * TODO javadoc
 * @author renken
 */
public class PLCState extends AState
{
	
	private static final Charset US_ASCII = Charset.forName("US-ASCII");


	private final IInput input;
	private final int bitnr;
	private final TIntObjectMap<Enum<? extends IStateMappable>> mapping;
	

	/**
	 * 
	 */
	public PLCState(IInput input, boolean string, IHasStates parent)
	{
		this(input.id(), input, map(input.type(), string), -1, null, parent);
	}

	public PLCState(String id, IInput input, int bitnr, IHasStates parent)
	{
		this(id, input, Type.BIT, bitnr, null, parent);
	}
	
	public PLCState(IInput input, Class<? extends Enum<? extends IStateMappable>> clazz, IHasStates parent)
	{
		this(input.id(), input, Type.OBJECT, -1, map(clazz), parent);
	}

	private PLCState(String id, IInput input, Type type, int bitnr, TIntObjectMap<Enum<? extends IStateMappable>> mapping, IHasStates parent)
	{
		super(id, type, parent);
		this.input = input;
		this.bitnr = bitnr;
		this.mapping = mapping;
		this.input.plc().addListener(IPLCAPI.EVENT_INPUT_CHANGED, (e) ->
		{
			if (e.getData() == this.input)
				((IInnerTalker) this.parent).postEvent(IStateAPI.EVENT_STATE_CHANGED, this);
		});
	}
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean bitValue() throws StateReadException
	{
		switch (this.input.type())
		{
			case UBYTE:
				return ((this.ubyteValue() & (1 << this.bitnr))) != 0;
			case SHORT:
				return ((this.shortValue() & (1 << this.bitnr))) != 0;
			case USHORT:
				return ((this.ushortValue() & (1 << this.bitnr))) != 0;
			case INT:
				return ((this.intValue() & (1 << this.bitnr))) != 0;
			case UINT:
				return ((this.uintValue() & (1 << this.bitnr))) != 0;
			default:
				throw new IllegalStateException("Cannot get bit wise information from FLOAT or GENERIC inputs.");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public short shortValue() throws StateReadException
	{
		try
		{
			return this.input.shortValue();
		}
		catch (IOException ex)
		{
			throw new StateReadException(ex);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int intValue() throws StateReadException
	{
		try
		{
			return this.input.intValue();
		}
		catch (IOException ex)
		{
			throw new StateReadException(ex);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public float floatValue() throws StateReadException
	{
		try
		{
			return this.input.floatValue();
		}
		catch (IOException ex)
		{
			throw new StateReadException(ex);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public short ubyteValue() throws StateReadException
	{
		try
		{
			return this.input.ubyteValue();
		}
		catch (IOException ex)
		{
			throw new StateReadException(ex);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int ushortValue() throws StateReadException
	{
		try
		{
			return this.input.ushortValue();
		}
		catch (IOException ex)
		{
			throw new StateReadException(ex);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long uintValue() throws StateReadException
	{
		try
		{
			return this.input.uintValue();
		}
		catch (IOException ex)
		{
			throw new StateReadException(ex);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String stringValue() throws StateReadException
	{
		return new String(this.genericValue(), US_ASCII);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte[] genericValue() throws StateReadException
	{
		try
		{
			return this.input.genericValue();
		}
		catch (IOException ex)
		{
			throw new StateReadException(ex);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public <O>O objectValue() throws StateReadException
	{
		int code;
		switch (this.type)
		{
			case INT:
				code = this.intValue();
				break;
			case SHORT:
				code = this.shortValue();
				break;
			case UBYTE:
				code = this.ubyteValue();
				break;
			case USHORT:
				code = this.ushortValue();
				break;
			default:
				throw new IllegalStateException("Enum Mappings for States of Type " + this.type + " not Supported.");
		}
		return GenericsToolkit.convertUnchecked(this.mapping.get(code));
	}
	
	private static final Type map(com.lodige.plc.IPLCAPI.Type inputType, boolean string)
	{
		switch (inputType)
		{
			case FLOAT:
				return Type.FLOAT;
			case GENERIC:
				return !string ? Type.GENERIC : Type.STRING;
			case INT:
				return Type.INT;
			case SHORT:
				return Type.SHORT;
			case UBYTE:
				return Type.UBYTE;
			case UINT:
				return Type.UINT;
			case USHORT:
				return Type.USHORT;
			default:
				throw new IllegalStateException();
		}
	}
	
	private static final TIntObjectMap<Enum<? extends IStateMappable>> map(Class<? extends Enum<? extends IStateMappable>> clazz)
	{
		TIntObjectMap<Enum<? extends IStateMappable>> map = new TIntObjectHashMap<>();
		for (Enum<? extends IStateMappable> e : clazz.getEnumConstants())
		{
			map.put(((IStateMappable) e).code(), e);
		}
		return map;
	}

}