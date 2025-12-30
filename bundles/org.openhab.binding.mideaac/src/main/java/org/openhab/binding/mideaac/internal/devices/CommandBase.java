/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.mideaac.internal.devices;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mideaac.internal.security.Crc8;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CommandBase} has the discover command, the routine poll command
 * and the enums for Operational Modes, Swing Modes, and Fan Speeds.
 *
 * @author Jacek Dobrowolski - Initial contribution
 * @author Bob Eckhoff - Add Java Docs, minor fixes
 */
@NonNullByDefault
public class CommandBase {
    private final Logger logger = LoggerFactory.getLogger(CommandBase.class);

    private static final byte[] DISCOVER_COMMAND = new byte[] { (byte) 0x5a, (byte) 0x5a, (byte) 0x01, (byte) 0x11,
            (byte) 0x48, (byte) 0x00, (byte) 0x92, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x7f, (byte) 0x75, (byte) 0xbd, (byte) 0x6b,
            (byte) 0x3e, (byte) 0x4f, (byte) 0x8b, (byte) 0x76, (byte) 0x2e, (byte) 0x84, (byte) 0x9c, (byte) 0x6e,
            (byte) 0x57, (byte) 0x8d, (byte) 0x65, (byte) 0x90, (byte) 0x03, (byte) 0x6e, (byte) 0x9d, (byte) 0x43,
            (byte) 0x42, (byte) 0xa5, (byte) 0x0f, (byte) 0x1f, (byte) 0x56, (byte) 0x9e, (byte) 0xb8, (byte) 0xec,
            (byte) 0x91, (byte) 0x8e, (byte) 0x92, (byte) 0xe5 };

    protected byte[] data = new byte[1];

    /**
     * Returns the command data for testing.
     *
     * @return command data
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Returns the command to discover devices.
     * Command is defined above
     *
     * @return discover command
     */
    public static byte[] discover() {
        return DISCOVER_COMMAND;
    }

    /**
     * Byte Array structure for Base commands
     */
    public CommandBase() {
        data = new byte[] { (byte) 0xaa,
                // request is 0x20; setting is 0x23 - This is the message length
                (byte) 0x20,
                // device type
                (byte) 0xac, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                // request is 0x03; setting is 0x02
                (byte) 0x03,
                // Byte0 - Data request/response type: 0x41 - check status; 0x40 - Set up
                (byte) 0x41,
                // Byte1
                (byte) 0x81,
                // Byte2 - operational_mode
                0x00,
                // Byte3
                (byte) 0xff,
                // Byte4
                0x03,
                // Byte5
                (byte) 0xff,
                // Byte6
                0x00,
                // Byte7 - Room Temperature Request: 0x02 - indoor_temperature, 0x03 - outdoor_temperature
                // when set, this is swing_mode
                0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                // Message ID
                0x00 };
        applyTimestamp();
    }

    protected void applyTimestamp() {
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        data[data.length - 1] = (byte) now.getSecond();
    }

    /**
     * Pulls the elements of the Base command together
     */
    public void compose() {
        logger.debug("Base Bytes before crypt {}", HexUtils.bytesToHex(data));
        byte crc8 = (byte) Crc8.calculate(Arrays.copyOfRange(data, 10, data.length));
        byte[] newData1 = new byte[data.length + 1];
        System.arraycopy(data, 0, newData1, 0, data.length);
        newData1[data.length] = crc8;
        data = newData1;
        byte chksum = checksum(Arrays.copyOfRange(data, 1, data.length));
        byte[] newData2 = new byte[data.length + 1];
        System.arraycopy(data, 0, newData2, 0, data.length);
        newData2[data.length] = chksum;
        data = newData2;
    }

    /**
     * Gets byte array
     * 
     * @return data array
     */
    public byte[] getBytes() {
        return data;
    }

    private static byte checksum(byte[] bytes) {
        int sum = 0;
        for (byte value : bytes) {
            sum = (byte) (sum + value);
        }
        return (byte) ((255 - (sum % 256)) + 1);
    }
}
