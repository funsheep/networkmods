package com.lodige.network.plc;

import github.javaappplatform.commons.log.Logger;
import github.javaappplatform.commons.util.Close;

import com.lodige.network.client.ClientConnection;
import com.lodige.network.client.ClientNetworkService;

public class ConnectDemoTest
{

	public static void main(String[] args) throws Exception
	{
		Logger.configureDefault();
		ClientNetworkService service = new ClientNetworkService("PLC", TCPProtocol.class);
		ClientConnection con = new ClientConnection("192.168.130.110", 102, null, service);
		con.connect();
		Thread.sleep(5 * 1000);
		Close.close(con);
	}

}
