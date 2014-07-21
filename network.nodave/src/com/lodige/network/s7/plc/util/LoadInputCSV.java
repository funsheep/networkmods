/**
 * network.nodave Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.network.s7.plc.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;

import com.lodige.network.s7.plc.IPLCAPI.Type;
import com.lodige.network.s7.plc.impl.NodavePLC;

/**
 * TODO javadoc
 * @author renken
 */
public class LoadInputCSV
{
	
	private final URL url;
	

	private LoadInputCSV(URL url)
	{
		this.url = url;
	}
	
	
	public void forPLC(NodavePLC plc) throws IOException
	{
		try (InputStream stream = this.url.openStream())
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream, Charset.forName("UTF-8")));
			String line;
			int db = -1;
			int off = -1;
			while ((line = reader.readLine()) != null)
			{
				String[] split = line.split(",");
				if (split.length <= 1)
					continue;
				if (split[0].equals("db"))
				{
					db = Integer.parseInt(split[1]);
					continue;
				}
	
				if (!split[0].equals("+"))
					off = Integer.parseInt(split[0]);
				
				Type type = Type.valueOf(split[1].toUpperCase());
				if (type != Type.GENERIC)
				{
					plc.createDBInput(db, off, type);
					off += type.size;
				}
				else
				{
					int length = Integer.parseInt(split[2]);
					plc.createGenericDBInput(db, off, length);
					off += length;
				}
			}
		}
	}
	
	public static final LoadInputCSV from(URL url)
	{
		return new LoadInputCSV(url);
	}
}
