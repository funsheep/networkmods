package com.lodige.faults;

import github.javaappplatform.commons.collection.IObservableSet;


public interface IHasFaults<F extends Fault>
{

	public IObservableSet<F> activeFaults();

}