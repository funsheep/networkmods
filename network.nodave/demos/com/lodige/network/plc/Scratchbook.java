package com.lodige.network.plc;

import com.lodige.network.plc.util.Converter;


public class Scratchbook
{
	
	public static void main(String[] args)
	{
		long[] val = { Long.MIN_VALUE, -10000000, -65001, -65000, -59999, -30, 0, 15, 59999, 65000, 66000, 3000000, Long.MAX_VALUE };
		for (long v : val)
		{
			int i = (int) (v);
			byte[] b = new byte[2];
			Converter.setUSBEWord(b, 0, i);
			System.out.println(v + " \t-> " + i +  "\t -> " + Converter.USBEWord(b, 0));
		}
	}

}
