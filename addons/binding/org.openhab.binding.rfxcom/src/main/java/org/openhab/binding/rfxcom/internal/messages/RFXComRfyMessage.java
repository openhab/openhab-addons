/**
 * openHAB, the open Home Automation Bus.
 * Copyright (C) 2010-2013, openHAB.org <admin@openhab.org>
 *
 * See the contributors.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or
 * combining it with Eclipse (or a modified version of that library),
 * containing parts covered by the terms of the Eclipse Public License
 * (EPL), the licensors of this Program grant you additional permission
 * to convey the resulting work.
 */
package org.openhab.binding.rfxcom.internal.messages;

import java.util.Arrays;
import java.util.List;

import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.library.items.RollershutterItem;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.rfxcom.RFXComValueSelector;
import org.openhab.binding.rfxcom.internal.RFXComException;

/**
 * RFXCOM data class for RFY (Somfy RTS) message.
 * 
 * @author Jürgen Richtsfeld - Initial contribution
 * @author Pauli Anttila
 */
public class RFXComRfyMessage extends RFXComBaseMessage {

	public enum Commands {
		STOP(0x00), 
		OPEN(0x01), 
		CLOSE(0x03), 
		UP_2SEC(0x11), 
		DOWN_2SEC(0x12);

		private final int command;

		Commands(int command) {
			this.command = command;
		}

		Commands(byte command) {
			this.command = command;
		}

		public byte toByte() {
			return (byte) command;
		}
	}

	public enum SubType {
		RFY(0), RFY_EXT(1);

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
					RFXComValueSelector.COMMAND);

	private final static List<RFXComValueSelector> supportedOutputValueSelectors = Arrays
			.asList(RFXComValueSelector.SHUTTER);

	public SubType subType = SubType.RFY;
	public int unitId = 0;
	/**
	 * valid numbers 0-4; 0 == all units
	 */
	public byte unitCode = 0;
	public Commands command = Commands.STOP;
	public byte signalLevel = 0xF; // maximum

	public RFXComRfyMessage() {
		packetType = PacketType.RFY;

	}

	public RFXComRfyMessage(byte[] data) {

		encodeMessage(data);
	}

	@Override
	public String toString() {
		String str = "";

		if (rawMessage != null) {
			str += super.toString();
		}
		str += ", Sub type = " + subType;
		str += ", Device Id = " + getDeviceId();
		str += ", Unit code = " + unitCode;
		str += ", Command = " + command;
		str += ", Signal level = " + signalLevel;

		return str;
	}

	@Override
	public void encodeMessage(byte[] data) {

		super.encodeMessage(data);

		subType = SubType.values()[super.subType];

		unitId = (data[4] & 0xFF) << 16 | (data[5] & 0xFF) << 8
				| (data[6] & 0xFF);

		unitCode = data[7];

		command = Commands.STOP;

		for (Commands loCmd : Commands.values()) {
			if (loCmd.toByte() == data[8]) {
				command = loCmd;
				break;
			}
		}
		signalLevel = (byte) ((data[12] & 0xF0) >> 4);

	}

	@Override
	public byte[] decodeMessage() {
		final byte[] data = new byte[13];

		data[0] = 12;
		data[1] = RFXComBaseMessage.PacketType.RFY.toByte();
		data[2] = subType.toByte();
		data[3] = seqNbr;
		data[4] = (byte) ((unitId >> 16) & 0xFF);
		data[5] = (byte) ((unitId >> 8) & 0xFF);
		data[6] = (byte) (unitId & 0xFF);
		data[7] = unitCode;
		data[8] = command.toByte();
		data[12] = (byte) ((signalLevel & 0x0F) << 4);

		return data;
	}

	@Override
	public String getDeviceId() {
		return unitId + ID_DELIMITER + unitCode;
	}

	/**
	 * this was copied from RFXComBlinds1Message.
	 */
	@Override
	public State convertToState(RFXComValueSelector valueSelector)
			throws RFXComException {
		State state = UnDefType.UNDEF;

		if (valueSelector.getItemClass() == NumberItem.class) {
			if (valueSelector == RFXComValueSelector.SIGNAL_LEVEL) {
				state = new DecimalType(signalLevel);
			} else {
				throw new RFXComException("Can't convert " + valueSelector
						+ " to NumberItem");
			}

		} else if (valueSelector.getItemClass() == RollershutterItem.class) {
			if (valueSelector == RFXComValueSelector.COMMAND) {

				switch (command) {
				case CLOSE:
					state = OpenClosedType.CLOSED;
					break;

				case OPEN:
					state = OpenClosedType.OPEN;
					break;

				default:
					break;
				}

			} else {
				throw new NumberFormatException("Can't convert "
						+ valueSelector + " to RollershutterItem");
			}

		} else {
			throw new NumberFormatException("Can't convert " + valueSelector
					+ " to " + valueSelector.getItemClass());
		}

		return state;
	}

	@Override
	public void setSubType(Object subType) throws RFXComException {
		this.subType = ((SubType) subType);
	}

	@Override
	public void setDeviceId(String deviceId) throws RFXComException {
		String[] ids = deviceId.split("\\" + ID_DELIMITER);
		if (ids.length != 2) {
			throw new RFXComException("Invalid device id '" + deviceId + "'");
		}

		this.unitId = (byte) Short.parseShort(ids[0]);
		this.unitCode = Byte.parseByte(ids[1]);
	}

	@Override
	public void convertFromState(RFXComValueSelector valueSelector, Type type)
			throws RFXComException {

		switch (valueSelector) {
		case SHUTTER:
			if (type instanceof OpenClosedType) {
				this.command = (type == OpenClosedType.CLOSED ? Commands.CLOSE : Commands.OPEN);
			} else if (type instanceof UpDownType) {
				this.command = (type == UpDownType.UP ? Commands.OPEN : Commands.CLOSE);
			} else if (type instanceof StopMoveType) {
				this.command = RFXComRfyMessage.Commands.STOP;

			} else {
				throw new NumberFormatException("Can't convert " + type + " to Command");
			}
			break;
		default:
			throw new RFXComException("Can't convert " + type + " to " + valueSelector);
		}
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
