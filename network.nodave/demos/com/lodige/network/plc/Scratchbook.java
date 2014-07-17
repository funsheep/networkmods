package com.lodige.network.plc;

import com.lodige.network.s7.util.Converter;


public class Scratchbook
{
	
	public static void main(String[] args)
	{
		long[] val = { 0, 15, 59999, 65000, 66000, 3000000, Long.MAX_VALUE };
		for (long v : val)
		{
			int i = (int) (v);
			long s = v & 0xFFFF;
			byte[] b = new byte[2];
			Converter.setUSBEWord(b, 0, i);
			System.out.println(v + " \t-> " + i +  "\t -> " + Converter.USBEWord(b, 0) + " : " + s);
		}
	}

	private static final byte[] a = new byte[2];
	private static final int convertToProtocolID(long sendID)
	{
		int i = (int) sendID;
		Converter.setUSBEWord(a, 0, i);
		return Converter.USBEWord(a, 0);
	}

}
