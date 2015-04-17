/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mox.protocol;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;


/**
 * @author Thomas Eichstaedt-Engelen (innoQ)
 * @since 2.0.0
 */
public class MoxMessageBuilder {

    private Logger logger = LoggerFactory.getLogger(MoxMessageBuilder.class);

	private static MoxMessageBuilder instance;
	private MoxMessage message; 
	
	
	private MoxMessageBuilder(MoxMessage message) {
		this.message = message;
	}
	
	public synchronized static MoxMessageBuilder messageBuilder(MoxMessage message) {
		if (instance == null) {
			instance = new MoxMessageBuilder(message);
		}
		return instance;
	}
	
	public MoxMessageBuilder withOid(int oid) {
		message.setOid(oid);
		return this;
	}
	
	public MoxMessageBuilder withSuboid(int suboid) {
		message.setSuboid(suboid);
		return this;
	}

	public MoxMessageBuilder withPriority(int priority) {
		message.setPriority(priority);
		return this;
	}

	public MoxMessageBuilder withCommandCode(MoxCommandCode commandCode) {
		message.setCommandCode(commandCode);
		return this;
	}
	
	public MoxMessageBuilder withStatusCode(MoxStatusCode code) {
		message.setStatusCode(code);
		return this;
	}

	public MoxMessageBuilder withValue(BigDecimal value) {
		message.setValue(value);
		return this;
	}
	
	
	public MoxMessageBuilder parseFrom(byte[] rawdata) {
		message.setHexString(new String(Hex.encodeHex(rawdata)));
		
		message.setPriority(readBytes(rawdata, 0, 1, false));
		message.setOid(readBytes(rawdata, 1, 3, false));
		message.setSuboid(readBytes(rawdata, 4, 1, false));
		message.setSubFunctionCode(readBytes(rawdata, 5, 1, false));
		message.setFunctionCode(readBytes(rawdata, 8, 2, false));
		
		int subFnCode = message.getSubFunctionCode();
		int fnCode = message.getFunctionCode();
		
		MoxCommandCode commandCode = MoxCommandCode.valueOf(subFnCode, fnCode);
		
		if (logger.isWarnEnabled() && commandCode!=null && MoxStatusCode.valueOf(subFnCode, fnCode) != null) {
			logger.warn("There seem to be a package combination which match on command and status codes. "
					+ "By default this is interpreted as commandCode. subFnCode={} fnCode={}", subFnCode, fnCode);
		}
		
		if (commandCode != null) {
			setCommandCodeValues(message, commandCode, rawdata);
		} else {
			MoxStatusCode statusCode = MoxStatusCode.valueOf(subFnCode, fnCode);	
			setStatusCodeValues(message, statusCode, rawdata);
			if (statusCode == null) {
				final String msg = "There is no CommandCode or StatusCode for low=" + subFnCode + ", high=" + fnCode;
				logger.error(msg);
				throw new IllegalArgumentException(msg);
			}
		} 
	
		return this;
	}
	
	private void setStatusCodeValues(MoxMessage message,
			MoxStatusCode code, byte[] rawdata) {
		message.setEventName(code.name());
		message.setStatusCode(code);
		switch(code) {
			// kWh values
			case POWER_ACTIVE:
			case POWER_REACTIVE:
			case POWER_APPARENT:
				message.setValue(new BigDecimal(readBytes(rawdata, 10, 4, true) / 1000.0));
				message.setValue(new BigDecimal(readBytes(rawdata, 10, 4, true) / 1000.0));
				break;
				
			// Watts and cos phi
			case POWER_ACTIVE_ENERGY:
			case POWER_FACTOR:
				message.setValue(new BigDecimal(readBytes(rawdata, 10, 4, true) / 1000.0));
				break;
			
			// Percent and dim time
			case LUMINOUS:
				message.setDimmerTime(readBytes(rawdata, 12, 2, true));
			case ONOFF:
				message.setValue(new BigDecimal(readBytes(rawdata, 10, 1, false)));
				break;
				
			default:
				throw new IllegalArgumentException("Unhandled status value " + code.name());
		}
	}

