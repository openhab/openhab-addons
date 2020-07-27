/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.ism8.server;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link KnxNetFrame} is used for handling the received KNX.Net frames
 *
 * @author Hans-Reiner Hoffmann - Initial contribution
 */
public class KnxNetFrame {
    public static byte[] knxHeader = new byte[6];
    public static byte[] connectionHeader = new byte[4];

    private final Logger logger = LoggerFactory.getLogger(KnxNetFrame.class);
    private ArrayList<SetDatapointValueMessage> valueMessages = new ArrayList<SetDatapointValueMessage>();
    private byte mainService;
    private byte subService = SubServiceType.SET_DATAPOINT_VALUE_REQUEST;
    private int startDataPoint;

    static {
        knxHeader[0] = (byte) 0x06; // Header size
        knxHeader[1] = (byte) 0x20; // Version (2.0)
        knxHeader[2] = (byte) 0xF0; // Object server request
        knxHeader[3] = (byte) 0x80; // Object server request
        knxHeader[4] = (byte) 0x00; // Frame size
        knxHeader[5] = (byte) 0x00; // Frame size

        connectionHeader[0] = (byte) 0x04; // Structure length
        connectionHeader[1] = (byte) 0x00; // Reserved
        connectionHeader[2] = (byte) 0x00; // Reserved
        connectionHeader[3] = (byte) 0x00; // Reserved
    }

    public KnxNetFrame() {
        this.valueMessages = new ArrayList<SetDatapointValueMessage>();
    }

    /**
     * Gets the main service of the KNX frame
     *
     */
    public byte getMainService() {
        return this.mainService;
    }

    /**
     * Sets the main service of the KNX frame
     *
     */
    public void setMainService(byte value) {
        this.mainService = value;
    }

    /**
     * Gets the sub service of the KNX frame
     *
     */
    public byte getSubService() {
        return this.subService;
    }

    /**
     * Sets the sub service of the KNX frame
     *
     */
    public void setSubService(byte value) {
        this.subService = value;
    }

    /**
     * Gets the start data-point of the KNX frame
     *
     */
    public int getStartDataPoint() {
        return this.startDataPoint;
    }

    /**
     * Sets the start data-point of the KNX frame
     *
     */
    public void setStartDataPoint(int value) {
        this.startDataPoint = value;
    }

    /**
     * Sets the value messages of the KNX frame
     *
     */
    public SetDatapointValueMessage[] getValueMessages() {
        SetDatapointValueMessage[] result = new SetDatapointValueMessage[this.valueMessages.size()];
        this.valueMessages.toArray(result);
        return result;
    }

    /**
     * Creates a KNX frame based on the data-array
     *
     */
    public static KnxNetFrame createKnxNetPackage(byte[] data, int amount) throws Exception {
        KnxNetFrame frame = new KnxNetFrame();
        if (data.length < 16 || amount < 16 || data.length < amount) {
            frame.logger.error("Length of the data too short for a KNXnet/IP package ({}).", data.length);
            return null;
        }

        if (data[0] != knxHeader[0] || data[1] != knxHeader[1] || data[2] != knxHeader[2] || data[3] != knxHeader[3]) {
            frame.logger.error("Incorrect KNXnet/IP header.");
            return null;
        }

        int frameSize = Byte.toUnsignedInt(data[4]) * 256 + Byte.toUnsignedInt(data[5]);
        if (frameSize != amount) {
            frame.logger.error("CreateKnxNetPackage: Error TelegrammLength/FrameSize missmatch. ({}/{})", data.length,
                    frameSize);
            return null;
        }

        frame.setMainService(data[10]);
        if (frame.getMainService() != (byte) 0xF0) {
            frame.logger.error("CreateKnxNetPackage: Main-Service not supported. ({})", frame.getMainService());
            return null;
        }

        if (data[11] == (byte) 0x06) {
            frame.setSubService(SubServiceType.SET_DATAPOINT_VALUE_REQUEST);
        } else if (data[11] == (byte) 0x86) {
            frame.setSubService(SubServiceType.SET_DATAPOINT_VALUE_RESULT);
        } else if (data[11] == (byte) 0xC1) {
            frame.setSubService(SubServiceType.DATAPOINT_VALUE_WRITE);
        } else if (data[11] == (byte) 0xD0) {
            frame.setSubService(SubServiceType.REQUEST_ALL_DATAPOINTS);
        } else {
            frame.logger.error("CreateKnxNetPackage: Sub-Service not supported. ({})", frame.getSubService());
            return null;
        }

        if (frame.getSubService() == SubServiceType.SET_DATAPOINT_VALUE_REQUEST) {
            frame.setStartDataPoint(Byte.toUnsignedInt(data[12]) * 256 + Byte.toUnsignedInt(data[13]));
            int numberOfDatapoints = Byte.toUnsignedInt(data[14]) * 256 + Byte.toUnsignedInt(data[15]);
            int offset = 16;

            ByteBuffer list = ByteBuffer.allocate(data.length);
            list.put(data);

            for (int i = 0; i < numberOfDatapoints; i++) {
                byte[] msgData = new byte[amount - offset];
                for (int j = 0; j < msgData.length; j++) {
                    msgData[j] = list.get(offset + j);
                }
                SetDatapointValueMessage msg = new SetDatapointValueMessage(msgData);
                offset = offset + msg.getLength() + 4;
                frame.valueMessages.add(msg);
            }
            return frame;
        }

        return null;
    }

    /**
     * Creates the answer of the KNX frame
     *
     */
    public byte[] createFrameAnswer() {
        ByteBuffer answer = ByteBuffer.allocate(17);
        if (this.getSubService() == SubServiceType.SET_DATAPOINT_VALUE_REQUEST) {
            answer.put(knxHeader);
            answer.put(5, (byte) 0x11); // static size (17 bytes)
            answer.put(connectionHeader);

            answer.put(this.getMainService());
            answer.put(SubServiceType.SET_DATAPOINT_VALUE_RESULT);
            byte low = (byte) (this.getStartDataPoint() & (byte) 0xFF);
            byte high = (byte) ((this.getStartDataPoint() & (byte) 0xFF) / 256);
            answer.put(high);
            answer.put(low);
            answer.put((byte) 0);
            answer.put((byte) 0);
            answer.put((byte) 0);
        }
        return answer.array();
    }
}
