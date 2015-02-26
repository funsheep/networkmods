/*
	This file is part of the d3fact network library.
	Copyright (C) 2007-2012 d3fact Project Team

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package com.lodige.network.msg;

import java.io.InputStream;

/**
 * TODO javadoc
 * @author funsheep
 */
public interface IMessage
{
	public int type();
	
	//FIXME move this into a subclass of message. e.g. InMessage or something - protocol may create those then
	public long sendID();


	public void data(byte[] dest, int off, int len);
	
	public InputStream data();

	public int size();

}