	private void setCommandCodeValues(MoxMessage message,
			MoxCommandCode code, byte[] rawdata) {
		
		message.setEventName(code.name());
		message.setCommandCode(code);
		
		switch(code) {
			case SET_LUMINOUS:
				message.setDimmerTime(readBytes(rawdata, 12, 2, true));
	
			case INCREASE:
			case DECREASE:
			case GET_ONOFF:
			case SET_ONOFF:
				message.setValue(new BigDecimal(readBytes(rawdata, 10, 1, false)));
				break;
				
			default:
				throw new IllegalArgumentException("Unhandled command value " + code.name());
		}
		
	}
	
	public byte[] toBytes() {
		if (message.getCommandCode() != null) {
			return toCommandBytes(message.getCommandCode());
		} else if (message.getStatusCode() != null) {
			return toStatusBytes(message.getStatusCode());
		}
		throw new IllegalStateException("Could not convert MoxMessage to bytes with unknown variant. At least a status or a command code has to be set.");
	}

	protected byte[] toCommandBytes(final MoxCommandCode code) {
		byte[] bytes;
		switch (code) {
			case SET_LUMINOUS:
				bytes = new byte[14];
				int value = message.getValue().setScale(0, RoundingMode.HALF_UP).intValue();
				int dimSpeed = Math.min(300, 0xffff); // TODO 300ms make dim speed configurable
				setBytes(bytes, 10, value);
				setBytes(bytes, 11, 0);
				setBytes(bytes, 12, dimSpeed%256, dimSpeed/256); 
				break;
			case SET_ONOFF:
				bytes = new byte[11];
				setBytes(bytes, 10, message.getValue().intValue());
				break;
			case GET_LUMINOUS:
			case GET_ONOFF:
				bytes = new byte[10];
				break;
			case INCREASE:
			case DECREASE:
				bytes = new byte[14];
				int amount = Math.min(message.getValue().intValue(), 100);
				setBytes(bytes, 10, amount, 0, 0xc8, 0);
				break;
			default:
				throw new NotImplementedException("This package cannot be created, yet");
		}

		return setDefaultBytes(bytes, code);
	}
	
	protected byte[] toStatusBytes(final MoxStatusCode code) {
		throw new NotImplementedException("This package cannot be created, yet");
	}
	
	protected byte[] setDefaultBytes(byte[] bytes, final MoxCode code) {
		int oid = message.getOid();
		setBytes(bytes, 0, message.getPriority());
		setBytes(bytes, 1, oid/512, oid/256, oid%256);
		setBytes(bytes, 4, 0x11); // TODO suboid
		setBytes(bytes, 5, code.getLow());
		setBytes(bytes, 6, 0, 0);
		setBytes(bytes, 8, code.getHigh()/256);
		setBytes(bytes, 9, code.getHigh() % 256);
		return bytes;
	}
	
	private static int readBytes(byte[] rawdata, int offset, int length, boolean le) {
	    int value = 0;
	    for (int i = 0; i < length; i++) {
	        int shift = le ? i * 8 : (length - 1 - i) * 8;
	        value += (rawdata[i + offset] & 0x000000FF) << shift;
	    }
	    return value;
	}

	private static byte[] setBytes(byte[] rawdata, int offset, int... values) {
		for (int i=0; i<values.length; i++) {
			byte currVal = (byte) (((byte) values[i]) & 0xff);
			rawdata[offset + i] = currVal;
		}
		return rawdata;
	}
	
	public static int[] getUnsignedIntArray(byte[] bytes) {
		if (bytes == null)
	        return null;

		int[] res = new int[bytes.length];

	    for (int i = 0; i < bytes.length; i++) {
		res[i] = bytes[i] & 0xff;
	    }
	    return res;
	}

    public MoxMessage build() {
        return this.message;
    }
    
}
