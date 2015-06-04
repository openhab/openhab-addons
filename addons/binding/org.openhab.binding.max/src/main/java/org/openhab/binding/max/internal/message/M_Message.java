/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.max.internal.message;

import java.util.ArrayList;

import org.apache.commons.net.util.Base64;
import org.openhab.binding.max.MaxBinding;
import org.openhab.binding.max.internal.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The M message contains metadata about the MAX!Cube setup. 
 * 
 * @author Andreas Heil (info@aheil.de) - Initial Contribution
 * @author Marcel Verpaalen - Room details parse
 * @since 1.4.0
 */
public final class M_Message extends Message {

	public ArrayList<RoomInformation> rooms;
	public ArrayList<DeviceInformation> devices;
	private Boolean hasConfiguration ;
	Logger logger = LoggerFactory.getLogger(MaxBinding.class);
	

	public M_Message(String raw) {
		super(raw);
		hasConfiguration = false;

		String[] tokens = this.getPayload().split(Message.DELIMETER);

		if (tokens.length > 1) try {
			byte[] bytes = Base64.decodeBase64(tokens[2].getBytes());
			
			hasConfiguration = true;
			logger.trace("*** M_Message trace**** ");
			logger.trace ("\tMagic? (expect 86) : {}", (int) bytes[0]);
			logger.trace ("\tVersion? (expect 2): {}", (int) bytes[1]);
			logger.trace ("\t#defined rooms in M: {}", (int) bytes[2]);
			
			
			rooms = new ArrayList<RoomInformation>();
			devices = new ArrayList<DeviceInformation>();

			int roomCount = bytes[2];

			int byteOffset = 3; // start of rooms

			/* process room */

			for (int i = 0; i < roomCount; i++) {

				int position = bytes[byteOffset++];

				int nameLength = (int) bytes[byteOffset++] & 0xff; 
				byte[] data = new byte[nameLength];
                System.arraycopy(bytes, byteOffset, data, 0, nameLength);
                byteOffset += nameLength;
                String name = new String(data, "UTF-8");

				String rfAddress = Utils.toHex(((int)bytes[byteOffset] & 0xff), ((int)bytes[byteOffset+1] & 0xff), ((int)bytes[byteOffset + 2] & 0xff));
				byteOffset += 3;

				rooms.add(new RoomInformation(position, name, rfAddress));
			}

			/* process devices */

			int deviceCount = bytes[byteOffset++];

			for (int deviceId = 0; deviceId < deviceCount; deviceId++) {
				DeviceType deviceType = DeviceType.create(bytes[byteOffset++]);

				String rfAddress = Utils.toHex(((int)bytes[byteOffset]&0xff), ((int)bytes[byteOffset+1]&0xff), ((int)bytes[byteOffset+2]&0xff));
				byteOffset += 3;

				String serialNumber = "";

				for (int i = 0; i < 10; i++) {
					serialNumber += (char) bytes[byteOffset++];
				}

				int nameLength = (int)bytes[byteOffset++] & 0xff;
                byte[] data = new byte[nameLength];
                System.arraycopy(bytes, byteOffset, data, 0, nameLength);
                byteOffset += nameLength;
                String deviceName = new String(data, "UTF-8");

				int roomId = (int)bytes[byteOffset++] & 0xff;
				devices.add(new DeviceInformation(deviceType, serialNumber, rfAddress, deviceName, roomId));	
			}
		}  catch (Exception e) {
			logger.info("Unknown error parsing the M Message: {}", e.getMessage(), e);
			logger.debug("\tRAW : {}", this.getPayload());
		}
		else {
			logger.info("No rooms defined. Configure your Max!Cube");
			hasConfiguration = false;
		} 
	}

	@Override
	public void debug(Logger logger) {
		logger.trace("=== M_Message === ");
		if (hasConfiguration) {
			logger.trace("\tRAW : {}", this.getPayload());
			for(RoomInformation room: rooms){
				logger.trace("\t=== Rooms ===");
				logger.trace("\tRoom Pos   : {}", room.getPosition());
				logger.trace("\tRoom Name  : {}", room.getName());
				logger.trace("\tRoom RF Adr: {}",  room.getRFAddress());
				for(DeviceInformation device: devices){
					if (room.getPosition() == device.getRoomId()) {
						logger.trace("\t=== Devices ===");
						logger.trace("\tDevice Type    : {}", device.getDeviceType());
						logger.trace("\tDevice Name    : {}", device.getName());
						logger.trace("\tDevice Serialnr: {}", device.getSerialNumber());
						logger.trace("\tDevice RF Adr  : {}", device.getRFAddress());
						logger.trace("\tRoom Id        : {}", device.getRoomId());
					}
				}

			}
		} 
		else {
			logger.debug("M-Message empty. No Configuration");
		}
	}

	@Override
	public MessageType getType() {
		return MessageType.M;
	}
}
