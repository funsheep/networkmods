package com.lodige.states;

import com.lodige.states.IStateAPI.Type;


public interface IState
{

	public String id();

	public Type type();

	public IHasStates parent();


	public boolean bitValue() throws StateReadException;

	public short shortValue() throws StateReadException;

	public int intValue() throws StateReadException;

	public float floatValue() throws StateReadException;

	public short ubyteValue() throws StateReadException;

	public int ushortValue() throws StateReadException;

	public long uintValue() throws StateReadException;

	public String stringValue() throws StateReadException;

	public byte[] genericValue() throws StateReadException;
	
	public <O> O objectValue() throws StateReadException;

}