/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.modbus.e3dc.internal.modbus;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link E3DCModbusConstans} Variables for register handling.
 * The numbers are taken from E3DC Modbus Spec Chapter 3.1 page 14 ff
 * Registers start from 0 (not 1!) so from the documented registers subtract 1
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class E3DCModbusConstans {
    // "String" registers at the beginning shall be read with very low frequency - 1 hour
    public static final int INFO_POLL_REFRESH_TIME_MS = 60 * 60 * 1000;

    // Constants where a certain Block starts and block size. Note: General offset is -1 so INFO_REG from E3DC Modbus
    // Spec starts at 1 but it's Register 0!
    public static final int INFO_REG_START = 0;
    public static final int INFO_REG_SIZE = 67;
    public static final int POWER_REG_START = 67;
    public static final int POWER_REG_SIZE = 16;
    public static final int EMS_REG_START = 83;
    public static final int EMS_REG_SIZE = 2;
    public static final int WALLBOX_REG_START = 87;
    public static final int WALLBOX_REG_SIZE = 8;
    public static final int STRINGS_REG_START = 95;
    public static final int STRINGS_REG_SIZE = 9;
    public static final int REGISTER_LENGTH = 104;

    /*
     * Some Registers are numbers but needs to be decoded into Bits
     */

    // Wallbox Bit Definitions according to chapter 3.1.5 page 15
    public static final int WB_AVAILABLE_BIT = 0;
    public static final int WB_SUNMODE_BIT = 1;
    public static final int WB_CHARGING_ABORTED_BIT = 2;
    public static final int WB_CHARGING_BIT = 3;
    public static final int WB_JACK_LOCKED_BIT = 4;
    public static final int WB_JACK_PLUGGED_BIT = 5;
    public static final int WB_SCHUKO_ON_BIT = 6;
    public static final int WB_SCHUKO_PLUGGED_BIT = 7;
    public static final int WB_SCHUKO_LOCKED_BIT = 8;
    public static final int WB_SCHUKO_RELAY16A_BIT = 9;
    public static final int WB_RELAY_16A_BIT = 10;
    public static final int WB_RELAY_32A_BIT = 11;
    public static final int WB_1PHASE_BIT = 12;

    // EMS Bit Definitions according to chapter 3.1.3 page 17
    public static final int EMS_CHARGING_LOCK_BIT = 0;
    public static final int EMS_DISCHARGING_LOCK_BIT = 1;
    public static final int EMS_AVAILABLE_BIT = 2;
    public static final int EMS_WEATHER_CHARGING_BIT = 3;
    public static final int EMS_REGULATION_BIT = 4;
    public static final int EMS_CHARGE_LOCKTIME_BIT = 5;
    public static final int EMS_DISCHARGE_LOCKTIME_BIT = 6;
}
