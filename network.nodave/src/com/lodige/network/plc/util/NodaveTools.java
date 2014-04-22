/**
 * network.nodave Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.network.plc.util;

import com.lodige.network.plc.INodave;

/**
 * TODO javadoc
 * 
 * @author renken
 */
public class NodaveTools
{

	public static String areaName(int area)
	{
		switch (area)
		{
			case INodave.DB:
				return "DB";
			case INodave.INPUTS:
				return "E";
			case INodave.OUTPUTS:
				return "A";
			case INodave.FLAGS:
				return "M";
			default:
				return "unknown area!";
		}
	}

	public static String strerror(int code)
	{
		switch (code)
		{
			case INodave.RESULT_OK:
				return "ok";
			case INodave.RESULT_MULTIPLE_BITS_NOT_SUPPORTED:
				return "the CPU does not support reading a bit block of length<>1";
			case INodave.RESULT_ITEM_NOT_AVAILABLE:
				return "the desired item is not available in the PLC";
			case INodave.RESULT_ITEM_NOT_AVAILABLE200:
				return "the desired item is not available in the PLC (200 family)";
			case INodave.RESULT_ADDRESS_OUT_OF_RANGE:
				return "the desired address is beyond limit for this PLC";
			case INodave.RESULT_CPU_RETURNED_NO_DATA:
				return "the PLC returned a packet with no result data";
			case INodave.RESULT_UNKNOWN_ERROR:
				return "the PLC returned an error code not understood by this library";
			case INodave.RESULT_EMPTY_RESULT_ERROR:
				return "this result contains no data";
			case INodave.RESULT_EMPTY_RESULT_SET_ERROR:
				return "cannot work with an undefined result set";
			case INodave.RESULT_CANNOT_EVALUATE_PDU:
				return "cannot evaluate the received PDU";
			case INodave.RESULT_WRITE_DATA_SIZE_MISMATCH:
				return "Write data size error";
			case INodave.RESULT_NO_PERIPHERAL_AT_ADDRESS:
				return "No data from I/O module";
			case INodave.RESULT_UNEXPECTED_FUNC:
				return "Unexpected function code in answer";
			case INodave.RESULT_UNKNOWN_DATA_UNIT_SIZE:
				return "PLC responds wit an unknown data type";
			case INodave.RESULT_SHORT_PACKET:
				return "Short packet from PLC";
			case INodave.RESULT_TIMEOUT:
				return "Timeout when waiting for PLC response";
			case 0x8000:
				return "function already occupied.";
			case 0x8001:
				return "not allowed in current operating status.";
			case 0x8101:
				return "hardware fault.";
			case 0x8103:
				return "object access not allowed.";
			case 0x8104:
				return "context is not supported.";
			case 0x8105:
				return "invalid address.";
			case 0x8106:
				return "data type not supported.";
			case 0x8107:
				return "data type not consistent.";
			case 0x810A:
				return "object does not exist.";
			case 0x8500:
				return "incorrect PDU size.";
			case 0x8702:
				return "address invalid.";
			case 0xd201:
				return "block name syntax error.";
			case 0xd202:
				return "syntax error function parameter.";
			case 0xd203:
				return "syntax error block type.";
			case 0xd204:
				return "no linked block in storage medium.";
			case 0xd205:
				return "object already exists.";
			case 0xd206:
				return "object already exists.";
			case 0xd207:
				return "block exists in EPROM.";
			case 0xd209:
				return "block does not exist.";
			case 0xd20e:
				return "no block does not exist.";
			case 0xd210:
				return "block number too big.";
			case 0xd240:
				return "unfinished block transfer in progress?";
			case 0xd241:
				return "protected by password.";
			default:
				return "no message defined for code: " + code + "!";
		}
	}

	private NodaveTools()
	{
		// no instance
	}

}
