/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mox.protocol;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;


enum VariableCode {
	ADDRESS(0x1),
	SUBOID(0x11),
	CMD_SET_CODE(0x204),
	CMD_GET_CODE(0x102);
	
	private int code;
	
	private VariableCode(int code) {
		this.code = code;
	}
	
	public int getCode() {
		return code;
	}
	
}

enum BroadcastCode {
	ADDRESS(0x0, -1),
	SUBOID(0x2, -1),
	CMD_CODE(0x1, 0x406);
			
	private int low;
	private int high;
	
	BroadcastCode(int low, int high) {
		this.low = low;
		this.high = high;
	}
	
	public int getLow() {
		return low;
	}
	
	public int getHigh() {
		return high;
	}
	
}


/**
 * @author Thomas Eichstaedt-Engelen (innoQ)
 * @since 0.8.0
 */
public class MoxMessage {
	
	private static final int MAX_SCALE = 8;
	private byte[] rawdata;
	private String hexString;
	
	/** it is always 0x2 in our application */
	private int priority;
	/** this is a unique number assigned to a LTCOP device in the home system */
	private int oid;
	/** this is the channel information of a LT COP device */
	private int suboid;
	
	private int subFunction;
	private int zeroPadding;
	private int functionCode;
	
	private String id;
	private MoxCommandCode commandCode;
	private BigDecimal value;
	private String unit;
	
	private int dimmerTime;
	private String eventName;

	private static Map<Integer, String> subOids = new HashMap<Integer, String>();

	static {
		subOids.put(17, "Channel 1"); // 0x11
		subOids.put(18, "Channel 2"); // 0x12
		subOids.put(19, "Channel 3"); // 0x13
		subOids.put(20, "Channel 4"); // 0x14
		subOids.put(21, "Channel 5"); // 0,15
		subOids.put(22, "Channel 6"); // 0x16
		subOids.put(23, "Channel 7"); // 0x17
		subOids.put(24, "Channel 8"); // 0x18
		subOids.put(49, "REM Data");  // 0x31
	}
	

	public MoxMessage(byte[] rawdata) {
		this.rawdata = rawdata;
		parseRawdata(rawdata);
	}

	public BigDecimal getValue() {
		return value;
	}
	
	public MoxCommandCode getCommandCode() {
		return commandCode;
	}
	
	
	private void parseRawdata(byte[] rawdata) {

		hexString = new String(Hex.encodeHex(rawdata));
		
		priority = readBytes(rawdata, 0, 1, false);
		oid = readBytes(rawdata, 1, 3, false);
		suboid = readBytes(rawdata, 4, 1, false);
		subFunction = readBytes(rawdata, 5, 1, false);
		zeroPadding = readBytes(rawdata, 6, 2, false);
		functionCode = readBytes(rawdata, 8, 2, false);
		
		id = oid + ":" + suboid;
		
		try {
			MoxCommandCode code = MoxCommandCode.valueOf(subFunction, functionCode);
			switch(code) {
				case POWER_ACTIVE:
				case POWER_REACTIVE:
				case POWER_APPARENT:
					commandCode = code;
					value = new BigDecimal(readBytes(rawdata, 10, 4, true) / 1000.0);
					unit = "W";
					break;
		
				case POWER_FACTOR:
				case POWER_ACTIVE_ENERGY:
					commandCode = code;
					value = new BigDecimal(readBytes(rawdata, 10, 4, true) / 1000.0);
					break;
		
				case LUMINOUS_GET:
					commandCode = code;
					value = new BigDecimal(readBytes(rawdata, 10, 1, false));
					break;
		
				case LUMINOUS_SET:
					dimmerTime = readBytes(rawdata, 12, 2, true);
				case INCREASE:
				case DECREASE:
				case STATUS:
				case ONOFF:
					eventName = code.name();
					value = new BigDecimal(readBytes(rawdata, 10, 1, false));
					break;
			}
			
			if (value != null && value instanceof BigDecimal) {
				value.setScale(MAX_SCALE, RoundingMode.HALF_UP);
			}

			if (commandCode != null) {
				id += ':' + commandCode.name();
			}
		} catch (IllegalArgumentException iae) {
			//
		}
	}
	
	private static int readBytes(byte[] rawdata, int offset, int length, boolean le) {
	    int value = 0;
	    for (int i = 0; i < length; i++) {
	        int shift = le ? i * 8 : (length - 1 - i) * 8;
	        value += (rawdata[i + offset] & 0x000000FF) << shift;
	    }
	    return value;
	}
	
	
	@Override
	public String toString() {
		return "MoxMessage [rawdata=" + Arrays.toString(rawdata)
				+ ", hexString=" + hexString + ", priority=" + priority
				+ ", oid=" + oid + ", suboid=" + suboid + ", subFunction="
				+ subFunction + ", zeroPadding=" + zeroPadding
				+ ", functionCode=" + functionCode + ", id=" + id
				+ ", commandCode=" + commandCode + ", value=" + value
				+ ", unit=" + unit + ", dimmerTime=" + dimmerTime
				+ ", eventName=" + eventName + "]";
	}
	
}
