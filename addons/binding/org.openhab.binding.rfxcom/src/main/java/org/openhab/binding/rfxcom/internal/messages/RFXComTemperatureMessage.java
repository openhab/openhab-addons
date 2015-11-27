/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.internal.messages;

import java.util.Arrays;
import java.util.List;

import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.rfxcom.RFXComValueSelector;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;

/**
 * RFXCOM data class for temperature and humidity message.
 * 
 * @author Pauli Anttila - Initial contribution
 */
public class RFXComTemperatureMessage extends RFXComBaseMessage {

	public enum SubType {
		UNDEF(0),
		THR128_138_THC138(1),
		THC238_268_THN122_132_THWR288_THRN122_AW129_131(2),
		THWR800(3),
		RTHN318(4),
		LACROSSE_TX2_TX3_TX4_TX17(5),
		TS15C(6),
		VIKING_02811(7),
		LACROSSE_WS2300(8),
		RUBICSON(9),
		TFA_30_3133(10),

		UNKNOWN(255);

		private final int subType;

		SubType(int subType) {
			this.subType = subType;
		}

		SubType(byte subType) {
			this.subType = subType;
		}

		public byte toByte() {
			return (byte) subType;
		}
	}

	private final static List<RFXComValueSelector> supportedInputValueSelectors = Arrays
			.asList(RFXComValueSelector.SIGNAL_LEVEL,
					RFXComValueSelector.BATTERY_LEVEL,
					RFXComValueSelector.TEMPERATURE);

	private final static List<RFXComValueSelector> supportedOutputValueSelectors = Arrays
			.asList();

	public SubType subType = SubType.THR128_138_THC138;
	public int sensorId = 0;
	public double temperature = 0;
	public byte signalLevel = 0;
	public byte batteryLevel = 0;

	public RFXComTemperatureMessage() {
		packetType = PacketType.TEMPERATURE;
	}

	public RFXComTemperatureMessage(byte[] data) {
		encodeMessage(data);
	}

	@Override
	public String toString() {
		String str = "";

		str += super.toString();
		str += ", Sub type = " + subType;
		str += ", Device Id = " + getDeviceId();
		str += ", Temperature = " + temperature;
		str += ", Signal level = " + signalLevel;
		str += ", Battery level = " + batteryLevel;

		return str;
	}

	@Override
	public void encodeMessage(byte[] data) {

		super.encodeMessage(data);

		try {
			subType = SubType.values()[super.subType];
		} catch (Exception e) {
			subType = SubType.UNKNOWN;
		}
		
		sensorId = (data[4] & 0xFF) << 8 | (data[5] & 0xFF);

		temperature = (short) ((data[6] & 0x7F) << 8 | (data[7] & 0xFF)) * 0.1;
		if ((data[6] & 0x80) != 0)
			temperature = -temperature;

		signalLevel = (byte) ((data[8] & 0xF0) >> 4);
		batteryLevel = (byte) (data[8] & 0x0F);
	}

	@Override
	public byte[] decodeMessage() {
		byte[] data = new byte[9];

		data[0] = 0x08;
		data[1] = RFXComBaseMessage.PacketType.TEMPERATURE.toByte();
		data[2] = subType.toByte();
		data[3] = seqNbr;
		data[4] = (byte) ((sensorId & 0xFF00) >> 8);
		data[5] = (byte) (sensorId & 0x00FF);

		short temp = (short) Math.abs(temperature * 10);
		data[6] = (byte) ((temp >> 8) & 0xFF);
		data[7] = (byte) (temp & 0xFF);
		if (temperature < 0)
			data[6] |= 0x80;

		data[8] = (byte) (((signalLevel & 0x0F) << 4) | (batteryLevel & 0x0F));

		return data;
	}

	@Override
	public String getDeviceId() {
		return String.valueOf(sensorId);
	}

	@Override
	public State convertToState(RFXComValueSelector valueSelector)
			throws RFXComException {
		
		State state = UnDefType.UNDEF;

		if (valueSelector.getItemClass() == NumberItem.class) {

			if (valueSelector == RFXComValueSelector.SIGNAL_LEVEL) {

				state = new DecimalType(signalLevel);

			} else if (valueSelector == RFXComValueSelector.BATTERY_LEVEL) {

				state = new DecimalType(batteryLevel);

			} else if (valueSelector == RFXComValueSelector.TEMPERATURE) {

				state = new DecimalType(temperature);

			} else {
				throw new RFXComException("Can't convert "
						+ valueSelector + " to NumberItem");
			}

		} else {

			throw new RFXComException("Can't convert " + valueSelector
					+ " to " + valueSelector.getItemClass());

		}

		return state;
	}

	@Override
	public void setSubType(Object subType) throws RFXComException {
		throw new RFXComException("Not supported");
	}

	@Override
	public void setDeviceId(String deviceId) throws RFXComException {
		throw new RFXComException("Not supported");
	}

	@Override
	public void convertFromState(RFXComValueSelector valueSelector, Type type)
			throws RFXComException {

		throw new RFXComException("Not supported");
	}

	@Override
	public Object convertSubType(String subType) throws RFXComException {
		
		for (SubType s : SubType.values()) {
			if (s.toString().equals(subType)) {
				return s;
			}
		}
		
		// try to find sub type by number
		try {
			return SubType.values()[Integer.parseInt(subType)];
		} catch (Exception e) {
			throw new RFXComException("Unknown sub type " + subType);
		}
	}
	
	@Override
	public List<RFXComValueSelector> getSupportedInputValueSelectors()
			throws RFXComException {
		return supportedInputValueSelectors;
	}
	
	@Override
	public List<RFXComValueSelector> getSupportedOutputValueSelectors()
			throws RFXComException {
		return supportedOutputValueSelectors;
	}
}
