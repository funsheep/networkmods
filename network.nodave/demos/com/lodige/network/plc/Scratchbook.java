package com.lodige.network.plc;

public class Scratchbook
{
	static String toBinary( byte... bytes )
	{
	    StringBuilder sb = new StringBuilder(bytes.length * Byte.SIZE);
	    for( int i = 0; i < Byte.SIZE * bytes.length; i++ )
	        sb.append((bytes[i / Byte.SIZE] << i % Byte.SIZE & 0x80) == 0 ? '0' : '1');
	    return sb.toString();
	}
	
	public static void main(String[] args)
	{
		System.out.println(toBinary((byte)0xE0));
		System.out.println(Integer.toBinaryString(0xE0));
		System.out.println(0xE0);
		System.out.println((byte)0xE0);
	}

}
