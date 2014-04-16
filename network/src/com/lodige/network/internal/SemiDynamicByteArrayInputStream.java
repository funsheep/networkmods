/**
 * network Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.network.internal;

import github.javaappplatform.commons.collection.SemiDynamicByteArray;

import java.io.IOException;
import java.io.InputStream;

/**
 * TODO javadoc
 * @author renken
 */
public class SemiDynamicByteArrayInputStream extends InputStream
{

	private final SemiDynamicByteArray array;
	private final int length;
	private final int offset;


	/**
	 * 
	 */
	public SemiDynamicByteArrayInputStream(SemiDynamicByteArray array, int off, int length)
	{
		this.array = array;
		this.array.cursor(off);
		this.offset = off;
		this.length = length;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read() throws IOException
	{
		if (this.array.cursor() == this.length+this.offset)
			return -1;
		return this.array.readDate();
	}

}
