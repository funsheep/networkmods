/**
 * icplatform Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.faults.hr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * TODO javadoc
 * @author renken
 */
public class LoadHRFaultDB
{

	private final HRFaultDB knownFaults = new HRFaultDB();


	public LoadHRFaultDB andDefineFault(String id, String deviceID, String description)
	{
		this.knownFaults.defineFault(id, deviceID, description);
		return this;
	}

	public LoadHRFaultDB andDefineFault(int db, int offset, int bit, String deviceID, String description)
	{
		final String id = String.valueOf(db) + '.' + offset + '.' + bit;
		this.knownFaults.defineFault(id, deviceID, description);
		return this;
	}
	
	public HRFaultDB andCompileDB()
	{
		return this.knownFaults;
	}
	
	
	public static final LoadHRFaultDB fromStream(InputStream stream) throws IOException
	{
		final LoadHRFaultDB faults = new LoadHRFaultDB();
		final BufferedReader reader = new BufferedReader(new InputStreamReader(stream, Charset.forName("UTF-8"))); //$NON-NLS-1$
		String line = reader.readLine();

		int area_db = -1;
		int area_off = -1;
		int area_bitOff = -1;
		while (line != null)
		{
			final String[] split = line.trim().split(","); //$NON-NLS-1$
			if (split.length > 0)
			{
				switch (split[0])
				{
					case "fault": //$NON-NLS-1$
						area_db = -1; area_off = -1; area_bitOff = -1;
						int db = Integer.parseInt(split[1]);
						int offset = Integer.parseInt(split[2]);
						int bit = Integer.parseInt(split[3]);
						faults.andDefineFault(db, offset, bit, split[4], split[5]);
						break;
					case "area": //$NON-NLS-1$
						area_db = -1; area_off = -1; area_bitOff = -1;
						area_db = Integer.parseInt(split[1]);
						area_off = Integer.parseInt(split[2]);
						area_bitOff = Integer.parseInt(split[3]);
						break;
					default:
						if (area_db == -1)
							throw new IllegalStateException();
						faults.andDefineFault(area_db, area_off, area_bitOff++, split[0], split[1]);
						break;
				}
			}
			line = reader.readLine();
		}
		return faults;
	}

	private LoadHRFaultDB()
	{
		//no external instance
	}
}
