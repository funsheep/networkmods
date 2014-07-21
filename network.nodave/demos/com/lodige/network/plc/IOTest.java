/**
 * network.nodave Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.network.plc;

import github.javaappplatform.platform.PlatformException;
import github.javaappplatform.platform.boot.IBootEntry;
import github.javaappplatform.platform.extension.Extension;
import github.javaappplatform.platform.job.ADoJob;
import github.javaappplatform.platform.job.JobPlatform;

import java.io.IOException;

import com.lodige.network.client.ClientNetworkService;
import com.lodige.network.s7.plc.IPLCAPI.Type;
import com.lodige.network.s7.plc.IPLCAPI.UpdateFrequency;
import com.lodige.network.s7.plc.impl.NodavePLC;
import com.lodige.network.s7.plc.util.PLCTools;
import com.lodige.network.s7.protocol.INodaveAPI.Area;
import com.lodige.network.s7.protocol.impl.TCPS7Protocol;

/**
 * TODO javadoc
 * @author renken
 */
public class IOTest implements IBootEntry
{

	@Override
	public void startup(Extension e) throws PlatformException
	{
		ClientNetworkService service = new ClientNetworkService("NodavePLC", TCPS7Protocol.class.getName()); //$NON-NLS-1$
		try
		{
			final NodavePLC plc = new NodavePLC("192.168.130.110", service); //$NON-NLS-1$
			plc.createDBInput(15, 20, Type.UINT).setUpdateMethod(UpdateFrequency.OFF); //$NON-NLS-1$
			plc.createDBInput(15, 10, Type.UBYTE).setUpdateMethod(UpdateFrequency.LOW); //$NON-NLS-1$
			(new ADoJob("Manually Poll PLCs") //$NON-NLS-1$
			{
				
				@Override
				public void doJob()
				{
					try
					{
						System.err.println(plc.getInput(PLCTools.uID(Area.DB, 15, 20)).ubyteValue()); //$NON-NLS-1$
						System.err.println(plc.getInput(PLCTools.uID(Area.DB, 15, 10)).uintValue()); //$NON-NLS-1$
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}).schedule(JobPlatform.MAIN_THREAD, true, 1500);
		}
		catch (IOException e1)
		{
			throw new PlatformException(e1);
		}
	}

	@Override
	public void shutdown() throws PlatformException
	{
		// TODO Auto-generated method stub
		
	}



}
