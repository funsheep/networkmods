package com.lodige.network.plc;

import github.javaappplatform.commons.log.Logger;
import github.javaappplatform.commons.util.Close;
import github.javaappplatform.commons.util.FastMersenneTwister;

import com.lodige.network.client.ClientConnection;
import com.lodige.network.client.ClientNetworkService;
import com.lodige.network.plc.INodaveAPI.Area;
import com.lodige.network.plc.msg.PDUReadResult;
import com.lodige.network.plc.msg.PDUWriteResult;
import com.lodige.network.plc.msg.Variable;
import com.lodige.network.plc.protocol.TCPProtocol;
import com.lodige.network.plc.util.Converter;

public class ConnectDemoTest
{

	public static void main(String[] args) throws Exception
	{
		Logger.configureDefault();
		ClientNetworkService service = new ClientNetworkService("NodavePLC", new TCPProtocol());
		ClientConnection con = new ClientConnection("192.168.130.110", 102, null, service);
		con.connect();

		PDUReadResult rr = Read.fromPLC(con).bytes(2).from(Area.DB).andDatabase(1).andWaitForResult();

		System.out.println(rr);
		Variable[] results = rr.getResults();

		for (Variable r : results)
		{
			System.out.println(r);
			System.out.println("DB1:DW0: " + Converter.USBEWord(r.data(), 0));
		}
		
//		System.out.println("DB1:DW1: " + Nodave.USBEWord(udata, 2));
//		System.out.println("DB1:DW32: " + Nodave.USBEWord(udata, 62));
		
//		System.out.println("Trying to read 64 bytes (32 words) from data block 1.");
//
//		dc.readBytes(Nodave.DB, 1, 0, 64, null);
//		a = dc.getWORD();
//		System.out.println("DB1:DW0:" + a);
//		a = dc.getWORD();
//		System.out.println("DB1:DW1: " + a);
//		a = dc.getWORD(62);
//		System.out.println("DB1:DW32: " + a);
//
		System.out.println("Trying to read 16 bytes from FW0.\n");

		rr = Read.fromPLC(con).bytes(16).from(Area.FLAGS).andDatabase(0).andWaitForResult();
//		System.out.println(ByteBuffer.allocate(16).put(rr.resultData()).getFloat(12));
		System.out.println(Converter.BEFloat(rr.resultData(), 12));
		
		byte b = (byte) (new FastMersenneTwister()).nextInt(255);
		PDUWriteResult result = Write.toPLC(con).data(b, b).to(Area.P).andDatabase(0).startAt(4).andWaitForResult();
		result.checkWriteResult();
		
//		dc.readBytes(Nodave.FLAGS, 0, 0, 16, null);
//		a = dc.getU32();
//		b = dc.getU32();
//		c = dc.getU32();
//		d = dc.getFloat();
//		System.out.println("3 DWORDS " + a + " " + b + " " + c);
//		System.out.println("1 Float: " + d);
//		if (doWrite) {
//			System.out.println(
//				"Now we write back these data after incrementing the first 3 by 1,2,3 and the float by 1.1.\n");
//			waitKey();
//			by = Nodave.bswap_32(a + 1);
//			dc.writeBytes(Nodave.FLAGS, 0, 0, 4, by);
//			by = Nodave.bswap_32(b + 1);
//			dc.writeBytes(Nodave.FLAGS, 0, 4, 4, by);
//			by = Nodave.bswap_32(c + 1);
//			dc.writeBytes(Nodave.FLAGS, 0, 8, 4, by);
//			by = Nodave.toPLCfloat(d + 1.1);
//			dc.writeBytes(Nodave.FLAGS, 0, 12, 4, by);
//			dc.readBytes(Nodave.FLAGS, 0, 0, 16, null);
//			a = dc.getU32();
//			b = dc.getU32();
//			c = dc.getU32();
//			d = dc.getFloat();
//			System.out.println("FD0: " + a);
//			System.out.println("FD4:" + b);
//			System.out.println("FD8:" + c);
//			System.out.println("FD12: " + d);
//			//					wait();
//		} // doWrite
//		long t1, t2;
//		if (doBenchmark) {
//			System.out.println(
//				"Now going to do read benchmark with minimum block length of 1.\n");
//			waitKey();
//			t1 = System.currentTimeMillis();
//			for (i = 0; i < 100; i++)
//				dc.readBytes(Nodave.FLAGS, 0, 0, 1, null);
//			t2 = System.currentTimeMillis();
//			double usec = (t2 - t1) * 0.1;
//
//			System.out.println("100 reads took " + usec + "secs.");
//
//			System.out.println(
//				"Now going to do read benchmark with shurely supported block length 100.\n");
//			waitKey();
//			t1 = System.currentTimeMillis();
//			for (i = 0; i < 100; i++)
//				dc.readBytes(Nodave.FLAGS, 0, 0, 100, null);
//			t2 = System.currentTimeMillis();
//			usec = (t2 - t1) * 0.1;
//
//			System.out.println("100 reads took " + usec + "secs.");
//			waitKey();
//			if (doWrite) {
//				System.out.println(
//					"Now going to do write benchmark with minimum block length of 1.\n");
//				waitKey();
//				t1 = System.currentTimeMillis();
//				by = Nodave.bswap_32(123);
//				for (i = 0; i < 100; i++)
//					dc.writeBytes(Nodave.FLAGS, 0, 0, 1, by);
//				t2 = System.currentTimeMillis();
//				usec = (t2 - t1) * 0.1;
//				System.out.println(
//					"100 writes took " + usec + "secs.");
//
//				System.out.println(
//					"Now going to do write benchmark with shurely supported block length 100.\n");
//				waitKey();
//				t1 = System.currentTimeMillis();
//				for (i = 0; i < 100; i++)
//					dc.writeBytes(Nodave.FLAGS, 0, 0, 100, null);
//				t2 = System.currentTimeMillis();
//				usec = (t2 - t1) * 0.1;
//				System.out.println(
//					"100 writes took " + usec + "secs.");
//
//				waitKey();
//			} // doWrite
//		} // doBenchmark
//		//		 wait();
//		
//		System.out.println("Now disconnecting\n");
		Close.close(con);
	}

}
