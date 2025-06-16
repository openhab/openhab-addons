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
package org.openhab.binding.mideaac.internal.handler;

import java.time.LocalDateTime;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mideaac.internal.Utils;
import org.openhab.binding.mideaac.internal.security.Crc8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CommandBase} has the discover command and the routine poll command
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

    protected byte[] data;

    /**
     * Operational Modes
     */
    public enum OperationalMode {
        AUTO(1),
        COOL(2),
        DRY(3),
        HEAT(4),
        FAN_ONLY(5),
        UNKNOWN(0);

        private final int value;

        private OperationalMode(int value) {
            this.value = value;
        }

        /**
         * Gets Operational Mode value
         * 
         * @return value
         */
        public int getId() {
            return value;
        }

        /**
         * Provides Operational Mode Common name
         * 
         * @param id integer from byte response
         * @return type
         */
        public static OperationalMode fromId(int id) {
            for (OperationalMode type : values()) {
                if (type.getId() == id) {
                    return type;
                }
            }
            return UNKNOWN;
        }
    }

    /**
     * Converts byte value to the Swing Mode label by version
     * Two versions of V3, Supported Swing or Non-Supported (4)
     * V2 set without leading 3, but reports with it (1)
     */
    public enum SwingMode {
        OFF3(0x30, 3),
        OFF4(0x00, 3),
        VERTICAL3(0x3C, 3),
        VERTICAL4(0xC, 3),
        HORIZONTAL3(0x33, 3),
        HORIZONTAL4(0x3, 3),
        BOTH3(0x3F, 3),
        BOTH4(0xF, 3),
        OFF2(0, 2),
        VERTICAL2(0xC, 2),
        VERTICAL1(0x3C, 2),
        HORIZONTAL2(0x3, 2),
        HORIZONTAL1(0x33, 2),
        BOTH2(0xF, 2),
        BOTH1(0x3F, 2),

        UNKNOWN(0xFF, 0);

        private final int value;
        private final int version;

        private SwingMode(int value, int version) {
            this.value = value;
            this.version = version;
        }

        /**
         * Gets Swing Mode value
         * 
         * @return value
         */
        public int getId() {
            return value;
        }

        /**
         * Gets device version for swing mode
         * 
         * @return version
         */
        public int getVersion() {
            return version;
        }

        /**
         * Gets Swing mode in common language horiontal, vertical, off, etc.
         * 
         * @param id integer from byte response
         * @param version device version
         * @return type
         */
        public static SwingMode fromId(int id, int version) {
            for (SwingMode type : values()) {
                if (type.getId() == id && type.getVersion() == version) {
                    return type;
                }
            }
            return UNKNOWN;
        }

        @Override
        public String toString() {
            // Drops the trailing 1 (V2 report) 2, 3 or 4 (nonsupported V3) from the swing mode
            return super.toString().replace("1", "").replace("2", "").replace("3", "").replace("4", "");
        }
    }

    /**
     * Converts byte value to the Fan Speed label by version.
     * Some devices do not support all speeds
     */
    public enum FanSpeed {
        AUTO2(102, 2),
        FULL2(100, 2),
        HIGH2(80, 2),
        MEDIUM2(50, 2),
        LOW2(30, 2),
        SILENT2(20, 2),
        UNKNOWN2(0, 2),

        AUTO3(102, 3),
        FULL3(0, 3),
        HIGH3(80, 3),
        MEDIUM3(60, 3),
        LOW3(40, 3),
        SILENT3(30, 3),
        UNKNOWN3(0, 3),

        UNKNOWN(0, 0);

        private final int value;

        private final int version;

        private FanSpeed(int value, int version) {
            this.value = value;
            this.version = version;
        }

        /**
         * Gets Fan Speed value
         * 
         * @return value
         */
        public int getId() {
            return value;
        }

        /**
         * Gets device version for Fan Speed
         * 
         * @return version
         */
        public int getVersion() {
            return version;
        }

        /**
         * Returns Fan Speed high, medium, low, etc
         * 
         * @param id integer from byte response
         * @param version version
         * @return type
         */
        public static FanSpeed fromId(int id, int version) {
            for (FanSpeed type : values()) {
                if (type.getId() == id && type.getVersion() == version) {
                    return type;
                }
            }
            return UNKNOWN;
        }

        @Override
        public String toString() {
            // Drops the trailing 2 or 3 from the fan speed
            return super.toString().replace("2", "").replace("3", "");
        }
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
        LocalDateTime now = LocalDateTime.now();
        data[data.length - 1] = (byte) now.getSecond();
        data[0x02] = (byte) 0xAC;
    }

    /**
     * Pulls the elements of the Base command together
     */
    public void compose() {
        logger.trace("Base Bytes before crypt {}", Utils.bytesToHex(data));
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
        sum = (byte) ((255 - (sum % 256)) + 1);
        return (byte) sum;
    }
}
