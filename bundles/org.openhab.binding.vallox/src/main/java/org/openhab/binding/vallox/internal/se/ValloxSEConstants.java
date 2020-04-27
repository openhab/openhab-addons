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
package org.openhab.binding.vallox.internal.se;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link ValloxSEConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Hauke Fuhrmann - Initial contribution
 * @author Miika Jukka - Moved all constants in same file
 */
@NonNullByDefault
public class ValloxSEConstants {

    public static final String BINDING_ID = "vallox";

    /**
     * List of all Thing Type UIDs
     */
    public static final ThingTypeUID THING_TYPE_VALLOX_SE_IP = new ThingTypeUID(BINDING_ID, "se-tcp");
    public static final ThingTypeUID THING_TYPE_VALLOX_SE_SERIAL = new ThingTypeUID(BINDING_ID, "se-serial");

    /**
     * Channel groups
     */
    public static final String CHANNEL_GROUP_FAN = "fanControl#";
    public static final String CHANNEL_GROUP_TEMPERATURE = "temperature#";
    public static final String CHANNEL_GROUP_SETTINGS = "setting#";
    public static final String CHANNEL_GROUP_STATUS = "status#";
    public static final String CHANNEL_GROUP_MAINTENANCE = "maintenance#";
    public static final String CHANNEL_GROUP_ALARM = "alarm#";

    /**
     * Special bytes
     */
    public static final byte POLL_BYTE = (byte) 0x00;
    public static final byte SUSPEND_BYTE = (byte) 0x91;
    public static final byte RESUME_BYTE = (byte) 0x8F;

    /**
     * Configuration parameters for ip thing
     */
    public static final String PARAMETER_TCP_HOST = "tcpHost";
    public static final String PARAMETER_TCP_PORT = "tcpPort";
    public static final int CONNECTION_TIMEOUT = 3000;
    public static final int SOCKET_READ_TIMEOUT = 10000;

    /**
     * Configuration parameters for serial thing
     */
    public static final String PARAMETER_SERIAL_PORT = "serialPort";
    public static final String PARAMETER_PANEL_NUMBER = "panelNumber";
    public static final int SERIAL_PORT_READ_TIMEOUT = 3000;
    public static final int SERIAL_TIMEOUT_MILLISECONDS = 1000;
    public static final int SERIAL_RECEIVE_THRESHOLD_BYTES = 1;
    public static final int SERIAL_RECEIVE_TIMEOUT_MILLISECONDS = 2000;

    /**
     * Connection parameters
     */
    public static final int SERIAL_BAUDRATE = 9600;
    public static final int TELEGRAM_LENGTH = 6;
    public static final int ACK_BYTE_LENGTH = 1;
    public static final byte DOMAIN = 0x01;

    /**
     * Addresses for sender and receiver
     */
    public static final byte ADDRESS_ALL_MAINBOARDS = 0x10;
    public static final byte ADDRESS_MASTER = 0x11;

    /**
     * Address for all panels. Used when receiving telegram from main unit to all panels
     */
    public static final byte ADDRESS_ALL_PANELS = 0x20;

    /**
     * Addresses for individual panels
     */
    public static final byte ADDRESS_PANEL1 = 0x21;
    public static final byte ADDRESS_PANEL2 = 0x22;
    public static final byte ADDRESS_PANEL3 = 0x23;
    public static final byte ADDRESS_PANEL4 = 0x24;
    public static final byte ADDRESS_PANEL5 = 0x25;
    public static final byte ADDRESS_PANEL6 = 0x26;
    public static final byte ADDRESS_PANEL7 = 0x27;
    public static final byte ADDRESS_PANEL8 = 0x28;

    /**
     * Byte addresses for control panels 1-8
     */
    public static final byte[] ADDRESS_PANEL_MAPPING = { 0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28 };

    /**
     * List for converting fan speeds from hex to integer
     */
    public static final byte[] FAN_SPEED_MAPPING = { 0x01, 0x03, 0x07, 0x0F, 0x1F, 0x3F, 0x7F, (byte) 0xFF };

    /**
     * List for converting temperatures from hex to integer
     */
    public static final byte[] TEMPERATURE_MAPPING = { -74, -70, -66, -62, -59, -56, -54, -52, -50, -48, -47, -46, -44,
            -43, -42, -41, -40, -39, -38, -37, -36, -35, -34, -33, -33, -32, -31, -30, -30, -29, -28, -28, -27, -27,
            -26, -25, -25, -24, -24, -23, -23, -22, -22, -21, -21, -20, -20, -19, -19, -19, -18, -18, -17, -17, -16,
            -16, -16, -15, -15, -14, -14, -14, -13, -13, -12, -12, -12, -11, -11, -11, -10, -10, -9, -9, -9, -8, -8, -8,
            -7, -7, -7, -6, -6, -6, -5, -5, -5, -4, -4, -4, -3, -3, -3, -2, -2, -2, -1, -1, -1, -1, 0, 0, 0, 1, 1, 1, 2,
            2, 2, 3, 3, 3, 4, 4, 4, 5, 5, 5, 5, 6, 6, 6, 7, 7, 7, 8, 8, 8, 9, 9, 9, 10, 10, 10, 11, 11, 11, 12, 12, 12,
            13, 13, 13, 14, 14, 14, 15, 15, 15, 16, 16, 16, 17, 17, 18, 18, 18, 19, 19, 19, 20, 20, 21, 21, 21, 22, 22,
            22, 23, 23, 24, 24, 24, 25, 25, 26, 26, 27, 27, 27, 28, 28, 29, 29, 30, 30, 31, 31, 32, 32, 33, 33, 34, 34,
            35, 35, 36, 36, 37, 37, 38, 38, 39, 40, 40, 41, 41, 42, 43, 43, 44, 45, 45, 46, 47, 48, 49, 49, 50, 51, 52,
            53, 53, 54, 55, 56, 57, 59, 60, 61, 62, 63, 65, 66, 68, 69, 71, 73, 75, 77, 79, 81, 82, 86, 90, 93, 97, 100,
            100, 100, 100, 100, 100, 100, 100, 100 };
}
