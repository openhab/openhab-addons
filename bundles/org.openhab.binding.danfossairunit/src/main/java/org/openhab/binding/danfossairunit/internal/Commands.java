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
package org.openhab.binding.danfossairunit.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link Commands} interface holds the commands which can be send to the air unit to read/write values or trigger
 * actions.
 *
 * @author Robert Bach - Initial contribution
 */

@NonNullByDefault
public class Commands {

    public static final byte[] DISCOVER_SEND = { 0x0c, 0x00, 0x30, 0x00, 0x11, 0x00, 0x12, 0x00, 0x13 };
    public static final byte[] DISCOVER_RECEIVE = { 0x0d, 0x00, 0x07, 0x00, 0x02, 0x02, 0x00 };
    public static final byte[] EMPTY = {};
    public static final byte[] GET_HISTORY = { 0x00, 0x30 };
    public static final byte[] REGISTER_0_READ = { 0x00, 0x04 };
    public static final byte[] REGISTER_1_READ = { 0x01, 0x04 };
    public static final byte[] REGISTER_1_WRITE = { 0x01, 0x06 };
    public static final byte[] REGISTER_2_READ = { 0x02, 0x04 };
    public static final byte[] REGISTER_4_READ = { 0x04, 0x04 };
    public static final byte[] REGISTER_6_READ = { 0x06, 0x04 };
    public static final byte[] MODE = { 0x14, 0x12 };
    public static final byte[] MANUAL_FAN_SPEED_STEP = { 0x15, 0x61 };
    public static final byte[] SUPPLY_FAN_SPEED = { 0x14, 0x50 };
    public static final byte[] EXTRACT_FAN_SPEED = { 0x14, 0x51 };
    public static final byte[] SUPPLY_FAN_STEP = { 0x14, 0x28 };
    public static final byte[] EXTRACT_FAN_STEP = { 0x14, 0x29 };
    public static final byte[] BASE_IN = { 0x14, 0x40 };
    public static final byte[] BASE_OUT = { 0x14, 0x41 };
    public static final byte[] BYPASS = { 0x14, 0x60 };
    public static final byte[] BYPASS_DEACTIVATION = { 0x14, 0x63 };
    public static final byte[] BOOST = { 0x15, 0x30 };
    public static final byte[] NIGHT_COOLING = { 0x15, 0x71 };
    public static final byte[] AUTOMATIC_BYPASS = { 0x17, 0x06 };
    public static final byte[] AUTOMATIC_RUSH_AIRING = { 0x17, 0x02 };
    public static final byte[] HUMIDITY = { 0x14, 0x70 };
    public static final byte[] ROOM_TEMPERATURE = { 0x03, 0x00 };
    public static final byte[] ROOM_TEMPERATURE_CALCULATED = { 0x14, (byte) 0x96 };
    public static final byte[] OUTDOOR_TEMPERATURE = { 0x03, 0x34 };
    public static final byte[] SUPPLY_TEMPERATURE = { 0x14, 0x73 };
    public static final byte[] EXTRACT_TEMPERATURE = { 0x14, 0x74 };
    public static final byte[] EXHAUST_TEMPERATURE = { 0x14, 0x75 };
    public static final byte[] BATTERY_LIFE = { 0x03, 0x0f };
    public static final byte[] FILTER_LIFE = { 0x14, 0x6a };
    public static final byte[] FILTER_PERIOD = { 0x14, 0x69 };
    public static final byte[] CURRENT_TIME = { 0x15, (byte) 0xe0 };
    public static final byte[] AWAY_TO = { 0x15, 0x20 };
    public static final byte[] AWAY_FROM = { 0x15, 0x21 };
    public static final byte[] UNIT_SERIAL = { 0x00, 0x25 }; // endpoint 4
    public static final byte[] UNIT_NAME = { 0x15, (byte) 0xe5 }; // endpoint 1
    public static final byte[] CCM_SERIAL = { 0x14, 0x6a }; // endpoint 0
}
