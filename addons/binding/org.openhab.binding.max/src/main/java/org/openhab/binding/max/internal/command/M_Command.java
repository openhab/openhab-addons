/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.max.internal.command;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.util.Base64;
import org.openhab.binding.max.internal.Utils;
import org.openhab.binding.max.internal.device.Device;
import org.openhab.binding.max.internal.device.RoomInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link M_Command} Creates the MAX! Cube the room & device name information update message.
 *
 * @author Marcel Verpaalen - Initial Contribution
 * @since 2.0
 */

public class M_Command extends CubeCommand {
    private final Logger logger = LoggerFactory.getLogger(M_Command.class);

    private static byte MAGIC_NR = 86;
    private static byte M_VERSION = 2;
    private static int MAX_NAME_LENGTH = 32;
    private static int MAX_GROUP_COUNT = 20;
    private static int MAX_DEVICES_COUNT = 140;
    private static int MAX_MSG_LENGTH = 1900;

    private ArrayList<Device> devices = new ArrayList<Device>();
    public ArrayList<RoomInformation> rooms = new ArrayList<RoomInformation>();

    public M_Command(ArrayList<Device> devices) {
        this.devices = new ArrayList<Device>(devices);
        roombuilder();
    }

    public M_Command(ArrayList<Device> devices, ArrayList<RoomInformation> rooms) {
        this.devices = new ArrayList<Device>(devices);
        this.rooms = new ArrayList<RoomInformation>(rooms);
        roombuilder();
    }

    public void listRooms() {
        for (RoomInformation room : rooms) {
            logger.debug("M-Command room info: {}", room.toString());
        }
    }

    public ArrayList<RoomInformation> getRooms() {
        return rooms;
    }

    private void roombuilder() {
        for (Device di : devices) {
            boolean foundRoom = false;

            for (RoomInformation room : rooms) {
                if (room.getPosition() == di.getRoomId()) {
                    foundRoom = true;
                }
            }
            // Add new rooms based on device information.
            // TODO check if it is allowed to have any device creating a room, or should it be a thermostat
            if (!foundRoom && di.getRoomId() != 0 && rooms.size() < MAX_GROUP_COUNT) {
                RoomInformation room = new RoomInformation(di.getRoomId(), di.getRoomName(), di.getRFAddress());
                rooms.add(room);
            }
        }
    }

    public byte[] concatenateByteArrays(List<byte[]> blocks) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        for (byte[] b : blocks) {
            os.write(b, 0, b.length);
        }
        return os.toByteArray();
    }

    @Override
    public String getCommandString() {
        int deviceCount = 0;
        int roomCount = 0;

        ByteArrayOutputStream message = new ByteArrayOutputStream();

        try {
            byte[] header = { MAGIC_NR, M_VERSION, (byte) rooms.size() };
            message.write(header);

            TreeSet<Integer> sortedRooms = new TreeSet<Integer>();
            for (RoomInformation room : rooms) {
                sortedRooms.add(room.getPosition());

            }

            for (Integer roomPos : sortedRooms) {
                for (RoomInformation room : rooms) {
                    if (room.getPosition() == roomPos) {
                        if (roomCount < MAX_GROUP_COUNT) {
                            byte[] roomName = StringUtils.abbreviate(room.getName(), MAX_NAME_LENGTH).getBytes("UTF-8");
                            byte[] nameLength = new byte[] { (byte) roomName.length };
                            byte[] rfAddress = Utils.hexStringToByteArray(room.getRFAddress());
                            message.write(roomPos.byteValue());
                            message.write(nameLength);
                            message.write(roomName);
                            message.write(rfAddress);
                        } else {
                            logger.warn("{} exceeds max number of rooms ({}). Ignored", room.toString(),
                                    MAX_GROUP_COUNT);
                        }
                        roomCount++;
                    }
                }
            }
            for (Device di : devices) {
                if (deviceCount < MAX_DEVICES_COUNT) {
                    deviceCount++;
                } else {
                    logger.warn("{} exceeds max number of devices ({}). Ignored", di.toString(), MAX_DEVICES_COUNT);
                }
            }
            message.write((byte) deviceCount);
            for (Device di : devices) {
                if (deviceCount > 0) {
                    byte[] deviceType = { (byte) di.getType().getValue() };
                    byte[] rfAddress = Utils.hexStringToByteArray(di.getRFAddress());
                    byte[] deviceName = StringUtils.abbreviate(di.getName(), MAX_NAME_LENGTH).getBytes("UTF-8");
                    byte[] nameLength = { (byte) deviceName.length };
                    byte[] serialNumber = di.getSerialNumber().getBytes();
                    byte[] roomId = { (byte) di.getRoomId() };

                    message.write(deviceType);
                    message.write(rfAddress);
                    message.write(serialNumber);
                    message.write(nameLength);
                    message.write(deviceName);
                    message.write(roomId);
                } else {
                    logger.warn("{} exceeds max number of devices ({}). Ignored", di.toString(), MAX_DEVICES_COUNT);
                }
                deviceCount--;
            }

            byte[] dst = { 0x01 };
            message.write(dst);

        } catch (IOException e) {
            logger.debug("Error while generating m: command: {}", e.getMessage(), e);

        }

        String encodedString = Base64.encodeBase64StringUnChunked(message.toByteArray());

        String commandString = "";
        int parts = (int) Math.round(encodedString.length() / MAX_MSG_LENGTH + 0.5);
        for (int i = 0; i < parts; i++) {
            String partString = StringUtils.abbreviate(encodedString.substring((i) * MAX_MSG_LENGTH), MAX_MSG_LENGTH);
            commandString = commandString + "m:" + String.format("%02d", i) + "," + partString + '\r' + '\n';
        }

        return commandString;
    }

    @Override
    public String getReturnStrings() {
        return "A:";
    }

}
