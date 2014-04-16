package com.lodige.network.plc;

import java.io.InputStream;
import java.net.Socket;

import github.javaappplatform.commons.log.Logger;
import github.javaappplatform.commons.util.Close;
import github.javaappplatform.commons.util.Strings;

import com.lodige.network.client.ClientConnection;
import com.lodige.network.client.ClientNetworkService;
import com.lodige.network.internal.InternalNetTools;

public class ConnectDemoTest
{

	public static void main(String[] args) throws Exception
	{
		Logger.configureDefault();
		ClientNetworkService service = new ClientNetworkService("PLC", TCPProtocol.class);
		ClientConnection con = new ClientConnection("192.168.130.110", 102, null, service);
		con.connect();
		Thread.sleep(5 * 10000);
		Close.close(con);
		
	}

}
