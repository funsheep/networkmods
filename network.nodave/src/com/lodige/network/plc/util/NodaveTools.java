/**
 * network.nodave Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.network.plc.util;

import github.javaappplatform.commons.collection.SemiDynamicByteArray;

import com.lodige.network.msg.IMessage;
import com.lodige.network.plc.INodaveAPI;
import com.lodige.network.plc.INodaveAPI.Func;
import com.lodige.network.plc.INodaveAPI.Result;

/**
 * TODO javadoc
 * 
 * @author renken
 */
public class NodaveTools
{

	/**
	 * return the number of the PDU
	 */
	public static final int getPDUNumber(IMessage msg, int header)
	{
		byte[] tmp = new byte[2];
		msg.data(tmp, header + 4);
		return Converter.USBEWord(tmp, 0);
	}


	public static final int msgType(SemiDynamicByteArray array, int headerStart)
	{
		final byte one = array.getDateFrom(headerStart + 1);
		if (one == 2 || one == 3)
		{
			final byte[] two = new byte[2];
			array.getDataFrom(two, headerStart + 10);
			if (Converter.USBEWord(two, 0) == Result.OK.code)
			{
				switch (getFunc(array, headerStart))
				{
					case READ:
						return INodaveAPI.MSG_PDU_READ;
					case WRITE:
						return INodaveAPI.MSG_PDU_WRITE;
					default:
						return INodaveAPI.MSG_PDU;
				}
			}
		}
		return INodaveAPI.MSG_OTHER;
	}

	private static final int getHeaderLength(SemiDynamicByteArray array, int header)
	{
		byte[] tmp = new byte[1];
		array.getDataFrom(tmp, header + 1);
		int headerLength = 10;
		if (tmp[0] == 2 || tmp[0] == 3)
			headerLength = 12;
		return headerLength;
	}
	
	private static final Func getFunc(SemiDynamicByteArray array, int protocolHeaderSize)
	{
		return Func.convert(Converter.USByte(new byte[] { array.getDateFrom(protocolHeaderSize + getHeaderLength(array, protocolHeaderSize)) }, 0));
	}


	public static String strerror(Result result)
	{
		return strerror(result.code);
	}
	
	public static String strerror(int code)
	{
		switch (Result.convert(code))
		{
			case OK:
				return "ok"; //$NON-NLS-1$
			case MULTIPLE_BITS_NOT_SUPPORTED:
				return "the CPU does not support reading a bit block of length<>1"; //$NON-NLS-1$
			case ITEM_NOT_AVAILABLE:
				return "the desired item is not available in the PLC"; //$NON-NLS-1$
			case ITEM_NOT_AVAILABLE200:
				return "the desired item is not available in the PLC (200 family)"; //$NON-NLS-1$
			case ADDRESS_OUT_OF_RANGE:
				return "the desired address is beyond limit for this PLC"; //$NON-NLS-1$
			case CPU_RETURNED_NO_DATA:
				return "the PLC returned a packet with no result data"; //$NON-NLS-1$
			case UNKNOWN_ERROR:
				return "the PLC returned the error code "+code+" not understood by this library"; //$NON-NLS-1$ //$NON-NLS-2$
			case EMPTY_RESULT:
				return "this result contains no data"; //$NON-NLS-1$
			case EMPTY_RESULTSET:
				return "cannot work with an undefined result set"; //$NON-NLS-1$
			case CANNOT_EVALUATE_PDU:
				return "cannot evaluate the received PDU"; //$NON-NLS-1$
			case WRITE_DATA_SIZE_MISMATCH:
				return "Write data size error"; //$NON-NLS-1$
			case NO_PERIPHERAL_AT_ADDRESS:
				return "No data from I/O module"; //$NON-NLS-1$
			case UNEXPECTED_FUNC:
				return "Unexpected function code in answer"; //$NON-NLS-1$
			case UNKNOWN_DATA_UNIT_SIZE:
				return "PLC responds wit an unknown data type"; //$NON-NLS-1$
			case SHORT_PACKET:
				return "Short packet from PLC"; //$NON-NLS-1$
			case TIMEOUT:
				return "Timeout when waiting for PLC response"; //$NON-NLS-1$
			case FUNCTION_ALREADY_OCCUPIED:
				return "function already occupied."; //$NON-NLS-1$
			case CURRENTLY_NOT_ALLOWED:
				return "not allowed in current operating status."; //$NON-NLS-1$
			case HARDWARE_FAULT:
				return "hardware fault."; //$NON-NLS-1$
			case ACCESS_NOT_ALLOWED:
				return "object access not allowed."; //$NON-NLS-1$
			case CONTEXT_UNSUPPORTED:
				return "context is not supported."; //$NON-NLS-1$
			case INVALID_ADDRESS:
			case INVALID_ADDRESS2:
				return "invalid address."; //$NON-NLS-1$
			case DATATYPE_UNSUPPORTED:
				return "data type not supported."; //$NON-NLS-1$
			case DATATYPE_INCONSITENT:
				return "data type not consistent."; //$NON-NLS-1$
			case OBJECT_NOT_EXISTENT:
				return "object does not exist."; //$NON-NLS-1$
			case INCORRECT_PDU_SIZE:
				return "incorrect PDU size."; //$NON-NLS-1$
			case BLOCKNAME_SYNTAX_ERROR:
				return "block name syntax error."; //$NON-NLS-1$
			case FUNCTION_PARAMETER_SYNTAX_ERROR:
				return "syntax error function parameter."; //$NON-NLS-1$
			case BLOCKTYPE_SYNTAX_ERROR:
				return "syntax error block type."; //$NON-NLS-1$
			case NO_BLOCK_ON_STORAGE:
				return "no linked block in storage medium."; //$NON-NLS-1$
			case OBJECT_ALREADY_EXISTS:
			case OBJECT_ALREADY_EXISTS2:
				return "object already exists."; //$NON-NLS-1$
			case BLOCK_ALREADY_EXISTS:
				return "block exists in EPROM."; //$NON-NLS-1$
			case BLOCK_NOT_EXISTENT:
				return "block does not exist."; //$NON-NLS-1$
			case BLOCK_ALREADY_EXISTS2:
				return "no block does not exist."; //$NON-NLS-1$
			case BLOCK_NUMBER_TOO_BIG:
				return "block number too big."; //$NON-NLS-1$
			case UNFINISHED_BLOCK_TRANSFER:
				return "unfinished block transfer in progress?"; //$NON-NLS-1$
			case PASSWORD_PROTECTED:
				return "protected by password."; //$NON-NLS-1$
		default:
				return "no message defined for code: " + code + "!"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	private NodaveTools()
	{
		// no instance
	}

}
