/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
    public static byte[] KnxHeader = new byte[6];
    public static byte[] ConnectionHeader = new byte[4];

    private final static Logger logger = LoggerFactory.getLogger(KnxNetFrame.class);
    private ArrayList<SetDatapointValueMessage> pValueMessages = new ArrayList<SetDatapointValueMessage>();
    private byte __MainService;
    private byte __SubService = SubServiceType.SetDatapointValueReq;
    private int __StartDataPoint;

    static {
        try {
            KnxHeader[0] = (byte) 0x06; // Header size
            KnxHeader[1] = (byte) 0x20; // Version (2.0)
            KnxHeader[2] = (byte) 0xF0; // Object server request
            KnxHeader[3] = (byte) 0x80; // Object server request
            KnxHeader[4] = (byte) 0x00; // Frame size
            KnxHeader[5] = (byte) 0x00; // Frame size

            ConnectionHeader[0] = (byte) 0x04; // Structure length
            ConnectionHeader[1] = (byte) 0x00; // Reserved
            ConnectionHeader[2] = (byte) 0x00; // Reserved
            ConnectionHeader[3] = (byte) 0x00; // Reserved
        } catch (Exception __err) {
            throw new ExceptionInInitializerError(__err);
        }

    }

    public KnxNetFrame() throws Exception {
        this.pValueMessages = new ArrayList<SetDatapointValueMessage>();
    }

    public byte getMainService() {
        return __MainService;
    }

    public void setMainService(byte value) {
        __MainService = value;
    }

    public byte getSubService() {
        return __SubService;
    }

    public void setSubService(byte value) {
        __SubService = value;
    }

    public int getStartDataPoint() {
        return __StartDataPoint;
    }

    public void setStartDataPoint(int value) {
        __StartDataPoint = value;
    }

    public SetDatapointValueMessage[] getValueMessages() throws Exception {
        SetDatapointValueMessage[] result = new SetDatapointValueMessage[this.pValueMessages.size()];
        this.pValueMessages.toArray(result);
        return result;
    }

    public static KnxNetFrame createKnxNetPackage(byte[] data, int amount) throws Exception {
        if (data.length < 16 || amount < 16 || data.length < amount) {
            logger.error("Length of the data too short for a KNXnet/IP package ({}).", data.length);
            return null;
        }

        if (data[0] != KnxHeader[0] || data[1] != KnxHeader[1] || data[2] != KnxHeader[2] || data[3] != KnxHeader[3]) {
            logger.error("Incorrect KNXnet/IP header.");
            return null;
        }

        int frameSize = Byte.toUnsignedInt(data[4]) * 256 + Byte.toUnsignedInt(data[5]);
        if (frameSize != amount) {
            logger.error("CreateKnxNetPackage: Error TelegrammLength/FrameSize missmatch. ({}/{})", data.length,
                    frameSize);
            return null;
        }

        KnxNetFrame frame = new KnxNetFrame();
        frame.setMainService(data[10]);
        if (frame.getMainService() != (byte) 0xF0) {
            logger.error("CreateKnxNetPackage: Main-Service not supported. ({})", frame.getMainService());
            return null;
        }

        if (data[11] == (byte) 0x06) {
            frame.setSubService(SubServiceType.SetDatapointValueReq);
        } else if (data[11] == (byte) 0x86) {
            frame.setSubService(SubServiceType.SetDatapointValueRes);
        } else if (data[11] == (byte) 0xC1) {
            frame.setSubService(SubServiceType.DatapointValueWrite);
        } else if (data[11] == (byte) 0xD0) {
            frame.setSubService(SubServiceType.RequestAllDatapoints);
        } else {
            logger.error("CreateKnxNetPackage: Sub-Service not supported. ({})", frame.getSubService());
            return null;
        }

        if (frame.getSubService() == SubServiceType.SetDatapointValueReq) {
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
                frame.pValueMessages.add(msg);
            }
            return frame;
        }

        return null;
    }

    public byte[] createFrameAnswer() throws Exception {
        ByteBuffer answer = ByteBuffer.allocate(17);
        if (this.getSubService() == SubServiceType.SetDatapointValueReq) {
            answer.put(KnxHeader);
            answer.put(5, (byte) 0x11); // static size (17 bytes)
            answer.put(ConnectionHeader);

            answer.put(this.getMainService());
            answer.put(SubServiceType.SetDatapointValueRes);
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