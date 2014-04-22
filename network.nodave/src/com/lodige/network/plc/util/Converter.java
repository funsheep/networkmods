package com.lodige.network.plc.util;

import github.javaappplatform.commons.collection.SemiDynamicByteArray;
import github.javaappplatform.commons.log.Logger;

public class Converter
{

	private static final Logger LOGGER = Logger.getLogger();

	public static byte[] toPLCfloat(float f)
	{
		int i = Float.floatToIntBits(f);
		LOGGER.debug("i: {}", Integer.valueOf(i));
		return bswap_32(i);
	}

	public static byte[] bswap_32(int a)
	{
		byte[] b = new byte[4];
		b[3] = (byte)(a & 0xff);
		a = a >> 8;
		b[2] = (byte)(a & 0xff);
		a = a >> 8;
		b[1] = (byte)(a & 0xff);
		a = a >> 8;
		b[0] = (byte)(a & 0xff);
		LOGGER.debug("In: {} out: {}", Integer.valueOf(a), b);
		return b;
	}

	public static byte[] bswap_16(short value)
	{
		int a = value;
		byte[] b = new byte[2];
		b[1] = (byte)(a & 0xff);
		a = a >> 8;
		b[0] = (byte)(a & 0xff);
		LOGGER.debug("In: {} out: {}", Integer.valueOf(a), b);
		return b;
	}

	public static long USBELong(byte[] b, int pos)
	{
		int i = b[pos];
		int j = b[pos + 1];
		int k = b[pos + 2];
		int l = b[pos + 3];
		// System.out.println(
		// pos + "  0:" + i + " 1:" + j + " 2:" + k + " 3:" + l);
		if (i < 0)
			i += 256;
		if (j < 0)
			j += 256;
		if (k < 0)
			k += 256;
		if (l < 0)
			l += 256;
		return (256 * k + l) + 65536L * (256 * i + j);
	}

	public static long SBELong(byte[] b, int pos)
	{
		int i = b[pos];
		int j = b[pos + 1];
		int k = b[pos + 2];
		int l = b[pos + 3];
		// if (i < 0)
		// i += 256;
		if (j < 0)
			j += 256;
		if (k < 0)
			k += 256;
		if (l < 0)
			l += 256;
		return (256 * k + l) + 65536L * (256 * i + j);
	}

	public static int USBEWord(byte[] b, int pos)
	{
		int i = b[pos];
		int k = b[pos + 1];
		if (i < 0)
			i += 256;
		if (k < 0)
			k += 256;
		return (256 * i + k);
	}

	public static int USBEWord(SemiDynamicByteArray b, int pos)
	{
		b.cursor(pos);
		int i = b.readDate();
		int k = b.readDate();
		if (i < 0)
			i += 256;
		if (k < 0)
			k += 256;
		return (256 * i + k);
	}

	public static void setUSBEWord(byte[] b, int pos, int val)
	{
		b[pos] = ((byte)(val / 0x100));
		b[pos + 1] = ((byte)(val % 0x100));
	}

	public static void setUSBEWord(SemiDynamicByteArray b, int pos, int val)
	{
		b.cursor(pos);
		b.put((byte)(val / 0x100));
		b.put((byte)(val % 0x100));
	}

	public static void setUSByte(byte[] b, int pos, int val)
	{
		b[pos] = ((byte)(val & 0xff));
	}

	public static void setUSBELong(byte[] b, int pos, long a)
	{
		b[pos + 3] = (byte)(a & 0xff);
		a = a >> 8;
		b[pos + 2] = (byte)(a & 0xff);
		a = a >> 8;
		b[pos + 1] = (byte)(a & 0xff);
		a = a >> 8;
		b[pos] = (byte)(a & 0xff);
	}

	public static void setBEFloat(byte[] b, int pos, float f)
	{
		int a = Float.floatToIntBits(f);
		b[pos + 3] = (byte)(a & 0xff);
		a = a >> 8;
		b[pos + 2] = (byte)(a & 0xff);
		a = a >> 8;
		b[pos + 1] = (byte)(a & 0xff);
		a = a >> 8;
		b[pos] = (byte)(a & 0xff);
	}

	public static int SBEWord(byte[] b, int pos)
	{
		int i = b[pos];
		int k = b[pos + 1];
		// if (i < 0)
		// i += 256;
		if (k < 0)
			k += 256;
		return (256 * i + k);
	}

	public static int USByte(byte[] b, int pos)
	{
		int i = b[pos];
		if (i < 0)
			i += 256;
		return (i);
	}

	public static int USByte(SemiDynamicByteArray b, int pos)
	{
		b.cursor(pos);
		int i = b.getDate();
		if (i < 0)
			i += 256;
		return (i);
	}

	public static int SByte(byte[] b, int pos)
	{
		int i = b[pos];
		return (i);
	}

	public static float BEFloat(byte[] b, int pos)
	{
		int i = 0;
		i |= USByte(b, pos);
		i <<= 8;
		i |= USByte(b, pos + 1);
		i <<= 8;
		i |= USByte(b, pos + 2);
		i <<= 8;
		i |= USByte(b, pos + 3);
		float f = Float.intBitsToFloat(i);
		LOGGER.debug("Converted: {}", Float.valueOf(f));
		return f;
	}

	private Converter()
	{
		// no instance
	}

}
