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
 * RFXCOM data class for energy message.
 * 
 * @author Unknown - Initial contribution
 * @author Pauli Anttila
 */
public class RFXComEnergyMessage  extends RFXComBaseMessage {

	private static float TOTAL_USAGE_CONVERSION_FACTOR = 223.666F;
	private static float WATTS_TO_AMPS_CONVERSION_FACTOR = 230F;
	
	public enum SubType {
		ELEC1(0),
		ELEC2(1),
		ELEC3(2),

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
					RFXComValueSelector.COMMAND,
					RFXComValueSelector.INSTANT_POWER,
					RFXComValueSelector.TOTAL_USAGE,
					RFXComValueSelector.INSTANT_AMPS,
					RFXComValueSelector.TOTAL_AMP_HOURS);

	private final static List<RFXComValueSelector> supportedOutputValueSelectors = Arrays
			.asList();

	public SubType subType = SubType.ELEC1;
	public int sensorId = 0;
	public byte count = 0;
	public double instantAmps = 0;
	public double totalAmpHours = 0;
	public double instantPower = 0;
	public double totalUsage = 0;
	public byte signalLevel = 0;
	public byte batteryLevel = 0;

	public RFXComEnergyMessage() {
		packetType = PacketType.ENERGY;
	}

	public RFXComEnergyMessage(byte[] data) {
		encodeMessage(data);
	}

	@Override
	public String toString() {
		String str = "";

		str += super.toString();
		str += ", Sub type = " + subType;
		str += ", Device Id = " + getDeviceId();
		str += ", Count = " + count;
		str += ", Instant Amps = " + instantAmps;
		str += ", Total Amp Hours = " + totalAmpHours;
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
		count = data[6];
		
		// all usage is reported in Watts based on 230V
		instantPower = ((data[7] & 0xFF) << 24 | (data[8] & 0xFF) << 16
				| (data[9] & 0xFF) << 8 | (data[10] & 0xFF))
				/ TOTAL_USAGE_CONVERSION_FACTOR;
		totalUsage = ((data[11] & 0xFF) << 40 | (data[12] & 0xFF) << 32
				| (data[13] & 0xFF) << 24 | (data[14] & 0xFF) << 16
				| (data[15] & 0xFF) << 8 | (data[16] & 0xFF));
		
		// convert to amps so external code can determine the watts based on local voltage
		instantAmps = instantPower / WATTS_TO_AMPS_CONVERSION_FACTOR;
		totalAmpHours = totalUsage / WATTS_TO_AMPS_CONVERSION_FACTOR;
		
		signalLevel = (byte) ((data[17] & 0xF0) >> 4);
		batteryLevel = (byte) (data[17] & 0x0F);
	}

	@Override
	public byte[] decodeMessage() {
		byte[] data = new byte[17];

		data[0] = 0x11;
		data[1] = RFXComBaseMessage.PacketType.ENERGY.toByte();
		data[2] = subType.toByte();
		data[3] = seqNbr;
		
		data[4] = (byte) ((sensorId & 0xFF00) >> 8);
		data[5] = (byte) (sensorId & 0x00FF);
		data[6] = count;

		long instantUsage = (long) instantPower;
		long totalUsage = (long) this.totalUsage;
		
		data[7] = (byte) ((instantUsage >> 24) & 0xFF);
		data[8] = (byte) ((instantUsage >> 16) & 0xFF);
		data[9] = (byte) ((instantUsage >> 8) & 0xFF);
		data[10] = (byte) (instantUsage & 0xFF);

		data[11] = (byte) ((totalUsage >> 40) & 0xFF);
		data[12] = (byte) ((totalUsage >> 32) & 0xFF);
		data[13] = (byte) ((totalUsage >> 24) & 0xFF);
		data[14] = (byte) ((totalUsage >> 16) & 0xFF);
		data[15] = (byte) ((totalUsage >> 8) & 0xFF);
		data[16] = (byte) (totalUsage & 0xFF);

		data[17] = (byte) (((signalLevel & 0x0F) << 4) | (batteryLevel & 0x0F));

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

			} else if (valueSelector == RFXComValueSelector.INSTANT_POWER) {

				state = new DecimalType(instantPower);

			} else if (valueSelector == RFXComValueSelector.TOTAL_USAGE) {

				state = new DecimalType(totalUsage);

			} else if (valueSelector == RFXComValueSelector.INSTANT_AMPS) {

				state = new DecimalType(instantAmps);

			} else if (valueSelector == RFXComValueSelector.TOTAL_AMP_HOURS) {

				state = new DecimalType(totalAmpHours);

			} else {
				
				throw new RFXComException("Can't convert " + valueSelector + " to NumberItem");
				
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
