/**
 * icplatform Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.logging;

import github.javaappplatform.commons.collection.IObservableSet;
import github.javaappplatform.commons.events.TalkerStub;
import github.javaappplatform.platform.extension.Extension;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * TODO javadoc
 * @author renken
 */
public class ObservableFixedSizeOrderedSet<E> extends TalkerStub implements IObservableSet<E>
{

	private final LinkedList<E> list;
	private final int maxSize;


	public ObservableFixedSizeOrderedSet(Extension ext)
	{
		this(ext.getProperty("maxsize", 10));
	}
	
	/**
	 * 
	 */
	public ObservableFixedSizeOrderedSet(int maxSize)
	{
		this.maxSize = maxSize;
		this.list = new LinkedList<>();
	}

	
	public synchronized boolean put(E e)
	{
		if (this.contains(e))
			return false;
		this.list.addFirst(e);
		if (this.size() > this.maxSize)
		{
			E old = this.list.removeLast();
			this.postEvent(EVENT_NEW_ELEMENT, old);
		}
		this.postEvent(EVENT_NEW_ELEMENT, e);
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<E> iterator()
	{
		return Collections.unmodifiableList(this.list).iterator();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized int size()
	{
		return this.list.size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized boolean isEmpty()
	{
		return this.list.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized boolean contains(Object object)
	{
		return this.list.contains(object);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized Object[] toArray()
	{
		return this.list.toArray();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized <T>T[] toArray(T[] array)
	{
		return this.list.toArray(array);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized boolean containsAll(Collection< ? > c)
	{
		return this.list.containsAll(c);
	}

}
