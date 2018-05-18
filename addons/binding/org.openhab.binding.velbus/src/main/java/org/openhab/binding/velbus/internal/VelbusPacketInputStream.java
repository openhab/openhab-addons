/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.velbus.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.openhab.binding.velbus.internal.packets.VelbusPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VelbusPacketInputStream} is a wrapper around an InputStream that
 * aggregates bytes from the input stream to meaningfull packets in the Velbus system.
 *
 * @author Cedric Boon - Initial contribution
 */
public class VelbusPacketInputStream {
    private Logger logger = LoggerFactory.getLogger(VelbusPacketInputStream.class);

    public InputStream inputStream;

    private Byte currentSTX = null;
    private Byte currentPriority = null;
    private Byte currentAddress = null;
    private Byte currentDataLength = null;
    private ArrayList<Byte> currentData = new ArrayList<Byte>();
    private Byte currentChecksum = null;

    public VelbusPacketInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public byte[] readPacket() throws IOException {
        int currentDataByte;

        while ((currentDataByte = inputStream.read()) > -1) {
            if (currentSTX == null) {
                if (((byte) currentDataByte) == VelbusPacket.STX) {
                    currentSTX = (byte) currentDataByte;
                } else {
                    resetCurrentState();
                    logger.debug("Packet with invalid start byte: {}", currentDataByte);
                }
            } else if (currentPriority == null) {
                if (((byte) currentDataByte) == VelbusPacket.PRIO_HI
                        || ((byte) currentDataByte) == VelbusPacket.PRIO_LOW) {
                    currentPriority = (byte) currentDataByte;
                } else {
                    resetCurrentState();
                    logger.debug("Packet with invalid priority received: {}", currentDataByte);
                }
            } else if (currentAddress == null) {
                currentAddress = (byte) currentDataByte;
            } else if (currentDataLength == null && currentDataByte <= 8) {
                currentDataLength = (byte) currentDataByte;
            } else if (currentDataLength == null) {
                currentDataLength = 1;
                currentData.add((byte) currentDataByte);
            } else if (currentData.size() < currentDataLength) {
                currentData.add((byte) currentDataByte);
            } else if (currentChecksum == null) {
                currentChecksum = (byte) currentDataByte;
                byte[] packet = getCurrentPacket();
                byte expectedChecksum = VelbusPacket.computeCRCByte(packet);

                if (currentChecksum != expectedChecksum) {
                    resetCurrentState();
                    logger.debug("Packet with invalid checksum received: {} instead of {}", currentChecksum,
                            expectedChecksum);
                }
            } else if (((byte) currentDataByte) == VelbusPacket.ETX) {
                byte[] packet = getCurrentPacket();

                resetCurrentState();

                return packet;
            } else {
                resetCurrentState();
                logger.debug("Packet with invalid ETX received: {}", currentDataByte);
            }
        }

        return null;
    }

    protected byte[] getCurrentPacket() {
        byte[] packet = new byte[6 + currentDataLength];
        packet[0] = currentSTX;
        packet[1] = currentPriority;
        packet[2] = currentAddress;
        packet[3] = currentDataLength;

        for (int i = 0; i < currentDataLength; i++) {
            packet[4 + i] = currentData.get(i);
        }

        packet[4 + currentDataLength] = currentChecksum;
        packet[5 + currentDataLength] = VelbusPacket.ETX;

        return packet;
    }

    protected void resetCurrentState() {
        currentSTX = null;
        currentPriority = null;
        currentAddress = null;
        currentDataLength = null;
        currentData = new ArrayList<Byte>();
        currentChecksum = null;
    }
}
