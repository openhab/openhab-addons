/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.novafinedust.internal.sds011protocol.messages;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.util.HexUtils;

/**
 * Base class holding information sent by the sensor to us
 *
 * @author Stefan Triller - Initial contribution
 *
 */
@NonNullByDefault
public class SensorReply {

    protected final byte header;
    protected final byte commandID;
    protected final byte[] payLoad;
    protected final byte[] deviceID;
    protected final byte checksum;
    protected final byte messageTail;

    /**
     * Creates the container for data received from the sensor
     *
     * @param bytes the data received from the sensor
     * @throws IllegalArgumentException Is thrown if less than 10 bytes are provided.
     */
    public SensorReply(byte[] bytes) {
        if (bytes.length != 10) {
            throw new IllegalArgumentException("was expecting 10 bytes, but received " + bytes.length);
        }
        this.header = bytes[0];
        this.commandID = bytes[1];
        this.payLoad = Arrays.copyOfRange(bytes, 2, 6);
        this.deviceID = Arrays.copyOfRange(bytes, 6, 8);
        this.checksum = bytes[8];
        this.messageTail = bytes[9];
    }

    /**
     * Gets the commandID byte. However there is the first data byte which holds a kind of "sub command" that has to be
     * evaluated too
     *
     * @return byte representing the commandID
     */
    public byte getCommandID() {
        return this.commandID;
    }

    /**
     * Gets the first byte from the data bytes (usually holds the
     * {@link org.openhab.binding.novafinedust.internal.sds011protocol.Command}) as a form of some sub command
     *
     * @return first byte from the data section of a reply
     */
    public byte getFirstDataByte() {
        return this.payLoad[0];
    }

    protected byte calculateChecksum() {
        byte sum = 0;
        for (byte b : payLoad) {
            sum += b;
        }
        for (byte b : deviceID) {
            sum += b;
        }
        return sum;
    }

    @Override
    public String toString() {
        return String.format("GeneralReply: [head=%x, commandID=%x, payload=%s, deviceID=%s, checksum=%s, tail=%x",
                header, commandID, HexUtils.bytesToHex(payLoad), HexUtils.bytesToHex(deviceID), checksum, messageTail);
    }
}
