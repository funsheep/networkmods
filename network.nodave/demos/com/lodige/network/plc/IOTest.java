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
import com.lodige.network.plc.protocol.TCPProtocol;
import com.lodige.plc.IPLCAPI.Type;
import com.lodige.plc.IPLCAPI.UpdateFrequency;
import com.lodige.plc.nodave.NodavePLC;

/**
 * TODO javadoc
 * @author renken
 */
public class IOTest implements IBootEntry
{

	@Override
	public void startup(Extension e) throws PlatformException
	{
		ClientNetworkService service = new ClientNetworkService("NodavePLC", new TCPProtocol());
		try
		{
			final NodavePLC plc = new NodavePLC("192.168.130.110", service);
			plc.createDBInput("Drive Motor", 15, 20, Type.UINT).setUpdateMethod(UpdateFrequency.OFF);
			plc.createDBInput("Operating Mode", 15, 10, Type.UBYTE).setUpdateMethod(UpdateFrequency.LOW);
			(new ADoJob("Manually Poll PLCs")
			{
				
				@Override
				public void doJob()
				{
					try
					{
						System.err.println(plc.getInput("Operating Mode").ubyteValue());
						System.err.println(plc.getInput("Drive Motor").uintValue());
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
