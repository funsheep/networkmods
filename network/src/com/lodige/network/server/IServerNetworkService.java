package com.lodige.network.server;

import java.io.IOException;

import com.lodige.network.INetworkService;

public interface IServerNetworkService extends INetworkService
{

	public void start() throws IOException;

}